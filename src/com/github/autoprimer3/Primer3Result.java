/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.autoprimer3;

/**
 *
 * @author David A. Parry <d.a.parry@leeds.ac.uk>
 */
public class Primer3Result {
    private Integer index;
    private String name;
    private String leftPrimer;
    private String rightPrimer;
    private Integer productSize;
    private String chromosome;
    private Integer leftPosition;
    private Integer rightPosition;
    Primer3Result(){
        this(0, null, null, null, 0, null, 0, 0);
    }
    Primer3Result(Integer i, String id, String l, String r, Integer size){
        this(i, id, l, r, size, null, 0, 0);
    }
    Primer3Result(Integer i, String id, String l, String r, Integer size, 
            String chr, Integer lPos, Integer rPos){
        index = i;
        name = id;
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
