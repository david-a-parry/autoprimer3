/*
* Copyright (C) 2013 David A. Parry
 * d.a.parry@leeds.ac.uk
 * https://sourceforge.net/projects/snpviewer/
 * 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.autoprimer3;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author david
 */
public class CoordComparator implements Comparator<String>{
    ChromComparator chromCompare = new ChromComparator();
    @Override
    public int compare(String s1, String s2){
        if (s1.matches("\\w+:\\d+-\\d+") && s2.matches("\\w+:\\d+-\\d+")){
            List<String> split1 =  Arrays.asList(s1.split(":"));
            List<String> split2 =  Arrays.asList(s2.split(":"));
            String chr1 = split1.get(0);
            String chr2 = split2.get(0);
            chr1 = chr1.replaceFirst("chr", "");
            chr2 = chr1.replaceFirst("chr", "");
            int chrComp = chromCompare.compare(chr1, chr2); 
            if (chrComp != 0){
                return chrComp;
            }else{
                List<String> pos1 = Arrays.asList(split1.get(1).split("-"));
                List<String> pos2 = Arrays.asList(split2.get(1).split("-"));
                int startComp = Integer.valueOf(pos1.get(0)).
                        compareTo(Integer.valueOf(pos2.get(0)));
                if (startComp != 0){
                    return startComp;
                }else{
                    return Integer.valueOf(pos1.get(0)).compareTo(Integer.valueOf(pos2.get(0)));
                }
            }
        }else if (s1.matches("\\w+:\\d+-\\d+")){
            return -1;
        }else if (s2.matches("\\w+:\\d+-\\d+")){
            return 1;
        }else{
            return s1.compareTo(s2);
        }
    }
}
