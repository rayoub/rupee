package edu.umkc.rupee.tm;

public class KabschLite {

    private double xc[] = new double[3];
    private double yc[] = new double[3];
    private double s1[] = new double[3];
    private double s2[] = new double[3];
    private double sx[] = new double[3];
    private double sy[] = new double[3];
    private double sz[] = new double[3];
    private double a[][] = new double[3][3];
    private double r[][] = new double[3][3];
    private double e[] = new double[3];
    private double rr[] = new double[6];
    private double c1[] = new double[3];
    private double c2[] = new double[3];

    private double sqrt3 = 1.73205080756888; 
    private double spur, cof;
    private int i, j, m;
    private double e0, d, h, g;
    private double cth, sth, sqrth, det, sigma;

    public double execute(double x[][], double y[][]) {
       
        int n = x.length;
        double rmsd = 0;

        // initializtation
        e0 = 0;
        for (i = 0; i < 3; i++) {
            xc[i] = 0.0;
            yc[i] = 0.0;
            s1[i] = 0.0;
            s2[i] = 0.0;
            sx[i] = 0.0;
            sy[i] = 0.0;
            sz[i] = 0.0;
        }
        for (i = 0; i < 3; i++) {
            for (j = 0; j < 3; j++) {
                a[i][j] = 0.0;
                r[i][j] = 0.0;
                if (i == j) {
                    a[i][j] = 1.0;
                }
            }
        }

        // guard condition
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
        for (i = 0; i < n; i++) {
            for (j = 0; j < 3; j++) {
                e0 += (x[i][j] - xc[j]) * (x[i][j] - xc[j]) + (y[i][j] - yc[j]) * (y[i][j] - yc[j]);
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
            } 
        } 

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
        rmsd = (e0 - d) - d;
        if (rmsd < 0.0) {
            rmsd = 0.0;
        }

        return rmsd;
    }
}
