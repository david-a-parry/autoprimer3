/*
 * Copyright (C) 2014 david
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * @author david
 */
public class AutoPrimer3Config implements Serializable{
    String fileSeparator = System.getProperty("file.separator");
    File configDir =  new File(System.getProperty("user.home") + fileSeparator
                    + ".AutoPrimer3");
    File configFile = new File (configDir  + fileSeparator + "config.ser");
    private HashMap<String, String> buildToMapMaster = new HashMap<>();
//maps build name to url e.g. hg19 => http://genome.cse.ucsc.edu:80/cgi-bin/das/hg19
//a list of IDs so we can retrieve them in the same order they're given
    private LinkedHashMap<String, String> buildToDescription = new LinkedHashMap<>();
//maps build name to description e.g. hg19 => 'Human Feb. 2009 (GRCh37/hg19) Genome at UCSC'
    private HashMap<String, ArrayList<String>> buildToTables = new HashMap<>();
    
    public HashMap<String, String> getBuildToMapMaster(){
        return buildToMapMaster;
    }
    
    public LinkedHashMap<String, String> getBuildToDescription(){
        return buildToDescription;
    }
    
    public  HashMap<String, ArrayList<String>> getBuildToTables(){
        return buildToTables;
    }
    
    public void setBuildToMapMaster(HashMap<String, String> buildMaster){
        buildToMapMaster = buildMaster;
    }
    
    public void setBuildToDescription(LinkedHashMap<String, String> buildDesc){
        buildToDescription = buildDesc;
    }
    
    public  void setBuildToTables(HashMap<String, ArrayList<String>> buildTables){
        buildToTables = buildTables;
    }
    
    public void writeConfig()throws IOException{
        writeConfig(buildToMapMaster, buildToDescription, buildToTables);
    }
    
    public void writeConfig(HashMap<String, String> buildToMap, 
            LinkedHashMap<String, String> buildToDescription,
            HashMap<String, ArrayList<String>> buildToTable) throws IOException{
        if (! configDir.exists()){
            configDir.mkdir();
        }
        File temp = File.createTempFile("config", ".ser");
        FileOutputStream fos = new FileOutputStream(temp);
        ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(fos));
        System.out.println(buildToMap.toString());
        System.out.println(buildToDescription.toString());
        System.out.println(buildToTable.toString());
        out.writeObject(buildToMap);
        out.writeObject(buildToDescription);
        out.writeObject(buildToTable);
        out.close();
        Files.move(temp.toPath(), configFile.toPath(), REPLACE_EXISTING);
    }
    
    public void readConfig() throws IOException, ClassNotFoundException{
        if (configFile.exists()){
            readConfigFile(configFile);
        }else{
            InputStream  is = new ObjectInputStream(new BufferedInputStream
            (this.getClass().getResourceAsStream("config.ser")));
            ObjectInputStream ois = new ObjectInputStream(is);
            buildToMapMaster = (HashMap<String, String>) ois.readObject();
            buildToDescription = (LinkedHashMap<String, String> ) ois.readObject();  
            buildToTables = (HashMap<String, ArrayList<String>>) ois.readObject();
            is.close();
            ois.close();
        }
    }
    
    private void readConfigFile(File config)throws IOException, ClassNotFoundException{
        if (! config.exists()){
            return;
        }
        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream
            (new FileInputStream(config)));
        buildToMapMaster = (HashMap<String, String>) ois.readObject();
        buildToDescription = (LinkedHashMap<String, String> ) ois.readObject();
        buildToTables = (HashMap<String, ArrayList<String>>) ois.readObject();
        ois.close();
    }
}
