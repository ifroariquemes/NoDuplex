/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.noduplex.controller;

import com.noduplex.model.TMDuplexFile;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;

/**
 *
 * @author natanaelsimoes
 */
public class TCMain implements Initializable {

    @FXML
    private TextField tfPath;
    @FXML
    private Button btPath;
    @FXML
    private Button btStart;
    @FXML
    private Label lbMessage;
    @FXML
    private ProgressBar pbProgress;
    @FXML
    private Button btAbout;

    private TCDuplexFile duplexFileController;
    @FXML
    private Label label;
    @FXML
    private TableView<TMDuplexFile> duplicatedFiles;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // TODO
            lbMessage.setVisible(false);
            pbProgress.setVisible(false);
            duplexFileController = new TCDuplexFile();
            updateDuplicatedFilesTable();
        } catch (ClassNotFoundException | SQLException | IOException ex) {
            Logger.getLogger(TCMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void btPathAction(ActionEvent event) throws ClassNotFoundException, SQLException, IOException {
        DirectoryChooser open = new DirectoryChooser();
        open.setTitle("Escolha uma pasta");
        File directory = open.showDialog(null);
        if (directory != null) {
            tfPath.setText(directory.getAbsolutePath());
            btStart.setDisable(false);
        } else {
            tfPath.setText("");
            btStart.setDisable(true);
        }
    }

    @FXML
    private void btStartAction(ActionEvent event) {
        duplexFileController.startGetFiles(tfPath.getText(), this);
    }

    @SuppressWarnings("empty-statement")
    private void updateDuplicatedFilesTable() throws ClassNotFoundException {
        ObservableList<TMDuplexFile> data = duplexFileController.listDuplicated();

        TableColumn nameCol = new TableColumn("Nome");
        nameCol.setMinWidth(130);
        stageResizeFontTable(nameCol);
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn pathCol = new TableColumn("Caminho");
        pathCol.setMinWidth(200);
        stageResizeFontTable(pathCol);
        pathCol.setCellValueFactory(new PropertyValueFactory<>("path"));

        TableColumn sizeCol = new TableColumn("Tamanho");
        sizeCol.setMinWidth(80);       
        stageResizeFontTable(sizeCol);
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("sizeForHuman"));

        /**
         * @todo Clicking at Size column the sort is made over String and not
         * Long(number). We need to find a way to sort this properly.
         * <p>
         * We tried to build a Comparator, but no success. If you have some idea
         * what is needed, plese contact us!
         * <p>
         * Our try:          <code>
         * sizeCol.setComparator(new Comparator<String>() {
         * @Override public int compare(String o1, String o2) { return
         * duplexFileController.getSizeFromHuman(o1).compareTo(duplexFileController.getSizeFromHuman(o2));
         * } });
         * </code>
         */
        TableColumn hashCol = new TableColumn("Hash");
        hashCol.setMinWidth(200);
        stageResizeFontTable(hashCol);
        hashCol.setCellValueFactory(new PropertyValueFactory<>("hash"));

        TableColumn exteCol = new TableColumn("Extensão");
        exteCol.setMinWidth(80);
        stageResizeFontTable(exteCol);
        exteCol.setCellValueFactory(new PropertyValueFactory<>("extension"));

        TableColumn erasCol = new TableColumn("Apagar?");
        erasCol.setMinWidth(70);
        erasCol.setCellFactory(new Callback<TableColumn<TMDuplexFile, Boolean>, TableCell<TMDuplexFile, Boolean>>() {
            @Override
            public TableCell<TMDuplexFile, Boolean> call(TableColumn<TMDuplexFile, Boolean> param) {
                return new CheckBoxTableCell<>();
            }
        });

        duplicatedFiles.setItems(data);
        duplicatedFiles.getColumns().addAll(nameCol, pathCol, sizeCol, hashCol, exteCol, erasCol);

    }

    private void stageResizeFontTable(TableColumn column) {
        column.setCellFactory(new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell<TMDuplexFile, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            this.setFont(Font.font(10));
                            setText(item);
                            setAlignment(Pos.CENTER);
                        }
                    }
                };
            }
        });
    }

    public void stageCalculateSize() {
        btStart.setDisable(true);
        lbMessage.setVisible(true);
        pbProgress.setVisible(true);
        pbProgress.getScene().setCursor(Cursor.WAIT);
    }

    public void stageCatchFiles() {
        pbProgress.setProgress(0);
    }

    public void stageInitial() {
        btStart.getScene().setCursor(Cursor.DEFAULT);
        btStart.setDisable(false);
        lbMessage.setVisible(false);
        pbProgress.setVisible(false);
    }

    public void setMessage(String message) {
        lbMessage.setText(message);
    }

    public void setProgress(Long progress) {
        pbProgress.setProgress(progress);
    }

    public ProgressBar getProgress() {
        return this.pbProgress;
    }

    public Label getLabel() {
        return this.lbMessage;
    }

}
