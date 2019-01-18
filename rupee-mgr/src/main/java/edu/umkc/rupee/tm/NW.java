package edu.umkc.rupee.tm;

public class NW {

/*    Please note this function is not a correct implementation of
*     the N-W dynamic programming because the score tracks back only
*     one layer of the matrix. This code was exploited in TM-align
*     because it is about 1.5 times faster than a complete N-W code
*     and does not influence much the final structure alignment result.
*/

    // secondary structure plus 
    public static void NWDP_TM(double score[][], boolean path[][], double val[][], int len1, int len2, double gap_open, int j2i[]) {

        // Output:
        // path: DP matrix indicating if matches along diagonal or not
        // val: value of optimal alignment at that point +1 to match and -1 to start a gap
        // j2i: alignment of secy(j) to secx(i)

        int i, j;
        double h, v, d;

        // initialization
        val[0][0] = 0;
        for (i = 0; i <= len1; i++) {
            val[i][0] = 0;
            path[i][0] = false; // not from diagonal
        }

        for (j = 0; j <= len2; j++) {
            val[0][j] = 0;
            path[0][j] = false; // not from diagonal

            // may as well init the alignment to all not aligned
            j2i[j] = -1; 
        }

        // iterate row major
        for (i = 1; i <= len1; i++) {
            for (j = 1; j <= len2; j++) {
       
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
            } // for i
        } // for j

        // trace back to extract the alignment
        i = len1;
        j = len2;
        while (i > 0 && j > 0) {
            if (path[i][j]) {

                // matched along diagonal
                j2i[j - 1] = i - 1;
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

    // get initial5 and DP iter
    public static void NWDP_TM(boolean path[][], double val[][], double x[][], double y[][], int len1, int len2, double t[], double u[][], double d02,
            double gap_open, int j2i[]) {
        
        // Output:
        // path: DP matrix indicating if matches along diagonal or not
        // val: value of optimal alignment at that point +1 to match and -1 to start a gap
        // j2i: alignment of secy(j) to secx(i)

        int i, j;
        double h, v, d;

        // initialization
        val[0][0] = 0;
        for (i = 0; i <= len1; i++) {
            val[i][0] = 0;
            path[i][0] = false; // not from diagonal
        }

        for (j = 0; j <= len2; j++) {
            val[0][j] = 0;
            path[0][j] = false; // not from diagonal
            
            // may as well init the alignment to all not aligned
            j2i[j] = -1; 
        }

        double xx[] = new double[3];
        double dij;

        // iterate row major
        for (i = 1; i <= len1; i++) {

            // transform the x coords and store in temp xx
            Functions.transform(t, u, x[i - 1], xx);

            for (j = 1; j <= len2; j++) {
                
                // from diagonal, calculate the distance with corresponding y coords 
                dij = Functions.dist(xx, y[j - 1]);
                d = val[i - 1][j - 1] + 1.0 / (1 + dij / d02);

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
            } // for i
        } // for j

        // trace back to extract the alignment
        i = len1;
        j = len2;
        while (i > 0 && j > 0) {
            if (path[i][j]) {

                // matched along diagonal
                j2i[j - 1] = i - 1;
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

    // secondary structure alignment
    public static void NWDP_TM(boolean path[][], double val[][], int secx[], int secy[], int len1, int len2, double gap_open, int j2i[]) {
        
        // Output:
        // path: DP matrix indicating if matches along diagonal or not
        // val: value of optimal alignment at that point +1 to match and -1 to start a gap
        // j2i: alignment of secy(j) to secx(i)

        // notice no penalty for extending a gap
        
        int i, j;
        double h, v, d;

        // initialization
        val[0][0] = 0;
        for (i = 0; i <= len1; i++) {
            val[i][0] = 0;
            path[i][0] = false; // not from diagonal
        }

        for (j = 0; j <= len2; j++) {
            val[0][j] = 0;
            path[0][j] = false; // not from diagonal

            // may as well init the alignment to all not aligned
            j2i[j] = -1;
        }

        // iterate row major
        for (i = 1; i <= len1; i++) {
            for (j = 1; j <= len2; j++) {
               
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

            } // for i
        } // for j

        // trace back to extract the alignment
        i = len1;
        j = len2;
        while (i > 0 && j > 0) {
       
            if (path[i][j]) {

                // matched along diagonal
                j2i[j - 1] = i - 1;
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
