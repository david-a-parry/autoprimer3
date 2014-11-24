package com.github.autoprimer3;


import java.util.HashMap;

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
/**
 *
 * @author David A. Parry <d.a.parry@leeds.ac.uk>
 */
public class BuildToMisprimingLibrary {
    public static final HashMap<String, String> buildToMisprime = new HashMap<>();
    static{
        initializeBuildsToMisprime();
    }
    
    public String getMisprimingLibrary(String build){
        if (buildToMisprime.containsKey(build)){
            return buildToMisprime.get(build);
        
        }else{
            return "none";
        }
    }
    
    public static void initializeBuildsToMisprime(){
        buildToMisprime.put("hg", "human");
        buildToMisprime.put("panTro", "human");
        buildToMisprime.put("gorGor", "human");
        buildToMisprime.put("ponAbe", "human");
        buildToMisprime.put("nomLeu", "human");
        buildToMisprime.put("rheMac", "human");
        buildToMisprime.put("papAnu", "human");
        buildToMisprime.put("saiBol", "human");
        buildToMisprime.put("calJac", "human");
        buildToMisprime.put("tarSyr", "human");
        buildToMisprime.put("micMur", "human");
        buildToMisprime.put("tupBel", "human");
        buildToMisprime.put("mm", "rodent");
        buildToMisprime.put("rn", "rodent");
        buildToMisprime.put("dipOrd", "rodent");
        buildToMisprime.put("criGri", "rodent");
        buildToMisprime.put("hetGla", "rodent");
        buildToMisprime.put("cavPor", "rodent");
        buildToMisprime.put("oryCun", "rodent");
        buildToMisprime.put("speTri", "rodent");
        buildToMisprime.put("ochPri", "rodent");
        buildToMisprime.put("dm", "drosophila");
        buildToMisprime.put("droSim", "drosophila");
        buildToMisprime.put("droSec", "drosophila");
        buildToMisprime.put("droYak", "drosophila");
        buildToMisprime.put("droEre", "drosophila");
        buildToMisprime.put("droAna", "drosophila");
        buildToMisprime.put("droPer", "drosophila");
        buildToMisprime.put("droVir", "drosophila");
        buildToMisprime.put("droMoj", "drosophila");
        buildToMisprime.put("droGri", "drosophila");
        buildToMisprime.put("anoGam", "drosophila");
        buildToMisprime.put("apiMel", "drosophila");
    }
}
