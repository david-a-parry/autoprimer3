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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import static com.github.autoprimer3.GetGeneCoordinates.conn;

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
        String fieldsToRetrieve = String.join(", ", fields);
        checkConnection();
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
    
    @Override
    public ArrayList<GeneDetails> getGeneFromId(String id, String build, String db) 
            throws SQLException, GetGeneExonsException{
        String fieldsToRetrieve = String.join(", ", fields);
        checkConnection();
        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT " + fieldsToRetrieve + 
                " FROM " + build + "." + db + " WHERE name='"+ id + "'");
        Statement stmt2 = conn.createStatement();
        ResultSet rs2 = stmt2.executeQuery("SELECT kgID, geneSymbol FROM " + build 
                + ".kgXref WHERE kgID='" + id +"'");
        String symbol = new String();
        while(rs2.next()){
            symbol = rs2.getString("geneSymbol");
        }
        return getTranscriptsFromResultSet(rs, symbol);
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
