/*things to add:
    server choice
    output reference sequence
    write primers and primer3 output to file
    limit size of regions, number of genes per run
*/

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
import javafx.scene.input.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
    @FXML
    MenuBar menuBar;
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
    Button clearButton;
    @FXML
    Label regionsLabel;
    
    
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
    @FXML
    CheckBox autoSelectMisprimingLibraryCheckBox;
    
    Boolean CANRUN = false;
    final BuildToMisprimingLibrary buildToMisprime = new BuildToMisprimingLibrary();
    Boolean autoSelectMisprime = true;
    final GetUcscBuildsAndTables buildsAndTables = new GetUcscBuildsAndTables();
    
    File primer3ex; 
    File thermoConfig;
    String defaultSizeRange = "150-250 100-300 301-400 401-500 501-600 "
                + "601-700 701-850 851-1000 1000-2000";
    HashMap<TextField, String> defaultPrimer3Values = new HashMap<>();
    String serverUrl = "http://genome-euro.ucsc.edu"; 
    
    File configDirectory;
    AutoPrimer3Config ap3Config;
    File misprimeDir;
    HashSet<String> checkedAlready = new HashSet<>();
//we use this hashmap to ensure we only check tables for genomes once per session
    
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
        menuBar.setUseSystemMenuBar(true);
        genesTextField.requestFocus();
        progressLabel2.textProperty().bind(progressLabel.textProperty());
        progressIndicator2.progressProperty().bind(progressIndicator.progressProperty());
        genomeChoiceBox2.itemsProperty().bind(genomeChoiceBox.itemsProperty());
        genomeChoiceBox2.selectionModelProperty().bind(genomeChoiceBox.selectionModelProperty());
        snpsChoiceBox2.itemsProperty().bind(snpsChoiceBox.itemsProperty());
        snpsChoiceBox2.selectionModelProperty().bind(snpsChoiceBox.selectionModelProperty());
        cancelButton2.cancelButtonProperty().bind(cancelButton.cancelButtonProperty());
        cancelButton2.onActionProperty().bind(cancelButton.onActionProperty());
        setLoading(true);
        try{
            ap3Config = new AutoPrimer3Config();
            ap3Config.readConfig();
        }catch (IOException|ClassNotFoundException ex){
            Dialogs configError = Dialogs.create().title("Config Error").
            masthead("Error Reading AutoPrimer3 Config").
            message("AutoPrimer3 encountered an error reading config details"
                    + " - please see the exception below and report this error.").
                    styleClass(Dialog.STYLE_CLASS_NATIVE);
            configError.showException(ex);
        }
        try{
            primer3ex = ap3Config.extractP3Executable();
            misprimeDir = ap3Config.extractMisprimingLibs();
            thermoConfig = ap3Config.extractThermoConfig();
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
                    if (autoSelectMisprime){
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                selectMisprimingLibrary(id);
                            }
                        });
                        
                    }
                    getBuildTables(id);
                }
            }
        });
        
        autoSelectMisprimingLibraryCheckBox.selectedProperty().addListener(
                new ChangeListener<Boolean>(){
            @Override
            public void changed (ObservableValue ov, Boolean value, final Boolean newValue){
                autoSelectMisprime = newValue;
                final String id = (String) genomeChoiceBox.getSelectionModel().getSelectedItem();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (newValue){
                            selectMisprimingLibrary(id);
                        }else{
                            misprimingLibraryChoiceBox.getSelectionModel().select("none");
                        }
                    }
                });
   
            }
        });
        
        genomeChoiceBox.getItems().clear();
        genomeChoiceBox.getItems().addAll(new ArrayList<>(ap3Config.getBuildToDescription().keySet()));
        genomeChoiceBox.getSelectionModel().selectFirst();
        
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
            checkUcscGenomes();
        }
        Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    genesTextField.requestFocus();
                }
            }
        );
    }
    
    private void selectMisprimingLibrary(String id){
        String stub = id.replaceAll("\\d*$", "");
        String lib = buildToMisprime.getMisprimingLibrary(stub);
        misprimingLibraryChoiceBox.getSelectionModel().select(lib);
    }
    
    private void checkUcscGenomes(){
        final Task<LinkedHashMap<String, String>> getGenomesTask = 
                new Task<LinkedHashMap<String, String>>(){
            @Override
            protected LinkedHashMap<String, String> call() {
                System.out.println("Checking genome list.");
                buildsAndTables.connectToUcsc();
                return buildsAndTables.getBuildToDescription();
            }
        };
        getGenomesTask.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                LinkedHashMap<String, String> buildIds = 
                        (LinkedHashMap<String, String>) e.getSource().getValue();
                boolean rewriteConfig = false;
                if (! ap3Config.getBuildToDescription().equals(buildIds)){
                    //warn and repopulate genome choicebox
                    //TO DO WARN!
                    System.out.println("Genome list has changed - repopulating genome choice box");
                    rewriteConfig = true;
                    String currentSel = (String) genomeChoiceBox.getSelectionModel().getSelectedItem();
                    genomeChoiceBox.getItems().clear();
                    genomeChoiceBox.getItems().addAll(buildIds.keySet());
                    if (genomeChoiceBox.getItems().contains(currentSel)){
                        genomeChoiceBox.getSelectionModel().select(currentSel);
                    }else{
                        genomeChoiceBox.getSelectionModel().selectFirst();
                    }
                    ap3Config.setBuildToDescription(buildIds);
                }else{
                    System.out.println("Genome list is the same.");
                }
                
                if (!ap3Config.getBuildToMapMaster().equals(buildsAndTables.getBuildToMapMaster())){
                    ap3Config.setBuildToMapMaster(buildsAndTables.getBuildToMapMaster());
                    rewriteConfig = true;
                }
                if (rewriteConfig){
                    try{
                        System.out.println("Writing output");
                        ap3Config.writeConfig();
                    }catch (IOException ex){
                        //TO DO - handle this error!
                        ex.printStackTrace();
                    }
                }
            }
        });

        getGenomesTask.setOnFailed(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                progressIndicator.setProgress(0);
                //TO DO - HANDLE THIS EXCEPTION
                System.out.println(e.getSource().getException());
            }
        });
       new Thread(getGenomesTask).start();
    }
    
    
    
    
    
    private void checkUcscTables(final String genome){
        //Background check that tables for given genome are up to date
        final Task<LinkedHashSet<String>> checkUcscTablesTask = 
            new Task<LinkedHashSet<String>>(){
            @Override
            protected LinkedHashSet<String> call() {
                System.out.println("Checking tables for " + genome);
                return buildsAndTables.getAvailableTables(genome);
            }
        };
        checkUcscTablesTask.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                System.out.println("Finished getting tables for " + genome);
                LinkedHashSet<String> tables = 
                        (LinkedHashSet<String>) e.getSource().getValue();
                if (! ap3Config.getBuildToTables().get(genome).equals(tables)){
                    /*Tables differ, but we need to check whether it affects
                    relevant tables (i.e. genes or SNPs)
                    */
                    System.out.println("Tables differ!");
                    if (configTablesDiffer(tables, 
                            ap3Config.getBuildToTables().get(genome))){
                        /*if available genes or snps differ we need to alert
                        user and change fields in choiceboxes
                        */
                        System.out.println("SNP/Gene tables differ!");
                        // TO DO - ALERT USER IF GENOME STILL SELECTED
                        String g = (String) genomeChoiceBox.getSelectionModel().getSelectedItem();
                        if (! g.equals(genome)){
                            String curSnp = (String) snpsChoiceBox.getSelectionModel().getSelectedItem();
                            snpsChoiceBox.getItems().clear();
                            snpsChoiceBox.getItems().add("No");
                            snpsChoiceBox.getItems().addAll(getSnpsFromTables(tables));
                            if (snpsChoiceBox.getItems().contains(curSnp)){
                                snpsChoiceBox.getSelectionModel().select(curSnp);
                            }else{
                                snpsChoiceBox.getSelectionModel().selectFirst();
                            }

                            String curGene = (String) databaseChoiceBox.getSelectionModel().getSelectedItem();
                            databaseChoiceBox.getItems().clear();
                            databaseChoiceBox.getItems().add("No");
                            databaseChoiceBox.getItems().addAll(getSnpsFromTables(tables));
                            if (databaseChoiceBox.getItems().contains(curGene)){
                                databaseChoiceBox.getSelectionModel().select(curGene);
                            }else{
                                databaseChoiceBox.getSelectionModel().selectFirst();
                            }
                        }
                    }/*even if available genes and snps are the same we just
                       change and rewrite rewrite the config file silently
                       to prevent this happening until tables change again
                     */
                    ap3Config.getBuildToTables().put(genome, tables);
                    try{
                        System.out.println("Writing output");
                        ap3Config.writeConfig();
                    }catch (IOException ex){
                        //TO DO - handle this error!
                        ex.printStackTrace();
                    }
                    
                }else{
                    System.out.println("Tables are the same");
                }
            }
        });
        checkUcscTablesTask.setOnFailed(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                //TO DO - HANDLE THIS EXCEPTION
                System.out.println(e.getSource().getException());
            }
        });
        
       new Thread(checkUcscTablesTask).start();
    }
    
    //only checks snp and gene tables in lists to see if they are the same
    private boolean configTablesDiffer(LinkedHashSet<String> tables, 
            LinkedHashSet<String> configTables){
        ArrayList<String> tableComp = new ArrayList<>();
        ArrayList<String> configComp = new ArrayList<>();
        for (String t: tables){
            if (matchesGeneTable(t) || matchesSnpTable(t)){
                tableComp.add(t);
            }
        }
        for (String t: configTables){
            if (matchesGeneTable(t) || matchesSnpTable(t)){
                configComp.add(t);
            }
        }
        return (tableComp.containsAll(configComp) && configComp.containsAll(tableComp));
    }
    
    private LinkedHashSet<String> getGenesFromTables(LinkedHashSet<String> tables){
        LinkedHashSet<String> genes = new LinkedHashSet<>();
        for (String t: tables){
            if (matchesGeneTable(t)){
                genes.add(t);
            }
        }
        return genes;
    }

    private LinkedHashSet<String> getSnpsFromTables(LinkedHashSet<String> tables){
        LinkedHashSet<String> snps = new LinkedHashSet<>();
        for (String t: tables){
            if (matchesSnpTable(t)){
                snps.add(t);
            }
        }
        return snps;
    }
    
    private boolean matchesGeneTable(String t){
        return (t.equals("refGene") || t.equals("knownGene") || 
                t.equals("ensGene") || t.equals("xenoRefGene"));
    }
    
    private boolean matchesSnpTable(String t){
        return t.matches("^snp\\d+(\\w+)*");
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
                genomeChoiceBox.getItems().addAll(buildIds.keySet());
                genomeChoiceBox.getSelectionModel().selectFirst();
                ap3Config.setBuildToDescription(buildIds);
                ap3Config.setBuildToMapMaster(buildsAndTables.getBuildToMapMaster());
                try{
                    System.out.println("Writing output");
                    ap3Config.writeConfig();
                }catch (IOException ex){
                    ex.printStackTrace();
                }
                setLoading(false);
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
        LinkedHashSet<String> genes = getGenesFromTables(tables);
        LinkedHashSet<String> snps = getSnpsFromTables(tables);
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
            if (! checkedAlready.contains(id)){
                checkUcscTables(id);
                checkedAlready.add(id);
                return;
            }
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
    
    public void loadRegionsFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select input file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "BED file", "*.bed", "VCF file", "*.vcf", "*vcf.gz", "text file", "*.txt", "*txt.gz"));
        fileChooser.setInitialDirectory(new File (System.getProperty("user.home")));
        setCanRun(false);
        final File inFile = fileChooser.showOpenDialog(mainPane.getScene().getWindow());
        if (inFile != null){
            final Task<ArrayList<String>> loadFileTask = 
                        new Task<ArrayList<String>>(){
                @Override
                protected ArrayList<String> call() {
                    ArrayList<String> regionStrings = new ArrayList<>();
                    try{
                        updateMessage("Opening " + inFile.getName());
                        BufferedReader br;
                        if (inFile.getName().endsWith(".gz")){
                            InputStream gzipStream = new GZIPInputStream(new FileInputStream(inFile));
                            Reader decoder = new InputStreamReader(gzipStream);
                            br = new BufferedReader(decoder);
                        }else{
                            br = new BufferedReader(new FileReader(inFile));
                        }
                        String line;
                        int n = 0;
                        while ((line = br.readLine()) != null) {
                           n++;
                           if (line.startsWith("#")){//skip header lines
                               continue;
                           }
                           updateMessage("Parsing line " + n + "...");
                           GenomicRegionSummary region = RegionParser.readRegion(line);
                           if (region != null){
                               String r = region.getChromosome() + ":" + 
                                       region.getStartPos() + "-" + region.getEndPos();
                               regionStrings.add(r);
                           }else{
                               //TO DO 
                                //display error!!!!
                                System.out.println("Invalid region:\t" + line);
                           }
                        }
                        br.close();
                    }catch(IOException ex){
                        //TO DO - display error
                        ex.printStackTrace();
                    }
                    return regionStrings;
                }
            };
            loadFileTask.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
                @Override
                public void handle (WorkerStateEvent e){
                    setRunning(false);
                    progressIndicator.progressProperty().unbind();
                    progressIndicator.progressProperty().set(0);
                    progressLabel.textProperty().unbind();
                    ArrayList<String> loadedRegions = (ArrayList<String>) 
                            e.getSource().getValue();
                    int n = 0;
                    for (String r: loadedRegions){
                        n++;
                        regionsTextArea.appendText(r + "\n");
                    }
                    progressLabel.setText("Added " + n + " regions from " 
                            + inFile.getName() + ".");
                }
            });
            loadFileTask.setOnCancelled(new EventHandler<WorkerStateEvent>(){
                @Override
                public void handle (WorkerStateEvent e){
                    setRunning(false);
                    progressLabel.textProperty().unbind();
                    progressLabel.setText("Loading cancelled");
                    progressIndicator.progressProperty().unbind();
                    progressIndicator.progressProperty().set(0);
                }
            });
            loadFileTask.setOnFailed(new EventHandler<WorkerStateEvent>(){
                @Override
                public void handle (WorkerStateEvent e){
                    setRunning(false);
                    progressLabel.textProperty().unbind();
                    progressLabel.setText("Loading failed!");
                    progressIndicator.progressProperty().unbind();
                    progressIndicator.progressProperty().set(0);
                }

            });
            cancelButton.setOnAction(new EventHandler<ActionEvent>(){
               @Override
               public void handle(ActionEvent actionEvent){
                    loadFileTask.cancel();
                }
            });
            progressIndicator.progressProperty().unbind();
            progressLabel.textProperty().unbind();
            progressIndicator.progressProperty().bind(loadFileTask.progressProperty());
            progressLabel.textProperty().bind(loadFileTask.messageProperty());
            setRunning(true);
            new Thread(loadFileTask).start();
        }
    }
    
    public void clearRegions(){
        regionsTextArea.clear();
        progressLabel.setText("");
    }
    
    public void designPrimersToCoordinates(){
        final String regionsInput = regionsTextArea.getText();
        final int optSize = Integer.valueOf(splitRegionsTextField.getText());
        final int flanks = Integer.valueOf(flankingRegionsTextField.getText());
        final int designBuffer = Integer.valueOf(minDistanceTextField.getText());    
        if (! regionsInput.isEmpty()){
            if (! checkDesignParameters()){
                return;
            }
            final Task<HashMap<String, ArrayList>> designTask = 
                    new Task<HashMap<String, ArrayList>>(){
                @Override
                protected HashMap<String, ArrayList> call() {
                ArrayList<GenomicRegionSummary> regions = new ArrayList<>();
                GetGeneCoordinates geneSearcher = null;
                ArrayList<Primer3Result> primers = new ArrayList<>();
                ArrayList<String> designs = new ArrayList<>();
                List<String> tempRegions = Arrays.asList(regionsInput.split("\\n"));
                int n = 1;
                for (String r: tempRegions){
                    if (! r.matches(".*\\w.*")){
                        continue;
                    }
                    //region parser methods
                    GenomicRegionSummary region = RegionParser.readRegion(r);
                    if (region != null){
                        region.setName("Region_" + n++);
                        region.setId(region.getChromosome() + ":" + region.getStartPos()
                                + "-" + region.getEndPos());
                        regions.add(region);
                    }else{
                        //TO DO 
                        //display error!!!!
                        System.out.println("Invalid region:\t" + r);
                    }
                }
                if (regions.isEmpty()){
                    //TO DO - complain about empty regions
                    return null;
                }
                SequenceFromDasUcsc seqFromDas = new SequenceFromDasUcsc();
                GenomicRegionSummary merger = new GenomicRegionSummary();
                merger.mergeRegionsByPosition(regions);
                regions = splitLargeRegionsMergeSmallRegions(regions, optSize, 
                        designBuffer, false);
                int pair = 0;
                for (GenomicRegionSummary r: regions){
                    int start = r.getStartPos() - flanks > 0 ? 
                                r.getStartPos() - flanks : 0;
                    int end = r.getEndPos() + flanks;
                    System.out.println("Region " + r.getChromosome() + ":" + 
                            r.getStartPos() + "-" + r.getEndPos());
                    System.out.println("Using start = " + start + " and end = "
                            + end);
                    String genome = (String) genomeChoiceBox.getSelectionModel().getSelectedItem();
                    String dna = seqFromDas.retrieveSequence(
                            genome, r.getChromosome(), start, end);
                    //System.out.println(dna);//debug only
                    String snpDb = (String) snpsChoiceBox.getSelectionModel().getSelectedItem();
                    ArrayList<GenomicRegionSummary> snps = new ArrayList<>();
                    if (! snpDb.equals("No")){
                        try{
                            if (geneSearcher == null){
                                geneSearcher = new GetGeneCoordinates();
                            }
                            snps = geneSearcher.GetSnpCoordinates
                                (r.getChromosome(), start, end, genome, snpDb);
                        }catch(SQLException ex){
                            //TO DO
                            ex.printStackTrace();
                        }
                    }
                    ArrayList<String> excludeRegions = new ArrayList<>();
                    for (GenomicRegionSummary s: snps){
                        if (s.getStartPos() < start){
                            continue;
                        }else if(s.getEndPos() > end){
                            break;
                        }
                        Integer excludeStart = s.getStartPos() - start - 1;
                        Integer excludeEnd = s.getEndPos() - start - 1;
                        Integer excludeLength = 1 +  excludeEnd - excludeStart;
                        if (excludeStart + excludeLength < dna.length()){
                            excludeRegions.add(excludeStart + "," + excludeLength);
                        }else if (excludeStart < dna.length() - 1 ){
                            int diff = dna.length() -1 - excludeStart;
                            excludeRegions.add(excludeStart + "," + diff);
                        }
                    }
                    //get info from text fields for primer3 options
                    String target = Integer.toString(flanks - designBuffer - 1) + 
                            "," + Integer.toString(r.getLength() + 2*designBuffer);
                    StringBuilder dnaTarget = new StringBuilder(
                            dna.substring(0, flanks -1).toLowerCase());
                    dnaTarget.append(dna.substring(flanks -1, flanks 
                            + r.getLength() - 1).toUpperCase());
                    dnaTarget.append(dna.substring(flanks + r.getLength() -1)
                            .toLowerCase());
                    String seqid = (r.getName() + ": " + r.getId());
                    //design primers
                    //updateMessage("Designing primers for part " + exonNumber + 
                      //      " of " + exonRegions.size() + "...");
                    ArrayList<String> result = designPrimers(seqid, 
                            dnaTarget.toString(), target, String.join(" ", excludeRegions));

                    //updateProgress(prog, 100);
                    designs.add(String.join("\n", result));

                    //parse primer3 output and write our output
                    primers.add(parsePrimer3Output(++pair,  r.getName(), r.getId(), 
                            r.getChromosome(), 1 + r.getStartPos() - flanks, result));

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
                            resultView.displayData(result.get("primers"), 
                                    result.get("design"), null);
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
                        e.getSource().getException().printStackTrace();
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
    }
    
    public boolean checkDesignParameters(){
        if (Integer.valueOf(flankingRegionsTextField.getText())
                <= (Integer.valueOf(minDistanceTextField.getText()) 
                + Integer.valueOf(maxSizeTextField.getText()))){
            Dialogs flanksError = Dialogs.create().title("Flanks Error").
                    masthead("Invalid values for 'Min distance'/"
                            + "'Flanking region' fields.").
                    message("Flanking region value must be greater than Min "
                            + "distance value  plus maximum primer size.")
                    .styleClass(Dialog.STYLE_CLASS_NATIVE);
            flanksError.showError();
            return false;
        }
        
        if (Integer.valueOf(optSizeTextField.getText()) < 
                Integer.valueOf(minSizeTextField.getText())){
            Dialogs sizeError = Dialogs.create().title("Primer Size Error").
                    masthead("Invalid values for primer size fields.").
                    message("Min Primer Size can not be greater than Opt "
                            + "Primer Size.")
                    .styleClass(Dialog.STYLE_CLASS_NATIVE);
            sizeError.showError();
            return false;
        }
        
        if (Integer.valueOf(maxSizeTextField.getText()) < 
                Integer.valueOf(optSizeTextField.getText())){
            Dialogs sizeError = Dialogs.create().title("Primer Size Error").
                    masthead("Invalid values for primer size fields.").
                    message("Max Primer Size can not be less than Opt "
                            + "Primer Size.")
                    .styleClass(Dialog.STYLE_CLASS_NATIVE);
            sizeError.showError();
            return false;
        }
        
        
        if (Double.valueOf(optTmTextField.getText()) < 
                Double.valueOf(minTmTextField.getText())){
            Dialogs sizeError = Dialogs.create().title("Primer TM Error").
                    masthead("Invalid values for primer TM fields.").
                    message("Min Primer TM can not be greater than Opt "
                            + "Primer TM.")
                    .styleClass(Dialog.STYLE_CLASS_NATIVE);
            sizeError.showError();
            return false;
        }
        
        if (Double.valueOf(maxTmTextField.getText()) < 
                Double.valueOf(optTmTextField.getText())){
            Dialogs sizeError = Dialogs.create().title("Primer TM Error").
                    masthead("Invalid values for primer TM fields.").
                    message("Max Primer TM can not be less than Opt "
                            + "Primer TM.")
                    .styleClass(Dialog.STYLE_CLASS_NATIVE);
            sizeError.showError();
            return false;
        }
        return true;
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
        if (! checkDesignParameters()){
            return;
        }
        
        setRunning(true);
        final Task<GeneSearchResult> geneSearchTask = 
                new Task<GeneSearchResult>(){
            @Override
            protected GeneSearchResult call() {
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
                    progressIndicator.progressProperty().unbind();
                    progressLabel.textProperty().unbind();
                    progressIndicator.progressProperty().set(0);
                    progressLabel.setText("No targets found");
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
                        progressIndicator.progressProperty().unbind();
                        progressLabel.textProperty().unbind();        
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
                        HashMap<String, Integer> geneExonOffsets = new HashMap<>();
                        HashMap<String, String> referenceSeqs = new HashMap<>();
                        //get DNA
                        int regionNumber = 0;
                        double incrementPerRegion = 100/genomicRegions.size();
                        HashSet<String> geneSymbolTracker = new HashSet<>();
                        HashSet<String> transcriptTracker = new HashSet<>();
                        SequenceFromDasUcsc seqFromDas = new SequenceFromDasUcsc();
                        for (GenomicRegionSummary r : genomicRegions){
                            regionNumber++;
                            updateMessage("Getting DNA for region " + regionNumber + 
                                    " of " + genomicRegions.size());
                            updateProgress(-1, -1);
                            HashMap<String, String> checkedName = new HashMap<>();
                            HashMap<String, String> checkedTranscript = new HashMap<>();
                                //only want to check names/ID has already been 
                                //used once per region i.e. for previous regions
                            String genome = (String) genomeChoiceBox.getSelectionModel().getSelectedItem();
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
                                if (! geneExonOffsets.containsKey(t.getSymbol())){
                                    geneExonOffsets.put(t.getSymbol(), 0);
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
                                            if (t.getStrand().equals("-") && 
                                                    e.getEnd() > t.getCdsEnd()){
                                                geneExonOffsets.put(t.getSymbol(), 
                                                        geneExonOffsets.get(t.getSymbol()) + 1);
                                            }else if (t.getStrand().equals("+") && 
                                                    e.getStart() < t.getCdsStart()){
                                                   geneExonOffsets.put(t.getSymbol(), 
                                                        geneExonOffsets.get(t.getSymbol()) + 1);
                                            }
                                            continue;
                                        }
                                    }
                                    String name = t.getSymbol();
                                    if (! checkedName.containsKey(name)){
                                        checkedName.put(name, checkDuplicate(name, geneSymbolTracker));
                                    }
                                    name = checkedName.get(name);
                                    String transcript = t.getId();
                                    if (!checkedTranscript.containsKey(transcript)){
                                        checkedTranscript.put(transcript, 
                                                checkDuplicate(transcript, transcriptTracker));
                                    }
                                    transcript = checkedTranscript.get(transcript);
                                    String id = transcript.concat("_ex").
                                            concat(Integer.toString(e.getOrder()));
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
                            referenceSeqs.put(r.getName(), 
                                    createRefserenceSequence(dna, r.getStartPos(),
                                            flanks, exonRegions, onMinusStrand));
                            //DEBUG
                            System.out.println("Ref " + r.getName() + ":");
                            System.out.println(referenceSeqs.get(r.getName()));
                            
                            numberExons(exonRegions, onMinusStrand, geneExonOffsets);
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
                        ArrayList<HashMap<String, String>> refList = new ArrayList<>();
                        refList.add(referenceSeqs);
                        primerResult.put("primers", primers);
                        primerResult.put("design", designs);
                        primerResult.put("ref", refList);
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
                            resultView.displayData(result.get("primers"), 
                                    result.get("design"), 
                                    (HashMap<String, String>) result.get("ref").get(0));
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
                progressIndicator.progressProperty().unbind();
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
        progressIndicator.progressProperty().unbind();
        progressIndicator.progressProperty().bind(geneSearchTask.progressProperty());
        progressLabel.textProperty().unbind(); 
        progressLabel.textProperty().bind(geneSearchTask.messageProperty());
        new Thread(geneSearchTask).start();   
    
    }
    
    private String createRefserenceSequence(String dna, int offset, int flanks,
            ArrayList<GenomicRegionSummary> exons, boolean revComp){
        StringBuilder dnaTarget = new StringBuilder();
        int prevEnd = 0;
        for (int i = 0; i < exons.size(); i++){
            int tStart = exons.get(i).getStartPos() - offset;
            int tEnd = 1 + exons.get(i).getEndPos() - offset;
            int subsStart = tStart - flanks > 0 ? tStart - flanks : 0;
            int subsEnd = tEnd + flanks - 1 < dna.length() ?  tEnd + flanks - 1 : dna.length();
            //make sure we don't overlap end with next exon region
            if (i < exons.size() -1){
                subsEnd = subsEnd < exons.get(i + 1).getStartPos() - offset ? 
                        subsEnd : exons.get(i + 1).getStartPos() - offset;
            }
            //make sure we don't overlap current flanks with previous flanks
            if (prevEnd > 0){
                if (prevEnd > subsStart){
                    subsStart = prevEnd;
                }
            }
            prevEnd = subsEnd;
            dnaTarget.append(
                    dna.substring(subsStart, tStart).toLowerCase());
            dnaTarget.append(dna.substring(tStart, tEnd-1)
                    .toUpperCase());
            dnaTarget.append(dna.substring(tEnd -1, subsEnd)
                    .toLowerCase());
        }
        if (revComp){
            return reverseComplement(dnaTarget.toString());
        }else{
            return dnaTarget.toString();
        }
    }
    
    /*dup will always be an unedited gene name
    we need to check whether we already have made an '(alt)' version
    by checking in dupStorer
    */
    private String checkDuplicate(String dup, HashSet<String> dupStorer){
        String dedupped;
        if (dupStorer.contains(dup)){
           dedupped =  dup + "(alt)";
           if (dupStorer.contains(dedupped)){
               for (int i = 1; i < 999; i++){
                   dedupped = dup + "(alt" + i + ")";
                   if (!dupStorer.contains(dedupped)){
                       break;
                   }
               }
           }
           dupStorer.add(dedupped);
           return dedupped;
        }else{
            dupStorer.add(dup);
            return dup;
        }
    }
    
    private void numberExons(ArrayList<GenomicRegionSummary> exonRegions,
            boolean minusStrand, HashMap<String, Integer> symbolToOffset){
        int n = 0;
        for (GenomicRegionSummary e: exonRegions){
            int offset = 0;
            if (symbolToOffset.containsKey(e.getName())){
                offset = symbolToOffset.get(e.getName());
            }
            if (minusStrand){
                e.setName(e.getName() + "_ex" + (exonRegions.size() - n + offset));
            }else{
                e.setName(e.getName() + "_ex" + (n+1 + offset));
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
                if (! splitRegions.get(i).getChromosome().equals(
                        splitRegions.get(i+1).getChromosome())){
                    splitAndMergedRegions.add(splitRegions.get(i));
                    continue;
                }
                int gap = splitRegions.get(i+1).getEndPos() - splitRegions.get(i).getStartPos();
                if (gap + (2*buffer)  <= optSize){
                    smallRegion = true;
                    String chrom = splitRegions.get(i).getChromosome();
                    int start = splitRegions.get(i).getStartPos();
                    int end = splitRegions.get(i+1).getEndPos();
                    String name = mergeNames(splitRegions.get(i).getName(), 
                            splitRegions.get(i+1).getName());
                    String id = mergeIds(splitRegions.get(i).getId(), 
                            splitRegions.get(i + 1).getId());
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
                merged.add(d + "_ex" + idToEx1.get(d) + "-" + idToEx2.get(d));
            }else{
                if (d.equals(idToEx1.get(d))){//if regions key and value will be identical
                    merged.add(d);
                }else{//if exons value will be exon number
                    merged.add(d + "_ex" + idToEx1.get(d));
                }
            }
        }
        for (String d: idToEx2.keySet()){
            if (! idToEx1.containsKey(d)){   
                if (d.equals(idToEx2.get(d))){//if regions key and value will be identical
                    merged.add(d);
                }else{//if exons value will be exon number
                    merged.add(d + "_ex" + idToEx2.get(d));
                }
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
                geneName1.get(0).equalsIgnoreCase(geneName2.get(0))){
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
        String db = (String) genomeChoiceBox.getSelectionModel().getSelectedItem();
        final Hyperlink pcrLink = new Hyperlink();
        pcrLink.setText("in-silico PCR");
        pcrLink.setDisable(true);
        String left = "NOT FOUND";
        String right = "NOT FOUND";
        Integer lpos = 0;
        Integer rpos = 0;
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
        res.setProductSize(Integer.valueOf(productSize));
        if (Integer.valueOf(productSize) > 0){
            Integer wpSize = 4000 > Integer.valueOf(productSize) * 2 ? 
                    4000 : Integer.valueOf(productSize) * 2;
            lpos = baseCoordinate + leftStart;
            rpos = baseCoordinate + rightStart;
            final String url = serverUrl + "/cgi-bin/hgPcr?db=" + db + 
                    "&wp_target=genome&wp_f=" + left + "&wp_r=" + right + 
                    "&wp_size=" + wpSize + 
                    "&wp_perfect=15&wp_good=15&boolshad.wp_flipReverse=0";
            pcrLink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    getHostServices().showDocument(url);
                    pcrLink.setVisited(true);
                    pcrLink.setUnderline(false);
                }
            });
            pcrLink.setDisable(false);
            pcrLink.setUnderline(true);
            res.setIsPcrLink(pcrLink);
        }
        String region = chrom + ":" + lpos + "-" + rpos;
        res.setRegion(region);
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
                    append(thermoConfig.toString())
                    .append(System.getProperty("file.separator")).append("\n");
            String misprimeLibrary = (String) 
                misprimingLibraryChoiceBox.getSelectionModel().getSelectedItem();
            if (!misprimeLibrary.isEmpty()){
                if (! misprimeLibrary.matches("none")){
                    p3_job.append("PRIMER_MISPRIMING_LIBRARY=")
                            .append(misprimeDir.toString())
                            .append(System.getProperty("file.separator"))
                            .append(misprimeLibrary).append("\n");
                    p3_job.append("PRIMER_MAX_LIBRARY_MISPRIMING=")
                            .append(maxMisprimeTextField.getText()).append("\n");
            
                }
            }
            
            p3_job.append("=");
            System.out.println(p3_job.toString());//debug only
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

