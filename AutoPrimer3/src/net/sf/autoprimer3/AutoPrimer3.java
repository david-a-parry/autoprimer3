/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.autoprimer3;

import net.sf.autoprimer3.GeneDetails.*;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 *
 * @author david
 */
public class AutoPrimer3 extends Application implements Initializable, Serializable{
    
    @Override
    public void start(final Stage primaryStage) {
        try {
            AnchorPane page = (AnchorPane) FXMLLoader.load(AutoPrimer3.class.getResource("AutoPrimer3.fxml"));
            Scene scene = new Scene(page);
            primaryStage.setScene(scene);
            primaryStage.setTitle("AutoPrimer3");
            scene.getStylesheets().add(AutoPrimer3.class.getResource("autoprimer3.css").toExternalForm());
            primaryStage.show();
/*            primaryStage.getIcons().add(new Image(this.getClass().
                    getResourceAsStream("icon.png")));
*/          
            //basic functionality test code
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
                }*/
                String dna = seqFromDas.retrieveSequence("hg19", r.getChromosome(), r.getTxStart(), r.getTxEnd());
                System.out.println(dna);
            }
            
        } catch (Exception ex) {
            Logger.getLogger(AutoPrimer3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
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
