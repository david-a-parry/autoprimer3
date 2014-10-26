/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.autoprimer3;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 *
 * @author David A. Parry <d.a.parry@leeds.ac.uk>
 * 
 * Connect to DAS server on UCSC and retrieve all genome builds in constructor
 * The method getAvailableTables returns all available tables for a given build
 */
public class GetUcscBuildsAndTables {
    
    private HashMap<String, String> buildToMapMaster = new HashMap<>();//maps build name to url e.g. hg19 => http://genome.cse.ucsc.edu:80/cgi-bin/das/hg19
    private ArrayList<String> buildIds = new ArrayList<>();//a list of IDs so we can retrieve them in the same order they're given
    private HashMap<String, String> buildToDescription = new HashMap<>();//maps build name to description e.g. hg19 => 'Human Feb. 2009 (GRCh37/hg19) Genome at UCSC'
    
    //get build names and DAS urls
    public void connectToUcsc(){
        try{
            SAXReader reader = new SAXReader();
            //URL url = new URL("http://genome.ucsc.edu/cgi-bin/das/dsn");
            URL url = new URL("http://genome-euro.ucsc.edu/cgi-bin/das/dsn");    
            Document dasXml;
            dasXml  = reader.read(url);
            Element root = dasXml.getRootElement();
            for ( Iterator i = root.elementIterator( "DSN" ); i.hasNext(); ) {
                Element dsn = (Element) i.next();
                Element source = dsn.element("SOURCE");
                Attribute build = source.attribute("id");
                Element mapmaster = dsn.element("MAPMASTER");
                Element desc = dsn.element("DESCRIPTION");
                buildToMapMaster.put(build.getValue(), mapmaster.getText());
                buildIds.add(build.getValue());
                buildToDescription.put(build.getValue(), desc.getText());
            }
        }catch (DocumentException|MalformedURLException ex){
            //TO DO - handle (throw) this error properly
            ex.printStackTrace();
        }
    }
    
    public HashMap<String, String> getBuildToMapMaster(){
        return buildToMapMaster;
    }
    
    public HashMap<String, String> getBuildToDescription(){
        return buildToDescription;
    }
    
    public ArrayList<String> getBuildIds(){
        return buildIds;
    }
    
    public String getGenomeDescription(String genome){
        return buildToDescription.get(genome);
    }
    
    public ArrayList<String> getAvailableTables(String build){
           if (buildIds.isEmpty()){
               this.connectToUcsc();
           }
        ArrayList<String> tables = new ArrayList<>();
        try{
            SAXReader reader = new SAXReader();
            URL url = new URL("http://genome.ucsc.edu/cgi-bin/das/" + build + "/types");    
            Document dasXml;
            dasXml  = reader.read(url);
            Element root = dasXml.getRootElement();
            Element gff = root.element("GFF");
            Element segment = gff.element("SEGMENT");
            for (Iterator i = segment.elementIterator("TYPE"); i.hasNext();){
                Element type = (Element) i.next();
                Attribute id = type.attribute("id");
                tables.add(id.getValue());
            }
            return tables;
        }catch (DocumentException|MalformedURLException ex){
            //TO DO - handle (throw) this error properly
            ex.printStackTrace();
            return null;
        }
    }
    
    public ArrayList<String> getAvailableTables(String build, String category){
        if (buildIds.isEmpty()){
            this.connectToUcsc();
        }
        ArrayList<String> tables = new ArrayList<>();
        try{
            SAXReader reader = new SAXReader();
            URL url = new URL("http://genome.ucsc.edu/cgi-bin/das/" + build + "/types");    
            Document dasXml;
            dasXml  = reader.read(url);
            Element root = dasXml.getRootElement();
            Element gff = root.element("GFF");
            Element segment = gff.element("SEGMENT");
            for (Iterator i = segment.elementIterator("TYPE"); i.hasNext();){
                Element type = (Element) i.next();
                Attribute id = type.attribute("id");
                Attribute cat = type.attribute("category");
                if (cat.getValue().equals(category)){
                    tables.add(id.getValue());
                }
            }                    
            return tables;
        }catch (DocumentException|MalformedURLException ex){
            //TO DO - handle (throw) this error properly
            ex.printStackTrace();
            return null;
        }
    }
    //uses 0-based coordinates for compatibility with gene tables, bed etc., although DAS uses 1-based
    public String retrieveSequence(String build, String chrom, Integer start, Integer end){
        if (buildIds.isEmpty()){
            this.connectToUcsc();
        }
        if (! buildToMapMaster.containsKey(build)){
            return null;
        }else{
            try{
                StringBuilder dna = new StringBuilder();
                URL genomeUrl = new URL(buildToMapMaster.get(build) + 
                        "/dna?segment="+chrom + ":" + (start + 1) + "," + end);
                SAXReader reader = new SAXReader();
                Document dasXml;
                dasXml  = reader.read(genomeUrl);
                Element root = dasXml.getRootElement();
                for ( Iterator i = root.elementIterator( "SEQUENCE" ); i.hasNext(); ) {
                    Element dsn = (Element) i.next();
                    Element seq = dsn.element("DNA");
                    String text = seq.getText().replaceAll("\n", "");
                    dna.append(text);
                    //dna.append(seq.getText());
                }
                return dna.toString();
            }catch(DocumentException|MalformedURLException ex){
                //TO DO - handle (throw) this error properly
                ex.printStackTrace();
                return null;
            }
            
        }
        
    }
    
}
