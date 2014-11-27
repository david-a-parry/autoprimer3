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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author David A. Parry <d.a.parry@leeds.ac.uk>
 */
public class RegionParser {

    
//------------------------------------------------------------------------------
    public static GenomicRegionSummary readRegion(String s){
        GenomicRegionSummary r = parseAsRegion(s);
        if (r != null){
            return r;
        }
        String[] tabSplit = s.split("\\t");
        r = parseAsBed(tabSplit);
        if (r != null){
            return r;
        }
        r = parseAsVcf(tabSplit);//finally we just return null if we can't parse this region
        return r;
    }
//------------------------------------------------------------------------------    
    public static GenomicRegionSummary parseAsRegion(String s){
        
        if (! s.matches("^\\w+:[\\d,]+$") && 
                ! s.matches("^\\w+:[\\d,]+-[\\d,]+$")){
            return null;
        }
        String[] chromSplit = s.split(":");
        if (chromSplit.length < 2){
            return null;
        }
        String[] posSplit = chromSplit[1].split("-");
        String start;
        String end;
        if (posSplit.length == 1){
            start = posSplit[0].replaceAll(",", "");
            end = start + 1;
        }else if (posSplit.length == 2){
            start = posSplit[0];
            end = posSplit[1];
        }else{
            return null;
        }
        if (! checkInteger(start) || ! checkInteger(end)){
            return null;
        }
        return new GenomicRegionSummary(chromSplit[0], 
                Integer.parseInt(start), Integer.parseInt(end));
    }
//------------------------------------------------------------------------------
    
    public static GenomicRegionSummary parseAsBed(String[] split){
        if (split.length < 3){
            return null;
        }
        String start = split[1].replaceAll(",", "");
        String end   = split[2].replaceAll(",", "");
        if (! checkInteger(start) || ! checkInteger(end)){
            return null;
        }
        return new GenomicRegionSummary(split[0], 
                Integer.parseInt(start) + 1, Integer.parseInt(end));
    }
//------------------------------------------------------------------------------
    
    public static GenomicRegionSummary parseAsVcf(String[] split){
        if (split.length < 2){
            return null;
        }
        String start = split[1].replaceAll(",", "");
        if (! checkInteger(start)){
            return null;
        }
        if (split.length >= 8){
            Pattern p = Pattern.compile("END=(\\d+)");
            Matcher m = p.matcher(split[7]);
            if (m.find()){
                return new GenomicRegionSummary(split[0], 
                        Integer.parseInt(start), Integer.parseInt(m.group(1)));
            }
        }
        if (split.length >= 4){
            Pattern p = Pattern.compile("^(([ATGCN]+),*)+$");
            Matcher m = p.matcher(split[3]);
            if (m.find()){
                String end = m.group(1);
                for (int i = 2; i <= m.groupCount(); i++){
                    end = m.group(i).length() > end.length() ? m.group(i) : end;
                }
                return new GenomicRegionSummary(split[0], 
                        Integer.parseInt(start), Integer.parseInt(end));
            }
        }
        if (split.length == 2){
            //we can take regions that are just chromosome and coordinate if
            //no other fields are present
            return new GenomicRegionSummary(split[0], 
                Integer.parseInt(start), Integer.parseInt(start));
        }
        return null;        
    }
    
//------------------------------------------------------------------------------    
    private static boolean checkInteger(String s){
        return s.matches("^\\d+$"); 
    }
//------------------------------------------------------------------------------
}
