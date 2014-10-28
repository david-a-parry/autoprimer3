/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.autoprimer3;

import com.github.autoprimer3.GeneDetails.*;
import com.github.autoprimer3.GeneDetails.Exon;
import static com.github.autoprimer3.ReverseComplementDNA.reverseComplement;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

/**
 *
 * @author david
 */
public class AutoPrimer3 extends Application implements Initializable{
    
    //Genes tab
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
    
    Boolean CANRUN = false;
    final GetUcscBuildsAndTables buildsAndTables = new GetUcscBuildsAndTables();;
    File primer3ex; 
    
    @Override
    public void start(final Stage primaryStage) {
        try {
            AnchorPane page = (AnchorPane) FXMLLoader.load(com.github.autoprimer3.AutoPrimer3.class.getResource("AutoPrimer3.fxml"));
            Scene scene = new Scene(page);
            primaryStage.setScene(scene);
            primaryStage.setTitle("AutoPrimer3");
            scene.getStylesheets().add(com.github.autoprimer3.AutoPrimer3.class.
                    getResource("autoprimer3.css").toExternalForm());
            primaryStage.show();
            
  
/*            primaryStage.getIcons().add(new Image(this.getClass().
                    getResourceAsStream("icon.png")));
*/          
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
                primer3ex = File.createTempFile("primer3", "exe");
                primer3ex.deleteOnExit();
                InputStream inputStream;
                if (System.getProperty("os.name").equals("Mac OS X")){
                        inputStream = this.getClass().
                                getResourceAsStream("primer3_core_macosx");
                }else if (System.getProperty("os.name").equals("Linux")){
                    if (System.getProperty("os.arch").equals("x86_64")){
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
                primer3ex.setExecutable(true);
            }catch(IOException ex){
                //TO DO - throw this properly
                ex.printStackTrace();
            }
            System.out.println(primer3ex);
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
                    final String id = (String) genomeChoiceBox.getItems().get(new_value.intValue());
                    genomeChoiceBox.setTooltip(new Tooltip (buildsAndTables.getGenomeDescription(id)));
                    getBuildTables(id);
                }
            });
            
            connectToUcsc();

//            genomeChoiceBox.getItems().clear();
//            genomeChoiceBox.getItems().addAll(genomes);
            
    }
    
    private void getBuildTables(final String id){
        setLoading(true);
        databaseChoiceBox.getItems().clear();
        snpsChoiceBox.getItems().clear();
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
    
    private void connectToUcsc(){
        progressIndicator.setProgress(-1);
        final Task<ArrayList<String>> getBuildsTask = new Task<ArrayList<String>>(){
            @Override
            protected ArrayList<String> call() {
                buildsAndTables.connectToUcsc();
                return buildsAndTables.getBuildIds();
            }
        };
        getBuildsTask.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
            @Override
            public void handle (WorkerStateEvent e){
                ArrayList<String> buildIds = (ArrayList<String>) e.getSource().getValue();
                genomeChoiceBox.getItems().clear();
                genomeChoiceBox.getItems().addAll(buildIds);
                genomeChoiceBox.getSelectionModel().selectFirst();
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
        Integer pair = 0;
        int flanks = Integer.valueOf(flankingRegionsTextField.getText());
        int designBuffer = Integer.valueOf(minDistanceTextField.getText());
        //get our transcript targets using databaseChoiceBox and genesTextField
        LinkedHashSet<String> searchStrings = new LinkedHashSet<>();
        Collections.addAll(searchStrings, genesTextField.getText().split("\\s+"));
        LinkedHashSet<String> notFound = new LinkedHashSet<>();
        LinkedHashSet<String> nonCodingTargets = new LinkedHashSet<>();
        LinkedHashSet<GeneDetails> targets = new LinkedHashSet<>();
        for (String s : searchStrings){
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
                    .nativeTitleBar();
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
                    actions(Dialog.Actions.YES, Dialog.Actions.NO).
                    nativeTitleBar().
                    showConfirm();
            if (response == Dialog.Actions.NO){
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
        
        HashMap<String, HashMap<String, String>> primers = 
                new HashMap<String, HashMap<String, String>>();
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
                ArrayList<Exon> exons = new ArrayList<>();
                if (designToChoiceBox.getSelectionModel().getSelectedItem()
                        .equals("Coding regions")){
                    exons = t.getCodingRegions();
                }else{
                    exons = t.getExons();
                }
                if (t.getStrand().equals("-")){
                    minus_strand++;
                }else{
                    plus_strand++;
                }
                for (Exon e : exons){
                    String id = t.getId().concat("_ex").
                            concat(Integer.toString(e.getOrder()));
                    String name = t.getSymbol();
                    GenomicRegionSummary ex = new GenomicRegionSummary(
                            r.getChromosome(), e.getStart(), 
                            e.getEnd(), null, null, id, name);
                    exonRegions.add(ex);
                }
            }
            merger.mergeRegionsByPosition(exonRegions);
            if (minus_strand > plus_strand){
                Collections.reverse(exonRegions);
            }
            //get substring for each exon and design primers using primer3
            for (GenomicRegionSummary er: exonRegions){
                int tStart = er.getStartPos() - r.getStartPos();
                int tEnd = 1 + er.getEndPos() - r.getStartPos();
                StringBuilder dnaTarget = new StringBuilder(
                        dna.substring(tStart - flanks, tStart).toLowerCase());
                dnaTarget.append(dna.substring(tStart, tEnd-1)
                        .toUpperCase());
                dnaTarget.append(dna.substring(tEnd -1, tEnd + flanks - 1)
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
                String seqid = (er.getName()) + ": " + er.getId();
                ArrayList<String> result = designPrimers(seqid, 
                        dnaTarget.toString(), target);
                designs.add(String.join("\n", result));
                
                //parse primer3 output and write our output
                primers.put(String.valueOf(pair), parsePrimer3Output(result));
                
            }
            
        }
        
    }
    
    //get left and right primer from Primer3 output
    private HashMap<String, String> parsePrimer3Output(ArrayList<String> output){
        HashMap<String, String> primers = new HashMap<>();
        String left = "NOT FOUND";
        String right = "NOT FOUND";
        String productSize = "0";
        for (String res: output){
            if (res.startsWith("LEFT PRIMER")){
                List<String> split = Arrays.asList(res.split(" "));
                left = split.get(split.size() -1);
            }else if (res.startsWith("RIGHT PRIMER")){
                List<String> split = Arrays.asList(res.split(" "));
                right = split.get(split.size() -1);
            }else if (res.startsWith("PRODUCT SIZE:")){
                List<String> split = Arrays.asList(res.split(" "));
                productSize = split.get(3);
                break;
            }
        }
        
        primers.put("LEFT", left);
        primers.put("RIGHT", right);
        primers.put("SIZE", productSize);
        return primers;
    }
    
    
    //for given parameters design primers and return result as an array of strings
    private ArrayList<String> designPrimers(String name, String dna, String target){
        ArrayList<String> result = new ArrayList<>();
        StringBuilder error = new StringBuilder(); 
        StringBuilder p3_job = new StringBuilder("SEQUENCE_TARGET=");
            p3_job.append(target).append("\n");
            p3_job.append("SEQUENCE_ID=").append(name).append("\n");
            p3_job.append("SEQUENCE_TEMPLATE=").append(dna).append("\n");
            p3_job.append("PRIMER_OPT_SIZE=20\n");
            p3_job.append("PRIMER_MIN_SIZE=18\n");
            p3_job.append("PRIMER_MAX_SIZE=27\n");
            p3_job.append("PRIMER_PRODUCT_SIZE_RANGE=150-250 100-300 301-400 401-500 501-600 601-700 701-850 851-1000 1000-2000\n");
            p3_job.append("PRIMER_TASK=pick_pcr_primers\n");
            p3_job.append("PRIMER_THERMODYNAMIC_PARAMETERS_PATH=").
                    append(System.getProperty("user.home")).
                    append("/NetBeansProjects/autoprimer3/src/com/github/autoprimer3/primer3_config/\n");
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
                        System.out.println(line);//debug only
                        result.add(line);
                    }
                    while ((line = errorbuf.readLine()) != null){
                        System.out.println(line);//debug only
                        error.append(line);
                    }
                    int exit = ps.waitFor();
                    System.out.println(exit);//debug only
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
        return start;
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
            if (searchString.equals("ENST\\d{11}")){
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
