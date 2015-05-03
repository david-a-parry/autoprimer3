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
import java.util.ArrayList;
import java.util.Arrays;

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
        String fieldsToRetrieve = String.join(", ", fields);
        checkConnection();
        ArrayList<GeneDetails> transcripts = new ArrayList<>();
        ResultSet rs2 = doQuery("SELECT COUNT(*) FROM "
                + "information_schema.tables  WHERE table_schema = '" + build +
                "' AND table_name = 'ensemblToGeneName';");
        while (rs2.next()){
            if (rs2.getInt("COUNT(*)") < 1){
                transcripts = getTranscriptsViaKgId(symbol, build, db, fieldsToRetrieve);
                return transcripts;
            }else{
                transcripts = getTranscriptsFromEnsemblToName(symbol, build, db, 
                        fieldsToRetrieve);
                return transcripts;
            }
        }
    return transcripts;
    }
    
    private ArrayList<GeneDetails> getTranscriptsFromEnsemblToName(String symbol, 
                String build, String db, String fieldsToRetrieve) 
            throws SQLException, GetGeneExonsException{
        ArrayList<GeneDetails> transcripts = new ArrayList<>();
        checkConnection();
        System.out.println("SELECT name, value FROM '"
            + build + ".ensemblToGeneName' WHERE value='" + symbol +"';");
            ResultSet rs2 = doQuery("SELECT name, value FROM "
            + build + ".ensemblToGeneName WHERE value='" + symbol +"';");
        while (rs2.next()){
            ResultSet rs = doQuery("SELECT " + fieldsToRetrieve + 
                " FROM " + build + "." + db +" WHERE name='"+ rs2.getString("name") + "'");
            transcripts.addAll(getTranscriptsFromResultSet(rs, symbol));
        }
        return transcripts;
    }
            
    private ArrayList<GeneDetails> getTranscriptsViaKgId(String symbol, 
            String build, String db, String fieldsToRetrieve) 
            throws SQLException, GetGeneExonsException{
        ArrayList<GeneDetails> transcripts = new ArrayList<>();
        checkConnection();
        ResultSet rs2 = doQuery("SELECT COUNT(*) FROM "
                    + "information_schema.tables  WHERE table_schema = '"+ 
                build +"' AND table_name = 'knownToEnsembl';");
        while (rs2.next()){
            if (rs2.getInt("COUNT(*)") > 0){
                ArrayList<String> kgids = new ArrayList<>();
                System.out.println("SELECT kgID, "
                        + "geneSymbol FROM " + build + ".kgXref WHERE "
                        + "geneSymbol='" + symbol +"';");
                ResultSet rs3 = doQuery("SELECT kgID, "
                        + "geneSymbol FROM " + build + ".kgXref WHERE "
                        + "geneSymbol='" + symbol +"';");
                while(rs3.next()){
                    kgids.add(rs3.getString("kgID"));
                }
                System.out.println("Select value FROM " + build 
                        + ".knownToEnsembl WHERE name='" + 
                        String.join(" or name=", kgids) + "';");
                ResultSet rs4 = doQuery("Select value FROM " + build 
                        + ".knownToEnsembl WHERE name='" + 
                        String.join(" or name=", kgids) + "';");
                while (rs4.next()){
                    ResultSet rs = doQuery("SELECT " + fieldsToRetrieve + 
                        " FROM " + build + "." + db +" WHERE name='"+ rs4.getString("value") + "'");
                    transcripts.addAll(getTranscriptsFromResultSet(rs, symbol));
                }
                return transcripts;
            }else{
                //TO DO!
                //can't find ensemblToGene or knownToEnsembl - throw an error?
                return  transcripts;
            }
        }
        return transcripts;
    }
    
    
    @Override
    public ArrayList<GeneDetails> getGeneFromId(String id, String build, String db) 
            throws SQLException, GetGeneExonsException{
        String fieldsToRetrieve = String.join(", ", fields);
        String symbol = new String();
        checkConnection();
        System.out.println("SELECT " + fieldsToRetrieve + 
                " FROM " + build + "." + db + " WHERE name='"+ id + "'");
        ResultSet rs = doQuery("SELECT " + fieldsToRetrieve + 
                " FROM " + build + "." + db + " WHERE name='"+ id + "'");
        ResultSet rs2 = doQuery("SELECT COUNT(*) FROM "
                + "information_schema.tables  WHERE table_schema = '" + build +
                "' AND table_name = 'ensemblToGeneName';");
        while (rs2.next()){
            if (rs2.getInt("COUNT(*)") < 1){
                symbol = getSymbolViaKgId(id, build);
            }else{
                symbol = getSymbolFromEnsemblToName(id, build);
            }
        }
        return getTranscriptsFromResultSet(rs, symbol);
    }
    
    private String getSymbolFromEnsemblToName(String id, String build) 
            throws SQLException{
        String symbol = new String();
        checkConnection();
        ResultSet rs2 = doQuery("SELECT name, value FROM "
            + build + ".ensemblToGeneName WHERE name='" + id +"';");
        while (rs2.next()){
            symbol = rs2.getString("value");
        }
        return symbol;
    }
    
    private String getSymbolViaKgId(String id, String build) throws SQLException{
        String symbol = new String();
        checkConnection();
        ResultSet rs2 = doQuery("SELECT COUNT(*) FROM "
                    + "information_schema.tables  WHERE table_schema = '"+ 
                build +"' AND table_name = 'knownToEnsembl';");
        while (rs2.next()){
            if (rs2.getInt("COUNT(*)") > 0){
                ResultSet rs3 = doQuery("Select name FROM " + build 
                        + ".knownToEnsembl WHERE value='" + 
                        id + "';");
                while (rs3.next()){
                    ResultSet rs4 = doQuery("SELECT geneSymbol FROM " 
                            + build + ".kgXref WHERE kgID='" + rs3.getString("name") +"';");
                    while (rs4.next()){
                        symbol = rs4.getString("geneSymbol");
                    }
                }
            }
        }
        return symbol;
    }
}//end of class
