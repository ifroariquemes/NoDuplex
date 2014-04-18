package com.noduplex.dao;

import com.noduplex.model.TMDuplexFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * "Here is the brain of our system, it stores everything!! Thank god it don't
 * drink or we will be lost haha"
 *
 * This class will help you to insert new files into database and make lists
 * like all files or just duplicated
 *
 * @author natanaelsimoes
 */
public class TDDuplexFile {

    private final Database db;

    /**
     * Starts initializing database connection, getting ready to use
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     */
    public TDDuplexFile() throws ClassNotFoundException, SQLException, IOException {
        db = Database.getInstance();
    }

    /**
     * Inserts file information into database
     *
     * @param file file to be inserted
     * @see TMDuplexFile
     */
    public void insert(TMDuplexFile file) {
        try {
            db.prepare("INSERT INTO file (name, path, size, hash, extension) VALUES (?,?,?,?,?)");
            db.setString(1, file.getName());
            db.setString(2, file.getPath());
            db.setLong(3, file.getSize());
            db.setString(4, file.getHash());
            db.setString(5, file.getExtension());
            db.execute();
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                this.update(file);
            } else {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Update file information if exists
     *
     * @param file file to be updated
     * @see TMDuplexFile
     */
    private void update(TMDuplexFile file) {
        try {
            db.prepare("UPDATE file SET name = ?, size = ?, hash = ?, extension = ? WHERE path = ?");
            db.setString(1, file.getName());
            db.setLong(2, file.getSize());
            db.setString(3, file.getHash());
            db.setString(4, file.getExtension());
            db.setString(5, file.getPath());
            db.execute();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Lists all registred files
     *
     * @return All registred files
     * @throws ClassNotFoundException
     */
    public List<TMDuplexFile> listAll() throws ClassNotFoundException {
        try {
            db.prepare("SELECT * FROM file");
            List<TMDuplexFile> files = new ArrayList();
            final ResultSet rs = db.select();
            while (rs.next()) {
                files.add(new TMDuplexFile() {
                    {
                        setHash(rs.getString("hash"));
                        setName(rs.getString("name"));
                        setPath(rs.getString("path"));
                        setSize(rs.getLong("size"));
                    }
                });
            }
            return files;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    /**
     * Lists all duplicated files
     *
     * @return All duplicated files
     * @throws ClassNotFoundException
     */
    public ObservableList<TMDuplexFile> listDuplicated() throws ClassNotFoundException {
        try {
            String query;
            query = " SELECT name, path, size, hash, extension FROM file ";
            query += " WHERE hash IN ";
            query += " ( SELECT hash FROM file GROUP BY(hash) HAVING count(hash) > 1 ) ";
            query += " ORDER BY (hash) ";
            db.prepare(query);
            ObservableList<TMDuplexFile> files = FXCollections.observableArrayList();
            final ResultSet rs = db.select();
            while (rs.next()) {
                files.add(new TMDuplexFile() {
                    {
                        setHash(rs.getString("hash"));
                        setName(rs.getString("name"));
                        setPath(rs.getString("path"));
                        setSize(rs.getLong("size"));
                    }
                });
            }
            return files;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}
