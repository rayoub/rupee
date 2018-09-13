package edu.umkc.rupee.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LCS {
    
    public static enum Direction { NONE, UP, LEFT, DIAGONAL };

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
                if (grams1.get(i-1).equals(grams2.get(j-1))) {

                    // diagonal
                    s[i][j] = s[i-1][j-1] + 1;
                }
                else if (s[i-1][j] >= s[i][j-1]) {

                    // up
                    s[i][j] = s[i-1][j] - 1;
                }
                else {

                    // left
                    s[i][j] = s[i][j-1] - 1;
                }
            }
        }

        return s[grams1.size()][grams2.size()];
    }

    public static void printLCS(List<Integer> grams1, List<Integer> grams2, Map<Integer, String> codeMap) {

        int[][] c = new int[grams1.size() + 1][grams2.size() + 1];
        Direction[][] b = new Direction[grams1.size() + 1][grams2.size() + 1];

        c[0][0] = 0;
        b[0][0] = Direction.NONE;
  
        // initialize first column 
        for (int i = 1; i <= grams1.size(); i++) {
            c[i][0] = 0;
            b[i][0] = Direction.UP;
        }

        // initialize first row
        for (int j = 1; j <= grams2.size(); j++) {
            c[0][j] = 0;
            b[0][j] = Direction.LEFT;
        }

        // build cost and pointer matrices
        for (int i = 1; i <= grams1.size(); i++) {
            for (int j = 1; j <= grams2.size(); j++) {
                if (grams1.get(i-1).equals(grams2.get(j-1))) {

                    // diagonal match
                    c[i][j] = c[i-1][j-1] + 1;
                    b[i][j] = Direction.DIAGONAL;
                }
                else if (c[i-1][j] >= c[i][j-1]) {

                    // up gap
                    c[i][j] = c[i-1][j];
                    b[i][j] = Direction.UP;
                }
                else {

                    // left gap
                    c[i][j] = c[i][j-1];
                    b[i][j] = Direction.LEFT;
                }
            }
        }

        // build output
        StringBuilder seq1 = new StringBuilder();
        StringBuilder seq2 = new StringBuilder();
        int i = grams1.size();
        int j = grams2.size();
        while (i != 0 || j != 0) {

            if (b[i][j] == Direction.DIAGONAL) {

                //diagonal match
                seq1.append(codeMap.get(grams1.get(i-1)));
                seq2.append(codeMap.get(grams2.get(j-1)));
                i = i - 1;
                j = j - 1;
            }
            else if (b[i][j] == Direction.UP) {

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
