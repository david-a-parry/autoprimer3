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

/**
 *
 * @author David A. Parry <d.a.parry@leeds.ac.uk>
 */
public class Primer3Result {
    private Integer index;
    private String name;
    private String transcripts;
    private String leftPrimer;
    private String rightPrimer;
    private Integer productSize;
    private String chromosome;
    private Integer leftPosition;
    private Integer rightPosition;
    Primer3Result(){
        this(0, null, null, null, null, 0, null, 0, 0);
    }
    Primer3Result(Integer i, String id, String tr, String l, String r, Integer size){
        this(i, id, tr, l, r, size, null, 0, 0);
    }
    Primer3Result(Integer i, String id, String tr, String l, String r, Integer size, 
            String chr, Integer lPos, Integer rPos){
        index = i;
        name = id;
        transcripts = tr;
        leftPrimer = l;
        rightPrimer = r;
        productSize = size;
        chromosome = chr;
        leftPosition = lPos;
        rightPosition = rPos;
    }
    public void setIndex(int i){
        index = i;
    }
    public void setName(String id){
        name = id;
    }
    public void setTranscripts(String tr){
        transcripts = tr;
    }
    public void setLeftPrimer(String seq){
        leftPrimer = seq;
    }
    public void setRightPrimer(String seq){
        rightPrimer = seq;
    }
    public void setProductSize(int size){
        productSize = size;
    }
    public void setChromosome(String chr){
        chromosome = chr;
    }
    public void setLeftPosition(Integer i){
        leftPosition = i;
    }
    public void setRightPosition(Integer i){
        rightPosition = i;
    }
    public Integer getIndex(){
        return index;
    }
    public String getName(){
        return name;
    }
    public String getTranscripts(){
        return transcripts;
    }
    public String getLeftPrimer(){
        return leftPrimer;
    }
    public String getRightPrimer(){
        return rightPrimer;
    }
    public Integer getProductSize(){
        return productSize;
    }
    public String getChromosome(){
        return chromosome;
    }
    public Integer getLeftPosition(){
        return leftPosition;
    }
    public Integer getRightPosition(){
        return rightPosition;
    }
}
