package edu.umkc.rupee.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.umkc.rupee.defs.SearchType;
import edu.umkc.rupee.tm.TmAlign;
import edu.umkc.rupee.tm.TmMode;

public class LCS {
    
    public static enum Direction { NONE, UP, LEFT, DIAGONAL_MATCH, DIAGONAL_MISMATCH };

    public static double getLCSScore(List<Integer> grams1, List<Integer> grams2, SearchType searchType) {
        
        if (!(grams1.size() > 0 && grams2.size() > 0))
            return Integer.MIN_VALUE;

        int[][] s = new int[grams1.size() + 1][grams2.size() + 1];

        s[0][0] = 0;
  
        // initialize first column 
        if (searchType == SearchType.FULL_LENGTH || searchType == SearchType.CONTAINED_IN) {
            for (int i = 1; i <= grams1.size(); i++) {
                s[i][0] = -1 * i;
            }
        }
        else { // CONTAINS
            for (int i = 1; i <= grams1.size(); i++) {
                s[i][0] = 0;
            }
        }

        // initialize first row
        if (searchType == SearchType.FULL_LENGTH || searchType == SearchType.CONTAINS) {
            for (int j = 1; j <= grams2.size(); j++) {
                s[0][j] = -1 * j;
            }
        }
        else { // CONTAINED_IN
            for (int j = 1; j <= grams2.size(); j++) {
                s[0][j] = 0;
            }
        }

        // build cost matrix 
        for (int i = 1; i <= grams1.size(); i++) {
            for (int j = 1; j <= grams2.size(); j++) {

                int diagonal = s[i-1][j-1];
                if (grams1.get(i-1).equals(grams2.get(j-1))) {
                    diagonal += 1;
                }
                else {
                    diagonal -= 1;
                }

                int up = s[i-1][j] - 1;
                int left = s[i][j-1] - 1;

                if (diagonal >= up && diagonal >= left) {
                    s[i][j] = diagonal;
                }
                else if (up >= left) {
                    s[i][j] = up;
                }
                else {
                    s[i][j] = left;
                }
            }
        }
        
        // max indices for max score
        int maxI = grams1.size();
        int maxJ = grams2.size();
        if (searchType == SearchType.CONTAINED_IN) {
            maxJ = 0;
            int maxScore = Integer.MIN_VALUE;
            int i = grams1.size();
            for (int j = 0; j <= grams2.size(); j++) {

                if (s[i][j] >= maxScore) {
                    maxScore = s[i][j];
                    maxJ = j;
                }
            }
        }
        else if (searchType == SearchType.CONTAINS) {
            maxI = 0;
            int maxScore = Integer.MIN_VALUE;
            int j = grams2.size();
            for (int i = 0; i <= grams1.size(); i++) {

                if (s[i][j] >= maxScore) {
                    maxScore = s[i][j];
                    maxI = i;
                }
            }
        }

        return s[maxI][maxJ];
    }

    public static double getLCSPlusScore(Grams grams1, Grams grams2, SearchType searchType) {

        if (!(grams1.getLength() > 0 && grams2.getLength() > 0))
            return 0.0;

        int[][] s = new int[grams1.getLength() + 1][grams2.getLength() + 1];
        Direction[][] d = new Direction[grams1.getLength() + 1][grams2.getLength() + 1];

        // *****************************************************************************    
        // *** run SW algorithm to get score and pointer matrices
        // *****************************************************************************    
        
        s[0][0] = 0;
        d[0][0] = Direction.NONE;
 
        // initialize first column 
        if (searchType == SearchType.FULL_LENGTH || searchType == SearchType.CONTAINED_IN) {
            for (int i = 1; i <= grams1.getLength(); i++) {
                s[i][0] = -1 * i;
                d[i][0] = Direction.UP;
            }
        }
        else { // CONTAINS
            for (int i = 1; i <= grams1.getLength(); i++) {
                s[i][0] = 0;
                d[i][0] = Direction.UP;
            }
        }

        // initialize first row
        if (searchType == SearchType.FULL_LENGTH || searchType == SearchType.CONTAINS) {
            for (int j = 1; j <= grams2.getLength(); j++) {
                s[0][j] = -1 * j;
                d[0][j] = Direction.LEFT;
            }
        }
        else { // CONTAINED_IN
            for (int j = 1; j <= grams2.getLength(); j++) {
                s[0][j] = 0;
                d[0][j] = Direction.LEFT;
            }
        }

        // build score and pointer matrices
        for (int i = 1; i <= grams1.getLength(); i++) {
            for (int j = 1; j <= grams2.getLength(); j++) {

                int diagonal = s[i-1][j-1];
                if (grams1.getGramsAsList().get(i-1).equals(grams2.getGramsAsList().get(j-1))) {
                    diagonal += 1;
                }
                else {
                    diagonal -= 1;
                }

                int up = s[i-1][j] - 1;
                int left = s[i][j-1] - 1;

                if (diagonal >= up && diagonal >= left) {
                    s[i][j] = diagonal;
                    if (grams1.getGramsAsList().get(i-1).equals(grams2.getGramsAsList().get(j-1))) {
                        d[i][j] = Direction.DIAGONAL_MATCH;
                    }
                    else {
                        d[i][j] = Direction.DIAGONAL_MISMATCH;
                    }
                }
                else if (up >= left) {
                    s[i][j] = up;
                    d[i][j] = Direction.UP;
                }
                else {
                    s[i][j] = left;
                    d[i][j] = Direction.LEFT;
                }
            }
        }

        // *****************************************************************************    
        // *** build the inverse map for tm-align descriptor alignment
        // *****************************************************************************    

        int xlen = grams1.getLength();
        int ylen = grams2.getLength();

        // max indices for backward trace
        int maxI = xlen;
        int maxJ = ylen;
        if (searchType == SearchType.CONTAINED_IN) {
            maxJ = 0;
            int maxScore = Integer.MIN_VALUE;
            int i = grams1.getLength();
            for (int j = 0; j <= grams2.getLength(); j++) {

                if (s[i][j] >= maxScore) {
                    maxScore = s[i][j];
                    maxJ = j;
                }
            }
        }
        else if (searchType == SearchType.CONTAINS) {
            maxI = 0;
            int maxScore = Integer.MIN_VALUE;
            int j = grams2.getLength();
            for (int i = 0; i <= grams1.getLength(); i++) {

                if (s[i][j] >= maxScore) {
                    maxScore = s[i][j];
                    maxI = i;
                }
            }
        }
    
        // initialize inverse map 
        int invmap[] = new int[ylen];
        for (int i = 0; i < ylen; i++) {
            invmap[i] = -1;
        }

        // build inverse map
        int i = maxI;
        int j = maxJ;
        while (
                (searchType == SearchType.FULL_LENGTH && i != 0 && j != 0)
                ||
                (searchType == SearchType.CONTAINED_IN && i != 0)
                ||
                (searchType == SearchType.CONTAINS && j != 0)
            ) {

            if (d[i][j] == Direction.DIAGONAL_MATCH) {

                //diagonal match
                invmap[j-1] = i-1;
                i = i - 1;
                j = j - 1;
            }
            else if (d[i][j] == Direction.DIAGONAL_MISMATCH) {

                //diagonal mismatch
                i = i - 1;
                j = j - 1;
            }
            else if (d[i][j] == Direction.UP) {

                // up gap
                i = i - 1;
            }
            else { // b[i][j] == Direction.LEFT

                // left gap
                j = j - 1;
            }
        }
        
        // get alignment length
        int alignLen = 0;
        for (i = 0; i < ylen; i++) {
            if (invmap[i] >= 0) {
                alignLen++;
            }
        }

        // guard on minimum alignment length
        if (alignLen < 2) {
            return 0.0;
        }
        
        // *****************************************************************************    
        // *** run the tm-align algorithm and return
        // *****************************************************************************    

/*
        // get x atom coordinates
        double xa[][] = new double[xlen][3];
        for (i = 0; i < xlen; i++) {
            xa[i][0] = grams1.getCoordsAsList().get(i * 3);
            xa[i][1] = grams1.getCoordsAsList().get(i * 3 + 1); 
            xa[i][2] = grams1.getCoordsAsList().get(i * 3 + 2); 
        }

        // get y atom coordinates
        double ya[][] = new double[ylen][3];
        for (i = 0; i < ylen; i++) {
            ya[i][0] = grams2.getCoordsAsList().get(i * 3);
            ya[i][1] = grams2.getCoordsAsList().get(i * 3 + 1); 
            ya[i][2] = grams2.getCoordsAsList().get(i * 3 + 2); 
        }
*/

        // pack coords
        double xa[][] = new double[alignLen][3];
        double ya[][] = new double[alignLen][3];
        int k = 0;
        for (j = 0; j < ylen; j++) {

            i = invmap[j];
            if (i >= 0) {
               
                xa[k][0] = grams1.getCoordsAsList().get(i * 3);
                xa[k][1] = grams1.getCoordsAsList().get(i * 3 + 1); 
                xa[k][2] = grams1.getCoordsAsList().get(i * 3 + 2); 
                ya[k][0] = grams2.getCoordsAsList().get(j * 3);
                ya[k][1] = grams2.getCoordsAsList().get(j * 3 + 1); 
                ya[k][2] = grams2.getCoordsAsList().get(j * 3 + 2); 

                k++;
            }
        }
        
        // initialize trivial inverse map 
        int invmap_trivial[] = new int[alignLen];
        for (i = 0; i < alignLen; i++) {
            invmap_trivial[i] = i;
        }

        double normalizeBy = 0;
        if (searchType == SearchType.FULL_LENGTH) {
            normalizeBy = alignLen;
        }
        else if (searchType == SearchType.CONTAINED_IN) {
            normalizeBy = xlen;
        }
        else { // CONTAINS
            normalizeBy = ylen;
        }

        TmAlign tm = new TmAlign(xa, ya, TmMode.FAST);
        double score = tm.alignDescriptors(invmap_trivial, searchType, normalizeBy);

        return score;
    }

    public static void printLCSFullLength(List<Integer> grams1, List<Integer> grams2, Map<Integer, String> codeMap) {

        int[][] s = new int[grams1.size() + 1][grams2.size() + 1];
        Direction[][] d = new Direction[grams1.size() + 1][grams2.size() + 1];

        s[0][0] = 0;
        d[0][0] = Direction.NONE;
  
        // initialize first column 
        for (int i = 1; i <= grams1.size(); i++) {
            s[i][0] = -1 * i;
            d[i][0] = Direction.UP;
        }

        // initialize first row
        for (int j = 1; j <= grams2.size(); j++) {
            s[0][j] = -1 * j;
            d[0][j] = Direction.LEFT;
        }

        // build cost and pointer matrices
        for (int i = 1; i <= grams1.size(); i++) {
            for (int j = 1; j <= grams2.size(); j++) {

                int diagonal = s[i-1][j-1];
                if (grams1.get(i-1).equals(grams2.get(j-1))) {
                    diagonal += 1;
                }
                else {
                    diagonal -= 1;
                }

                int up = s[i-1][j] - 1;
                int left = s[i][j-1] - 1;

                if (diagonal >= up && diagonal >= left) {
                    s[i][j] = diagonal;
                    if (grams1.get(i-1).equals(grams2.get(j-1))) {
                        d[i][j] = Direction.DIAGONAL_MATCH;
                    }
                    else {
                        d[i][j] = Direction.DIAGONAL_MISMATCH;
                    }
                }
                else if (up >= left) {
                    s[i][j] = up;
                    d[i][j] = Direction.UP;
                }
                else {
                    s[i][j] = left;
                    d[i][j] = Direction.LEFT;
                }
            }
        }

        // build original sequence output
        StringBuilder org1 = new StringBuilder();
        StringBuilder org2 = new StringBuilder();
        for (int k = grams1.size() - 1; k >= 0; k--) {
            org1.append(" " + codeMap.get(grams1.get(k)));
        }
        for (int k = grams2.size() - 1; k >= 0; k--) {
            org2.append(" " + codeMap.get(grams2.get(k)));
        }

        // build aligned sequence output
        StringBuilder seq1 = new StringBuilder();
        StringBuilder seq2 = new StringBuilder();
        int i = grams1.size();
        int j = grams2.size();
        while (i != 0 && j != 0) {

            if (d[i][j] == Direction.DIAGONAL_MATCH) {

                //diagonal match
                seq1.append(codeMap.get(grams1.get(i-1)));
                seq2.append(codeMap.get(grams2.get(j-1)));
                i = i - 1;
                j = j - 1;
            }
            else if (d[i][j] == Direction.DIAGONAL_MISMATCH) {

                //diagonal mismatch
                seq1.append("** ");
                seq2.append("** ");
                i = i - 1;
                j = j - 1;
            }
            else if (d[i][j] == Direction.UP) {

                // up gap
                seq1.append(codeMap.get(grams1.get(i-1))); 
                seq2.append("-- ");
                i = i - 1;
            }
            else { // b[i][j] == Direction.LEFT

                // left gap
                seq1.append("-- ");
                seq2.append(codeMap.get(grams2.get(j-1))); 
                j = j - 1;
            }
        }

        // print output
        System.out.println("");
        System.out.println("Sequences:");
        System.out.println("");
        System.out.println(org1.reverse());
        System.out.println("");
        System.out.println(org2.reverse());
        System.out.println("");
        System.out.println("Aligned Sequences:");
        System.out.println("");
        System.out.println(seq1.reverse());
        System.out.println("");
        System.out.println(seq2.reverse());
    }

    public static void printLCSContainment(List<Integer> grams1, List<Integer> grams2, Map<Integer, String> codeMap) {

        int[][] s = new int[grams1.size() + 1][grams2.size() + 1];
        Direction[][] d = new Direction[grams1.size() + 1][grams2.size() + 1];

        s[0][0] = 0;
        d[0][0] = Direction.NONE;
  
        // initialize first column 
        for (int i = 1; i <= grams1.size(); i++) {
            s[i][0] = -1 * i;
            d[i][0] = Direction.UP;
        }

        // initialize first row
        for (int j = 1; j <= grams2.size(); j++) {
            s[0][j] = 0;
            d[0][j] = Direction.LEFT;
        }

        // build cost and pointer matrices
        for (int i = 1; i <= grams1.size(); i++) {
            for (int j = 1; j <= grams2.size(); j++) {

                int diagonal = s[i-1][j-1];
                if (grams1.get(i-1).equals(grams2.get(j-1))) {
                    diagonal += 1;
                }
                else {
                    diagonal -= 1;
                }

                int up = s[i-1][j] - 1;
                int left = s[i][j-1] - 1;

                if (diagonal >= up && diagonal >= left) {
                    s[i][j] = diagonal;
                    if (grams1.get(i-1).equals(grams2.get(j-1))) {
                        d[i][j] = Direction.DIAGONAL_MATCH;
                    }
                    else {
                        d[i][j] = Direction.DIAGONAL_MISMATCH;
                    }
                }
                else if (up >= left) {
                    s[i][j] = up;
                    d[i][j] = Direction.UP;
                }
                else {
                    s[i][j] = left;
                    d[i][j] = Direction.LEFT;
                }
            }
        }

        int i,j;

        // search for max value
        int maxScore = Integer.MIN_VALUE;
        int maxJ = 0;
        i = grams1.size();
        for (j = 0; j <= grams2.size(); j++) {

            if (s[i][j] >= maxScore) {
                maxScore = s[i][j];
                maxJ = j;
            }
        }

        // build original sequence output
        StringBuilder org1 = new StringBuilder();
        StringBuilder org2 = new StringBuilder();
        for (int k = grams1.size() - 1; k >= 0; k--) {
            org1.append(" " + codeMap.get(grams1.get(k)));
        }
        for (int k = grams2.size() - 1; k >= 0; k--) {
            org2.append(" " + codeMap.get(grams2.get(k)));
        }

        // build aligned sequence output
        StringBuilder seq1 = new StringBuilder();
        StringBuilder seq2 = new StringBuilder();
        i = grams1.size();
        j = maxJ;
        while (i != 0) {

            if (d[i][j] == Direction.DIAGONAL_MATCH) {

                //diagonal match
                seq1.append(codeMap.get(grams1.get(i-1)));
                seq2.append(codeMap.get(grams2.get(j-1)));
                i = i - 1;
                j = j - 1;
            }
            else if (d[i][j] == Direction.DIAGONAL_MISMATCH) {

                //diagonal mismatch
                seq1.append("** ");
                seq2.append("** ");
                i = i - 1;
                j = j - 1;
            }
            else if (d[i][j] == Direction.UP) {

                // up gap
                seq1.append(codeMap.get(grams1.get(i-1)));
                seq2.append("-- ");
                i = i - 1;
            }
            else { // b[i][j] == Direction.LEFT

                // left gap
                seq1.append("-- ");
                seq2.append(codeMap.get(grams2.get(j-1)));
                j = j - 1;
            }
        }

        // print output
        System.out.println("");
        System.out.println("Sequences:");
        System.out.println("");
        System.out.println(org1.reverse());
        System.out.println("");
        System.out.println(org2.reverse());
        System.out.println("");
        System.out.println("Aligned Sequences:");
        System.out.println("");
        System.out.println(seq1.reverse());
        System.out.println("");
        System.out.println(seq2.reverse());
    }

    public static Map<Integer, String>  getCodeMap(List<Integer> grams1, List<Integer> grams2) {
     
        // get a list of distinct grams
        List<Integer> copy1 = grams1.stream().collect(Collectors.toList());
        List<Integer> copy2 = grams2.stream().collect(Collectors.toList());
        copy1.addAll(copy2);
        List<Integer> grams = copy1.stream().distinct().sorted().collect(Collectors.toList());

        // get a list of codes
        List<String> codes = new ArrayList<>();
        for (char c1 = 'A'; c1 <= 'Z'; c1++) {
            for(char c2 = 'A'; c2 <= 'Z'; c2++) {
                codes.add("" + c1 + c2 + " ");
            }
        }
     
        // build a code map for the grams
        Map<Integer, String> map = new HashMap<>();
        for (int i = 0; i < grams.size(); i++) {
            map.put(grams.get(i), codes.get(i)); 
        }
        return map;
    }
}
