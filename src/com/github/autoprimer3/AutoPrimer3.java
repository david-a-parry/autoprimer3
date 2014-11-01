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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
    //TO DO!
    
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
    HashMap<String, ArrayList<String>> buildToTable = new HashMap<>();
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
          
            /*basic functionality test code
            SequenceFromDasUcsc seqFromDas = new SequenceFromDasUcsc();
            GetEnsemblGeneCoordinates getter = new GetEnsemblGeneCoordinates();
            ArrayList<GeneDetails> refSeq = getter.getGeneFromSymbol("PIK3R1", "hg19", "ensGene");
            for (GeneDetails r: refSeq){
                System.out.println(r.getId() + "\t"+ r.getCdsStart() + "\t"+ 
                        r.getCdsEnd() + "\t"+ r.getChromosome());
            /*    for (Exon e: r.getExons()){
                    String dna = seqFromDas.retrieveSequence("hg19", r.getChromosome(),
                            e.getStart(), e.getEnd());
                    System.out.println(dna);
                }
                String dna = seqFromDas.retrieveSequence("hg19", r.getChromosome(), r.getTxStart(), r.getTxEnd());
                System.out.println(dna);
            }*/
            
        } catch (Exception ex) {
            Logger.getLogger(AutoPrimer3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
//            buildsAndTables.connectToUcsc();
//            ArrayList<String> genomeBuilds = buildsAndTables.getBuildIds();
//            ObservableList<String> genomes = FXCollections.observableList(genomeBuilds);
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
            public void changed (ObservableValue ov, Number value, Number new_value){ 
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
        File misprimeDir = mispriming_libs.toFile();
        misprimingLibraryChoiceBox.getItems().add("none");
        for (File f: misprimeDir.listFiles()){
            misprimingLibraryChoiceBox.getItems().add(f.getName());
        }
        misprimingLibraryChoiceBox.getSelectionModel().selectFirst();
        minDistanceTextField.addEventFilter(KeyEvent.KEY_TYPED, checkNumeric());
        flankingRegionsTextField.addEventFilter(KeyEvent.KEY_TYPED, checkNumeric());
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
        defaultPrimer3Values.put(splitRegionsTextField, "500");
        defaultPrimer3Values.put(maxMisprimeTextField, "12");
        defaultPrimer3Values.put(sizeRangeTextField, defaultSizeRange);
        resetValuesButton.setOnAction(new EventHandler<ActionEvent>(){
           @Override
           public void handle(ActionEvent actionEvent){
                resetPrimerSettings();
            }
        });
            
        mainTabPane.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Tab>(){
                    @Override
                    public void changed (ObservableValue<? extends Tab> ov, Tab t, Tab t1) {
                        if (t.equals(primerTab)){
                            resetEmptyPrimerSettings();
                        }
                    }
                }
        );
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
    
    
    private void setTables(ArrayList<String> tables){
        ArrayList<String> genes = new ArrayList<>();
        ArrayList<String> snps = new ArrayList<>();
        for (String t: tables){
            if (t.equals("refGene") || t.equals("knownGene") || 
                    t.equals("ensGene") || t.equals("xenoRefGene")){
                genes.add(t);
            }else if (t.matches("^snp\\d+(\\w+)*")){
                snps.add(t);
            }
        }
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
        final Task<ArrayList<String>> getTablesTask = new Task<ArrayList<String>>(){
            @Override
            protected ArrayList<String> call() {
                System.out.println("Called getTablesTask...");
                return buildsAndTables.getAvailableTables(id);
            }
        };

        getTablesTask.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                System.out.println("getTablesTask succeeded.");
                ArrayList<String> tables = (ArrayList<String>) e.getSource().getValue();
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
               
        int flanks = Integer.valueOf(flankingRegionsTextField.getText());
        int designBuffer = Integer.valueOf(minDistanceTextField.getText());
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
        
        //get our transcript targets using databaseChoiceBox and genesTextField
        Integer pair = 0;
        LinkedHashSet<String> searchStrings = new LinkedHashSet<>();
        Collections.addAll(searchStrings, genesTextField.getText().split("\\s+"));
        LinkedHashSet<String> notFound = new LinkedHashSet<>();
        LinkedHashSet<String> nonCodingTargets = new LinkedHashSet<>();
        LinkedHashSet<GeneDetails> targets = new LinkedHashSet<>();
        for (String s : searchStrings){
            if (! s.matches(".*\\w.*")){
                continue;
            }
            ArrayList<GeneDetails> found = getGeneDetails(s);
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
        
        //check we've found at least one target and warn if any search term could not be found
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
                return;
            }
        }
        
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
        
        ArrayList<Primer3Result> primers = 
                new ArrayList<Primer3Result>();
        ArrayList<String> designs = new ArrayList<>();
        //get FASTA sequence
        for (GenomicRegionSummary r : genomicRegions){
            String genome = (String) genomeChoiceBox.getSelectionModel().getSelectedItem();
            SequenceFromDasUcsc seqFromDas = new SequenceFromDasUcsc();
            String dna = seqFromDas.retrieveSequence(
                    genome, r.getChromosome(), r.getStartPos(), r.getEndPos());
            //System.out.println(dna);//debug only
            
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
                    GenomicRegionSummary ex = new GenomicRegionSummary();
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
            merger.mergeRegionsByPosition(exonRegions);
            if (minus_strand > plus_strand){
                Collections.reverse(exonRegions);
            }
            //get substring for each exon and design primers using primer3
            int geneExon = 0;
            for (GenomicRegionSummary er: exonRegions){
                geneExon++;
                int tStart = er.getStartPos() - r.getStartPos();
                int tEnd = 1 + er.getEndPos() - r.getStartPos();
                //TO DO
                //get SNPs using mysql query like:
                //select name,chrom,chromStart,chromEnd,observed from hg19.snp141Common where chrom='chr1' and chromEND >= 93992837 and chromStart < 94121149 ;
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
                
                //get info from text fields for primer3 options
                
                String target =  Integer.toString(flanks - designBuffer) + 
                        "," + Integer.toString(tEnd - tStart + (designBuffer * 2));
                String exonName = er.getName()+ "_ex" + geneExon ;
                String seqid = (exonName + ": " + er.getId());
                ArrayList<String> result = designPrimers(seqid, 
                        dnaTarget.toString(), target);
                designs.add(String.join("\n", result));
                
                //parse primer3 output and write our output
                primers.add(parsePrimer3Output(++pair,  exonName, er.getId(), 
                        r.getChromosome(), 1 + er.getStartPos() - flanks, result));
                
            }
            
        }
        if (! primers.isEmpty()){
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
                resultView.displayData(primers, designs);
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
        }else{
            Dialogs noPrimersError = Dialogs.create().title("No PrimersFound").
                    masthead("No primers found for your targets.").
                    message("No primer designs were attempted for your targets")
                    .styleClass(Dialog.STYLE_CLASS_NATIVE);
            noPrimersError.showError();        }
       
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
    private ArrayList<String> designPrimers(String name, String dna, String target){
        ArrayList<String> result = new ArrayList<>();
        StringBuilder error = new StringBuilder(); 
        StringBuilder p3_job = new StringBuilder("SEQUENCE_TARGET=");
            p3_job.append(target).append("\n");
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

    
    private ArrayList<GeneDetails> getGeneDetails(String searchString){
        ArrayList<GeneDetails> genes = new ArrayList<>();
        if (databaseChoiceBox.getSelectionModel().getSelectedItem().equals("refGene") 
                || databaseChoiceBox.getSelectionModel().getSelectedItem().equals("xenoRefGene")){
            GetGeneCoordinates geneSearcher = new GetGeneCoordinates();
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
            GetUcscGeneCoordinates geneSearcher = new GetUcscGeneCoordinates();
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
            GetEnsemblGeneCoordinates geneSearcher = new GetEnsemblGeneCoordinates();
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
    }
    
    private void setLoading(boolean loading){
        setCanRun(!loading);
        refreshButton.setDisable(loading);
        genomeChoiceBox.setDisable(loading);
        databaseChoiceBox.setDisable(loading);
        snpsChoiceBox.setDisable(loading);
        designToChoiceBox.setDisable(loading);
        minDistanceTextField.setDisable(loading);
        flankingRegionsTextField.setDisable(loading);
        genesTextField.setDisable(loading);
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

