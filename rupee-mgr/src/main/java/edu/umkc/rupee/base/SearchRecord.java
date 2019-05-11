package edu.umkc.rupee.base;

import edu.umkc.rupee.defs.SearchType;

public class SearchRecord {
    
    private int n;
    private String dbId;
    private String pdbId;
    private String sortKey;
    private SearchType searchType;
    private boolean similarLength;
    private double similarity;
    private double rmsd;
    private double tmScore;
    
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

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    public boolean isSimilarLength() {
        return similarLength;
    }

    public void setSimilarLength(boolean essentiallyFullLength) {
        this.similarLength = essentiallyFullLength;
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
