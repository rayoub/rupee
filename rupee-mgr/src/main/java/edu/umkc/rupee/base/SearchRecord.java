package edu.umkc.rupee.base;

public class SearchRecord {

    private int n;
    private int recordCount;
    private String dbId1;
    private String dbId2;
    private String pdbId;
    private int similarityRank;
    private double similarity;
    private double rmsd;
    private double tmScore;

    public SearchRecord(String dbId, String pdbId, double similarity) {

        this.dbId2 = dbId;
        this.pdbId = pdbId;
        this.similarity = similarity;
    }
    
    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public String getDbId1() {
        return dbId1;
    }

    public void setDbId1(String dbId1) {
        this.dbId1 = dbId1;
    }

    public String getDbId2() {
        return dbId2;
    }

    public void setDbId2(String dbId2) {
        this.dbId2 = dbId2;
    }

    public String getPdbId() {
        return pdbId;
    }

    public void setPdbId(String pdbId) {
        this.pdbId = pdbId;
    }

    public int getSimilarityRank() {
        return similarityRank;
    }

    public void setSimilarityRank(int similarityRank) {
        this.similarityRank = similarityRank;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public double getRmsd() {
        return rmsd;
    }

    public void setRmsd(double rmsd) {
        this.rmsd = rmsd;
    }

    public double getTmScore() {
        return tmScore;
    }

    public void setTmScore(double tmScore) {
        this.tmScore = tmScore;
    }
}
