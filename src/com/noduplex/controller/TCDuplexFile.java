package com.noduplex.controller;

import com.noduplex.dao.TDDuplexFile;
import com.noduplex.model.TMDuplexFile;
import java.awt.Toolkit;
import org.apache.commons.codec.digest.DigestUtils;
import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.controlsfx.dialog.Dialogs;

/**
 * This class have some methods to manipulate files, get theis sizes, hashes and
 * cool stuffs
 *
 * @todo Delete duplicated files, before delete, verify if all files exists
 * @todo Scheduling
 * @author Filip Caetano
 * @author Natanael Simoes
 */
public class TCDuplexFile {

    private final TDDuplexFile duplexFileDAO;
    private final static double KB_FACTOR = 1024;
    private final static double MB_FACTOR = 1024 * KB_FACTOR;
    private final static double GB_FACTOR = 1024 * MB_FACTOR;

    /**
     * Initializes everything
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     */
    public TCDuplexFile() throws ClassNotFoundException, SQLException, IOException {
        this.duplexFileDAO = new TDDuplexFile();
    }

    public void startGetFiles(String initialPath, TCMain document) {

        final File initialDirectory = new File(initialPath);

        Task task;
        task = new Task<Void>() {
            private long totalSize = 0;
            private long actualSize = 0;

            @Override
            public Void call() throws InterruptedException, IOException {
                long start = System.nanoTime();
                updateProgress(-1, 100);
                calculateSize(initialDirectory.getPath());
                updateProgress(0, 100);
                catchFiles(initialDirectory);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                updateMessage("Done!");
                Toolkit.getDefaultToolkit().beep();
                document.stageInitial();
                Dialogs.create().title("Mensagem do sistema").masthead(null).message("Análise concluída com sucesso!").showInformation();
            }

            /**
             * Calculates the total size of all files beign catch, used to build
             * the progress bar
             *
             * @param startPath
             * @throws IOException
             */
            public void calculateSize(String startPath) throws IOException {
                final AtomicLong size = new AtomicLong(0);
                Path path = Paths.get(startPath);

                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file,
                            BasicFileAttributes attrs) throws IOException {
                        size.addAndGet(attrs.size());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc)
                            throws IOException {
                        System.out.println("skipped: " + file + "e=" + exc);
                        return FileVisitResult.CONTINUE;
                    }
                });

                this.totalSize = size.get();
            }

            /**
             * Catches recursively all files inside a directory then saves their
             * information at database
             *
             * @param actualDirectory the directory to catch files
             * @return Nothing, just null to sync with the caller thread
             * @throws IOException
             */
            public Void catchFiles(File actualDirectory) throws IOException {
                try {
                    File fileList[] = actualDirectory.listFiles();
                    for (File file : fileList) {
                        TMDuplexFile duplexFile = new TMDuplexFile();
                        if (file.isFile()) {
                            duplexFile.setHash(generateHash(file));
                            if (duplexFile.getHash() != null) {
                                duplexFile.setName(file.getName());
                                duplexFile.setPath(file.getAbsolutePath());
                                duplexFile.setSize(file.length());
                                updateMessage(duplexFile.getPath());
                                actualSize += duplexFile.getSize();
                                updateProgress(actualSize * 100 / totalSize, 100);
                                duplexFileDAO.insert(duplexFile);
                            }
                        } else if (file.isDirectory() && !Files.isSymbolicLink(Paths.get(file.getPath()))) {
                            catchFiles(file);
                        }
                    }
                } finally {
                    return null;
                }
            }
        };

        document.stageCalculateSize();
        document.stageCatchFiles();
        document.getProgress().progressProperty().bind(task.progressProperty());
        document.getLabel().textProperty().bind(task.messageProperty());
        new Thread(task).start();
    }

    /**
     * Generates the hash of a file
     *
     * @param file the file
     * @return the hash (md5)
     */
    public String generateHash(File file) {
        try {
            InputStream is = new FileInputStream(file);
            return DigestUtils.md5Hex(is);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Deletes a single file
     *
     * @param path the path to file
     */
    public void deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            Dialogs.create().title("Erro do sistema").message(String.format("O arquivo %s não existe", file.getName())).showError();
        } else {
            file.delete();
        }
    }

    /**
     * Lists all duplicated files
     *
     * @return All duplicated files
     * @throws ClassNotFoundException
     */
    public ObservableList<TMDuplexFile> listDuplicated() throws ClassNotFoundException {
        return duplexFileDAO.listDuplicated();
    }

    public Double getSizeFromHuman(String size) {
        int spaceNdx = size.indexOf(" ");
        double ret = Double.parseDouble(size.substring(0, spaceNdx).replace(",", "."));
        switch (size.substring(spaceNdx + 1)) {
            case "GB":
                return ret * GB_FACTOR;
            case "MB":
                return ret * MB_FACTOR;
            case "KB":
                return ret * KB_FACTOR;
        }
        return (double)-1;
    }

}
