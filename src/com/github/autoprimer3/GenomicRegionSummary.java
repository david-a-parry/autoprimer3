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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
*
* @author david
*/
public class GenomicRegionSummary implements Comparable<GenomicRegionSummary>, Serializable{
    private Integer startPos;
    private Integer endPos;
    private Integer length;
    private String startId;
    private String endId;
    private String chromosome = new String();
    private String id = new String();
    private String name = new String();
    GenomicRegionSummary(){
        this(null, 0, 0, null, null, null, null);
    }
    GenomicRegionSummary(String chrom, int sp, int ep){
        this(chrom, sp, ep, null, null, null, null);
    }
    GenomicRegionSummary(String chrom, int sp, int ep, String sd, String ed){
        this(chrom, sp, ep, sd, ed, null, null);
    }
    GenomicRegionSummary(int sp, int ep){
        this(null, sp, ep,  null, null, null, null);
    }
    GenomicRegionSummary(int sp, int ep, String sd, String ed){
        this(null, sp, ep, sd, ed, null, null);
    }
    GenomicRegionSummary(int sp, int ep, String d){
        this(null, sp, ep, null, null, d, null);
    }
    GenomicRegionSummary(String chrom, int sp, int ep, String sd, String ed, String d, String nm){
        startPos = sp;
        endPos = ep;
        chromosome = chrom;
        startId = sd;
        endId = ed;
        length = endPos - startPos;
        id = d;
        name = nm;
    }
    
    public void setChromosome(String c){
        chromosome = c;
    }
    public void setStartId(String d){
        startId = d;
    }
    public void setEndId(String d){
        endId = d;
    }
    public void setStartPos(int i){
        startPos = i;
    }    
    public void setEndPos(int i){
        endPos = i;
    }
    public void setId(String d){
        id = d;
    }
    public void setName(String n){
        name = n;
    }
    public String getChromosome(){
        return chromosome;
    }
    public String getStartId(){
        return startId;
    }
    public String getEndId(){
        return endId;
    }
    public int getStartPos(){
        return startPos;
    }
    public int getEndPos(){
        return endPos;
    }
    public String getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    
    public boolean isEmpty(){
        return startPos == 0 && endPos == 0;
    }
    
    public Integer getLength(){
        length = endPos - startPos;
        return length;
    }
    public String getCoordinateString(){
        return chromosome + ":" + startPos + "-" + endPos; 
    }
    public String getBedLine(){
        return chromosome + "\\t" + startPos + "\\t" + endPos;
    }
    public String getIdLine(){
        return startId + ";" + endId;
    }

    @Override
    public int compareTo(GenomicRegionSummary r){
        if (chromosome == null && r.getChromosome() != null){
            return 1;
        }else if (chromosome != null && r.getChromosome() == null){
            return -1;
        }else if(chromosome != null && r.getChromosome() != null){
            ChromComparator chromCompare = new ChromComparator();
            int i = chromCompare.compare(chromosome,r.getChromosome());
            //int i =  chromosome.compareToIgnoreCase(r.getChromosome());
            if (i == 0){
                i = startPos - r.getStartPos();
                if (i != 0){
                    return i;
                }else{
                    return endPos - r.getEndPos();
                }
            }else{
                return i;
            }
        }else{
            int i = startPos - r.getStartPos();
            if (i != 0){
                return i;
            }else{
                return endPos - r.getEndPos();
            }
        }
    }
    public void mergeRegionsByPosition(ArrayList<GenomicRegionSummary> regions){
        if (regions.size() < 2){
            return;
        }
        Collections.sort(regions);
        Iterator<GenomicRegionSummary> rIter = regions.iterator();
        GenomicRegionSummary previousRegion = rIter.next();
        ArrayList<GenomicRegionSummary> merged = new ArrayList<>();
        while (rIter.hasNext()){
            GenomicRegionSummary r = rIter.next();
            if (r.getChromosome() == null && 
                    previousRegion.getChromosome() == null){
                if (previousRegion.getEndPos() >= r.getStartPos()){
                    if (previousRegion.getEndPos() < r.getEndPos()){
                        previousRegion.setEndPos(r.getEndPos());
                        previousRegion.setName(previousRegion.getName().concat("/").concat(r.getName()));
                        previousRegion.setId(previousRegion.getId().concat("/").concat(r.getId()));

                    }
                }else{
                    merged.add(previousRegion);
                    previousRegion = r;
                }
            }else if (r.getChromosome() == null || 
                    previousRegion.getChromosome() == null){
                merged.add(previousRegion);
                previousRegion = r;
            }else{//both not null
                if (r.getChromosome().equalsIgnoreCase(previousRegion.getChromosome())
                        && previousRegion.getEndPos() >= r.getStartPos()){
                    if (previousRegion.getEndPos() <= r.getEndPos()){
                        previousRegion.setEndPos(r.getEndPos());
                        if (!previousRegion.getName().equals(r.getName())){
                            previousRegion.setName(previousRegion.getName().
                                    concat("/").concat(r.getName()));
                        }
                        if (!previousRegion.getId().equals(r.getId())){
                            previousRegion.setId(previousRegion.getId().
                                    concat("/").concat(r.getId()));
                        }
                    }
                }else{
                    merged.add(previousRegion);
                    previousRegion = r;
                }
            }
        }
        merged.add(previousRegion);
        regions.clear();
        regions.addAll(merged);
    }

}//end of GenomicRegionSummaryclass
