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
        String fieldsToRetrieve = String.join(", ", fields );
        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT " + fieldsToRetrieve + 
                " FROM " + build + "." + db +" WHERE name2='"+ symbol+ "'");
        return getTranscriptsFromResultSet(rs);
    }
    
    public ArrayList<GeneDetails> getGeneFromId(String id, String build, String db) 
            throws SQLException, GetGeneExonsException{
        String fieldsToRetrieve = String.join(", ", fields);
        stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT " + fieldsToRetrieve + 
                " FROM " + build + "." + db + " WHERE name='"+ id + "'");
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
    
    public ArrayList<GenomicRegionSummary> GetSnpCoordinates(
            String chrom, int start, int end, String build, String db)
            throws SQLException{
        ArrayList<GenomicRegionSummary> snpCoordinates = new ArrayList<> ();
        stmt = conn.createStatement();
        //user may not have preceded chromosome with 'chr' which is fine for
        //sequence retrieval but not for snp retrieval in genomes which use it
        ArrayList<String> chromosomes = new ArrayList<>();
        ResultSet chromSet = stmt.executeQuery("SELECT DISTINCT(chrom) AS chrom "
                + "FROM " + build + "." + db);
        while (chromSet.next()){
            chromosomes.add(chromSet.getString("chrom"));
        }
        if (! chromosomes.contains(chrom)){
            chrom = "chr" + chrom;
            if (!chromosomes.contains(chrom)){
                return snpCoordinates;
            }
        }
        ResultSet rs = stmt.executeQuery("SELECT chromStart,chromEnd from " + 
                build+ "." + db + " where chrom='" + chrom + 
                "' and chromEND >= " + start + " and chromStart < " + end);
        
        while (rs.next()){
            snpCoordinates.add(new GenomicRegionSummary(chrom,
                    Integer.valueOf(rs.getString("chromStart")) + 1, 
                    Integer.valueOf(rs.getString("chromEnd"))));
        }
        return snpCoordinates;
    }

}
