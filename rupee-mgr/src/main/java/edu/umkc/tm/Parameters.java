package edu.umkc.tm;

public class Parameters {

    private double normalizeBy; 
    private double d0;
    private double d02;
    private double d0Bounded;
    private double scoreD8;
    private double scoreD8Squared;

    public double getNormalizeBy() {
        return normalizeBy;
    }

    public void setNormalizeBy(double normalizeBy) {
        this.normalizeBy = normalizeBy;
    }

    public double getD0() {
        return d0;
    }

    public void setD0(double d0) {
        this.d0 = d0;
    }

    public double getD02() {
        return d02;
    }

    public void setD02(double d02) {
        this.d02 = d02;
    }

    public double getD0Bounded() {
        return d0Bounded;
    }

    public void setD0Bounded(double d0Bounded) {
        this.d0Bounded = d0Bounded;
    }

    public double getScoreD8() {
        return scoreD8;
    }

    public void setScoreD8(double scoreD8) {
        this.scoreD8 = scoreD8;
    }

    public double getScoreD8Squared() {
        return scoreD8Squared;
    }

    public void setScoreD8Squared(double scoreD8Squared) {
        this.scoreD8Squared = scoreD8Squared;
    }

    public static Parameters getSearchParameters(int xlen, int ylen) {

        Parameters params = new Parameters();

        params.normalizeBy = Math.min(xlen, ylen); 
                                            
        // set d0 term
        if (params.normalizeBy <= 19) {
            params.d0 = 0.168; 
        } else {
            // equation (5) from Zhang, 2004
            params.d0 = (1.24 * Math.pow((params.normalizeBy * 1.0 - 15), 1.0 / 3.0) - 1.8);
        }

        params.d0 = params.d0 + 0.8;
        params.d02 = params.d0 * params.d0;
       
        // set bounded d0 term 
        params.d0Bounded = params.d0;
        if (params.d0Bounded > 8)
            params.d0Bounded = 8;
        if (params.d0Bounded < 4.5)
            params.d0Bounded = 4.5;
       
        params.scoreD8 = 1.5 * Math.pow(Math.min(xlen, ylen) * 1.0, 0.3) + 3.5;
        params.scoreD8Squared = params.scoreD8 * params.scoreD8;

        return params;
    }

    public static Parameters getFinalParameters(double xlen, double ylen, double len) {

        Parameters params = new Parameters();
        
        params.normalizeBy = len; 

        if (params.normalizeBy <= 21) {
            params.d0 = 0.5;
        } else {
            // equation (5) from Zhang, 2004
            params.d0 = (1.24 * Math.pow((params.normalizeBy * 1.0 - 15), 1.0 / 3.0) - 1.8);
        }

        params.d0 = Math.max(params.d0, 0.5);
        params.d02 = params.d0 * params.d0;

        // set bounded d0 term 
        params.d0Bounded = params.d0;
        if (params.d0Bounded > 8)
            params.d0Bounded = 8;
        if (params.d0Bounded < 4.5)
            params.d0Bounded = 4.5;

        params.scoreD8 = 1.5 * Math.pow(Math.min(xlen, ylen) * 1.0, 0.3) + 3.5;
        params.scoreD8Squared = params.scoreD8 * params.scoreD8;

        return params;
    }
}
