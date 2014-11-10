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

import java.util.LinkedHashSet;

/**
 *
 * @author David A. Parry <d.a.parry@leeds.ac.uk>
 */
public class GeneSearchResult {
    //details of hits
    private LinkedHashSet<GeneDetails> found = new LinkedHashSet();
    //search terms that weren't found
    private LinkedHashSet<String> notFound = new LinkedHashSet();
    //search terms where only non-coding targets were found
    private LinkedHashSet<String> nonCodingTargets = new LinkedHashSet();
    private GetGeneCoordinates geneSearcher;
    GeneSearchResult(LinkedHashSet<GeneDetails> hits, LinkedHashSet<String> misses,
            LinkedHashSet<String> noncoding){
        found = hits;
        notFound = misses;
        nonCodingTargets = noncoding;
    }
    
    GeneSearchResult(LinkedHashSet<GeneDetails> hits, LinkedHashSet<String> misses,
            LinkedHashSet<String> noncoding, GetGeneCoordinates g){
        found = hits;
        notFound = misses;
        nonCodingTargets = noncoding;
        geneSearcher = g;
    }
    
    
    public void setFound(LinkedHashSet<GeneDetails> f){
        found = f;
    }
    public void setNotFound(LinkedHashSet<String> n){
        notFound = n;
    }
    public void setNonCoding (LinkedHashSet<String> n){
        nonCodingTargets = n;
    }
    public void setGeneSearcher(GetGeneCoordinates g){
        geneSearcher = g;
    }
    public LinkedHashSet<GeneDetails> getFound(){
        return found;
    }
    public LinkedHashSet<String> getNotFound(){
        return notFound;
    }
    public LinkedHashSet<String> getNonCodingTargets(){
        return nonCodingTargets;
    }
    public GetGeneCoordinates getGeneSearcher(){
        return geneSearcher;
    }
}
