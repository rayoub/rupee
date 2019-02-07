package edu.umkc.rupee.lib;

import java.util.List;

public class LCS {
    
    public static enum Direction { NONE, UP, LEFT, DIAGONAL_MATCH, DIAGONAL_MISMATCH };

    // when normalizing by the average for full-length comparisons
    public static int getLCSScoreFullLength(List<Integer> grams1, List<Integer> grams2) {

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
    public static int getLCSScoreContainment(List<Integer> grams1, List<Integer> grams2) {

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

    public static void printLCSFullLength(List<Integer> grams1, List<Integer> grams2) {

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

        // build output
        StringBuilder seq1 = new StringBuilder();
        StringBuilder seq2 = new StringBuilder();
        int i = grams1.size();
        int j = grams2.size();
        while (i != 0 || j != 0) {

            if (d[i][j] == Direction.DIAGONAL_MATCH) {

                //diagonal match
                seq1.append((grams1.get(i-1) % Constants.DEC_POW_3) / Constants.DEC_POW_2);
                seq2.append((grams2.get(j-1) % Constants.DEC_POW_3) / Constants.DEC_POW_2);
                i = i - 1;
                j = j - 1;
            }
            else if (d[i][j] == Direction.DIAGONAL_MISMATCH) {

                //diagonal mismatch
                seq1.append("*");
                seq2.append("*");
                i = i - 1;
                j = j - 1;
            }
            else if (d[i][j] == Direction.UP) {

                // up gap
                seq1.append((grams1.get(i-1) % Constants.DEC_POW_3) / Constants.DEC_POW_2);
                seq2.append("-");
                i = i - 1;
            }
            else { // b[i][j] == Direction.LEFT

                // left gap
                seq1.append("-");
                seq2.append((grams2.get(j-1) % Constants.DEC_POW_3) / Constants.DEC_POW_2);
                j = j - 1;
            }
        }

        // print output
        System.out.println(seq1.reverse());
        System.out.println("");
        System.out.println(seq2.reverse());
    }

    public static void printLCSContainment(List<Integer> grams1, List<Integer> grams2) {

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

        // build output
        StringBuilder seq1 = new StringBuilder();
        StringBuilder seq2 = new StringBuilder();
        i = grams1.size();
        j = maxJ;
        while (i != 0) {

            if (d[i][j] == Direction.DIAGONAL_MATCH) {

                //diagonal match
                seq1.append((grams1.get(i-1) % Constants.DEC_POW_3) / Constants.DEC_POW_2);
                seq2.append((grams2.get(j-1) % Constants.DEC_POW_3) / Constants.DEC_POW_2);
                i = i - 1;
                j = j - 1;
            }
            else if (d[i][j] == Direction.DIAGONAL_MISMATCH) {

                //diagonal mismatch
                seq1.append("*");
                seq2.append("*");
                i = i - 1;
                j = j - 1;
            }
            else if (d[i][j] == Direction.UP) {

                // up gap
                seq1.append((grams1.get(i-1) % Constants.DEC_POW_3) / Constants.DEC_POW_2);
                seq2.append("-");
                i = i - 1;
            }
            else { // b[i][j] == Direction.LEFT

                // left gap
                seq1.append("-");
                seq2.append((grams2.get(j-1) % Constants.DEC_POW_3) / Constants.DEC_POW_2);
                j = j - 1;
            }
        }

        // print output
        System.out.println(seq1.reverse());
        System.out.println("");
        System.out.println(seq2.reverse());
    }
}
