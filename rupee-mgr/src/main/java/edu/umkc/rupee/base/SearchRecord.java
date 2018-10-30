package edu.umkc.rupee.base;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;

public class SearchRecord extends PGobject {

    private String searchHash;
    private int n;
    private String dbId;
    private String pdbId;
    private String sortKey;
    private double similarity;
    private double rmsd;
    private double tmScore;

    public void set(ResultSet rs) throws SQLException {

        this.searchHash = rs.getString("search_hash");
        this.n = rs.getInt("n");
        this.dbId = rs.getString("db_id");
        this.pdbId = rs.getString("pdb_id");
        this.sortKey = rs.getString("sort_key");
        this.similarity = rs.getDouble("similarity");
        this.rmsd = rs.getDouble("rmsd");
        this.tmScore = rs.getDouble("tm_score");
    }
    
    public String getSearchHash() {
        return searchHash;
    }

    public void setSearchHash(String searchHash) {
        this.searchHash = searchHash;
    }

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

    @Override
    public String getValue() {
        return "(" + searchHash + "," + n + "," + dbId + "," + pdbId + "," + sortKey + "," + similarity + "," + rmsd + "," + tmScore + ")";
    }
}
