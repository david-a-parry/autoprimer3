/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.autoprimer3;

import com.sun.deploy.util.StringUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import static net.sf.autoprimer3.GetGeneCoordinates.conn;

/**
 *
 * @author David A. Parry <d.a.parry@leeds.ac.uk>
 */
public class GetUcscGeneCoordinates extends GetGeneCoordinates {
    final ArrayList<String> fields = new ArrayList<>(Arrays.asList("name", "chrom", 
            "txStart", "txEnd", "cdsStart", "cdsEnd", "exonCount", "exonStarts", 
            "exonEnds", "strand"));  
    @Override
    public ArrayList<GeneDetails> getGeneFromSymbol(String symbol, String build, String db) 
            throws SQLException, GetGeneExonsException{
        ArrayList<GeneDetails> transcripts = new ArrayList<>();
        String fieldsToRetrieve = StringUtils.join(fields, ", ");
        stmt = conn.createStatement();
        Statement stmt2 = conn.createStatement();
        ResultSet rs2 = stmt2.executeQuery("SELECT kgID, geneSymbol FROM " + build + ".kgXref WHERE "
                        + "geneSymbol='" + symbol +"'");
        while (rs2.next()){
            ResultSet rs = stmt.executeQuery("SELECT " + fieldsToRetrieve + 
                " FROM " + build + "." + db +" WHERE name='"+ 
                    rs2.getString("kgID") + "'");
            transcripts.addAll(getTranscriptsFromResultSet(rs, symbol));
        }
        return transcripts;
    }
    
    protected ArrayList<GeneDetails> getTranscriptsFromResultSet(ResultSet rs, String symbol) 
             throws SQLException, GetGeneExonsException{
        ArrayList<HashMap<String, String>> genes = new ArrayList<>();
        while (rs.next()){
            HashMap<String, String> geneCoords = new HashMap<>();
            for (String f: fields){
                geneCoords.put(f, rs.getString(f));
            }
            geneCoords.put("name2", symbol);
            genes.add(geneCoords);
        }
        ArrayList<GeneDetails> transcriptsToReturn = new ArrayList<>();
        for (HashMap<String, String> gene: genes){
            GeneDetails geneDetails = geneHashToGeneDetails(gene);
            transcriptsToReturn.add(geneDetails);
        }
        return transcriptsToReturn;
    }
    
    
}
