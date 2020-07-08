package edu.umkc.rupee.tm;

public enum TmMode {

    REGULAR(30,20),
    FAST(2,2),
    ALIGN_TEXT(30,20),
    ALIGN_3D(30,20);

    private int dpIterations;
    private int scoreIterations;

    TmMode(int dpIterations, int scoreIterations) {
       
        this.dpIterations = dpIterations;
        this.scoreIterations = scoreIterations;
    }

    public int getDpIterations() {
        return dpIterations;
    }

    public int getScoreIterations() {
        return scoreIterations;
    }
}
