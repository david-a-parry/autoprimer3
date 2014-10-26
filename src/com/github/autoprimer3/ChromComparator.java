/*
 * Snp Viewer - a program for viewing SNP data and identifying regions of homozygosity
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

import java.util.Comparator;

/**
 *
 * @author david
 */
public class ChromComparator implements Comparator<String>{
    @Override
    public int compare(String s1, String s2){
        if (s1.matches("^\\d+$") && s2.matches("^\\d+$")){
            return Integer.valueOf(s1).compareTo(Integer.valueOf(s2));
        }else if (s1.matches("^\\d+$")){
            return -1;
        }else if (s2.matches("^\\d+$")){
            return +1;
        }else if (s1.matches("^[gG]\\S+") || s2.matches("^[gG]\\S+")){
            if (s1.matches("^[gG]\\S+") && s2.matches("^[gG]\\S+")){
                return s1.compareTo(s2);
            }
            else if (s2.matches("^[gG]\\S+")){
                return +1;
            }else{
                return -1;
            }
        }else if (s1.matches("^[mM]\\w*") || s2.matches("^[mM]\\w*")){
            if (s1.matches("^[mM]\\w*") && s2.matches("^[mM]\\w*")){
                return s1.compareTo(s2);
            }else if (s1.matches("^[mM]\\w*")){
                return +1;
            }else {
                return -1;
            }
        }else{
            return s1.compareTo(s2);
        }
    }
}
