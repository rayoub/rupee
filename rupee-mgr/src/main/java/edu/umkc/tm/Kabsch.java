package edu.umkc.tm;


/* 
*************************************************************************
Implemetation of Kabsch algoritm for finding the best rotation matrix
-------------------------------------------------------------------------
x    - x(i,m) are coordinates of atom m in set x            (input)
y    - y(i,m) are coordinates of atom m in set y            (input)
n    - n is number of atom pairs                            (input)
mode  - 0:calculate rms only                                (input)
1:calculate u,t only                                (takes medium)
2:calculate rms,u,t                                 (takes longer)
rms   - sum of w*(ux+t-y)**2 over all atom pairs            (output)
u    - u(i,j) is   rotation  matrix for best superposition  (output)
t    - t(i)   is translation vector for best superposition  (output)
*************************************************************************
*/

public class Kabsch {

    private double xc[] = new double[3];
    private double yc[] = new double[3];
    private double a[][] = new double[3][3];
    private double b[][] = new double[3][3];
    private double r[][] = new double[3][3];
    private double e[] = new double[3];
    private double rr[] = new double[6];
    private double ss[] = new double[6];
    private double c1[] = new double[3];
    private double c2[] = new double[3];
    private double s1[] = new double[3];
    private double s2[] = new double[3];
    private double sx[] = new double[3];
    private double sy[] = new double[3];
    private double sz[] = new double[3];

    private int ip[] = { 0, 1, 3, 1, 2, 4, 3, 4, 5 };
    private int ip2312[] = { 1, 2, 0, 1 };
    private double sqrt3 = 1.73205080756888, tol = 0.01;
    private int mm, nn;
    private double spur, cof;

    private int i, j, m, m1, l, k;
    private double e0, rmsd, d, h, g;
    private double cth, sth, sqrth, p, det, sigma;

    private int a_failed = 0, b_failed = 0;
    private double epsilon = 0.00000001;

    public double execute(double x[][], double y[][], int n, int mode, double t[], double u[][]) {


        // initializtation
        rmsd = 0;
        e0 = 0;
        for (i = 0; i < 3; i++) {
            s1[i] = 0.0;
            s2[i] = 0.0;

            sx[i] = 0.0;
            sy[i] = 0.0;
            sz[i] = 0.0;
        }

        for (i = 0; i < 3; i++) {
            xc[i] = 0.0;
            yc[i] = 0.0;
            t[i] = 0.0;
            for (j = 0; j < 3; j++) {
                u[i][j] = 0.0;
                r[i][j] = 0.0;
                a[i][j] = 0.0;
                if (i == j) {
                    u[i][j] = 1.0;
                    a[i][j] = 1.0;
                }
            }
        }

        if (n < 1) {
            return rmsd;
        }

        // compute centers for vector sets x, y
        for (i = 0; i < n; i++) {
            for (j = 0; j < 3; j++) {
                c1[j] = x[i][j];
                c2[j] = y[i][j];

                s1[j] += c1[j];
                s2[j] += c2[j];
            }

            for (j = 0; j < 3; j++) {
                sx[j] += c1[0] * c2[j];
                sy[j] += c1[1] * c2[j];
                sz[j] += c1[2] * c2[j];
            }
        }
        for (i = 0; i < 3; i++) {
            xc[i] = s1[i] / n;
            yc[i] = s2[i] / n;
        }
        if (mode == 2 || mode == 0) {
            for (mm = 0; mm < n; mm++) {
                for (nn = 0; nn < 3; nn++) {
                    e0 += (x[mm][nn] - xc[nn]) * (x[mm][nn] - xc[nn]) + (y[mm][nn] - yc[nn]) * (y[mm][nn] - yc[nn]);
                }
            }
        }
        for (j = 0; j < 3; j++) {
            r[j][0] = sx[j] - s1[0] * s2[j] / n;
            r[j][1] = sy[j] - s1[1] * s2[j] / n;
            r[j][2] = sz[j] - s1[2] * s2[j] / n;
        }

        //compute determinat of matrix r
        det = r[0][0] * (r[1][1] * r[2][2] - r[1][2] * r[2][1])
            - r[0][1] * (r[1][0] * r[2][2] - r[1][2] * r[2][0])
            + r[0][2] * (r[1][0] * r[2][1] - r[1][1] * r[2][0]);
        sigma = det;

        // compute tras(r)*r
        m = 0;
        for (j = 0; j < 3; j++) {
            for (i = 0; i <= j; i++) {
                rr[m] = r[0][i] * r[0][j] + r[1][i] * r[1][j] + r[2][i] * r[2][j];
                m++;
            }
        }

        spur = (rr[0] + rr[2] + rr[5]) / 3.0;
        cof = (((((rr[2] * rr[5] - rr[4] * rr[4]) + rr[0] * rr[5]) - rr[3] * rr[3]) + rr[0] * rr[2])
                - rr[1] * rr[1]) / 3.0;
        det = det * det;

        for (i = 0; i<3; i++)
        {
            e[i] = spur;
        }

        if (spur > 0) {

            d = spur * spur;
            h = d - cof;
            g = (spur * cof - det) / 2.0 - spur * h;

            if (h > 0) {
                sqrth = Math.sqrt(h);
                d = h * h * h - g * g;
                if (d < 0.0)
                    d = 0.0;
                d = Math.atan2(Math.sqrt(d), -g) / 3.0;
                cth = sqrth * Math.cos(d);
                sth = sqrth * sqrt3 * Math.sin(d);
                e[0] = (spur + cth) + cth;
                e[1] = (spur - cth) + sth;
                e[2] = (spur - cth) - sth;

                if (mode != 0) {// compute a
                    for (l = 0; l < 3; l = l + 2) {
                        d = e[l];
                        ss[0] = (d - rr[2]) * (d - rr[5]) - rr[4] * rr[4];
                        ss[1] = (d - rr[5]) * rr[1] + rr[3] * rr[4];
                        ss[2] = (d - rr[0]) * (d - rr[5]) - rr[3] * rr[3];
                        ss[3] = (d - rr[2]) * rr[3] + rr[1] * rr[4];
                        ss[4] = (d - rr[0]) * rr[4] + rr[1] * rr[3];
                        ss[5] = (d - rr[0]) * (d - rr[2]) - rr[1] * rr[1];

                        if (Math.abs(ss[0]) <= epsilon)
                            ss[0] = 0.0;
                        if (Math.abs(ss[1]) <= epsilon)
                            ss[1] = 0.0;
                        if (Math.abs(ss[2]) <= epsilon)
                            ss[2] = 0.0;
                        if (Math.abs(ss[3]) <= epsilon)
                            ss[3] = 0.0;
                        if (Math.abs(ss[4]) <= epsilon)
                            ss[4] = 0.0;
                        if (Math.abs(ss[5]) <= epsilon)
                            ss[5] = 0.0;

                        if (Math.abs(ss[0]) >= Math.abs(ss[2])) {
                            j = 0;
                            if (Math.abs(ss[0]) < Math.abs(ss[5])) {
                                j = 2;
                            }
                        } else if (Math.abs(ss[2]) >= Math.abs(ss[5])) {
                            j = 1;
                        } else {
                            j = 2;
                        }

                        d = 0.0;
                        j = 3 * j;
                        for (i = 0; i < 3; i++) {
                            k = ip[i + j];
                            a[i][l] = ss[k];
                            d = d + ss[k] * ss[k];
                        }

                        // if( d > 0.0 ) d = 1.0 / sqrt(d);
                        if (d > epsilon)
                            d = 1.0 / Math.sqrt(d);
                        else
                            d = 0.0;
                        for (i = 0; i < 3; i++) {
                            a[i][l] = a[i][l] * d;
                        }
                    } // for l

                    d = a[0][0] * a[0][2] + a[1][0] * a[1][2] + a[2][0] * a[2][2];
                    if ((e[0] - e[1]) > (e[1] - e[2])) {
                        m1 = 2;
                        m = 0;
                    } else {
                        m1 = 0;
                        m = 2;
                    }
                    p = 0;
                    for (i = 0; i < 3; i++) {
                        a[i][m1] = a[i][m1] - d * a[i][m];
                        p = p + a[i][m1] * a[i][m1];
                    }
                    if (p <= tol) {
                        p = 1.0;
                        for (i = 0; i < 3; i++) {
                            if (p < Math.abs(a[i][m])) {
                                continue;
                            }
                            p = Math.abs(a[i][m]);
                            j = i;
                        }
                        k = ip2312[j];
                        l = ip2312[j + 1];
                        p = Math.sqrt(a[k][m] * a[k][m] + a[l][m] * a[l][m]);
                        if (p > tol) {
                            a[j][m1] = 0.0;
                            a[k][m1] = -a[l][m] / p;
                            a[l][m1] = a[k][m] / p;
                        } else {// goto 40
                            a_failed = 1;
                        }
                    } // if p<=tol
                    else {
                        p = 1.0 / Math.sqrt(p);
                        for (i = 0; i < 3; i++) {
                            a[i][m1] = a[i][m1] * p;
                        }
                    } // else p<=tol
                    if (a_failed != 1) {
                        a[0][1] = a[1][2] * a[2][0] - a[1][0] * a[2][2];
                        a[1][1] = a[2][2] * a[0][0] - a[2][0] * a[0][2];
                        a[2][1] = a[0][2] * a[1][0] - a[0][0] * a[1][2];
                    }
                } // if(mode!=0)
            } // h>0

            // compute b anyway
            if (mode != 0 && a_failed != 1)// a is computed correctly
            {
                // compute b
                for (l = 0; l < 2; l++) {
                    d = 0.0;
                    for (i = 0; i < 3; i++) {
                        b[i][l] = r[i][0] * a[0][l] + r[i][1] * a[1][l] + r[i][2] * a[2][l];
                        d = d + b[i][l] * b[i][l];
                    }
                    // if( d > 0.0 ) d = 1.0 / sqrt(d);
                    if (d > epsilon)
                        d = 1.0 / Math.sqrt(d);
                    else
                        d = 0.0;
                    for (i = 0; i < 3; i++) {
                        b[i][l] = b[i][l] * d;
                    }
                }
                d = b[0][0] * b[0][1] + b[1][0] * b[1][1] + b[2][0] * b[2][1];
                p = 0.0;

                for (i = 0; i < 3; i++) {
                    b[i][1] = b[i][1] - d * b[i][0];
                    p += b[i][1] * b[i][1];
                }

                if (p <= tol) {
                    p = 1.0;
                    for (i = 0; i < 3; i++) {
                        if (p < Math.abs(b[i][0])) {
                            continue;
                        }
                        p = Math.abs(b[i][0]);
                        j = i;
                    }
                    k = ip2312[j];
                    l = ip2312[j + 1];
                    p = Math.sqrt(b[k][0] * b[k][0] + b[l][0] * b[l][0]);
                    if (p > tol) {
                        b[j][1] = 0.0;
                        b[k][1] = -b[l][0] / p;
                        b[l][1] = b[k][0] / p;
                    } else {
                        // goto 40
                        b_failed = 1;
                    }
                } // if( p <= tol )
                else {
                    p = 1.0 / Math.sqrt(p);
                    for (i = 0; i < 3; i++) {
                        b[i][1] = b[i][1] * p;
                    }
                }
                if (b_failed != 1) {
                    b[0][2] = b[1][0] * b[2][1] - b[1][1] * b[2][0];
                    b[1][2] = b[2][0] * b[0][1] - b[2][1] * b[0][0];
                    b[2][2] = b[0][0] * b[1][1] - b[0][1] * b[1][0];
                    // compute u
                    for (i = 0; i < 3; i++) {
                        for (j = 0; j < 3; j++) {
                            u[i][j] = b[i][0] * a[j][0] + b[i][1] * a[j][1] + b[i][2] * a[j][2];
                        }
                    }
                }

                // compute t
                for (i = 0; i < 3; i++) {
                    t[i] = ((yc[i] - u[i][0] * xc[0]) - u[i][1] * xc[1]) - u[i][2] * xc[2];
                }
            } // if(mode!=0 && a_failed!=1)
        } // spur>0
        else // just compute t and errors
        {
            // compute t
            for (i = 0; i < 3; i++) {
                t[i] = ((yc[i] - u[i][0] * xc[0]) - u[i][1] * xc[1]) - u[i][2] * xc[2];
            }
        } // else spur>0

        // compute rms
        for (i = 0; i < 3; i++) {
            if (e[i] < 0)
                e[i] = 0;
            e[i] = Math.sqrt(e[i]);
        }
        d = e[2];
        if (sigma < 0.0) {
            d = -d;
        }
        d = (d + e[1]) + e[0];

        if (mode == 2 || mode == 0) {
            rmsd = (e0 - d) - d;
            if (rmsd < 0.0)
                rmsd = 0.0;
        }

        return rmsd;
    }
}
