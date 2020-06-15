package edu.umkc.rupee.base;

public class SearchRecord {
    
    private int n;
    private String dbId;
    private String pdbId;
    private String sortKey;
    private double similarity;
    private double similarityQ;
    private double tmScore;
    private double tmScoreQ;
    private double rmsd;
    private double qScore;
    private double ssapScore;

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public String getDbId() {
        return dbId;
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }

    public String getPdbId() {
        return pdbId;
    }

    public void setPdbId(String pdbId) {
        this.pdbId = pdbId;
    }

    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public double getSimilarityQ() {
        return similarityQ;
    }

    public void setSimilarityQ(double similarityQ) {
        this.similarityQ = similarityQ;
    }

    public double getTmScore() {
        return tmScore;
    }

    public void setTmScore(double tmScore) {
        this.tmScore = tmScore;
    }

    public double getTmScoreQ() {
        return tmScoreQ;
    }

    public void setTmScoreQ(double tmScoreQ) {
        this.tmScoreQ = tmScoreQ;
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
}
