package edu.umkc.tm;

public class SsapScoring {

    // constants
    private static double FINAL_SCORE_SCALING = 1000.0;
    private static double RESIDUE_A_VALUE = 500.0;
    private static double RESIDUE_B_VALUE = 10.0;
    private static double GAP_PENALTY = RESIDUE_A_VALUE / RESIDUE_B_VALUE;
    private static int NUM_EXCLUDED = 5;
    private static double INTEGER_SCALING = 10.0;
    private static double MIN_SCORE_CUTOFF = 10.0;
    private static double MAX_SCORE_CUTOFF = (RESIDUE_A_VALUE / MIN_SCORE_CUTOFF) - RESIDUE_B_VALUE;

    // natural log
    private static double MAX_LOG = Math.log((RESIDUE_A_VALUE / RESIDUE_B_VALUE) * FINAL_SCORE_SCALING);

    // structure privates
    private double[][]  _aatoms;
    private double[][]  _batoms;
    private int _alen, _blen;

    public SsapScoring(double[][] aatoms, double[][] batoms) { 

        _aatoms = aatoms; 
        _batoms = batoms; 

        _alen = _aatoms.length;
        _blen = _batoms.length;
    }

    public double getScore(SsapAlignment alignment) {

        // score matrix
        int maxAlignLen = _alen + _blen + 10;
        double[][] scoreMatrix = new double[maxAlignLen][maxAlignLen];

        // distance calculations
        for (int i = 0; i < alignment.getLength(); i++) {

            if (alignment.hasBothPositions(i)) {

                int iaPos = alignment.getAPosition(i);
                int ibPos = alignment.getBPosition(i);
                        
                for (int j = 0; j < alignment.getLength(); j++) {

                    if (alignment.hasBothPositions(j)) {

                        int jaPos = alignment.getAPosition(j);
                        int jbPos = alignment.getBPosition(j);

                        if (isComparable(iaPos, jaPos) && isComparable(ibPos, jbPos)) {

                            double[] fromAtomA = _aatoms[iaPos];
                            double[] fromAtomB = _batoms[ibPos];
                            double[] toAtomA = _aatoms[jaPos];
                            double[] toAtomB = _batoms[jbPos]; 

                            double ax = (toAtomA[0] - fromAtomA[0]) * INTEGER_SCALING;
                            double ay = (toAtomA[1] - fromAtomA[1]) * INTEGER_SCALING;
                            double az = (toAtomA[2] - fromAtomA[2]) * INTEGER_SCALING;
                            
                            double bx = (toAtomB[0] - fromAtomB[0]) * INTEGER_SCALING;
                            double by = (toAtomB[1] - fromAtomB[1]) * INTEGER_SCALING;
                            double bz = (toAtomB[2] - fromAtomB[2]) * INTEGER_SCALING;

                            double sumOfSquares = Math.pow(ax - bx, 2) + Math.pow(ay - by, 2) + Math.pow(az - bz, 2);

                            double scaledA = RESIDUE_A_VALUE * INTEGER_SCALING * INTEGER_SCALING;
                            double scaledB = RESIDUE_B_VALUE * INTEGER_SCALING * INTEGER_SCALING;

                            double score = 0.0;
                            if (sumOfSquares < (MAX_SCORE_CUTOFF * INTEGER_SCALING * INTEGER_SCALING)) {
                                score = scaledA / (sumOfSquares + scaledB);
                            }
                            scoreMatrix[jbPos][jaPos] += score;
                        }
                    }
                }
            }
        }

        boolean isFirstWithBothPos = true;
        boolean prevHadBothPos = false;
        int aPosPrev = 0;
        int bPosPrev = 0;
        double maxScore = 0;

        // accumulate score 
        for (int i = 0; i < alignment.getLength(); i++) {

            boolean hasBothPos = alignment.hasBothPositions(i);
            if (hasBothPos) {

                int aPos = alignment.getAPosition(i);
                int bPos = alignment.getBPosition(i);

                double gap = 0;
                if (!isFirstWithBothPos) {
                    if (!prevHadBothPos || (aPos != aPosPrev + 1) || (bPos != bPosPrev + 1)) {
                        gap = GAP_PENALTY;
                    }
                }
                maxScore += scoreMatrix[bPos][aPos] - gap;
                isFirstWithBothPos = false;
            }

            prevHadBothPos = hasBothPos;
            if (prevHadBothPos) {
                aPosPrev = alignment.getAPosition(i);
                bPosPrev = alignment.getBPosition(i);
            }
        }

        // nonnegative
        maxScore = Math.max(maxScore, 0.0);

        // normalize
        int maxLen = Math.max(_alen, _blen);
        int numComparable = (maxLen - NUM_EXCLUDED) * (maxLen - NUM_EXCLUDED - 1);
        if (maxScore != 0.0 && numComparable > 0) {

            double scaledScore = (maxScore * FINAL_SCORE_SCALING) / numComparable;
            double finalScore = (100.0 * Math.log(scaledScore)) / MAX_LOG;
            maxScore = finalScore;
        }

        return maxScore;
    }

    /* STATIC HELPER FUNCTIONS COPIED OVER AND NOT DECOUPLED TO THE NTH DEGREE */

    private static boolean isComparable(int fromPos, int toPos) {

        int max = Math.max(fromPos, toPos);
        int min = Math.min(fromPos, toPos);
        int diff = max - min; 
        
        return diff > NUM_EXCLUDED;
    }
}




