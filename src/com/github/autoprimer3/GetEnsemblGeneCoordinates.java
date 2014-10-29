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
import static com.github.autoprimer3.GetGeneCoordinates.conn;

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
        Statement stmt3 = conn.createStatement();
        ResultSet rs2 = stmt2.executeQuery("SELECT COUNT(*) FROM "
                + "information_schema.tables  WHERE table_schema = '" + build +
                "' AND table_name = 'ensemblToGeneName';");
        ResultSet rs3; 
        while (rs2.next()){
            if (rs2.getInt("COUNT(*)") < 1){
                ResultSet rs4 = stmt2.executeQuery("SELECT COUNT(*) FROM "
                + "information_schema.tables  WHERE table_schema = '"+ build +"' "
                        + "AND table_name = 'knownToEnsembl';");
                while (rs4.next()){
                    if (rs4.getInt("COUNT(*)") > 0){
                        ArrayList<String> kgids = new ArrayList<>();
                        Statement stmt4 = conn.createStatement();
                        System.out.println("SELECT kgID, "
                                + "geneSymbol FROM " + build + ".kgXref WHERE "
                                + "geneSymbol='" + symbol +"';");
                        ResultSet rs5 = stmt4.executeQuery("SELECT kgID, "
                                + "geneSymbol FROM " + build + ".kgXref WHERE "
                                + "geneSymbol='" + symbol +"';");
                        while(rs5.next()){
                            kgids.add(rs5.getString("kgID"));
                        }
                        rs3 = stmt.executeQuery("Select value FROM '" + build 
                                + ".knownToEnsembl' WHERE name='" + 
                                String.join(" or name=", kgids) + "';");
                        while (rs3.next()){
                            ResultSet rs = stmt.executeQuery("SELECT " + fieldsToRetrieve + 
                                " FROM " + build + "." + db +" WHERE name='"+ rs3.getString("value") + "'");
                            transcripts.addAll(getTranscriptsFromResultSet(rs, symbol));
                        }
                        return transcripts;
                    }else{
                        //TO DO!
                        //can't find ensemblToGene or knownToEnsembl - thrown an error!
                        return  null;
                    }
                }
            }else{
                System.out.println("SELECT name, value FROM '"
                + build + ".ensemblToGeneName' WHERE value='" + symbol +"';");
                rs3 = stmt2.executeQuery("SELECT name, value FROM "
                + build + ".ensemblToGeneName WHERE value='" + symbol +"';");
                while (rs3.next()){
                    ResultSet rs = stmt.executeQuery("SELECT " + fieldsToRetrieve + 
                        " FROM " + build + "." + db +" WHERE name='"+ rs3.getString("name") + "'");
                    transcripts.addAll(getTranscriptsFromResultSet(rs, symbol));
                }
                return transcripts;
            }
            break;
        }
        return null;
    }
    
    
}
