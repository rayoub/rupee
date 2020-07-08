package edu.umkc.rupee.tm;

public class QScore {

    public static double getQScore(double rmsd, int xlen, int ylen, int alignLen) {

        // Q-score compatible with SSM
        double qScore = Math.pow(alignLen, 2.0) / ((1 + Math.pow(rmsd/3.0, 2.0)) * xlen * ylen);
        return qScore;
    }

}
