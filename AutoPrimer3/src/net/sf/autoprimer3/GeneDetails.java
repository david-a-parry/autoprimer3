/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.autoprimer3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author David A. Parry <d.a.parry@leeds.ac.uk>
 */

// all coordinates are 0-based
public class GeneDetails {
    private String symbol;
    private String id;
    private String chrom;
    private String strand;
    private Integer txStart;
    private Integer txEnd;
    private Integer cdsStart;
    private Integer cdsEnd;
    private Integer totalExons;
    private ArrayList<Exon> exons = new ArrayList<>();
    
    public void setSymbol(String geneSymbol){
        symbol = geneSymbol;
    }
    public void setId(String geneId){
        id = geneId;
    }
    public void setChromosome(String chromosome){
        chrom = chromosome;
    }
    public void setStrand(String geneStrand){
        strand = geneStrand;
    }
    public void setTxStart(Integer geneTxStart){
        txStart = geneTxStart;
    }
    public void setTxEnd(Integer geneTxEnd){
        txEnd = geneTxEnd;
    }
    public void setCdsStart(Integer geneCdsStart){
        cdsStart = geneCdsStart;
    }
    public void setCdsEnd(Integer geneCdsEnd){
        cdsEnd = geneCdsEnd;
    }
    public void setTotalExons(Integer geneTotalExons){
        totalExons = geneTotalExons;
    }
    public void setTotalExons(){
        totalExons = exons.size();
    }
    public void setExons(ArrayList<Exon> geneExons){
        exons = geneExons;
    }
    
    public void setExons(String starts, String ends) throws GeneExonsException{
        List<String> s = Arrays.asList(starts.split(","));
        List<String> e = Arrays.asList(ends.split(","));
        if (s.isEmpty() || e.isEmpty()){
            throw new GeneExonsException("No exons found in strings passed to setExons method.");
        }
        if (s.size() != e.size()){
            throw new GeneExonsException("Number of exons starts does not equal "
                    + "number of exon ends from strings passed to setExons method.");
        }
        for (int i = 0; i < s.size(); i++){
            Exon exon = new Exon(Integer.parseInt(s.get(i)), Integer.parseInt(e.get(i)));
            exons.add(exon);
        }
    }
    
    public String getSymbol(){
        return symbol;
    }
    public String getId(){
        return id;
    }
    public String getStrand(){
        return strand;
    }
    public String getChromosome(){
        return chrom;
    }
    public Integer getTxStart(){
        return txStart;
    }
    public Integer getTxEnd(){
        return txEnd;
    }
    public Integer getCdsStart(){
        return cdsStart;
    }
    public Integer getCdsEnd(){
        return cdsEnd;
    }
    public Integer getTotalExons(){
        return totalExons;
    }
    public ArrayList<Exon> getExons(){
        return exons;
    }
    
    public ArrayList<Exon> getCodingRegions(){
        ArrayList<Exon> codingExons = new ArrayList<>();
        for (Iterator<Exon> it = exons.iterator(); it.hasNext();) {
            Exon exon = it.next();
            if (exon.getEnd() < cdsStart){//whole exon before cds, skip
                continue;
            }else if (exon.getStart() < cdsStart){//cds start is in this exon
                if (exon.getEnd() <= cdsEnd){
                    codingExons.add(new Exon(cdsStart, exon.getEnd()));
                }else{
                    codingExons.add(new Exon(cdsStart, cdsEnd));
                }
            }else if (exon.getStart() < cdsEnd){//within cds
                if (exon.getEnd() > cdsEnd){
                    codingExons.add(new Exon(exon.getStart(), cdsEnd));
                }else{
                    codingExons.add(new Exon(exon.getStart(), exon.getEnd()));
                }
            }else{//outside CDS
                continue;
            }
        }
        return codingExons;
    }
    
    public boolean isCoding(){
        if (cdsStart == null || cdsEnd == null){
            return false;
        }
        if (cdsStart < cdsEnd){
            return true;
        }else{
            return false;
        }
    }

    
    public class Exon{
        private Integer start;
        private Integer end;
        
        /*TO DO - implement coding and exon numbering annotations
        private boolean isCoding;
        private Integer codingRegionStart;
        private Integer exonNumber;
        private Integer codingExonNumber;
        */
        
        Exon(){
            this(null, null);
        }
        
        Exon(Integer exonStart, Integer exonEnd){
            start = exonStart;
            end = exonEnd;
        }
        
        public void setStart(Integer exonStart){
            start = exonStart;
        }
        public void setEnd(Integer exonEnd){
            end = exonEnd;
        }
        public Integer getStart(){
            return start;
        }
        public Integer getEnd(){
            return end;
        }
        public Integer getLength(){
            if (start != null && end != null){
                return start - end;
            }else{
                return null;
            }
        }
    }
    
    public class GeneExonsException extends Exception{
        public GeneExonsException() { super(); }
        public GeneExonsException(String message) { super(message); }
        public GeneExonsException(String message, Throwable cause) { super(message, cause); }
        public GeneExonsException(Throwable cause) { super(cause); }
    }
}
