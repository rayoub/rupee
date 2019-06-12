package edu.umkc.rupee.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableDouble;

import edu.umkc.rupee.tm.Functions;
import edu.umkc.rupee.tm.Parameters;
import edu.umkc.rupee.tm.TmAlign;
import edu.umkc.rupee.tm.TmMode;

public class LCS {
    
    public static enum Direction { NONE, UP, LEFT, DIAGONAL_MATCH, DIAGONAL_MISMATCH };

    // when normalizing by the average for full-length comparisons
    public static double getLCSScoreFullLength(List<Integer> grams1, List<Integer> grams2) {

        int[][] s = new int[grams1.size() + 1][grams2.size() + 1];

        s[0][0] = 0;
  
        // initialize first column 
        for (int i = 1; i <= grams1.size(); i++) {
            s[i][0] = -1 * i;
        }

        // initialize first row
        for (int j = 1; j <= grams2.size(); j++) {
            s[0][j] = -1 * j;
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

        return s[grams1.size()][grams2.size()];
    }

    // when normalizing for containment searches (grams1 contained in grams2)
    public static double getLCSScoreContainment(List<Integer> grams1, List<Integer> grams2) {

        int[][] s = new int[grams1.size() + 1][grams2.size() + 1];

        s[0][0] = 0;
  
        // initialize first column 
        for (int i = 1; i <= grams1.size(); i++) {
            s[i][0] = -1 * i;
        }

        // initialize first row
        for (int j = 1; j <= grams2.size(); j++) {
            s[0][j] = 0;
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

        // search for max value
        int maxScore = Integer.MIN_VALUE;
        int i = grams1.size();
        for (int j = 0; j <= grams2.size(); j++) {

            if (s[i][j] >= maxScore) {
                maxScore = s[i][j];
            }
        }

        return maxScore;
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
        while (i != 0 || j != 0) {

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

    public static double getLCSPlusFullLength(Grams grams1, Grams grams2) {

        if (!(grams1.getLength() > 0 && grams2.getLength() > 0))
            return 0.0;

        int[][] s = new int[grams1.getLength() + 1][grams2.getLength() + 1];
        Direction[][] d = new Direction[grams1.getLength() + 1][grams2.getLength() + 1];

        s[0][0] = 0;
        d[0][0] = Direction.NONE;
  
        // initialize first column 
        for (int i = 1; i <= grams1.getLength(); i++) {
            s[i][0] = -1 * i;
            d[i][0] = Direction.UP;
        }

        // initialize first row
        for (int j = 1; j <= grams2.getLength(); j++) {
            s[0][j] = -1 * j;
            d[0][j] = Direction.LEFT;
        }

        // build cost and pointer matrices
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
       
        // chain lengths 
        int xlen = grams1.getLength();
        int ylen = grams2.getLength();
    
        // initialize inverse map 
        int invmap[] = new int[ylen];
        for (int i = 0; i < ylen; i++) {
            invmap[i] = -1;
        }

        // build inverse map
        int i = xlen;
        int j = ylen;
        while (i != 0 || j != 0) {

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
        int align_len = 0;
        for (i = 0; i < ylen; i++) {
            if (invmap[i] >= 0) {
                align_len++;
            }
        }

        // guard on minimum alignment length
        if (align_len < 2) {
            return 0.0;
        }
        
        // pack coords
        double xtm[][] = new double[align_len][3];
        double ytm[][] = new double[align_len][3];
        int k = 0;
        for (j = 0; j < ylen; j++) {

            i = invmap[j];
            if (i >= 0) {
                
                xtm[k][0] = grams1.getCoordsAsList().get(i * 3);
                xtm[k][1] = grams1.getCoordsAsList().get(i * 3 + 1); 
                xtm[k][2] = grams1.getCoordsAsList().get(i * 3 + 2); 
                ytm[k][0] = grams2.getCoordsAsList().get(j * 3);
                ytm[k][1] = grams2.getCoordsAsList().get(j * 3 + 1); 
                ytm[k][2] = grams2.getCoordsAsList().get(j * 3 + 2); 

                k++;
            }
        }
        
        // initialize trivial inverse map
        invmap = new int[align_len];
        for (i = 0; i < align_len; i++) {
            invmap[i] = i;
        }
        
        // initialize dp inverse map 
        int invmap_dp[] = new int[align_len];
        for (i = 0; i < align_len; i++) {
            invmap_dp[i] = -1;
        }

        // run tm-align for aligned grams
        TmAlign tm = new TmAlign(align_len, align_len, TmMode.FAST);
        double[][] xt = new double[align_len][3];
        double[] t = new double[3];
        double[][] u = new double[3][3];
        
        Parameters params = Parameters.getRupeeParameters(align_len);
        int simplify_step = 1; 
        int score_sum_method = 0; 
        double max_score;

        // get the initial rotation matrix and score
        max_score = tm.detailed_search_wrapper(xtm, ytm, align_len, align_len, invmap, t, u, simplify_step, score_sum_method, false, params);

        // try to improve the rotation matrix
        tm.dp_iteration_rupee(xtm, ytm, align_len, align_len, t, u, invmap_dp, TmMode.FAST.getDpIterations(), false, params);
           
        // perform the rotation on the original coords
        Functions.do_rotation(xtm, xt, align_len, t, u);

        // check the new score 
        MutableDouble score = new MutableDouble(0.0);
        int sat_indices[] = new int[align_len];
        tm.calculate_tm_score(xt, ytm, align_len, params.getD0Bounded(), sat_indices, score, score_sum_method, false, params);
        if (score.getValue() > max_score) {
            max_score = score.getValue();
        }

        return max_score;
    }
}
