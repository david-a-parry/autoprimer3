/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.autoprimer3;

import java.net.MalformedURLException;
import java.net.URL;
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
 */
public class SequenceFromDasUcsc {
    HashMap<String, String> buildToMapMaster = new HashMap<>();//maps build name to url e.g. hg19 => http://genome.cse.ucsc.edu:80/cgi-bin/das/hg19
    SequenceFromDasUcsc(){//get build names and DAS urls
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
    //uses 0-based coordinates for compatibility with gene tables, bed etc., although DAS uses 1-based
    public String retrieveSequence(String build, String chrom, Integer start, Integer end){
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
