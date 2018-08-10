package edu.umkc.rupee.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LCS {

    // *********************************************************************
    // LCS
    // *********************************************************************

    public static int[][] getLCSCostMatrix(List<Integer> grams1, List<Integer> grams2) {

        int[][] c = new int[grams1.size() + 1][grams2.size() + 1];
  
        // initialize first column 
        for (int i = 0; i <= grams1.size(); i++) {
            c[i][0] = 0;
        }

        // initialize first row
        for (int j = 0; j <= grams2.size(); j++) {
            c[0][j] = 0;
        }

        for (int i = 1; i <= grams1.size(); i++) {
            for (int j = 1; j <= grams2.size(); j++) {
                if (grams1.get(i-1).equals(grams2.get(j-1))) {

                    // diagonal
                    c[i][j] = c[i-1][j-1] + 1;
                }
                else if (c[i-1][j] >= c[i][j-1]) {

                    // up
                    c[i][j] = c[i-1][j];
                }
                else {

                    // left
                    c[i][j] = c[i][j-1];
                }
            }
        }

        return c;
    }

    public static int getLCSLength(List<Integer> grams1, List<Integer> grams2) {

        int[][] costMatrix = getLCSCostMatrix(grams1, grams2);

        return costMatrix[grams1.size()][grams2.size()];
    }

    public static void printLCS(List<Integer> grams1, List<Integer> grams2, Map<Integer, String> codeMap) {

        int[][] c = new int[grams1.size() + 1][grams2.size() + 1];
        int[][] b = new int[grams1.size() + 1][grams2.size() + 1];
  
        // initialize first column 
        for (int i = 0; i <= grams1.size(); i++) {
            c[i][0] = 0;
            b[i][0] = 1;
        }

        // initialize first row
        for (int j = 0; j <= grams2.size(); j++) {
            c[0][j] = 0;
            b[0][j] = -1;
        }

        // build cost and pointer matrices
        for (int i = 1; i <= grams1.size(); i++) {
            for (int j = 1; j <= grams2.size(); j++) {
                if (grams1.get(i-1).equals(grams2.get(j-1))) {

                    // diagonal match
                    c[i][j] = c[i-1][j-1] + 1;
                    b[i][j] = 0;
                }
                else if (c[i-1][j-1] >= c[i-1][j] && c[i-1][j-1] >= c[i][j-1]) {

                    // diagonal mismatch
                    c[i][j] = c[i-1][j-1];
                    b[i][j] = -2;
                }
                else if (c[i-1][j] >= c[i][j-1]) {

                    // up gap
                    c[i][j] = c[i-1][j];
                    b[i][j] = 1;
                }
                else {

                    // left gap
                    c[i][j] = c[i][j-1];
                    b[i][j] = -1;
                }
            }
        }

        // build output
        StringBuilder seq1 = new StringBuilder();
        StringBuilder seq2 = new StringBuilder();
        int i = grams1.size();
        int j = grams2.size();
        while (i != 0 || j != 0) {

            if (b[i][j] == 0) {

                //diagonal match
                seq1.append(codeMap.get(grams1.get(i-1)));
                seq2.append(codeMap.get(grams2.get(j-1)));
                i = i - 1;
                j = j - 1;
            }
            else if (b[i][j] == -2) {

                //diagonal mismatch
                seq1.append("** ");
                seq2.append("** ");
                i = i - 1;
                j = j - 1;
            }
            else if (b[i][j] == 1) {

                // up gap
                seq1.append(codeMap.get(grams1.get(i-1)));
                seq2.append("-- ");
                i = i - 1;
            }
            else { // b[i][j] == -1

                // left gap
                seq1.append("-- ");
                seq2.append(codeMap.get(grams2.get(j-1)));
                j = j - 1;
            }
        }

        // print output
        System.out.println(seq1.reverse());
        System.out.println("");
        System.out.println(seq2.reverse());
    }
    
    // *********************************************************************
    // Semi-Global LCS
    // *********************************************************************

    public static LCSResults getSemiGlobalLCS(List<Integer> grams1, List<Integer> grams2) {

        int[][] c = new int[grams1.size() + 1][grams2.size() + 1];
        int[][] b = new int[grams1.size() + 1][grams2.size() + 1];
  
        // initialize first column 
        for (int i = 0; i <= grams1.size(); i++) {
            c[i][0] = 0;
            b[i][0] = 1;
        }

        // initialize first row
        for (int j = 0; j <= grams2.size(); j++) {
            c[0][j] = 0;
            b[0][j] = -1;
        }

        // build cost and pointer matrices
        for (int i = 1; i <= grams1.size(); i++) {
            for (int j = 1; j <= grams2.size(); j++) {
                if (grams1.get(i-1).equals(grams2.get(j-1))) {

                    // diagonal match
                    c[i][j] = c[i-1][j-1] + 1;
                    b[i][j] = 0;
                }
                else if (c[i-1][j-1] >= c[i-1][j] && c[i-1][j-1] >= c[i][j-1]) {

                    // diagonal mismatch
                    c[i][j] = c[i-1][j-1];
                    b[i][j] = -2;
                }
                else if (c[i-1][j] >= c[i][j-1]) {

                    // up gap
                    c[i][j] = c[i-1][j];
                    b[i][j] = 1;
                }
                else {

                    // left gap
                    c[i][j] = c[i][j-1];
                    b[i][j] = -1;
                }
            }
        }

        // get min and max match indices
        int iMin = Integer.MAX_VALUE;
        int iMax = Integer.MIN_VALUE;
        int jMin = Integer.MAX_VALUE;
        int jMax = Integer.MIN_VALUE; 
        int k = grams1.size();
        int l = grams2.size();
        while (k != 0 || l != 0) {

            if (b[k][l] == 0) {

                //diagonal match
                iMin = Math.min(k, iMin);
                iMax = Math.max(k, iMax);
                jMin = Math.min(l, jMin);
                jMax = Math.max(l, jMax);
                k = k - 1;
                l = l - 1;
            }
            else if (b[k][l] == -2) {

                //diagonal mismatch
                k = k - 1;
                l = l - 1;
            }
            else if (b[k][l] == 1) {

                // up gap
                k = k - 1;
            }
            else { // b[k][l] == -1

                // left gap
                l = l - 1;
            }
        }

        LCSResults results = new LCSResults();
        results.matchCount = c[grams1.size()][grams2.size()];
        results.iMin = iMin;
        results.iMax = iMax;
        results.jMin = jMin;
        results.jMax = jMax;

        return results;
    }

    public static void printSemiGlobalLCS(List<Integer> grams1, List<Integer> grams2, Map<Integer, String> codeMap) {

        int[][] c = new int[grams1.size() + 1][grams2.size() + 1];
        int[][] b = new int[grams1.size() + 1][grams2.size() + 1];
  
        // initialize first column 
        for (int i = 0; i <= grams1.size(); i++) {
            c[i][0] = 0;
            b[i][0] = 1;
        }

        // initialize first row
        for (int j = 0; j <= grams2.size(); j++) {
            c[0][j] = 0;
            b[0][j] = -1;
        }

        // build cost and pointer matrices
        for (int i = 1; i <= grams1.size(); i++) {
            for (int j = 1; j <= grams2.size(); j++) {
                if (grams1.get(i-1).equals(grams2.get(j-1))) {

                    // diagonal match
                    c[i][j] = c[i-1][j-1] + 1;
                    b[i][j] = 0;
                }
                else if (c[i-1][j-1] >= c[i-1][j] && c[i-1][j-1] >= c[i][j-1]) {

                    // diagonal mismatch
                    c[i][j] = c[i-1][j-1];
                    b[i][j] = -2;
                }
                else if (c[i-1][j] >= c[i][j-1]) {

                    // up gap
                    c[i][j] = c[i-1][j];
                    b[i][j] = 1;
                }
                else {

                    // left gap
                    c[i][j] = c[i][j-1];
                    b[i][j] = -1;
                }
            }
        }
        
        // get min and max match indices
        int iMin = Integer.MAX_VALUE;
        int iMax = Integer.MIN_VALUE;
        int jMin = Integer.MAX_VALUE;
        int jMax = Integer.MIN_VALUE; 
        int k = grams1.size();
        int l = grams2.size();
        while (k != 0 || l != 0) {

            if (b[k][l] == 0) {

                //diagonal match
                iMin = Math.min(k, iMin);
                iMax = Math.max(k, iMax);
                jMin = Math.min(l, jMin);
                jMax = Math.max(l, jMax);
                k = k - 1;
                l = l - 1;
            }
            else if (b[k][l] == -2) {

                //diagonal mismatch
                k = k - 1;
                l = l - 1;
            }
            else if (b[k][l] == 1) {

                // up gap
                k = k - 1;
            }
            else { // b[k][l] == -1

                // left gap
                l = l - 1;
            }
        }

        // build output
        StringBuilder seq1 = new StringBuilder();
        StringBuilder seq2 = new StringBuilder();
        int i = iMax; 
        int j = jMax; 
        while (i >= iMin || j >= jMin) {

            if (b[i][j] == 0) {

                //diagonal match
                seq1.append(codeMap.get(grams1.get(i-1)));
                seq2.append(codeMap.get(grams2.get(j-1)));
                i = i - 1;
                j = j - 1;
            }
            else if (b[i][j] == -2) {

                //diagonal mismatch
                seq1.append("** ");
                seq2.append("** ");
                i = i - 1;
                j = j - 1;
            }
            else if (b[i][j] == 1) {

                // up gap
                seq1.append(codeMap.get(grams1.get(i-1)));
                seq2.append("-- ");
                i = i - 1;
            }
            else { // b[i][j] == -1

                // left gap
                seq1.append("-- ");
                seq2.append(codeMap.get(grams2.get(j-1)));
                j = j - 1;
            }
        }

        // print output
        System.out.println(seq1.reverse());
        System.out.println("");
        System.out.println(seq2.reverse());
    }
    
    // *********************************************************************
    // Common 
    // *********************************************************************
    
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
