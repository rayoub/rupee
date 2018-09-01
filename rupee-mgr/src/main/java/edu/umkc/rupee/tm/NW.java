package edu.umkc.rupee.tm;

/*    Please note this fucntion is not a correct implementation of
*     the N-W dynamic programming because the score tracks back only
*     one layer of the matrix. This code was exploited in TM-align
*     because it is about 1.5 times faster than a complete N-W code
*     and does not influence much the final structure alignment result.
*/
public class NW {

    public static void NWDP_TM(int len1, int len2, double gap_open, int j2i[]) {

        // NW dynamic programming for alignment
        // not a standard implementation of NW algorithm
        // Input: score[1:len1, 1:len2], and gap_open
        // Output: j2i[1:len2] \in {1:len1} U {-1}
        // Variables.path[0:len1, 0:len2]=1,2,3, from diagonal, horizontal,
        // vertical

        int i, j;
        double h, v, d;

        // initialization
        Variables.val[0][0] = 0;
        for (i = 0; i <= len1; i++) {
            Variables.val[i][0] = 0;
            Variables.path[i][0] = false; // not from diagonal
        }

        for (j = 0; j <= len2; j++) {
            Variables.val[0][j] = 0;
            Variables.path[0][j] = false; // not from diagonal
            j2i[j] = -1; // all are not aligned, only use j2i[1:len2]
        }

        // decide matrix and Variables.path
        for (i = 1; i <= len1; i++) {
            for (j = 1; j <= len2; j++) {
                d=Variables.val[i-1][j-1]+Variables.score[i][j]; //diagonal

                // symbol insertion in horizontal (= a gap in vertical)
                h = Variables.val[i - 1][j];
                if (Variables.path[i - 1][j]) // aligned in last position
                    h += gap_open;

                // symbol insertion in vertical
                v = Variables.val[i][j - 1];
                if (Variables.path[i][j - 1]) // aligned in last position
                    v += gap_open;

                if (d >= h && d >= v) {
                    Variables.path[i][j] = true; // from diagonal
                    Variables.val[i][j] = d;
                } else {
                    Variables.path[i][j] = false; // from horizontal
                    if (v >= h)
                        Variables.val[i][j] = v;
                    else
                        Variables.val[i][j] = h;
                }
            } // for i
        } // for j

        // trace back to extract the alignment
        i = len1;
        j = len2;
        while (i > 0 && j > 0) {
            if (Variables.path[i][j]) // from diagonal
            {
                j2i[j - 1] = i - 1;
                i--;
                j--;
            } else {
                h = Variables.val[i - 1][j];
                if (Variables.path[i - 1][j])
                    h += gap_open;

                v = Variables.val[i][j - 1];
                if (Variables.path[i][j - 1])
                    v += gap_open;

                if (v >= h)
                    j--;
                else
                    i--;
            }
        }

    }

    public static void NWDP_TM(double x[][], double y[][], int len1, int len2, double t[], double u[][], double d02,
            double gap_open, int j2i[]) {
        // NW dynamic programming for alignment
        // not a standard implementation of NW algorithm
        // Input: vectors x, y, rotation matrix t, u, scale factor d02, and
        // gap_open
        // Output: j2i[1:len2] \in {1:len1} U {-1}
        // Variables.path[0:len1, 0:len2]=1,2,3, from diagonal, horizontal,
        // vertical

        int i, j;
        double h, v, d;

        // initialization
        Variables.val[0][0] = 0;
        for (i = 0; i <= len1; i++) {
            Variables.val[i][0] = 0;
            Variables.path[i][0] = false; // not from diagonal
        }

        for (j = 0; j <= len2; j++) {
            Variables.val[0][j] = 0;
            Variables.path[0][j] = false; // not from diagonal
            j2i[j] = -1; // all are not aligned, only use j2i[1:len2]
        }
        double xx[] = new double[3];
        double dij;

        // decide matrix and Variables.path
        for (i = 1; i <= len1; i++) {
            Functions.transform(t, u, x[i-1], xx);
            for (j = 1; j <= len2; j++) {
                // d=Variables.val[i-1][j-1]+score[i][j]; //diagonal
                dij=Functions.dist(xx, y[j-1]);
                dij = 0;
                d = Variables.val[i - 1][j - 1] + 1.0 / (1 + dij / d02);

                // symbol insertion in horizontal (= a gap in vertical)
                h = Variables.val[i - 1][j];
                if (Variables.path[i - 1][j]) // aligned in last position
                    h += gap_open;

                // symbol insertion in vertical
                v = Variables.val[i][j - 1];
                if (Variables.path[i][j - 1]) // aligned in last position
                    v += gap_open;

                if (d >= h && d >= v) {
                    Variables.path[i][j] = true; // from diagonal
                    Variables.val[i][j] = d;
                } else {
                    Variables.path[i][j] = false; // from horizontal
                    if (v >= h)
                        Variables.val[i][j] = v;
                    else
                        Variables.val[i][j] = h;
                }
            } // for i
        } // for j

        // trace back to extract the alignment
        i = len1;
        j = len2;
        while (i > 0 && j > 0) {
            if (Variables.path[i][j]) // from diagonal
            {
                j2i[j - 1] = i - 1;
                i--;
                j--;
            } else {
                h = Variables.val[i - 1][j];
                if (Variables.path[i - 1][j])
                    h += gap_open;

                v = Variables.val[i][j - 1];
                if (Variables.path[i][j - 1])
                    v += gap_open;

                if (v >= h)
                    j--;
                else
                    i--;
            }
        }
    }

    // +ss
    public static void NWDP_TM(int secx[], int secy[], int len1, int len2, double gap_open, int j2i[]) {
        // NW dynamic programming for alignment
        // not a standard implementation of NW algorithm
        // Input: secondary structure secx, secy, and gap_open
        // Output: j2i[1:len2] \in {1:len1} U {-1}
        // Variables.path[0:len1, 0:len2]=1,2,3, from diagonal, horizontal,
        // vertical

        int i, j;
        double h, v, d;

        // initialization
        Variables.val[0][0] = 0;
        for (i = 0; i <= len1; i++) {
            Variables.val[i][0] = 0;
            Variables.path[i][0] = false; // not from diagonal
        }

        for (j = 0; j <= len2; j++) {
            Variables.val[0][j] = 0;
            Variables.path[0][j] = false; // not from diagonal
            j2i[j] = -1; // all are not aligned, only use j2i[1:len2]
        }

        // decide matrix and Variables.path
        for (i = 1; i <= len1; i++) {
            for (j = 1; j <= len2; j++) {
                // d=Variables.val[i-1][j-1]+score[i][j]; //diagonal
                if (secx[i - 1] == secy[j - 1]) {
                    d = Variables.val[i - 1][j - 1] + 1.0;
                } else {
                    d = Variables.val[i - 1][j - 1];
                }

                // symbol insertion in horizontal (= a gap in vertical)
                h = Variables.val[i - 1][j];
                if (Variables.path[i - 1][j]) // aligned in last position
                    h += gap_open;

                // symbol insertion in vertical
                v = Variables.val[i][j - 1];
                if (Variables.path[i][j - 1]) // aligned in last position
                    v += gap_open;

                if (d >= h && d >= v) {
                    Variables.path[i][j] = true; // from diagonal
                    Variables.val[i][j] = d;
                } else {
                    Variables.path[i][j] = false; // from horizontal
                    if (v >= h)
                        Variables.val[i][j] = v;
                    else
                        Variables.val[i][j] = h;
                }
            } // for i
        } // for j

        // trace back to extract the alignment
        i = len1;
        j = len2;
        while (i > 0 && j > 0) {
            if (Variables.path[i][j]) // from diagonal
            {
                j2i[j - 1] = i - 1;
                i--;
                j--;
            } else {
                h = Variables.val[i - 1][j];
                if (Variables.path[i - 1][j])
                    h += gap_open;

                v = Variables.val[i][j - 1];
                if (Variables.path[i][j - 1])
                    v += gap_open;

                if (v >= h)
                    j--;
                else
                    i--;
            }
        }
    }
}
