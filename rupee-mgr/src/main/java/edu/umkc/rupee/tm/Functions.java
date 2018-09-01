package edu.umkc.rupee.tm;

public class Functions {

    public static void PrintErrorAndQuit(String sErrorString) {
        System.err.println(sErrorString);
        System.exit(1);
    }

    public static char AAmap(String AA) {
        char A = ' ';
        if (AA.equals("BCK"))
            A = 'X';
        else if (AA.equals("GLY"))
            A = 'G';
        else if (AA.equals("ALA"))
            A = 'A';
        else if (AA.equals("SER"))
            A = 'S';
        else if (AA.equals("CYS"))
            A = 'C';
        else if (AA.equals("VAL"))
            A = 'V';
        else if (AA.equals("THR"))
            A = 'T';
        else if (AA.equals("ILE"))
            A = 'I';
        else if (AA.equals("PRO"))
            A = 'P';
        else if (AA.equals("MET"))
            A = 'M';
        else if (AA.equals("ASP"))
            A = 'D';
        else if (AA.equals("ASN"))
            A = 'N';
        else if (AA.equals("LEU"))
            A = 'L';
        else if (AA.equals("LYS"))
            A = 'K';
        else if (AA.equals("GLU"))
            A = 'E';
        else if (AA.equals("GLN"))
            A = 'Q';
        else if (AA.equals("ARG"))
            A = 'R';
        else if (AA.equals("HIS"))
            A = 'H';
        else if (AA.equals("PHE"))
            A = 'F';
        else if (AA.equals("TYR"))
            A = 'Y';
        else if (AA.equals("TRP"))
            A = 'W';
        else if (AA.equals("CYX"))
            A = 'C';
        else
            A = 'Z'; // ligand

        return A;
    }

    public static void AAmap3(char A, String AA) {
        if (A == 'X')
            AA = "BCK";
        else if (A == 'G')
            AA = "GLY";
        else if (A == 'A')
            AA = "ALA";
        else if (A == 'S')
            AA = "SER";
        else if (A == 'C')
            AA = "CYS";
        else if (A == 'V')
            AA = "VAL";
        else if (A == 'T')
            AA = "THR";
        else if (A == 'I')
            AA = "ILE";
        else if (A == 'P')
            AA = "PRO";
        else if (A == 'M')
            AA = "MET";
        else if (A == 'D')
            AA = "ASP";
        else if (A == 'N')
            AA = "ASN";
        else if (A == 'L')
            AA = "LEU";
        else if (A == 'K')
            AA = "LYS";
        else if (A == 'E')
            AA = "GLU";
        else if (A == 'Q')
            AA = "GLN";
        else if (A == 'R')
            AA = "ARG";
        else if (A == 'H')
            AA = "HIS";
        else if (A == 'F')
            AA = "PHE";
        else if (A == 'Y')
            AA = "TYR";
        else if (A == 'W')
            AA = "TRP";
        else if (A == 'C')
            AA = "CYX";
        else
            AA = "UNK";
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
