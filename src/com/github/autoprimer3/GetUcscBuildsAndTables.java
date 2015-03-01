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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    
    private HashMap<String, String> buildToMapMaster = new HashMap<>();
//maps build name to url e.g. hg19 => http://genome.cse.ucsc.edu:80/cgi-bin/das/hg19
//a list of IDs so we can retrieve them in the same order they're given
    private LinkedHashMap<String, String> buildToDescription = new LinkedHashMap<>();
//maps build name to description e.g. hg19 => 'Human Feb. 2009 (GRCh37/hg19) Genome at UCSC'
    
    //get build names and DAS urls
    public void connectToUcsc() throws DocumentException, MalformedURLException{
        SAXReader reader = new SAXReader();
        URL url = new URL("http://genome.ucsc.edu/cgi-bin/das/dsn");
        //URL url = new URL("http://genome-euro.ucsc.edu/cgi-bin/das/dsn");    
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
            buildToDescription.put(build.getValue(), desc.getText());
        }
    }
    
    public HashMap<String, String> getBuildToMapMaster(){
        return buildToMapMaster;
    }
    
    public LinkedHashMap<String, String> getBuildToDescription(){
        return buildToDescription;
    }
    
    public ArrayList<String> getBuildIds(){
        return new ArrayList<>(buildToDescription.keySet());
    }
    
    public String getGenomeDescription(String genome){
        return buildToDescription.get(genome);
    }
    
    public LinkedHashSet<String> getAvailableTables(String build) 
            throws DocumentException, MalformedURLException{
        if (buildToDescription.isEmpty()){
           this.connectToUcsc();
        }
        LinkedHashSet<String> tables = new LinkedHashSet<>();
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
    }
    
    public LinkedHashSet<String> getAvailableTables(String build, String category)
            throws DocumentException, MalformedURLException{
        if (buildToDescription.isEmpty()){
            this.connectToUcsc();
        }
        LinkedHashSet<String> tables = new LinkedHashSet<>();
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
    public String retrieveSequence(String build, String chrom, Integer start, Integer end)
            throws DocumentException, MalformedURLException{
        if (buildToDescription.isEmpty()){
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
