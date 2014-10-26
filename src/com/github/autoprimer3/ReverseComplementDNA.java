/*
 * baSeq
 * Copyright (C) 2013,2014 David A. Parry
 * d.a.parry@leeds.ac.uk
 * https://sourceforge.net/projects/baseq/
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

/**
 *
 * @author David A. Parry
 */

public class ReverseComplementDNA{
	private ReverseComplementDNA(){};
	public static String reverseComplement(String dna){
		String rev = reverse(dna);
		return complement(rev);
	}
        
//------------------------------------------------------------------------------

        
	public static String reverse(String dna){
		StringBuffer rev = new StringBuffer(dna);
		rev = rev.reverse();
		return rev.toString();
	}

//------------------------------------------------------------------------------

	public static String complement(String dna){
		StringBuilder comp = new StringBuilder(dna.length());
		for (int i = 0; i < dna.length(); i++){
			char nt = dna.charAt(i);
			char cnt;
			switch (nt){
				case 'a': 
					cnt = 't';
					break;
				case 'c': 
					cnt = 'g';
					break;
				case 'g': 
					cnt = 'c';
					break;
				case 't': 
					cnt = 'a';
					break;
                                case 'u': 
					cnt = 'a';
					break;
                                case 'y':
                                        cnt = 'r';
                                        break;
                                case 'r':
                                        cnt = 'y';
                                        break;
                                case 'k':
                                        cnt = 'm';
                                        break;
                                case 'm':
                                        cnt = 'k';
                                        break;
                                case 'd':
                                        cnt = 'h';
                                        break;
                                case 'v':
                                        cnt = 'b';
                                        break;
                                case 'h':
                                        cnt = 'd';
                                        break;
                                case 'b':
                                        cnt = 'v';
                                        break;
				case 'A': 
					cnt = 'T';
					break;
				case 'C': 
					cnt = 'G';
					break;
				case 'G': 
					cnt = 'C';
					break;
				case 'T': 
					cnt = 'A';
					break;
                                case 'U':
                                        cnt = 'A';
                                        break;
                                case 'Y':
                                        cnt = 'R';
                                        break;
                                case 'R':
                                        cnt = 'Y';
                                        break;
                                case 'K':
                                        cnt = 'M';
                                        break;
                                case 'M':
                                        cnt = 'K';
                                        break;
                                case 'D':
                                        cnt = 'H';
                                        break;
                                case 'V':
                                        cnt = 'B';
                                        break;
                                case 'H':
                                        cnt = 'D';
                                        break;
                                case 'B':
                                        cnt = 'V';
                                        break;
				default:
					cnt = nt;//applies to w, s, n or x codes as well as unrecognised
					break;
			}
			comp.append(cnt);
		}
		return comp.toString();
	}
}
