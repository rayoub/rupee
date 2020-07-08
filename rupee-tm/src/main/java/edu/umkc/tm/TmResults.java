package edu.umkc.tm;

public class TmResults {

    private int chainLength1;
    private int chainLength2;
    private int alignedLength;
    private double tmScoreQ;
    private double tmScoreT;
    private double tmScoreAvg;
    private double rmsd;
    private double qScore;
    private double ssapScore = -1;
    private String output;

    public int getChainLength1() {
        return chainLength1;
    }

    public void setChainLength1(int chainLength1) {
        this.chainLength1 = chainLength1;
    }

    public int getChainLength2() {
        return chainLength2;
    }

    public void setChainLength2(int chainLength2) {
        this.chainLength2 = chainLength2;
    }

    public int getAlignedLength() {
        return alignedLength;
    }

    public void setAlignedLength(int alignedLength) {
        this.alignedLength = alignedLength;
    }

    public double getTmScoreQ() {
        return tmScoreQ;
    }

    public void setTmScoreQ(double tmScoreQ) {
        this.tmScoreQ = tmScoreQ;
    }

    public double getTmScoreT() {
        return tmScoreT;
    }

    public void setTmScoreT(double tmScoreT) {
        this.tmScoreT = tmScoreT;
    }

    public double getTmScoreAvg() {
        return tmScoreAvg;
    }

    public void setTmScoreAvg(double tmScoreAvg) {
        this.tmScoreAvg = tmScoreAvg;
    }

    public double getRmsd() {
        return rmsd;
    }

    public void setRmsd(double rmsd) {
        this.rmsd = rmsd;
    }

    public double getQScore() {
        return qScore;
    }

    public void setQScore(double qScore) {
        this.qScore = qScore;
    }

    public double getSsapScore() {
        return ssapScore;
    }

    public void setSsapScore(double ssapScore) {
        this.ssapScore = ssapScore;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
