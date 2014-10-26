/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.autoprimer3;

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
public class GetEnsemblGeneCoordinates extends GetUcscGeneCoordinates {
    final ArrayList<String> fields = new ArrayList<>(Arrays.asList("name", "chrom", 
            "txStart", "txEnd", "cdsStart", "cdsEnd", "exonCount", "exonStarts", 
            "exonEnds", "strand"));  
    @Override
    public ArrayList<GeneDetails> getGeneFromSymbol(String symbol, String build, String db) 
            throws SQLException, GetGeneCoordinates.GetGeneExonsException{
        ArrayList<GeneDetails> transcripts = new ArrayList<>();
        String fieldsToRetrieve = String.join(", ", fields);
        stmt = conn.createStatement();
        Statement stmt2 = conn.createStatement();
        ResultSet rs2 = stmt2.executeQuery("SELECT name, value FROM "
                + build + ".ensemblToGeneName WHERE " + "value='" + symbol +"'");
        while (rs2.next()){
            ResultSet rs = stmt.executeQuery("SELECT " + fieldsToRetrieve + 
                " FROM " + build + "." + db +" WHERE name='"+ rs2.getString("name") + "'");
            transcripts.addAll(getTranscriptsFromResultSet(rs, symbol));
        }
        return transcripts;
    }
    
    
}
