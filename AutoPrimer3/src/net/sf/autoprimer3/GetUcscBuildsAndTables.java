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
    //get build names and DAS urls
    GetUcscBuildsAndTables(){
        try{
            SAXReader reader = new SAXReader();
            URL url = new URL("https://genome.ucsc.edu/cgi-bin/das/dsn");    
            Document dasXml;
            dasXml  = reader.read(url);
            Element root = dasXml.getRootElement();
            for ( Iterator i = root.elementIterator( "DSN" ); i.hasNext(); ) {
                Element dsn = (Element) i.next();
                Element source = dsn.element("SOURCE");
                Attribute build = source.attribute("id");
                Element mapmaster = dsn.element("MAPMASTER");
                buildToMapMaster.put(build.getValue(), mapmaster.getText());
            }
        }catch (DocumentException|MalformedURLException ex){
            //TO DO - handle (throw) this error properly
            ex.printStackTrace();
        }
    }
    public ArrayList<String> getAvailableTables(String build){
        ArrayList<String> tables = new ArrayList<>();
        try{
            SAXReader reader = new SAXReader();
            URL url = new URL("http://genome.ucsc.edu/cgi-bin/das/" + build + " /types");    
            Document dasXml;
            dasXml  = reader.read(url);
            Element root = dasXml.getRootElement();
            for ( Iterator i = root.elementIterator( "SEGMENT" ); i.hasNext(); ) {
                Element dsn = (Element) i.next();
                Element type = dsn.element("TYPE");
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
        ArrayList<String> tables = new ArrayList<>();
        try{
            SAXReader reader = new SAXReader();
            URL url = new URL("http://genome.ucsc.edu/cgi-bin/das/" + build + " /types");    
            Document dasXml;
            dasXml  = reader.read(url);
            Element root = dasXml.getRootElement();
            for ( Iterator i = root.elementIterator( "SEGMENT" ); i.hasNext(); ) {
                Element dsn = (Element) i.next();
                Element type = dsn.element("TYPE");
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
    
}
