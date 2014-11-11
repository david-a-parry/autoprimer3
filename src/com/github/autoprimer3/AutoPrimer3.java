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

import com.github.autoprimer3.GeneDetails.Exon;
import static com.github.autoprimer3.ReverseComplementDNA.reverseComplement;
import java.io.BufferedInputStream;
import javafx.scene.input.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

/**
 *
 * @author david
 */
public class AutoPrimer3 extends Application implements Initializable{
    
    @FXML
    AnchorPane mainPane;
    //tabs
    @FXML
    TabPane mainTabPane;
    @FXML
    Tab genesTab;
    @FXML
    Tab primerTab;
    @FXML
    Tab coordTab;
    //Genes tab components
    @FXML 
    Button runButton;
    @FXML
    Button cancelButton;
    @FXML
    Button refreshButton;
    @FXML
    ChoiceBox genomeChoiceBox;
    @FXML
    ChoiceBox databaseChoiceBox;
    @FXML
    ChoiceBox snpsChoiceBox;
    @FXML
    ChoiceBox designToChoiceBox;
    @FXML
    TextField minDistanceTextField;
    @FXML
    TextField flankingRegionsTextField;
    @FXML
    TextField genesTextField;
    @FXML
    ProgressIndicator progressIndicator = new ProgressIndicator();
    @FXML
    Label progressLabel;
    
    //Coordinates tab components
    @FXML
    TextArea regionsTextArea;
    @FXML
    ChoiceBox genomeChoiceBox2;
    @FXML
    ChoiceBox snpsChoiceBox2;
    @FXML
    TextField minDistanceTextField2;
    @FXML
    TextField flankingRegionsTextField2;
    @FXML
    ProgressIndicator progressIndicator2 = new ProgressIndicator();
    @FXML
    Label progressLabel2;
    @FXML 
    Button runButton2;
    @FXML
    Button cancelButton2;
    @FXML
    Button loadFileButton;
    @FXML
    Label loadFileLabel;
    
    //Primer3 Settings tab components
    @FXML
    TextField minSizeTextField;
    @FXML
    TextField optSizeTextField;
    @FXML
    TextField maxSizeTextField;
    @FXML
    TextField maxDiffTextField;
    @FXML
    TextField sizeRangeTextField;
    @FXML
    TextField maxMisprimeTextField;
    @FXML
    TextField minTmTextField;
    @FXML
    TextField optTmTextField;
    @FXML
    TextField maxTmTextField;
    @FXML
    TextField splitRegionsTextField;
    @FXML
    ChoiceBox misprimingLibraryChoiceBox;
    @FXML
    Button resetValuesButton;
    
    Boolean CANRUN = false;
    final GetUcscBuildsAndTables buildsAndTables = new GetUcscBuildsAndTables();
    LinkedHashMap<String, String> buildsToDescriptions = new LinkedHashMap<>();
    HashMap<String, String> buildToMap = new HashMap<>();
    HashMap<String, LinkedHashSet<String>> buildToTable = new HashMap<>();
    File primer3ex; 
    Path mispriming_libs;
    Path thermo_config;
    String defaultSizeRange = "150-250 100-300 301-400 401-500 501-600 "
                + "601-700 701-850 851-1000 1000-2000";
    HashMap<TextField, String> defaultPrimer3Values = new HashMap<>();
        
        
    File configDirectory;
    AutoPrimer3Config ap3Config = new AutoPrimer3Config();
    
    @Override
    public void start(final Stage primaryStage) {
        try {
            AnchorPane page = (AnchorPane) FXMLLoader.load(com.github.autoprimer3.AutoPrimer3.class.getResource("AutoPrimer3.fxml"));
            Scene scene = new Scene(page);
            primaryStage.setScene(scene);
            primaryStage.setTitle("AutoPrimer3");
            //scene.getStylesheets().add(com.github.autoprimer3.AutoPrimer3.class.
            //        getResource("autoprimer3.css").toExternalForm());
            primaryStage.setResizable(false);
            primaryStage.show();
            primaryStage.getIcons().add(new Image(this.getClass().
                    getResourceAsStream("icon.png")));
            
            
        } catch (Exception ex) {
            Logger.getLogger(AutoPrimer3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        genesTextField.requestFocus();
        progressLabel2.textProperty().bind(progressLabel.textProperty());
        progressIndicator2.progressProperty().bind(progressIndicator.progressProperty());
        genomeChoiceBox2.selectionModelProperty().bind(genomeChoiceBox.selectionModelProperty());
        snpsChoiceBox2.selectionModelProperty().bind(snpsChoiceBox.selectionModelProperty());
        setLoading(true);
        try{
            ap3Config.readConfig();
            buildsToDescriptions = ap3Config.getBuildToDescription();
            buildToMap = ap3Config.getBuildToMapMaster();
            buildToTable = ap3Config.getBuildToTables();
        }catch (IOException|ClassNotFoundException ex){
            Dialogs configError = Dialogs.create().title("Config Error").
            masthead("Error Reading AutoPrimer3 Config").
            message("AutoPrimer3 encountered an error reading config details"
                    + " - please see the exception below and report this error.").
                    styleClass(Dialog.STYLE_CLASS_NATIVE);
            configError.showException(ex);
        }
        try{
            primer3ex = File.createTempFile("primer3", "exe");
            primer3ex.deleteOnExit();
            InputStream inputStream;
            if (System.getProperty("os.name").equals("Mac OS X")){
                    inputStream = this.getClass().
                            getResourceAsStream("primer3_core_macosx");
            }else if (System.getProperty("os.name").equals("Linux")){
                if (System.getProperty("os.arch").endsWith("64")){
                    inputStream = this.getClass().
                            getResourceAsStream("primer3_core");
                }else{
                    inputStream = this.getClass().
                            getResourceAsStream("primer3_core32");
                }
            }else{
                inputStream = this.getClass().
                            getResourceAsStream("primer3_core");
            }
            OutputStream outputStream = new FileOutputStream(primer3ex);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
            }
            inputStream.close();
            outputStream.close();
            primer3ex.setExecutable(true);
            File mispriming_zip = File.createTempFile("misprime", ".zip" );
            mispriming_libs = Files.createTempDirectory("mispriming_lib");
            mispriming_zip.deleteOnExit();
            inputStream = this.getClass().
                    getResourceAsStream("mispriming_libraries.zip");
            outputStream = new FileOutputStream(mispriming_zip);
            while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
            }
            inputStream.close();
            outputStream.close();
            ZipFile zip = new ZipFile(mispriming_zip);
            zip.extractAll(mispriming_libs.toString());
            thermo_config = Files.createTempDirectory("thermo_config");
            File thermo_zip = File.createTempFile("primer_config", ".zip");
            thermo_zip.deleteOnExit();
            inputStream = this.getClass().
                    getResourceAsStream("primer3_config.zip");
            outputStream = new FileOutputStream(thermo_zip);
            while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
            }
            zip = new ZipFile(thermo_zip);
            zip.extractAll(thermo_config.toString());
        }catch(IOException|ZipException ex){
            //TO DO - catch this properly
            ex.printStackTrace();
        }
        designToChoiceBox.getSelectionModel().selectFirst();
        refreshButton.setOnAction(new EventHandler<ActionEvent>(){
           @Override
           public void handle(ActionEvent actionEvent){
                refreshDatabase();
            }
        });

        genomeChoiceBox.getSelectionModel().selectedIndexProperty().addListener
            (new ChangeListener<Number>(){
            @Override
            public void changed (ObservableValue ov, Number value, final Number new_value){ 
                if (new_value.intValue() >= 0){
                    final String id = (String) genomeChoiceBox.getItems().get(new_value.intValue());
                    genomeChoiceBox.setTooltip(new Tooltip (ap3Config.getBuildToDescription().get(id)));
                    getBuildTables(id);
                }
            }
        });
        
        
        genomeChoiceBox.getItems().clear();
        genomeChoiceBox.getItems().addAll(new ArrayList<>(buildsToDescriptions.keySet()));
        genomeChoiceBox.getSelectionModel().selectFirst();
        genomeChoiceBox2.getItems().clear();
        genomeChoiceBox2.getItems().addAll(new ArrayList<>(buildsToDescriptions.keySet()));
        genomeChoiceBox2.getSelectionModel().selectFirst();
        File misprimeDir = mispriming_libs.toFile();
        misprimingLibraryChoiceBox.getItems().add("none");
        for (File f: misprimeDir.listFiles()){
            misprimingLibraryChoiceBox.getItems().add(f.getName());
        }
        misprimingLibraryChoiceBox.getSelectionModel().selectFirst();
        minDistanceTextField.addEventFilter(KeyEvent.KEY_TYPED, checkNumeric());
        minDistanceTextField2.addEventFilter(KeyEvent.KEY_TYPED, checkNumeric());
        flankingRegionsTextField.addEventFilter(KeyEvent.KEY_TYPED, checkNumeric());
        flankingRegionsTextField2.addEventFilter(KeyEvent.KEY_TYPED, checkNumeric());
        minSizeTextField.addEventFilter(KeyEvent.KEY_TYPED, checkNumeric());
        optSizeTextField.addEventFilter(KeyEvent.KEY_TYPED, checkNumeric());
        maxSizeTextField.addEventFilter(KeyEvent.KEY_TYPED, checkNumeric());
        maxDiffTextField.addEventFilter(KeyEvent.KEY_TYPED, checkNumeric());
        maxMisprimeTextField.addEventFilter(KeyEvent.KEY_TYPED, checkNumeric());
        sizeRangeTextField.addEventFilter(KeyEvent.KEY_TYPED, checkRange());
        minTmTextField.addEventFilter(KeyEvent.KEY_TYPED, checkDecimal());
        optTmTextField.addEventFilter(KeyEvent.KEY_TYPED, checkDecimal());
        maxTmTextField.addEventFilter(KeyEvent.KEY_TYPED, checkDecimal());
        splitRegionsTextField.addEventFilter(KeyEvent.KEY_TYPED, checkNumeric());
        defaultPrimer3Values.put(minSizeTextField, "18");
        defaultPrimer3Values.put(optSizeTextField, "20");
        defaultPrimer3Values.put(maxSizeTextField, "27");
        defaultPrimer3Values.put(maxDiffTextField, "10");
        defaultPrimer3Values.put(minTmTextField, "57.0");
        defaultPrimer3Values.put(optTmTextField, "59.0");
        defaultPrimer3Values.put(maxTmTextField, "62.0");
        defaultPrimer3Values.put(splitRegionsTextField, "300");
        defaultPrimer3Values.put(maxMisprimeTextField, "12");
        defaultPrimer3Values.put(sizeRangeTextField, defaultSizeRange);
        
        minDistanceTextField.textProperty().addListener(
                new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, final String newValue){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!minDistanceTextField2.getText().equals(newValue)){
                        minDistanceTextField2.setText(newValue);
                        }
                    }
                });
            }
        });
        
        minDistanceTextField2.textProperty().addListener(
                new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, final String newValue){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!minDistanceTextField.getText().equals(newValue)){
                        minDistanceTextField.setText(newValue);
                        }
                    }
                });
            }
        });
        
        flankingRegionsTextField.textProperty().addListener(
                new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, final String newValue){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!flankingRegionsTextField2.getText().equals(newValue)){
                        flankingRegionsTextField2.setText(newValue);
                        }
                    }
                });
            }
        });
        
        flankingRegionsTextField2.textProperty().addListener(
                new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, final String newValue){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!flankingRegionsTextField.getText().equals(newValue)){
                            flankingRegionsTextField.setText(newValue);
                        }
                    }
                });
            }
        });
        
        resetValuesButton.setOnAction(new EventHandler<ActionEvent>(){
           @Override
           public void handle(ActionEvent actionEvent){
                resetPrimerSettings();
            }
        });
            
        mainTabPane.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Tab>(){
                    @Override
                    public void changed (ObservableValue<? extends Tab> ov, 
                            Tab ot, Tab nt) {
                        if (ot.equals(primerTab)){
                            resetEmptyPrimerSettings();
                        }
                        if (nt.equals(genesTab)){
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    genesTextField.requestFocus();
                                }
                            });
                        }
                    }
                }
        );
        //this error is actually quite annoying, we should change this so that
        //it is only checked on design or change of tab focus
        sizeRangeTextField.focusedProperty().addListener(new ChangeListener<Boolean>(){
            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                    Boolean oldValue, Boolean newValue ){
                if (!sizeRangeTextField.isFocused()){
                    if (!checkSizeRange(sizeRangeTextField)){
                        displaySizeRangeError();
                    }
                }
            }
        });
        
        if (ap3Config.getBuildToDescription().isEmpty()){
            connectToUcsc();   
        }else{
            setLoading(false);
        }
        Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    genesTextField.requestFocus();
                }
            }
        );
    }
    
    private void connectToUcsc(){
        progressIndicator.setProgress(-1);
        final Task<LinkedHashMap<String, String>> getBuildsTask = 
                new Task<LinkedHashMap<String, String>>(){
            @Override
            protected LinkedHashMap<String, String> call() {
                buildsAndTables.connectToUcsc();
                return buildsAndTables.getBuildToDescription();
            }
        };
        getBuildsTask.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                LinkedHashMap<String, String> buildIds = 
                        (LinkedHashMap<String, String>) e.getSource().getValue();
                genomeChoiceBox.getItems().clear();
                if (! buildsToDescriptions.equals(buildIds)){
                    genomeChoiceBox.getItems().addAll(buildIds.keySet());
                    genomeChoiceBox.getSelectionModel().selectFirst();
                    genomeChoiceBox2.getItems().addAll(buildIds.keySet());
                    genomeChoiceBox2.getSelectionModel().selectFirst();
                    ap3Config.setBuildToDescription(buildIds);
                    ap3Config.setBuildToMapMaster(buildsAndTables.getBuildToMapMaster());
                    ap3Config.setBuildToTables(buildToTable);
                    try{
                        System.out.println("Writing output");
                        ap3Config.writeConfig();
                    }catch (IOException ex){
                        ex.printStackTrace();
                    }
                }
                setLoading(false);
                
//                    progressIndicator.setProgress(0);
                /*keep progress running as we have just indirectly called the
                 * getTablesTask by selecting our first genome
                 */
            }
        });

        getBuildsTask.setOnFailed(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                progressIndicator.setProgress(0);
                System.out.println(e.getSource().getException());
                setLoading(false);
                setCanRun(false);
            }
        });
        getBuildsTask.setOnCancelled(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                progressIndicator.setProgress(0);                
                progressLabel.setText("UCSC connection cancelled.");
                setLoading(false);
                setCanRun(false);
            }
        });
        cancelButton.setOnAction(new EventHandler<ActionEvent>(){
           @Override
           public void handle(ActionEvent actionEvent){
                getBuildsTask.cancel();

            }
       });
        progressLabel.setText("Connecting to UCSC...");
        new Thread(getBuildsTask).start();
    }
    
    
    private void setTables(LinkedHashSet<String> tables){
        LinkedHashSet<String> genes = new LinkedHashSet<>();
        LinkedHashSet<String> snps = new LinkedHashSet<>();
        for (String t: tables){
            if (t.equals("refGene") || t.equals("knownGene") || 
                    t.equals("ensGene") || t.equals("xenoRefGene")){
                genes.add(t);
            }else if (t.matches("^snp\\d+(\\w+)*")){
                snps.add(t);
            }
        }
        databaseChoiceBox.getItems().clear();
        if (genes.isEmpty()){
            databaseChoiceBox.getItems().add("No gene databases found - please choose another genome.");
            setCanRun(false);
            databaseChoiceBox.getSelectionModel().selectFirst();
        }else{
            databaseChoiceBox.getItems().addAll(genes);
            if (databaseChoiceBox.getItems().contains("refGene")){
                databaseChoiceBox.getSelectionModel().select("refGene");
            }else{
                databaseChoiceBox.getSelectionModel().selectFirst();
            }
        }
        snpsChoiceBox.getItems().clear();
        snpsChoiceBox.getItems().add("No");
        snpsChoiceBox.getItems().addAll(snps);
        snpsChoiceBox.getSelectionModel().selectFirst();
    }
    
    private void getBuildTables(final String id){
        databaseChoiceBox.getItems().clear();
        snpsChoiceBox.getItems().clear();
        if (ap3Config.getBuildToTables().containsKey(id)){
            setTables(ap3Config.getBuildToTables().get(id));
            return;
        }
        setLoading(true);
        progressLabel.setText("Getting database information for " + id);
        final Task<LinkedHashSet<String>> getTablesTask = new Task<LinkedHashSet<String>>(){
            @Override
            protected LinkedHashSet<String> call() {
                System.out.println("Called getTablesTask...");
                return buildsAndTables.getAvailableTables(id);
            }
        };

        getTablesTask.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                System.out.println("getTablesTask succeeded.");
                LinkedHashSet<String> tables = (LinkedHashSet<String>) e.getSource().getValue();
                if (! tables.equals(ap3Config.getBuildToTables().get(id))){
                    ap3Config.getBuildToTables().put(id, tables);
                    ap3Config.setBuildToDescription(ap3Config.getBuildToDescription());
                    ap3Config.setBuildToMapMaster(ap3Config.getBuildToMapMaster());
                    try{
                        System.out.println("Writing output");
                        ap3Config.writeConfig();
                    }catch (IOException ex){
                        ex.printStackTrace();
                    }
                }
                setTables(tables);
                progressIndicator.setProgress(0);
                progressLabel.setText("");
                setLoading(false);
            }
        });

        getTablesTask.setOnFailed(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                progressLabel.setText("Get Tables Task Failed");
                System.out.println("getTablesTask failed.");
                System.out.println(e.getSource().getException());
                setLoading(false);
                setCanRun(false);
            }
        });

        getTablesTask.setOnCancelled(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                progressLabel.setText("Get Tables Task Cancelled");
                System.out.println("getTablesTask cancelled.");
                setLoading(false);
                setCanRun(false);
            }
        });

        cancelButton.setOnAction(new EventHandler<ActionEvent>(){
           @Override
           public void handle(ActionEvent actionEvent){
                getTablesTask.cancel();

            }
       });

        progressIndicator.setProgress(-1);
        new Thread(getTablesTask).start();
    }
    
    
    private void displaySizeRangeError(){
        Dialogs sizeRangeError = Dialogs.create().title("Invalid Size Range").
                masthead("Invalid Primer Product Size Range values").
                message("Primer Product Size Range field must be in the format '"
                        + "100-200 200-400' etc.")
                .styleClass(Dialog.STYLE_CLASS_NATIVE);
        sizeRangeError.showError();
    }
    
    EventHandler<KeyEvent> checkNumeric(){
        return new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent ke) {
                if (!ke.getCharacter().matches("\\d")){
                    ke.consume();
                }
            }
        };
    }
    
    EventHandler<KeyEvent> checkDecimal(){
        return new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent ke) {
                if (!ke.getCharacter().matches("[\\d.]")){
                    ke.consume();
                }
            }
        };
    }
    
    EventHandler<KeyEvent> checkRange(){
        return new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent ke) {
                if (!ke.getCharacter().matches("[\\d-\\s]")){
                    ke.consume();
                }
            }
        };
    }
    
    private boolean checkSizeRange(TextField field){
        List<String> split = Arrays.asList(field.getText().split("\\s+"));
        for (String s: split){
            if (!s.matches("\\d+-\\d+")){
                return false;
            }
        }
        return true;
    }
            
    private void resetPrimerSettings(){
        for (TextField f: defaultPrimer3Values.keySet()){
            f.setText(defaultPrimer3Values.get(f));
        }
    }
    
    private void resetEmptyPrimerSettings(){
        for (TextField f: defaultPrimer3Values.keySet()){
            if (f.getText().isEmpty()){
                f.setText(defaultPrimer3Values.get(f));
            }else if (f.getText().trim().length() < 1){
                f.setText(defaultPrimer3Values.get(f));
            }
        }
        
    }
    
    
    public void refreshDatabase(){
        if (genomeChoiceBox.getSelectionModel().isEmpty()){//implies no connection to UCSC
            connectToUcsc();
        }else{//we've got a connection to UCSC but want to refresh the database info for current build
            final String id = (String) genomeChoiceBox.getSelectionModel().getSelectedItem();
                getBuildTables(id);
        }
    }
    
    public void designPrimersToGene(){
        //check that we've got at least one gene in our input box
        if (!genesTextField.getText().matches(".*\\w.*")){
            return;//TO DO show ERROR dialog maybe
        }
        resetEmptyPrimerSettings();
        if (!checkSizeRange(sizeRangeTextField)){
            displaySizeRangeError();
            return;
        }
        final int optSize = Integer.valueOf(splitRegionsTextField.getText());
        final int flanks = Integer.valueOf(flankingRegionsTextField.getText());
        final int designBuffer = Integer.valueOf(minDistanceTextField.getText());
        if (flanks <= (designBuffer + Integer.valueOf(maxSizeTextField.getText()))){
            Dialogs flanksError = Dialogs.create().title("Flanks Error").
                    masthead("Invalid values for 'Min distance'/"
                            + "'Flanking region' fields.").
                    message("Flanking region value must be greater than Min "
                            + "distance value  plus maximum primer size.")
                    .styleClass(Dialog.STYLE_CLASS_NATIVE);
            flanksError.showError();
            return;
        }
        
        if (Integer.valueOf(optSizeTextField.getText()) < 
                Integer.valueOf(minSizeTextField.getText())){
            Dialogs sizeError = Dialogs.create().title("Primer Size Error").
                    masthead("Invalid values for primer size fields.").
                    message("Min Primer Size can not be greater than Opt "
                            + "Primer Size.")
                    .styleClass(Dialog.STYLE_CLASS_NATIVE);
            sizeError.showError();
            return;
        }
        
        if (Integer.valueOf(maxSizeTextField.getText()) < 
                Integer.valueOf(optSizeTextField.getText())){
            Dialogs sizeError = Dialogs.create().title("Primer Size Error").
                    masthead("Invalid values for primer size fields.").
                    message("Max Primer Size can not be less than Opt "
                            + "Primer Size.")
                    .styleClass(Dialog.STYLE_CLASS_NATIVE);
            sizeError.showError();
            return;
        }
        
        
        if (Double.valueOf(optTmTextField.getText()) < 
                Double.valueOf(minTmTextField.getText())){
            Dialogs sizeError = Dialogs.create().title("Primer TM Error").
                    masthead("Invalid values for primer TM fields.").
                    message("Min Primer TM can not be greater than Opt "
                            + "Primer TM.")
                    .styleClass(Dialog.STYLE_CLASS_NATIVE);
            sizeError.showError();
            return;
        }
        
        if (Double.valueOf(maxTmTextField.getText()) < 
                Double.valueOf(optTmTextField.getText())){
            Dialogs sizeError = Dialogs.create().title("Primer TM Error").
                    masthead("Invalid values for primer TM fields.").
                    message("Max Primer TM can not be less than Opt "
                            + "Primer TM.")
                    .styleClass(Dialog.STYLE_CLASS_NATIVE);
            sizeError.showError();
            return;
        }
        
        
        final Task<GeneSearchResult> geneSearchTask = 
                new Task<GeneSearchResult>(){
            @Override
            protected GeneSearchResult call() {
                HashMap<String, LinkedHashSet> searchResults = new HashMap<>();
                updateMessage("Connecting to UCSC database...");
                GetGeneCoordinates geneSearcher = getGeneSearcher();
                if (geneSearcher == null){
                    updateMessage("Connection failed.");
                    return null;
                }
                updateMessage("Connection succeeded.");
                
                LinkedHashSet<String> searchStrings = new LinkedHashSet<>();
                Collections.addAll(searchStrings, genesTextField.getText().split("\\s+"));
                LinkedHashSet<String> notFound = new LinkedHashSet<>();
                LinkedHashSet<String> nonCodingTargets = new LinkedHashSet<>();
                LinkedHashSet<GeneDetails> targets = new LinkedHashSet<>();

                searchStrings.removeAll(Arrays.asList(null, ""));
                for (String s : searchStrings){
                    if (! s.matches(".*\\w.*")){
                        continue;
                    }
                    updateMessage("searching for " + s + "...");
                    ArrayList<GeneDetails> found = getGeneDetails(s, geneSearcher);
                    if (found.isEmpty()){
                        notFound.add(s);
                    }else{
                        if (designToChoiceBox.getSelectionModel().
                                getSelectedItem().equals("Coding regions")){
                            boolean matched = false;
                            for (GeneDetails f : found){
                                if (f.isCoding()){
                                    matched = true;
                                    targets.add(f);
                                }else{
                                    nonCodingTargets.add(f.getId());
                                }
                            }
                            if (! matched){
                                notFound.add(s);
                            }
                        }else{
                            targets.addAll(found);
                        }
                    }
                }
                return new GeneSearchResult(targets, notFound, nonCodingTargets,
                        geneSearcher);
            }
         };
        
        geneSearchTask.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                progressIndicator.progressProperty().unbind();
                progressLabel.textProperty().unbind();        
                GeneSearchResult result = 
                        (GeneSearchResult) e.getSource().getValue();
                final LinkedHashSet<GeneDetails> targets = result.getFound();
                LinkedHashSet<String> notFound = result.getNotFound();
                LinkedHashSet<String> nonCodingTargets = result.getNonCodingTargets();
                final GetGeneCoordinates geneSearcher = result.getGeneSearcher();
                if (targets.isEmpty()){
                    String message = "No targets found for your search terms.";
                    if (!nonCodingTargets.isEmpty()){
                        message = message + " " + nonCodingTargets.size() + 
                                " non-coding transcripts were found.\n";
                    }

                    Dialogs noTargetsError = Dialogs.create().title("No Genes Found").
                            masthead("No targets found.").
                            message(message)
                            .styleClass(Dialog.STYLE_CLASS_NATIVE);
                    noTargetsError.showError();
                    setRunning(false);
                    return;
                }else if (! notFound.isEmpty()){
                    StringBuilder message = new StringBuilder("Matching transcripts could"
                            + "not be found for the following genes using your search "
                            + "parameters:\n" + String.join(", ", notFound) + ".\n\n");
                    if (!nonCodingTargets.isEmpty()){
                        message.append("The following transcripts were found but are "
                                + "non-coding:\n"+ String.join(", ", nonCodingTargets) 
                                + ".\n\n");
                    }
                    message.append("Continue anyway?");
                    Action response = Dialogs.create().title("").
                            masthead("Could Not Find Some Genes").
                            message(message.toString()).
                            actions(Dialog.ACTION_YES, Dialog.ACTION_NO).
                            styleClass(Dialog.STYLE_CLASS_NATIVE).
                            showConfirm();

                    if (response == Dialog.ACTION_NO){
                        setRunning(false);
                        progressLabel.setText("Design cancelled");
                        progressIndicator.progressProperty().set(0);
                        return;
                    }
                }
                
                final Task<HashMap<String, ArrayList>> designTask = 
                        new Task<HashMap<String, ArrayList>>(){
                    @Override
                    protected HashMap<String, ArrayList> call() {

                    //get our transcript targets using databaseChoiceBox and genesTextField
                        double prog = 0;
                        Integer pair = 0;
                        //check we've found at least one target and warn if any search term could not be found

                        //find 5' and 3' ends of transcripts/CDS + flanks
                        ArrayList<GenomicRegionSummary> genomicRegions = new ArrayList<>();
                        for (GeneDetails t : targets){
                            int start = getGeneStart(t, flanks);
                            int end = getGeneEnd(t, flanks);
                            GenomicRegionSummary r = new GenomicRegionSummary(t.getChromosome(), 
                                    start, end, null, null, t.getId(), t.getSymbol());
                            genomicRegions.add(r);
                        }
                        GenomicRegionSummary merger = new GenomicRegionSummary();
                        merger.mergeRegionsByPosition(genomicRegions);

                        ArrayList<Primer3Result> primers = new ArrayList<>();
                        ArrayList<String> designs = new ArrayList<>();
                        //get FASTA sequence
                        int regionNumber = 0;
                        double incrementPerRegion = 100/genomicRegions.size();
                        for (GenomicRegionSummary r : genomicRegions){
                            regionNumber++;
                            updateMessage("Getting DNA for region " + regionNumber + 
                                    " of " + genomicRegions.size());
                            updateProgress(-1, -1);
                            String genome = (String) genomeChoiceBox.getSelectionModel().getSelectedItem();
                            SequenceFromDasUcsc seqFromDas = new SequenceFromDasUcsc();
                            String dna = seqFromDas.retrieveSequence(
                                    genome, r.getChromosome(), r.getStartPos(), r.getEndPos());
                            //System.out.println(dna);//debug only
                            String snpDb = (String) snpsChoiceBox.getSelectionModel().getSelectedItem();
                            ArrayList<GenomicRegionSummary> snps = new ArrayList<>();
                            if (! snpDb.equals("No")){
                                try{
                                    updateMessage("Getting SNPs for region " + regionNumber + 
                                    " of " + genomicRegions.size());
                                    updateProgress(-1, -1);
                                    snps = geneSearcher.GetSnpCoordinates
                                        (r.getChromosome(), r.getStartPos(), r.getEndPos(), 
                                        genome, snpDb);
                                }catch(SQLException ex){
                                    //TO DO
                                    ex.printStackTrace();
                                }
                            }

                            ArrayList<GenomicRegionSummary> exonRegions = new ArrayList<>();
                            int minus_strand = 0;
                            int plus_strand = 0;
                            for (GeneDetails t : targets){
                                if (! t.getChromosome().equals(r.getChromosome())){
                                    continue;
                                }
                                int start = getGeneStart(t, flanks);
                                if (start > r.getEndPos()){
                                    continue;
                                }
                                int end = getGeneEnd(t, flanks);
                                if (end < r.getStartPos()){
                                    continue;
                                }
                                ArrayList<Exon> exons = t.getExons();

                                if (t.getStrand().equals("-")){
                                    minus_strand++;
                                }else{
                                    plus_strand++;
                                }
                                for (Exon e : exons){
                                    if (designToChoiceBox.getSelectionModel().getSelectedItem()
                                        .equals("Coding regions")){
                                        if (! e.isCodingExon()){
                                            continue;
                                        }
                                    }
                                    String id = t.getId().concat("_ex").
                                            concat(Integer.toString(e.getOrder()));
                                    String name = t.getSymbol();
                                    GenomicRegionSummary ex; 
                                    if (designToChoiceBox.getSelectionModel().getSelectedItem()
                                        .equals("Coding regions")){
                                        Exon ce = e.getExonCodingRegion();
                                        ex = new GenomicRegionSummary(
                                                r.getChromosome(), ce.getStart(), 
                                                ce.getEnd(), null, null, id, name);
                                    }else{
                                        ex = new GenomicRegionSummary(
                                                r.getChromosome(), e.getStart(), 
                                                e.getEnd(), null, null, id, name);
                                    }
                                    exonRegions.add(ex);
                                }
                            }
                            boolean onMinusStrand = false;
                            if (minus_strand > plus_strand){
                                onMinusStrand = true;
                            }
                            merger.mergeRegionsByPosition(exonRegions);
                            numberExons(exonRegions, onMinusStrand);
                            exonRegions = splitLargeRegionsMergeSmallRegions(exonRegions, 
                                    optSize, designBuffer, onMinusStrand);
                            if (onMinusStrand){
                                Collections.reverse(exonRegions);
                            }
                            //get substring for each exon and design primers using primer3
                            double incrementPerExRegion = 
                                    incrementPerRegion /exonRegions.size();
                            int exonNumber = 0;
                            for (GenomicRegionSummary er: exonRegions){
                                exonNumber++;
                                prog += incrementPerExRegion;
                                int tStart = er.getStartPos() - r.getStartPos();
                                int tEnd = 1 + er.getEndPos() - r.getStartPos();
                                int subsStart = tStart - flanks > 0 ? tStart - flanks : 0;
                                int subsEnd = tEnd + flanks - 1 < dna.length() ?  tEnd + flanks - 1 : dna.length();
                                StringBuilder dnaTarget = new StringBuilder(
                                        dna.substring(subsStart, tStart).toLowerCase());
                                dnaTarget.append(dna.substring(tStart, tEnd-1)
                                        .toUpperCase());
                                dnaTarget.append(dna.substring(tEnd -1, subsEnd)
                                        .toLowerCase());
                                if (minus_strand > plus_strand){
                                    dnaTarget = new StringBuilder(reverseComplement
                                            (dnaTarget.toString())
                                    );
                                }
                                //System.out.println(er.getName() + ": " + er.getId());//debug only
                                //System.out.println(dnaTarget.toString());//debug only

                                // to do - combine snp searching and gene finding to make
                                //more efficient(i.e. use same connection)
                                ArrayList<String> excludeRegions = new ArrayList<>();

                                for (GenomicRegionSummary s: snps){
                                    if (s.getStartPos() < r.getStartPos() + subsStart){
                                        continue;
                                    }else if(s.getEndPos() > r.getStartPos() + subsEnd){
                                        break;
                                    }
                                    Integer excludeStart = s.getStartPos() - r.getStartPos()
                                             - subsStart - 1;
                                    Integer excludeEnd = s.getEndPos() - r.getStartPos()
                                            - subsStart - 1;
                                    Integer excludeLength = 1 +  excludeEnd - excludeStart;
                                    if (onMinusStrand){
                                        excludeStart = dnaTarget.length() - excludeStart
                                                - excludeLength;
                                    }
                                    if (excludeStart + excludeLength < dnaTarget.length()){
                                        excludeRegions.add(excludeStart + "," + excludeLength);
                                    }else if (excludeStart < dnaTarget.length() - 1 ){
                                        int diff = dnaTarget.length() -1 - excludeStart;
                                        excludeRegions.add(excludeStart + "," + diff);
                                    }
                                }
                                //get info from text fields for primer3 options
                                String target = Integer.toString(flanks - designBuffer) + 
                                        "," + Integer.toString(tEnd - tStart + (designBuffer * 2));
                                String seqid = (er.getName() + ": " + er.getId());
                                //design primers
                                updateMessage("Designing primers for part " + exonNumber + 
                                        " of " + exonRegions.size() + "...");
                                ArrayList<String> result = designPrimers(seqid, 
                                        dnaTarget.toString(), target, String.join(" ", excludeRegions));

                                updateProgress(prog, 100);
                                designs.add(String.join("\n", result));

                                //parse primer3 output and write our output
                                primers.add(parsePrimer3Output(++pair,  er.getName(), er.getId(), 
                                        r.getChromosome(), 1 + er.getStartPos() - flanks, result));

                            }

                        }
                        HashMap<String, ArrayList> primerResult = new HashMap<>();
                        primerResult.put("primers", primers);
                        primerResult.put("design", designs);
                        return primerResult;
                    }
                };
                progressIndicator.progressProperty().unbind();
                progressIndicator.progressProperty().bind(designTask.progressProperty());
                progressLabel.textProperty().bind(designTask.messageProperty());
                designTask.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
                    @Override
                    public void handle (WorkerStateEvent e){
                        progressIndicator.progressProperty().unbind();
                        progressIndicator.progressProperty().set(100);
                        progressLabel.textProperty().unbind();
                        setRunning(false);
                        HashMap<String, ArrayList> result = 
                                (HashMap<String, ArrayList>) e.getSource().getValue();
                        if (result == null){
                            return;
                        }
                        if (result.get("primers").isEmpty()){
                            progressLabel.setText("No primers designed.");
                            Dialogs noPrimersError = Dialogs.create().title("No PrimersFound").
                                masthead("No primers found for your targets.").
                                message("No primer designs were attempted for your targets")
                                .styleClass(Dialog.STYLE_CLASS_NATIVE);
                                noPrimersError.showError();       
                                progressIndicator.progressProperty().set(0);
                            return;
                        }
                        if (result.get("design").isEmpty()){
                            progressLabel.setText("No primers designed.");
                            Dialogs noPrimersError = Dialogs.create().title("No PrimersFound").
                                masthead("No primers found for your targets.").
                                message("No primer designs were attempted for your targets")
                                .styleClass(Dialog.STYLE_CLASS_NATIVE);
                            noPrimersError.showError();       
                            progressIndicator.progressProperty().set(0);
                            return;
                        }
                        progressLabel.setText(result.get("primers").size() +
                                " primer pairs designed.");
                        FXMLLoader tableLoader = new FXMLLoader(getClass().
                                               getResource("Primer3ResultView.fxml"));
                        try{
                            Pane tablePane = (Pane) tableLoader.load();
                            Primer3ResultViewController resultView = 
                                    (Primer3ResultViewController) tableLoader.getController();
                            Scene tableScene = new Scene(tablePane);
                            Stage tableStage = new Stage();
                            tableStage.setScene(tableScene);
                            //tableScene.getStylesheets().add(AutoPrimer3.class
                            //        .getResource("autoprimer3.css").toExternalForm());
                            resultView.displayData(result.get("primers"), result.get("design"));
                            tableStage.setTitle("AutoPrimer3 Results");
                            tableStage.getIcons().add(new Image(this.getClass()
                                    .getResourceAsStream("icon.png")));
                            tableStage.initModality(Modality.NONE);
                            tableStage.show();
                       }catch (Exception ex){
            //               Dialogs.showErrorDialog(null, "Error displaying"
            //                       + " results from Find Regions Method.",
            //                       "Find Regions Error!", "SnpViewer", ex);
                           ex.printStackTrace();
                       }
                    }
                });
                designTask.setOnCancelled(new EventHandler<WorkerStateEvent>(){
                    @Override
                    public void handle (WorkerStateEvent e){
                        setRunning(false);
                        progressLabel.textProperty().unbind();
                        progressLabel.setText("Design cancelled");
                        progressIndicator.progressProperty().unbind();
                        progressIndicator.progressProperty().set(0);
                    }

                });
                designTask.setOnFailed(new EventHandler<WorkerStateEvent>(){
                    @Override
                    public void handle (WorkerStateEvent e){
                        setRunning(false);
                        progressLabel.textProperty().unbind();
                        progressLabel.setText("Design failed!");
                        progressIndicator.progressProperty().unbind();
                        progressIndicator.progressProperty().set(0);
                    }

                });
                cancelButton.setOnAction(new EventHandler<ActionEvent>(){
                   @Override
                   public void handle(ActionEvent actionEvent){
                        designTask.cancel();
                    }
                });
                setRunning(true);
                new Thread(designTask).start();
            }

            

        });
        
        geneSearchTask.setOnCancelled(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                setRunning(false);
                progressLabel.textProperty().unbind();
                progressLabel.setText("Design cancelled");
                progressIndicator.progressProperty().set(0);
            }

        });
        geneSearchTask.setOnFailed(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                setRunning(false);
                progressLabel.textProperty().unbind();
                progressLabel.setText("Search failed!");
                progressIndicator.progressProperty().unbind();
                progressIndicator.progressProperty().set(0);
            }

        });
        cancelButton.setOnAction(new EventHandler<ActionEvent>(){
           @Override
           public void handle(ActionEvent actionEvent){
                geneSearchTask.cancel();
            }
        });
        setRunning(true);
        new Thread(geneSearchTask).start();   
    
    }
    
    private void numberExons(ArrayList<GenomicRegionSummary> exonRegions,
            boolean minusStrand){
        int n = 0;
        for (GenomicRegionSummary e: exonRegions){
            if (minusStrand){
                e.setName(e.getName() + "_ex" + (exonRegions.size() - n));
            }else{
                e.setName(e.getName() + "_ex" + (n+1));
            }
            n++;
        }
    }
    
    private ArrayList<GenomicRegionSummary> splitLargeRegionsMergeSmallRegions(
            ArrayList<GenomicRegionSummary> regions, Integer optSize, 
            Integer buffer, boolean minusStrand){
        ArrayList<GenomicRegionSummary> splitRegions = new ArrayList<>();
        for (GenomicRegionSummary r: regions){
            if (r.getLength() > optSize){
                //divide length by maxSize to determine no of products to make
                Double products = Math.ceil(r.getLength().doubleValue()/ 
                        optSize.doubleValue());
                if (products.intValue() < 2){
                    splitRegions.add(r);
                    continue;
                }
                //divide length by no. products and make each product
                Double productSize = r.getLength().doubleValue()/products;
                for (int i = 0; i < products.intValue(); i++){
                    int increment = i * productSize.intValue();
                    int startPos = r.getStartPos() + increment;
                    int endPos = startPos + productSize.intValue();
                    endPos = endPos < r.getEndPos() ? endPos : r.getEndPos();
                    String name;
                    if (minusStrand){
                        name = r.getName() + "_part" + (products.intValue() - i);
                    }else{
                        name = r.getName() + "_part" + (i+1);
                    }
                    ArrayList<String> ids = new ArrayList<>(); 
                    for (String id : r.getId().split("/")){
                        if (minusStrand){
                            ids.add(id + "_part" + (products.intValue() - i));
                        }else{
                            ids.add(id + "_part" + (i+1));
                        }
                    }
                    GenomicRegionSummary s = new GenomicRegionSummary(
                        r.getChromosome(), startPos, endPos,
                        r.getStartId(), r.getEndId(), String.join("/", ids), name);
                    if (i == products.intValue() - 1){
                        s.setEndPos(r.getEndPos());
                    }
                    splitRegions.add(s);
                }
            }else{
                splitRegions.add(r);
            }
        }
        ArrayList<GenomicRegionSummary> splitAndMergedRegions = new ArrayList<>();
        boolean smallRegion;
        do{
            smallRegion = false;
            splitAndMergedRegions.clear();
            for (int i = 0; i < splitRegions.size() - 1; i++){
                //merge any small and close regions
                int gap = splitRegions.get(i+1).getEndPos() - splitRegions.get(i).getStartPos();
                if (gap + (2*buffer)  <= optSize){
                    smallRegion = true;
                    String chrom = splitRegions.get(i).getChromosome();
                    int start = splitRegions.get(i).getStartPos();
                    int end = splitRegions.get(i+1).getEndPos();
                    String name = mergeNames(splitRegions.get(i).getName(), 
                            splitRegions.get(i+1).getName());
                    String id = mergeIds(splitRegions.get(i).getId(), 
                            splitRegions.get(i).getId());
                    splitAndMergedRegions.add(new GenomicRegionSummary(chrom, 
                            start, end, null, null, id, name));
                    for (int j = i +2; j < splitRegions.size(); j++){
                        splitAndMergedRegions.add(splitRegions.get(j));
                    }
                    splitRegions.clear();
                    splitRegions.addAll(splitAndMergedRegions);
                    break;
                }else{
                    splitAndMergedRegions.add(splitRegions.get(i));
                }
            }
            splitAndMergedRegions.add(splitRegions.get(splitRegions.size()-1));
        }while (smallRegion);
        return splitAndMergedRegions;
    }
    
    //create a new id from two genomic regions' ids
    private String mergeIds(String id1, String id2){
        ArrayList<String> merged = new ArrayList<>();
        List<String> ids1 = Arrays.asList(id1.split("/"));
        List<String> ids2 = Arrays.asList(id2.split("/"));
        LinkedHashMap<String, String> idToEx1 = new LinkedHashMap<>();
        LinkedHashMap<String, String> idToEx2 = new LinkedHashMap<>();
        for (String d: ids1){
            List<String> split = Arrays.asList(d.split("_ex"));
            if (split.size() > 1){
                idToEx1.put(split.get(0), split.get(1));
            }else{
                idToEx1.put(d, d);
            }
        }
        for (String d: ids2){
            List<String> split = Arrays.asList(d.split("_ex"));
            if (split.size() > 1){
                idToEx2.put(split.get(0), split.get(1));
            }else{
                idToEx2.put(d, d);
            }
        }
        for (String d: idToEx1.keySet()){
            if (idToEx2.containsKey(d)){
                merged.add(d + "_ex" + idToEx1.get(d) + idToEx2.get(d));
            }else{
                merged.add(d + "_ex" + idToEx1.get(d));
            }
        }
        for (String d: idToEx2.keySet()){
            if (! idToEx1.containsKey(d)){
                merged.add(d + "_ex" + idToEx2.get(d));
            }
        }
        return String.join("/", merged);
    }

    
    //create a new name from two genomic regions' names
    private String mergeNames(String name1, String name2){
        String name;
        List<String> geneName1 = Arrays.asList(name1.split("_ex"));
        List<String> geneName2 = Arrays.asList(name2.split("_ex"));
        if (geneName1.size() >= 2 && geneName2.size() >= 2 && 
                geneName1.get(0).equals(geneName2.get(0))){
            ArrayList<Integer> sizes = new ArrayList<>();
            for (int i = 1; i < geneName1.size(); i++){
                sizes.add(Integer.valueOf(geneName1.get(i)));
            }
            for (int i = 1; i < geneName2.size(); i++){
                sizes.add(Integer.valueOf(geneName2.get(i)));
            }
            Collections.sort(sizes);
            name = geneName1.get(0) + "_ex" + sizes.get(0) + 
                    "-" + sizes.get(sizes.size()-1);
        }else{
            name = name1 + "_and_" +  name2;
        }
        return name;
    }
    
    //get left and right primer from Primer3 output
    private Primer3Result parsePrimer3Output(int index, String name, String id,
            String chrom, int baseCoordinate, ArrayList<String> output){
        String left = "NOT FOUND";
        String right = "NOT FOUND";
        Integer leftStart = 0;
        Integer rightStart = 0;
       String productSize = "0";
        for (String res: output){
            if (res.startsWith("LEFT PRIMER")){
                List<String> split = Arrays.asList(res.split(" +"));
                left = split.get(split.size() -1);
                leftStart = Integer.valueOf(split.get(2));
            }else if (res.startsWith("RIGHT PRIMER")){
                List<String> split = Arrays.asList(res.split(" +"));
                right = split.get(split.size() -1);
                rightStart = Integer.valueOf(split.get(2));
            }else if (res.startsWith("PRODUCT SIZE:")){
                List<String> split = Arrays.asList(res.split(" +"));
                productSize = split.get(2).replaceAll("[^\\d/]", "");
                break;
            }
        }
        Primer3Result res = new Primer3Result();
        res.setLeftPrimer(left);
        res.setRightPrimer(right);
        res.setName(name);
        res.setTranscripts(id);
        res.setIndex(index);
        res.setChromosome(chrom);
        res.setProductSize(Integer.valueOf(productSize));
        if (Integer.valueOf(productSize) > 0){
            res.setLeftPosition(baseCoordinate + leftStart);
            res.setRightPosition(baseCoordinate + rightStart);
        }else{
            res.setLeftPosition(0);
            res.setRightPosition(0);
        }
        return res;
    }
    
    
    //for given parameters design primers and return result as an array of strings
    private ArrayList<String> designPrimers(String name, String dna, 
            String target, String exclude){
        ArrayList<String> result = new ArrayList<>();
        StringBuilder error = new StringBuilder(); 
        StringBuilder p3_job = new StringBuilder("SEQUENCE_TARGET=");
            p3_job.append(target).append("\n");
            p3_job.append("SEQUENCE_EXCLUDED_REGION=").append(exclude).append("\n");
            p3_job.append("SEQUENCE_ID=").append(name).append("\n");
            p3_job.append("SEQUENCE_TEMPLATE=").append(dna).append("\n");
            p3_job.append("PRIMER_TASK=pick_pcr_primers\n");
            p3_job.append("PRIMER_OPT_SIZE=").append(optSizeTextField.getText())
                    .append("\n");
            p3_job.append("PRIMER_MIN_SIZE=").append(minSizeTextField.getText())
                    .append("\n");
            p3_job.append("PRIMER_MAX_SIZE=").append(maxSizeTextField.getText())
                    .append("\n");
            p3_job.append("PRIMER_PRODUCT_SIZE_RANGE=")
                    .append(sizeRangeTextField.getText()).append("\n");
            p3_job.append("PRIMER_MIN_TM=")
                    .append(minTmTextField.getText()).append("\n");
            p3_job.append("PRIMER_OPT_TM=")
                    .append(optTmTextField.getText()).append("\n");
            p3_job.append("PRIMER_MAX_TM=")
                    .append(maxTmTextField.getText()).append("\n");
            p3_job.append("PRIMER_PAIR_MAX_DIFF_TM=")
                    .append(maxDiffTextField.getText()).append("\n");
            p3_job.append("PRIMER_THERMODYNAMIC_PARAMETERS_PATH=").
                    append(thermo_config.toString())
                    .append(System.getProperty("file.separator")).append("\n");
            String misprimeLibrary = (String) 
                misprimingLibraryChoiceBox.getSelectionModel().getSelectedItem();
            if (!misprimeLibrary.isEmpty()){
                if (! misprimeLibrary.matches("none")){
                    p3_job.append("PRIMER_MISPRIMING_LIBRARY=")
                            .append(mispriming_libs.toString())
                            .append(System.getProperty("file.separator"))
                            .append(misprimeLibrary).append("\n");
                    p3_job.append("PRIMER_MAX_LIBRARY_MISPRIMING=")
                            .append(maxMisprimeTextField.getText()).append("\n");
            
                }
            }
            
            p3_job.append("=");
            //System.out.println(p3_job.toString());//debug only
            ArrayList<String> command = new ArrayList<>();
            command.add(primer3ex.getAbsolutePath());
            command.add("-format_output");
            try{
                Process ps = new ProcessBuilder(command).start();
                try{
                    BufferedReader errorbuf = new BufferedReader
                            (new InputStreamReader( ps.getErrorStream()));
                    BufferedReader inbuf = new BufferedReader
                            (new InputStreamReader( ps.getInputStream()));
                    BufferedWriter out = new BufferedWriter(
                            new OutputStreamWriter(ps.getOutputStream()));
                    out.write(p3_job.toString());
                    out.flush();
                    out.close();

                    String line;
                    while ((line = inbuf.readLine()) != null) {
                        //System.out.println(line);//debug only
                        result.add(line);
                    }
                    while ((line = errorbuf.readLine()) != null){
                        System.out.println(line);//debug only
                        error.append(line);
                    }
                    int exit = ps.waitFor();
                    //System.out.println(exit);//debug only
                }catch(InterruptedException ex){
                    ex.printStackTrace();
                }
            }catch (IOException ex){
                //TO DO - Handle error!
                ex.printStackTrace();
            }

        return result;
    }
    
    /*this method gets the start coordinates of a gene based on 
    the values for the designToChoiceBox and the Flanking region choice box
    */
        
    private int getGeneStart(GeneDetails g, int flanks){
        int start;
        if (designToChoiceBox.getSelectionModel().getSelectedItem().equals("Coding regions")){
            start = g.getCdsStart();
        }else{
            start = g.getTxStart();
        }
        start -= flanks;
        if (start > 0){
            return start;
        }else{
            return 0;
        }
    }
    /*this method gets the end coordinates of a gene based on 
    the values for the designToChoiceBox and the Flanking region choice box
    */
        
    private int getGeneEnd(GeneDetails g, int flanks){
        int end;
        if (designToChoiceBox.getSelectionModel().getSelectedItem().equals("Coding regions")){
            end = g.getCdsEnd();
        }else{
            end = g.getTxEnd();
        }
        end += flanks;
        return end;
    }

    private GetGeneCoordinates getGeneSearcher(){
        if (databaseChoiceBox.getSelectionModel().getSelectedItem().equals("refGene") 
                || databaseChoiceBox.getSelectionModel().getSelectedItem().equals("xenoRefGene")){
            return new GetGeneCoordinates();
        }else if (databaseChoiceBox.getSelectionModel().getSelectedItem().equals("knownGene")){
            return new GetUcscGeneCoordinates();
        }else if (databaseChoiceBox.getSelectionModel().getSelectedItem().equals("ensGene")){
            return new GetEnsemblGeneCoordinates();
        }else{
            return null;
        }
    }
    
    private ArrayList<GeneDetails> getGeneDetails(String searchString, 
            GetGeneCoordinates geneSearcher){
        ArrayList<GeneDetails> genes = new ArrayList<>();
        if (databaseChoiceBox.getSelectionModel().getSelectedItem().equals("refGene") 
                || databaseChoiceBox.getSelectionModel().getSelectedItem().equals("xenoRefGene")){
            if (searchString.matches("[NX][MR]_\\w+(.\\d)*")){
                //is accession, need to remove the version number if present
                searchString = searchString.replaceAll("\\.\\d$", "");
                try{
                    genes.addAll(geneSearcher.getGeneFromId(searchString, 
                            (String) genomeChoiceBox.getSelectionModel().getSelectedItem(),
                            (String) databaseChoiceBox.getSelectionModel().getSelectedItem()));
                }catch(SQLException | GetGeneCoordinates.GetGeneExonsException ex){
                    //TO DO!
                    ex.printStackTrace();
                }
            }else{
                //is gene symbol (?)
                try{
                    genes.addAll(geneSearcher.getGeneFromSymbol(searchString, 
                            (String) genomeChoiceBox.getSelectionModel().getSelectedItem(),
                            (String) databaseChoiceBox.getSelectionModel().getSelectedItem()));
                }catch(SQLException | GetGeneCoordinates.GetGeneExonsException ex){
                    //TO DO!
                    ex.printStackTrace();
                }
            }
            
        }else if (databaseChoiceBox.getSelectionModel().getSelectedItem().equals("knownGene")){
            if (searchString.equals("uc\\d{3}[a-z]{3}\\.\\d")){
                //is accession
                try{
                    genes.addAll(geneSearcher.getGeneFromId(searchString, 
                            (String) genomeChoiceBox.getSelectionModel().getSelectedItem(),
                            (String) databaseChoiceBox.getSelectionModel().getSelectedItem()));
                }catch(SQLException | GetGeneCoordinates.GetGeneExonsException ex){
                    //TO DO!
                    ex.printStackTrace();
                }
            }else{
                //is gene symbol (?)
                try{
                    genes.addAll(geneSearcher.getGeneFromSymbol(searchString, 
                            (String) genomeChoiceBox.getSelectionModel().getSelectedItem(),
                            (String) databaseChoiceBox.getSelectionModel().getSelectedItem()));
                }catch(SQLException | GetGeneCoordinates.GetGeneExonsException ex){
                    //TO DO!
                    ex.printStackTrace();
                }
            }
        }else if (databaseChoiceBox.getSelectionModel().getSelectedItem().equals("ensGene")){
            if (searchString.matches("ENS\\w*T\\d{11}.*\\d*")){
                //is accession
                try{
                    genes.addAll(geneSearcher.getGeneFromId(searchString, 
                            (String) genomeChoiceBox.getSelectionModel().getSelectedItem(),
                            (String) databaseChoiceBox.getSelectionModel().getSelectedItem()));
                }catch(SQLException | GetGeneCoordinates.GetGeneExonsException ex){
                    //TO DO!
                    ex.printStackTrace();
                }
            }else{
                //is gene symbol (?)
                try{
                    genes.addAll(geneSearcher.getGeneFromSymbol(searchString, 
                            (String) genomeChoiceBox.getSelectionModel().getSelectedItem(),
                            (String) databaseChoiceBox.getSelectionModel().getSelectedItem()));
                }catch(SQLException | GetGeneCoordinates.GetGeneExonsException ex){
                    //TO DO!
                    ex.printStackTrace();
                }
            }
        }
                /*if (t.equals("refGene") || t.equals("knownGene") || 
                            t.equals("ensGene") || t.equals("xenoRefGene"))*/
        for (int i = 0; i < genes.size(); i++){
            System.out.println(genes.get(i).getSymbol() + ":" + genes.get(i).getId()
                     + ":" + genes.get(i).getChromosome() + ":" + genes.get(i).getTxStart()
                     + "-" + genes.get(i).getTxEnd());
        }
        return genes;
    }
    
    public Boolean getCanRun(){
        return CANRUN;
    }

    private void setCanRun(boolean designable){
        CANRUN = designable;
        runButton.setDisable(!CANRUN);
        cancelButton.setDisable(CANRUN);
        runButton2.setDisable(!CANRUN);
        cancelButton2.setDisable(CANRUN);
    }
    
    private void setRunning(boolean running){
        setCanRun(!running);
        refreshButton.setDisable(running);
        genomeChoiceBox.setDisable(running);
        genomeChoiceBox2.setDisable(running);
        databaseChoiceBox.setDisable(running);
        snpsChoiceBox.setDisable(running);
        snpsChoiceBox2.setDisable(running);
        designToChoiceBox.setDisable(running);
        minDistanceTextField.setDisable(running);
        flankingRegionsTextField.setDisable(running);
        flankingRegionsTextField2.setDisable(running);
        genesTextField.setDisable(running);
    }
    
    private void setLoading(boolean loading){
        setCanRun(!loading);
        setRunning(loading);
        if (loading){
            progressIndicator.setProgress(-1);
        }else{
            progressIndicator.setProgress(0);
        }
    }
    
    
    
    
    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(AutoPrimer3.class, (java.lang.String[])null);
    }
    
}

