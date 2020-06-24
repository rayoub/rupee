package edu.umkc.tm;

public class NW {

/*    Please note this function is not a correct implementation of
*     the N-W dynamic programming because the score tracks back only
*     one layer of the matrix. This code was exploited in TM-align
*     because it is about 1.5 times faster than a complete N-W code
*     and does not influence much the final structure alignment result.
*/
    
    public static void dp_dist(
            boolean path[][], double val[][], 
            double x[][], double y[][], 
            int xlen, int ylen, 
            double t[], double u[][], 
            double d02, double gap_open, 
            int invmap[]) {
        
        int i, j;
        double h, v, d;

        val[0][0] = 0;

        // initialize first column
        for (i = 0; i <= xlen; i++) {
            val[i][0] = 0;
            path[i][0] = false; // not from diagonal
        }

        // initialize first row
        for (j = 0; j <= ylen; j++) {
            val[0][j] = 0;
            path[0][j] = false; // not from diagonal
        }
        
        // initialize inverse map
        for (j = 0; j < ylen; j++) {
            invmap[j] = -1; 
        }

        double xt[] = new double[3];
        double dist;

        // iterate row major
        for (i = 1; i <= xlen; i++) {

            // transform the x coords and store in xt
            Functions.transform(t, u, x[i - 1], xt);

            for (j = 1; j <= ylen; j++) {
                
                // diagonal 
                dist = Functions.dist(xt, y[j - 1]);
                d = val[i - 1][j - 1] + 1.0 / (1 + dist / d02);

                // from left with possible gap open
                h = val[i - 1][j];
                if (path[i - 1][j]) 
                    h += gap_open;

                // from top with possible gap open
                v = val[i][j - 1];
                if (path[i][j - 1]) 
                    v += gap_open;

                if (d >= h && d >= v) {

                    // matched
                    path[i][j] = true; 
                    val[i][j] = d;
                } 
                else {

                    // not matched
                    path[i][j] = false; 
                    if (v >= h)
                        val[i][j] = v;
                    else
                        val[i][j] = h;
                }

            } // for j
        } // for i

        // trace back to extract the alignment
        i = xlen;
        j = ylen;
        while (i > 0 && j > 0) {
            if (path[i][j]) {

                // matched along diagonal
                invmap[j - 1] = i - 1;
                i--;
                j--;
            } 
            else {

                // determine how we got here
                h = val[i - 1][j];
                if (path[i - 1][j])
                    h += gap_open;

                v = val[i][j - 1];
                if (path[i][j - 1])
                    v += gap_open;

                if (v >= h)
                    j--;
                else
                    i--;
            }
        }
    }

    public static void dp_ss(
            boolean path[][], double val[][], 
            int secx[], int secy[], 
            int xlen, int ylen, 
            double gap_open, 
            int invmap[]) {
        
        int i, j;
        double h, v, d;

        val[0][0] = 0;

        // initialize first column
        for (i = 0; i <= xlen; i++) {
            val[i][0] = 0;
            path[i][0] = false; // not from diagonal
        }

        // initialize first row
        for (j = 0; j <= ylen; j++) {
            val[0][j] = 0;
            path[0][j] = false; // not from diagonal
        }
        
        // initialize inverse map
        for (j = 0; j < ylen; j++) {
            invmap[j] = -1; 
        }

        // iterate row major
        for (i = 1; i <= xlen; i++) {
            for (j = 1; j <= ylen; j++) {
               
                // from diagonal 
                if (secx[i - 1] == secy[j - 1]) {
                    d = val[i - 1][j - 1] + 1.0; // matched
                } else {
                    d = val[i - 1][j - 1]; // not matched
                }

                // from left with possible gap open
                h = val[i - 1][j];
                if (path[i - 1][j])
                    h += gap_open;

                // from top with possible gap open
                v = val[i][j - 1];
                if (path[i][j - 1]) 
                    v += gap_open;

                if (d >= h && d >= v) {

                    // matched
                    path[i][j] = true; 
                    val[i][j] = d;
                } 
                else {

                    // not matched
                    path[i][j] = false; 
                    if (v >= h)
                        val[i][j] = v;
                    else
                        val[i][j] = h;
                }

            } // for j
        } // for i

        // trace back to extract the alignment
        i = xlen;
        j = ylen;
        while (i > 0 && j > 0) {
       
            if (path[i][j]) {

                // matched along diagonal
                invmap[j - 1] = i - 1;
                i--;
                j--;
            } 
            else {

                // determine how we got here
                h = val[i - 1][j];
                if (path[i - 1][j])
                    h += gap_open;

                v = val[i][j - 1];
                if (path[i][j - 1])
                    v += gap_open;

                if (v >= h)
                    j--;
                else
                    i--;
            }
        }
    }
    
    public static void dp_score(
            double score[][], boolean path[][], double val[][], 
            int xlen, int ylen, 
            double gap_open, 
            int invmap[]) {

        int i, j;
        double h, v, d;

        val[0][0] = 0;
        
        // initialize first column
        for (i = 0; i <= xlen; i++) {
            val[i][0] = 0;
            path[i][0] = false; // not from diagonal
        }

        // initialize first row
        for (j = 0; j <= ylen; j++) {
            val[0][j] = 0;
            path[0][j] = false; // not from diagonal
        }
        
        // initialize inverse map
        for (j = 0; j < ylen; j++) {
            invmap[j] = -1; 
        }

        // iterate row major
        for (i = 1; i <= xlen; i++) {
            for (j = 1; j <= ylen; j++) {
       
                // from diagonal 
                d = val[i - 1][j - 1] + score[i][j]; 

                // from left with possible gap open
                h = val[i - 1][j];
                if (path[i - 1][j]) 
                    h += gap_open;

                // from top with possible gap open
                v = val[i][j - 1];
                if (path[i][j - 1]) 
                    v += gap_open;

                if (d >= h && d >= v) {

                    // matched 
                    path[i][j] = true; 
                    val[i][j] = d;
                } 
                else {

                    // not matched
                    path[i][j] = false; 
                    if (v >= h)
                        val[i][j] = v;
                    else
                        val[i][j] = h;
                }

            } // for j
        } // for i

        // trace back to extract the alignment
        i = xlen;
        j = ylen;
        while (i > 0 && j > 0) {
            if (path[i][j]) {

                // matched along diagonal
                invmap[j - 1] = i - 1;
                i--;
                j--;
            } 
            else {

                // determine how we got here
                h = val[i - 1][j];
                if (path[i - 1][j])
                    h += gap_open;

                v = val[i][j - 1];
                if (path[i][j - 1])
                    v += gap_open;

                if (v >= h)
                    j--;
                else
                    i--;
            }
        }
    }
}
