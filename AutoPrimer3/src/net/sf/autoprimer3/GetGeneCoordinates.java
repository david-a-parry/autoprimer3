/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.autoprimer3;

import com.sun.deploy.util.StringUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author David A. Parry <d.a.parry@leeds.ac.uk>
 */
public class GetGeneCoordinates {//default handles RefSeq and Encode genes
    static Connection conn;
    Statement stmt;
    final ArrayList<String> fields = new ArrayList<>(Arrays.asList("name", "chrom", 
            "txStart", "txEnd", "cdsStart", "cdsEnd", "exonCount", "exonStarts", 
            "exonEnds", "strand", "name2"));    
    static {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://genome-mysql.cse.ucsc.edu" +
               "?user=genomep&password=password&no-auto-rehash");
        }catch (SQLException ex){
            throw new ExceptionInInitializerError(ex);
        }
    }
    public ArrayList<GeneDetails> getGeneFromSymbol(String symbol, String build, String db) 
            throws SQLException, GetGeneExonsException{
        String fieldsToRetrieve = StringUtils.join(fields, ", ");
        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT " + fieldsToRetrieve + 
                " FROM " + build + "." + db +" WHERE name2='"+ symbol+ "'");
        return getTranscriptsFromResultSet(rs);
    }
    
    public ArrayList<GeneDetails> getGeneFromId(String id) 
            throws SQLException, GetGeneExonsException{
        String fieldsToRetrieve = StringUtils.join(fields, ", ");
        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT " + fieldsToRetrieve + 
                " FROM hg19.refGene WHERE name='"+ id + "'");
        return getTranscriptsFromResultSet(rs);
    }
    
    protected ArrayList<GeneDetails> getTranscriptsFromResultSet(ResultSet rs) 
             throws SQLException, GetGeneExonsException{
        ArrayList<HashMap<String, String>> genes = new ArrayList<>();
        while (rs.next()){
            HashMap<String, String> geneCoords = new HashMap<>();
            for (String f: fields){
                geneCoords.put(f, rs.getString(f));
            }
            genes.add(geneCoords);
        }
        ArrayList<GeneDetails> transcriptsToReturn = new ArrayList<>();
        for (HashMap<String, String> gene: genes){
            GeneDetails geneDetails = geneHashToGeneDetails(gene);
            transcriptsToReturn.add(geneDetails);
        }
        return transcriptsToReturn;
    }
    
    protected GeneDetails geneHashToGeneDetails(HashMap<String,String> gene) 
            throws GetGeneExonsException{
        GeneDetails geneDetails = new GeneDetails();
        geneDetails.setSymbol(gene.get("name2"));
        geneDetails.setId(gene.get("name"));
        geneDetails.setStrand(gene.get("strand"));
        geneDetails.setChromosome(gene.get("chrom"));
        geneDetails.setTxStart(Integer.parseInt(gene.get("txStart")));
        geneDetails.setTxEnd(Integer.parseInt(gene.get("txEnd")));
        geneDetails.setCdsStart(Integer.parseInt(gene.get("cdsStart")));
        geneDetails.setCdsEnd(Integer.parseInt(gene.get("cdsEnd")));
        geneDetails.setTotalExons(Integer.parseInt(gene.get("exonCount")));
        try{
            geneDetails.setExons(gene.get("exonStarts"), gene.get("exonEnds"));
        }catch (GeneDetails.GeneExonsException ex){
            throw new GetGeneExonsException("Error parsing exons for transcript "
                    + geneDetails.getId() + ", gene " + geneDetails.getSymbol(), ex);
        }
        return geneDetails;
    }
    
    public class GetGeneExonsException extends Exception{
        public GetGeneExonsException() { super(); }
        public GetGeneExonsException(String message) { super(message); }
        public GetGeneExonsException(String message, Throwable cause) { super(message, cause); }
        public GetGeneExonsException(Throwable cause) { super(cause); }
    }

}
