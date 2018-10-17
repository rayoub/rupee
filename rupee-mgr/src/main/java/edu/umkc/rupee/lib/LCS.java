package edu.umkc.rupee.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LCS {
    
    public static enum Direction { NONE, UP, LEFT, DIAGONAL_MATCH, DIAGONAL_MISMATCH };

    public static int getLCSScore(List<Integer> grams1, List<Integer> grams2) {

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

    public static void printLCS(List<Integer> grams1, List<Integer> grams2, Map<Integer, String> codeMap) {

        int[][] s = new int[grams1.size() + 1][grams2.size() + 1];
        Direction[][] d = new Direction[grams1.size() + 1][grams2.size() + 1];

        s[0][0] = 0;
        d[0][0] = Direction.NONE;
  
        // initialize first column 
        for (int i = 1; i <= grams1.size(); i++) {
            s[i][0] = 0;
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

        // build output
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

                //diagonal match
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
