package edu.umkc.rupee.tm;

public class Functions {

    public static void PrintErrorAndQuit(String sErrorString) {
        System.err.println(sErrorString);
        System.exit(1);
    }

    public static double dist(double x[], double y[]) {
        double d1 = x[0] - y[0];
        double d2 = x[1] - y[1];
        double d3 = x[2] - y[2];

        return (d1 * d1 + d2 * d2 + d3 * d3);
    }

    public static double dot(double a[], double b[]) {
        return (a[0] * b[0] + a[1] * b[1] + a[2] * b[2]);
    }

    public static void transform(double t[], double u[][], double x[], double x1[]) {
        x1[0] = t[0] + dot(u[0], x);
        x1[1] = t[1] + dot(u[1], x);
        x1[2] = t[2] + dot(u[2], x);
    }

    public static void do_rotation(double x[][], double x1[][], int len, double t[], double u[][]) {
        for (int i = 0; i < len; i++) {
            transform(t, u, x[i], x1[i]);
        }
    }
}
