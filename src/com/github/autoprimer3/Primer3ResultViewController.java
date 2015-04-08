/*
 * Copyright (C) 2014 David A. Parry <d.a.parry@leeds.ac.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.autoprimer3;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.System.getProperty;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.apache.poi.ss.usermodel.CreationHelper;

/**
 * FXML Controller class
 *
 * @author david
 */
public class Primer3ResultViewController implements Initializable {

   @FXML
   AnchorPane resultPane;
   @FXML
   MenuBar menuBar;
   @FXML
   MenuItem writeFileMenuItem;
   @FXML
   MenuItem writeDesignMenuItem;
   @FXML
   MenuItem writeRefsMenuItem;
   @FXML
   MenuItem closeMenuItem;
   @FXML
   TableView<Primer3Result> primerTable;
   @FXML
   TableColumn indexCol;
   @FXML
   TableColumn nameCol;
   @FXML
   TableColumn idCol;
   @FXML
   TableColumn leftPrimerCol;
   @FXML
   TableColumn rightPrimerCol;
   @FXML
   TableColumn productSizeCol;
   @FXML
   TableColumn regionCol;
   @FXML
   TableColumn ispcrCol;
   @FXML
   TableColumn ispcrResCol;
   @FXML
   Button checkIsPcrButton;
   @FXML
   Button closeButton;
   @FXML 
   Label summaryLabel;
   @FXML
   TextArea designTextSummary;
   @FXML
   TextArea referenceTextArea;
   @FXML
   Tab refTab;
   @FXML
   ChoiceBox refChoiceBox;
   @FXML
   ProgressBar progressBar;
   
   
   NumberFormat nf = NumberFormat.getNumberInstance();
   CoordComparator coordCompare = new CoordComparator();
   HashMap<String, String> refSeqs;
   String summary = null; 
   
   private final ObservableList<Primer3Result> data = FXCollections.observableArrayList();
   
   @Override
    public void initialize(URL url, ResourceBundle rb) {
        menuBar.setUseSystemMenuBar(true);
        progressBar.setVisible(false);
        
        indexCol.setCellValueFactory(new 
                PropertyValueFactory<Primer3Result, Integer>("index"));
        leftPrimerCol.setCellValueFactory(new 
                PropertyValueFactory<Primer3Result, String>("leftPrimer"));
        nameCol.setCellValueFactory(new
                PropertyValueFactory<Primer3Result, String>("name"));
        idCol.setCellValueFactory(new
                PropertyValueFactory<Primer3Result, String>("transcripts"));
        rightPrimerCol.setCellValueFactory(new 
                PropertyValueFactory<Primer3Result, String>("rightPrimer"));
        productSizeCol.setCellValueFactory(new 
                PropertyValueFactory<Primer3Result, Integer>("productSize"));
        regionCol.setCellValueFactory(new 
                PropertyValueFactory<Primer3Result, String>("region"));
        ispcrCol.setCellValueFactory(new 
                PropertyValueFactory<Primer3Result, Hyperlink>("isPcrLink"));
        ispcrResCol.setCellValueFactory(new 
                PropertyValueFactory<Primer3Result, Hyperlink>("isPcrResults"));
        ispcrResCol.setVisible(false);
        primerTable.getSortOrder().add(indexCol);
        primerTable.getSortOrder().add(regionCol);
        primerTable.getSortOrder().add(productSizeCol);
        regionCol.setComparator(coordCompare);
        primerTable.getSelectionModel().setCellSelectionEnabled(true);
        primerTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        refTab.selectedProperty().addListener(new ChangeListener<Boolean>(){
            @Override
            public void changed (ObservableValue ov, Boolean value, final Boolean newValue){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        refChoiceBox.setVisible(newValue);
                    }
                });
            }
        });
        
        writeFileMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try{
                    writePrimersToFile();
                }catch(final IOException ex){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Action writeFailed = Dialogs.create().title("Writing failed").
                                masthead("Could not write primers to file").
                                message("Exception encountered when attempting to write "
                                        + "primers to file. See below:").
                                styleClass(Dialog.STYLE_CLASS_NATIVE).
                                showException(ex);
                        }
                    });
                }
            }
        });
        
        writeDesignMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try{
                    writeDesignToFile();
                }catch(final IOException ex){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Action writeFailed = Dialogs.create().title("Writing failed").
                                masthead("Could not write primer designs to file").
                                message("Exception encountered when attempting to write "
                                        + "design details to file. See below:").
                                styleClass(Dialog.STYLE_CLASS_NATIVE).
                                showException(ex);
                        }
                    });
                }
            }
        });
        
        writeRefsMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try{
                    writeRefSeqsToFile();
                }catch(final IOException ex){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Action writeFailed = Dialogs.create().title("Writing failed").
                                masthead("Could not write reference sequences to file").
                                message("Exception encountered when attempting to write "
                                        + "reference sequences to file. See below:").
                                styleClass(Dialog.STYLE_CLASS_NATIVE).
                                showException(ex);
                        }
                    });
                }
            }
        });
        
        setCheckIsPcrButton();
                
        closeMenuItem.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent e){
                Platform.runLater(new Runnable(){
                    @Override
                    public void run(){
                        Stage stage = (Stage) resultPane.getScene().getWindow();
                        stage.close();
                    }
                });
            }
        });
        
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ObservableList<TablePosition> posList = primerTable.getSelectionModel().getSelectedCells();
                int old_r = -1;
                StringBuilder clipboardString = new StringBuilder();
                for (TablePosition p : posList) {
                    int r = p.getRow();
                    int c = p.getColumn();
                    Object cell = primerTable.getColumns().get(c).getCellData(r);
                    if (cell == null)
                        cell = "";
                    if (old_r == r)
                        clipboardString.append('\t');
                    else if (old_r != -1)
                        clipboardString.append('\n');
                    clipboardString.append(cell);
                    old_r = r;
                    
                }
                final ClipboardContent content = new ClipboardContent();
                content.putString(clipboardString.toString());
                Clipboard.getSystemClipboard().setContent(content);
            }
        });
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(copyItem);
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        primerTable.setContextMenu(menu);
        
        
        closeButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent e){
                Platform.runLater(new Runnable(){
                    @Override
                    public void run(){
                        Stage stage = (Stage) closeButton.getScene().getWindow();
                        stage.close();
                    }
                });
            }
        });
        
    }
    
    public void displayData(ArrayList<Primer3Result> regions, 
            ArrayList<String> design, final HashMap<String, String> ref){
        int totalPairs = 0;
        int failures = 0;
        refSeqs = ref;
        if (ref != null){
            refTab.setDisable(false);
            writeRefsMenuItem.setDisable(false);
            refChoiceBox.getItems().clear();
            refChoiceBox.getItems().addAll(ref.keySet());
            refChoiceBox.getSelectionModel().selectedIndexProperty().addListener
                (new ChangeListener<Number>(){
                @Override
                public void changed (ObservableValue ov, Number value, final Number new_value){ 
                    if (new_value.intValue() >= 0){
                        final String id = (String) refChoiceBox.getItems().get(new_value.intValue());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                //System.out.println("Selecting " + id );
                                //System.out.println("Seq =  " + ref.get(id) );
                                referenceTextArea.setText(splitStringOnLength(
                                        ref.get(id), 60, "\n"));
                            }
                        });
                    }

                }
            });
            refChoiceBox.getSelectionModel().selectFirst();
        }else{
            refTab.setDisable(true);
            writeRefsMenuItem.setDisable(true);
        }
        StringBuilder designString = new StringBuilder();
        for (Primer3Result r: regions){
            data.add(r);
            primerTable.setItems(data);
            if (r.getProductSize() > 0){
                totalPairs++;
            }else{
                failures++;
            }
        }
        for (String s: design){
            designString.append(s).append("\n");
        }
        designTextSummary.setText(designString.toString());
        StringBuilder labelText = new StringBuilder (totalPairs + " primer pairs designed");
        if (failures > 0){
            labelText.append(". ").append(failures).append(" design");
            if (failures > 1){
                labelText.append("s");
            }
            labelText.append(" failed.");
        }
        summary = labelText.toString();
        summaryLabel.setText(summary);
        
    }
    
    private String splitStringOnLength(String s, Integer n, String sep){
        StringBuilder split = new StringBuilder();
        for (int i = 0; i < s.length(); i += n){
            split.append(s.substring(i, Math.min(i + n, s.length())));
            split.append(sep);
        }
        return split.toString();
    }
    
    private void writePrimersToFile() throws IOException{
        if (data.isEmpty()){
            Dialogs noPrimersError = Dialogs.create().title("Nothing to save").
                    masthead("No primers to save").
                    message("No primers were designed, no file can be saved.")
                    .styleClass(Dialog.STYLE_CLASS_NATIVE);
            noPrimersError.showError();
            return;
        }
       FileChooser fileChooser = new FileChooser();
       fileChooser.getExtensionFilters().addAll(
               new FileChooser.ExtensionFilter("Excel  (*.xlsx)", "*.xlsx"),
               new FileChooser.ExtensionFilter("CSV  (*.csv)", "*.csv"),
               new FileChooser.ExtensionFilter("Text  (*.txt)", "*.txt")
       );
       fileChooser.setTitle("Write primers to file...");
       fileChooser.setInitialDirectory(new File(getProperty("user.home")));
       File wFile = fileChooser.showSaveDialog(resultPane.getScene().getWindow());
       if (wFile == null){
           return;
       }else if (! wFile.getName().endsWith(".xlsx") && 
                 !wFile.getName().endsWith(".csv")   &&
                 !wFile.getName().endsWith(".txt")){
            String ext = //annoying bug with filechooser means extension might not be appended
                fileChooser.selectedExtensionFilterProperty().get().getExtensions().get(0).substring(1);
            wFile = new File(wFile.getAbsolutePath() + ext);
       }
       if (wFile.getName().endsWith(".xlsx")){
           writePrimersToExcel(wFile);
       }else if (wFile.getName().endsWith(".csv")){
           writePrimersToCsv(wFile);
       }else{
           writePrimersToTsv(wFile);
       }
    }
    
    private void writePrimersToExcel(final File f) throws IOException{
       final Service<Void> service = new Service<Void>(){
            @Override
            protected Task<Void> createTask(){
                return new Task<Void>(){
                    @Override
                    protected Void call() throws IOException {
                        BufferedOutputStream bo = new BufferedOutputStream(new 
                           FileOutputStream(f));
                        Workbook wb = new XSSFWorkbook();
                        CellStyle hlink_style = wb.createCellStyle();
                        Font hlink_font = wb.createFont();
                        hlink_font.setUnderline(Font.U_SINGLE);
                        hlink_font.setColor(IndexedColors.BLUE.getIndex());
                        hlink_style.setFont(hlink_font);
                        CreationHelper createHelper = wb.getCreationHelper();
                        Sheet listSheet = wb.createSheet();
                        Sheet detailsSheet = wb.createSheet();
                        Row row = null;
                        int rowNo = 0;
                        int sheetNo = 0;
                        wb.setSheetName(sheetNo++, "List");
                        wb.setSheetName(sheetNo++, "Details");
                        
                        row = listSheet.createRow(rowNo++);
                        String header[] = {"Primer", "Sequence", "Product Size (bp)"};
                        for (int col = 0; col < header.length; col ++){
                            Cell cell = row.createCell(col);
                            cell.setCellValue(header[col]);
                        }
                        
                        updateMessage("Writing primers . . .");
                        updateProgress(0, data.size() * 3);
                        int n = 0;
                        for (Primer3Result r: data){
                            n++;
                            updateMessage("Writing primer list " + n + " . . .");
                            row = listSheet.createRow(rowNo++);
                            int col = 0;
                            Cell cell = row.createCell(col++);
                            cell.setCellValue(r.getName() + "F");
                            cell = row.createCell(col++);
                            cell.setCellValue(r.getLeftPrimer());
                            cell = row.createCell(col++);
                            cell.setCellValue(r.getProductSize());
                            updateProgress(n, data.size() * 3);
                            updateMessage("Writing primer list " + n + " . . .");
                            row = listSheet.createRow(rowNo++);
                            col = 0;
                            cell = row.createCell(col++);
                            cell.setCellValue(r.getName() + "R");
                            cell = row.createCell(col++);
                            cell.setCellValue(r.getRightPrimer());
                            cell = row.createCell(col++);
                            cell.setCellValue(r.getProductSize());
                            n++;
                            updateProgress(n, data.size() * 3);
                        }
                        rowNo = 0;
                        row = detailsSheet.createRow(rowNo++);
                        ArrayList<String> detailsHeader = new ArrayList<>(
                        Arrays.asList("Name", "Other IDs", "Left Primer", "Right Primer", 
                                "Product Size (bp)", "Region", "in-silico PCR"));
                        if (ispcrResCol.isVisible()){
                            detailsHeader.add("in-silico PCR Results");
                        }
                        for (int col = 0; col < detailsHeader.size(); col ++){
                            Cell cell = row.createCell(col);
                            cell.setCellValue(detailsHeader.get(col));
                        }
                        int m = 0;
                        for (Primer3Result r: data){
                            m++;
                            updateMessage("Writing details for pair " + m + " . . .");
                            row = detailsSheet.createRow(rowNo++);
                            int col = 0;
                            Cell cell = row.createCell(col++);
                            cell.setCellValue(r.getName());
                            cell = row.createCell(col++);
                            cell.setCellValue(r.getTranscripts());
                            cell = row.createCell(col++);
                            cell.setCellValue(r.getLeftPrimer());
                            cell = row.createCell(col++);
                            cell.setCellValue(r.getRightPrimer());
                            cell = row.createCell(col++);
                            cell.setCellValue(r.getProductSize());
                            cell = row.createCell(col++);
                            cell.setCellValue(r.getRegion());
                            cell = row.createCell(col++);
                            if (r.getIsPcrUrl() != null){
                                cell.setCellValue("isPCR");
                                org.apache.poi.ss.usermodel.Hyperlink hl = 
                                    createHelper.createHyperlink(org.apache.poi.ss.usermodel.Hyperlink.LINK_URL);
                                hl.setAddress(r.getIsPcrUrl());
                                cell.setHyperlink(hl);
                                cell.setCellStyle(hlink_style);
                            }else{
                                cell.setCellValue("");
                            }
                            if (ispcrResCol.isVisible()){
                                cell = row.createCell(col++);
                                if (r.getIsPcrResults() != null){
                                    cell.setCellValue(r.getIsPcrResults());
                                }else{
                                    cell.setCellValue("");
                                }
                            }
                            updateProgress(n + m, data.size() * 3);
                        }
                        
                        
                        updateMessage("Wrote " + data.size() + " primer pairs to file.");
                        wb.write(bo);
                        bo.close();
                        return null;
                    }
                };
            }
            
        };
       
        service.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
                @Override
                public void handle (WorkerStateEvent e){
                    
                     Action response = Dialogs.create().title("Done").
                        masthead("Finished writing").
                        message("Primers successfully written to " + 
                                f.getAbsolutePath() + "\n\nDo you want to open "
                                + "this file now?").
                        actions(Dialog.ACTION_YES, Dialog.ACTION_NO).
                        styleClass(Dialog.STYLE_CLASS_NATIVE).
                        showConfirm();

                    if (response == Dialog.ACTION_YES){
                        try{
                            openFile(f);
                        } catch (IOException ex) {
                            Action openFailed = Dialogs.create().title("Open failed").
                                masthead("Could not open output file").
                                message("Exception encountered when attempting to open "
                                        + "the saved file. See below:").
                                styleClass(Dialog.STYLE_CLASS_NATIVE).
                                showException(ex);
                        }
                    }
                    progressBar.progressProperty().unbind();
                    progressBar.setVisible(false);
                    summaryLabel.textProperty().unbind();
                    summaryLabel.setText(summary);
                    closeButton.setDisable(false);
                    closeMenuItem.setDisable(false);
                    setCheckIsPcrButton();
                }
        });
        service.setOnCancelled(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                Dialogs writeCancelled = Dialogs.create().title("Writing Cancelled").
                    masthead("Cancelled writing to file").
                    message("User cancelled writing primers to file.").
                    styleClass(Dialog.STYLE_CLASS_NATIVE);
                writeCancelled.showInformation();
                progressBar.progressProperty().unbind();
                progressBar.setVisible(false);
                summaryLabel.textProperty().unbind();
                summaryLabel.setText(summary);
                closeButton.setDisable(false);
                closeMenuItem.setDisable(false);
                setCheckIsPcrButton();
            }
        });
        service.setOnFailed(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                Action writeFailed = Dialogs.create().title("Writing failed").
                    masthead("Could not write primers to file").
                    message("Exception encountered when attempting to write "
                            + "primers to file. See below:").
                    styleClass(Dialog.STYLE_CLASS_NATIVE).
                    showException(e.getSource().getException());
                progressBar.progressProperty().unbind();
                progressBar.setVisible(false);
                summaryLabel.textProperty().unbind();
                summaryLabel.setText(summary);
                closeButton.setDisable(false);
                closeMenuItem.setDisable(false);
                setCheckIsPcrButton();
            }
        });
        progressBar.setVisible(true);
        progressBar.progressProperty().bind(service.progressProperty());
        summaryLabel.textProperty().bind(service.messageProperty());
        closeButton.setDisable(true);
        closeMenuItem.setDisable(true);
        checkIsPcrButton.setText("Cancel");
        checkIsPcrButton.setOnAction(new EventHandler<ActionEvent>(){
           @Override
           public void handle(ActionEvent actionEvent){
                service.cancel();

            }
       });
        service.start();
    }
    
    private void writePrimersToCsv(final File f) throws IOException{
        writePrimersToText(f, ",");
    }
    
    private void writePrimersToTsv(final File f) throws IOException{
        writePrimersToText(f, "\t");
    }
    
    //takes output file and delimiter string as arguments
    private void writePrimersToText(final File f, final String d) throws IOException{
        final Service<Void> service = new Service<Void>(){
            @Override
            protected Task<Void> createTask(){
                return new Task<Void>(){
                    @Override
                    protected Void call() throws IOException {
                        FileWriter fw = new FileWriter(f.getAbsoluteFile());
                        BufferedWriter bw = new BufferedWriter(fw);
                        updateMessage("Writing primers . . .");
                        updateProgress(0, data.size() * 2);
                        int n = 0;
                        for (Primer3Result r: data){
                            n++;
                            updateMessage("Writing primer " + n + " . . .");
                            updateProgress(n, data.size());
                            bw.write(r.getName() + "F" + d + r.getLeftPrimer());
                            bw.newLine();
                            n++;
                            updateMessage("Writing primer " + n + " . . .");
                            updateProgress(n, data.size());
                            bw.write(r.getName() + "R" + d + r.getRightPrimer());
                            bw.newLine();
                        }
                        updateMessage("Wrote " + n + " primers to file.");
                        bw.close();
                        return null;
                    }
                };
            }
            
        };
        
        
        service.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
                @Override
                public void handle (WorkerStateEvent e){
                    /*
                    Dialogs.create().title("Done").
                            masthead("Finished writing").
                            message("Primers successfully written to " + 
                                    f.getAbsolutePath()).
                            styleClass(Dialog.STYLE_CLASS_NATIVE).
                            showInformation();
                    */
                    Action response = Dialogs.create().title("Done").
                                masthead("Finished writing").
                                message("Primers successfully written to " + 
                                        f.getAbsolutePath() + "\n\nDo you want to open "
                                + "this file now?").
                                actions(Dialog.ACTION_YES, Dialog.ACTION_NO).
                                styleClass(Dialog.STYLE_CLASS_NATIVE).
                                showConfirm();

                    if (response == Dialog.ACTION_YES){
                        try{
                            //Desktop.getDesktop().open(f);
                            openFile(f);
                        }catch (IOException ex){
                            Action openFailed = Dialogs.create().title("Open failed").
                                masthead("Could not open output file").
                                message("Exception encountered when attempting to open "
                                        + "the saved file. See below:").
                                styleClass(Dialog.STYLE_CLASS_NATIVE).
                                showException(ex);
                        }
                    }
                    progressBar.progressProperty().unbind();
                    progressBar.setVisible(false);
                    summaryLabel.textProperty().unbind();
                    summaryLabel.setText(summary);
                    closeButton.setDisable(false);
                    closeMenuItem.setDisable(false);
                    setCheckIsPcrButton();
                }
        });
        service.setOnCancelled(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                Dialogs writeCancelled = Dialogs.create().title("Writing Cancelled").
                    masthead("Cancelled writing to file").
                    message("User cancelled writing primers to file.").
                    styleClass(Dialog.STYLE_CLASS_NATIVE);
                writeCancelled.showInformation();
                progressBar.progressProperty().unbind();
                progressBar.setVisible(false);
                summaryLabel.textProperty().unbind();
                summaryLabel.setText(summary);
                closeButton.setDisable(false);
                closeMenuItem.setDisable(false);
                setCheckIsPcrButton();
            }
        });
        service.setOnFailed(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                Action writeFailed = Dialogs.create().title("Writing failed").
                    masthead("Could not write primers to file").
                    message("Exception encountered when attempting to write "
                            + "primers to file. See below:").
                    styleClass(Dialog.STYLE_CLASS_NATIVE).
                    showException(e.getSource().getException());
                progressBar.progressProperty().unbind();
                progressBar.setVisible(false);
                summaryLabel.textProperty().unbind();
                summaryLabel.setText(summary);
                closeButton.setDisable(false);
                closeMenuItem.setDisable(false);
                setCheckIsPcrButton();
            }
        });
        progressBar.setVisible(true);
        progressBar.progressProperty().bind(service.progressProperty());
        summaryLabel.textProperty().bind(service.messageProperty());
        closeButton.setDisable(true);
        closeMenuItem.setDisable(true);
        checkIsPcrButton.setText("Cancel");
        checkIsPcrButton.setOnAction(new EventHandler<ActionEvent>(){
           @Override
           public void handle(ActionEvent actionEvent){
                service.cancel();

            }
       });
        service.start();
    }
    
    private void writeDesignToFile() throws IOException{        
        if (designTextSummary.getText().isEmpty()){
            Dialogs noPrimersError = Dialogs.create().title("Nothing to save").
                    masthead("No designs to save").
                    message("No primer designs were made, no file can be saved.")
                    .styleClass(Dialog.STYLE_CLASS_NATIVE);
            noPrimersError.showError();
            return;
        }
       FileChooser fileChooser = new FileChooser();
       fileChooser.getExtensionFilters().add(
               new FileChooser.ExtensionFilter("Text  (*.txt)", "*.txt"));
       fileChooser.setTitle("Write primer designs to file...");
       fileChooser.setInitialDirectory(new File(getProperty("user.home")));
       File wFile = fileChooser.showSaveDialog(resultPane.getScene().getWindow());
       if (wFile == null){
           return;
       }else if (!wFile.getName().endsWith(".txt")){
            //annoying bug with filechooser means extension might not be appended
            wFile = new File(wFile.getAbsolutePath() + ".txt");
       }
        String des[] = designTextSummary.getText().split("\n");//want platform agnostic newlines
        FileWriter fw = new FileWriter(wFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        for (String d: des){
            bw.write(d);
            bw.newLine();
        }
        bw.close();
        
        Action response = Dialogs.create().title("Done").
                    masthead("Finished writing").
                    message("Primer designs successfully written to " + 
                            wFile.getAbsolutePath() + "\n\nDo you want to open "
                    + "this file now?").
                    actions(Dialog.ACTION_YES, Dialog.ACTION_NO).
                    styleClass(Dialog.STYLE_CLASS_NATIVE).
                    showConfirm();

        if (response == Dialog.ACTION_YES){
            try{
                //Desktop.getDesktop().open(f);
                openFile(wFile);
            }catch (IOException ex){
                Action openFailed = Dialogs.create().title("Open failed").
                    masthead("Could not open output file").
                    message("Exception encountered when attempting to open "
                            + "the saved file. See below:").
                    styleClass(Dialog.STYLE_CLASS_NATIVE).
                    showException(ex);
            }
        }
    }
    
    private void writeRefSeqsToFile() throws IOException{        
        if (refSeqs.isEmpty()){
            Dialogs noPrimersError = Dialogs.create().title("Nothing to save").
                    masthead("No reference sequences to save").
                    message("No reference sequences created, no file to be saved.")
                    .styleClass(Dialog.STYLE_CLASS_NATIVE);
            noPrimersError.showError();
            return;
        }
       FileChooser fileChooser = new FileChooser();
       fileChooser.getExtensionFilters().add(
               new FileChooser.ExtensionFilter("Text  (*.txt)", "*.txt"));
       fileChooser.setTitle("Write reference sequences to file...");
       fileChooser.setInitialDirectory(new File(getProperty("user.home")));
       File wFile = fileChooser.showSaveDialog(resultPane.getScene().getWindow());
       if (wFile == null){
           return;
       }else if (!wFile.getName().endsWith(".txt")){
            //annoying bug with filechooser means extension might not be appended
            wFile = new File(wFile.getAbsolutePath() + ".txt");
       }
        FileWriter fw = new FileWriter(wFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        for (String id: refSeqs.keySet()){
            bw.write(">" + id);
            bw.newLine();
            bw.write(splitStringOnLength(refSeqs.get(id), 60, System.getProperty("line.separator")));
            bw.newLine();
        }
        bw.close();
        
        Action response = Dialogs.create().title("Done").
                    masthead("Finished writing").
                    message("Reference sequences successfully written to " + 
                            wFile.getAbsolutePath() + "\n\nDo you want to open "
                    + "this file now?").
                    actions(Dialog.ACTION_YES, Dialog.ACTION_NO).
                    styleClass(Dialog.STYLE_CLASS_NATIVE).
                    showConfirm();

        if (response == Dialog.ACTION_YES){
            try{
                //Desktop.getDesktop().open(f);
                openFile(wFile);
            }catch (IOException ex){
                Action openFailed = Dialogs.create().title("Open failed").
                    masthead("Could not open output file").
                    message("Exception encountered when attempting to open "
                            + "the saved file. See below:").
                    styleClass(Dialog.STYLE_CLASS_NATIVE).
                    showException(ex);
            }
        }
    }
    
    private void checkIsPcrResults() throws MalformedURLException, IOException{
        
        final Service<ObservableList<Primer3Result>> service = new Service<ObservableList<Primer3Result>>(){
            @Override
            protected Task<ObservableList<Primer3Result>> createTask(){
                return new Task<ObservableList<Primer3Result>>(){
                    @Override
                    protected ObservableList<Primer3Result> call() throws IOException {
                        final ObservableList<Primer3Result> newData = FXCollections.observableArrayList();
                        updateProgress(0, data.size());
                        int p = 0;
                        for (Primer3Result r: data){
                            p++;
                            updateMessage("Checking primer pair " + p + " . . .");
                            r.setIsPcrResults(0);
                            if (r.getIsPcrUrl() != null){
                                URL url = new URL(r.getIsPcrUrl());
                                BufferedReader in = new BufferedReader(
                                    new InputStreamReader(url.openStream()));
                                String inputLine;
                                while ((inputLine = in.readLine()) != null){
                                    if (inputLine.contains("PcrResult=pack")){
                                        Integer n = r.getIsPcrResults();
                                        r.setIsPcrResults(++n);
                                    }
                                }
                            }
                            updateProgress(p, data.size());
                            newData.add(r);
                        }
                        return newData;
                    }
                };
            }
            
        };
        
        service.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
                @Override
                public void handle (WorkerStateEvent e){
                    ObservableList<Primer3Result> d = 
                            (ObservableList<Primer3Result>) e.getSource().getValue();
                    data.removeAll(data);
                    data.addAll(d);
                    final ArrayList<Primer3Result> nonSpecific = new ArrayList<>();
                    for (Primer3Result r: data){
                        if (r.getIsPcrResults() > 1){
                            nonSpecific.add(r);
                        }
                    }
                    StringBuilder msg = new StringBuilder();
                    if (nonSpecific.size() > 0){
                        msg.append("WARNING: ").append(nonSpecific.size()).append(" primer pair");
                        if (nonSpecific.size() == 1){
                            msg.append( " appears ");
                        }else{
                            msg.append("s appear ");
                        }
                        msg.append("to amplify more than one genomic region.\n\n");
                    }
                    msg.append(d.size()).append(" total primer pairs checked by in-silico PCR");
                    final String message = msg.toString();
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run(){
                            ispcrResCol.setVisible(true);
                            progressBar.progressProperty().unbind();
                            progressBar.setVisible(false);
                            summaryLabel.textProperty().unbind();
                            summaryLabel.setText(summary);
                            writeFileMenuItem.setDisable(false);
                            closeButton.setDisable(false);
                            closeMenuItem.setDisable(false);
                            setCheckIsPcrButton();
                            Dialogs inf = Dialogs.create().title("Done").
                            masthead("Finished Checking isPCR Results").
                            message(message).
                            styleClass(Dialog.STYLE_CLASS_NATIVE);
                            if (nonSpecific.isEmpty()){
                                inf.showInformation();
                            }else{
                                inf.showWarning();
                            }
                        }
                    });
                    
                }
        });
        service.setOnFailed(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                Action writeFailed = Dialogs.create().title("isPCR failed").
                    masthead("Could not check primer pairs by in-silico PCR").
                    message("Exception encountered when attempting to check "
                            + "primers by in-silico PCR. See below:").
                    styleClass(Dialog.STYLE_CLASS_NATIVE).
                    showException(e.getSource().getException());
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
                progressBar.setVisible(false);
                summaryLabel.textProperty().unbind();
                summaryLabel.setText(summary);
                writeFileMenuItem.setDisable(false);
                closeButton.setDisable(false);
                closeMenuItem.setDisable(false);
                setCheckIsPcrButton();
            }
        });
        service.setOnCancelled(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                Dialogs writeCancelled = Dialogs.create().title("isPCR Cancelled").
                    masthead("Primer pair checks cancelled").
                    message("isPCR checks by in-silico PCR were cancelled by user.").
                    styleClass(Dialog.STYLE_CLASS_NATIVE);
                writeCancelled.showInformation();
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
                progressBar.setVisible(false);
                summaryLabel.textProperty().unbind();
                summaryLabel.setText(summary);
                writeFileMenuItem.setDisable(false);
                closeButton.setDisable(false);
                closeMenuItem.setDisable(false);
                setCheckIsPcrButton();
            }
        });
        checkIsPcrButton.setText("Cancel");
        checkIsPcrButton.setOnAction(new EventHandler<ActionEvent>(){
           @Override
           public void handle(ActionEvent actionEvent){
                service.cancel();

            }
       });
        progressBar.setVisible(true);
        progressBar.progressProperty().bind(service.progressProperty());
        summaryLabel.textProperty().bind(service.messageProperty());
        writeFileMenuItem.setDisable(true);
        closeButton.setDisable(true);
        closeMenuItem.setDisable(true);
        service.start();
        
    }
    
    private void setCheckIsPcrButton(){
        checkIsPcrButton.setText("Check isPCR Results");
        checkIsPcrButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent e){
                try{
                    checkIsPcrResults();
                }catch(final IOException ex){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Action writeFailed = Dialogs.create().title("isPCR Check Failed").
                                masthead("Error performing in-silico PCR checks on primers.").
                                message("Exception encountered when attempting perform in-silico "
                                        + "PCR checks via UCSC webiste. See below:").
                                styleClass(Dialog.STYLE_CLASS_NATIVE).
                                showException(ex);
                        }
                    });
                }
            }
        });
    }
    
    
    private void openFile(File f) throws IOException{
        String command;
        //Desktop.getDesktop().open(f);
        if (System.getProperty("os.name").equals("Linux")) {
            command = "xdg-open " + f;
        }else if (System.getProperty("os.name").equals("Mac OS X")) {
            command = "open " + f;
        }else if (System.getProperty("os.name").contains("Windows")){
            command = "cmd /C start " + f;
        }else {
            return;
        }
        Runtime.getRuntime().exec(command);
    }
}
