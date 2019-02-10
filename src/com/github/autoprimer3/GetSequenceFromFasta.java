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
 * @author David A. Parry
 */
import java.io.BufferedReader;
import net.sf.picard.reference.*;
import net.sf.samtools.SAMFileHeader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.sf.samtools.util.SequenceUtil;

public class GetSequenceFromFasta {
    File fastaFile;
    ReferenceSequenceFile fastaRef;
    File fastaDict ;
    
    GetSequenceFromFasta (File fasta) 
            throws SequenceFromFastaException, SamHeaderFromDictException{
        
        fastaFile = fasta;
        fastaDict = new File(fasta.getName() + ".fai");
        if (! fastaDict.exists()){
            throw new SequenceFromFastaException("Fasta dictionary " + 
                    fastaDict.getAbsolutePath() + " for fasta file " +
                    fastaFile.getAbsolutePath() + " does not exist.");
        }        
        fastaRef = ReferenceSequenceFileFactory.getReferenceSequenceFile(fasta);
    }
    
    public String retrieveSequence(String chrom, Integer start, Integer end, String strand)
        throws SequenceFromFastaException{
        try{
            final ReferenceSequence seq = fastaRef.getSubsequenceAt(chrom, start, end);
            final byte[] bases = seq.getBases();
            if (strand != null && strand.equals("-")){
                SequenceUtil.reverseComplement(bases);
            }
            return new String(bases);
        }catch (UnsupportedOperationException ex){
            throw new SequenceFromFastaException("Error retrieving fasta sequence "
                    + "from " + fastaFile.getName(), ex);
        }
    }
    
    public SAMFileHeader getSamHeaderFromDict  (File dict) throws SamHeaderFromDictException{
        StringBuilder dictString = new StringBuilder();
        try{
            FileReader fileReader = new FileReader(dict);
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null){
                lineCount++;
                List<String> splitLine = Arrays.asList(line.split("\t"));
                if (splitLine.size() != 5){
                    throw new SamHeaderFromDictException("Invalid sequence dictionary"
                    + " - invalid number of fields. Should be 5, found " + splitLine.size());
                }
                for (int i = 1; i < splitLine.size(); i++){//check all but chrom field are integers
                    try{
                        Integer.parseInt(splitLine.get(i));
                    }catch(NumberFormatException ex){
                        throw new SamHeaderFromDictException("Invalid sequence "
                                + "dictionary" + " - invalid field in line " + 
                                lineCount + ", field " + (1 + i) + ". Value '" 
                                + splitLine.get(i) + "' is not an integer.", ex);
                    }
                }// all non-chrom fields are integers
                dictString.append("@SQ\tSN:").append(splitLine.get(0))
                        .append("\tLN:").append(splitLine.get(1)).append("\n");
            }
        }catch(IOException ex){
            throw new SamHeaderFromDictException("IO error reading sequence "
                    + "dictionary file " + dict.getName(), ex);
        }
        if (dictString.length() < 1){
            throw new SamHeaderFromDictException("Dictionary file is empty");
        }
        SAMFileHeader header = new SAMFileHeader();
        header.setTextHeader(dictString.toString());
        return header;
    }
    
    public class SamHeaderFromDictException extends Exception{
        public SamHeaderFromDictException() { super(); }
        public SamHeaderFromDictException(String message) { super(message); }
        public SamHeaderFromDictException(String message, Throwable cause) { super(message, cause); }
        public SamHeaderFromDictException(Throwable cause) { super(cause); }
    }
    
    public class SequenceFromFastaException extends Exception{
        public SequenceFromFastaException() { super(); }
        public SequenceFromFastaException(String message) { super(message); }
        public SequenceFromFastaException(String message, Throwable cause) { super(message, cause); }
        public SequenceFromFastaException(Throwable cause) { super(cause); }
    }
}