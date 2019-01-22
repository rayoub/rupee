package edu.umkc.rupee.lib;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.util.PGobject;

import edu.umkc.rupee.defs.AlignCriteria;

public class AlignmentScores extends PGobject {

    private String version;
    private String dbId1;
    private String dbId2;
    private double ceRmsd = 0.0;
    private double ceTmScore = -1.0;
    private double fatCatRmsd = 0.0;
    private double fatCatTmScore = -1.0;
    private double tmRmsd = 0.0;
    private double tmTmScore = -1.0;

    public AlignmentScores() { }

    public AlignmentScores(ResultSet rs) throws SQLException {
  
        this.version = rs.getString("version"); 
        this.dbId1 = rs.getString("db_id_1");
        this.dbId2 = rs.getString("db_id_2");
        this.ceRmsd = rs.getDouble("ce_rmsd");
        this.ceTmScore = rs.getDouble("ce_tm_score");
        this.fatCatRmsd = rs.getDouble("fatcat_rmsd");
        this.fatCatTmScore = rs.getDouble("fatcat_tm_score");
        this.tmRmsd = rs.getDouble("tm_rmsd");
        this.tmTmScore = rs.getDouble("tm_tm_score");
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public double getCeRmsd() {
        return ceRmsd;
    }

    public void setCeRmsd(double ceRmsd) {
        this.ceRmsd = ceRmsd;
    }

    public double getCeTmScore() {
        return ceTmScore;
    }

    public void setCeTmScore(double ceTmScore) {
        this.ceTmScore = ceTmScore;
    }
    
    public double getFatCatRmsd() {
        return fatCatRmsd;
    }

    public void setFatCatRmsd(double fatCatRmsd) {
        this.fatCatRmsd = fatCatRmsd;
    }

    public double getFatCatTmScore() {
        return fatCatTmScore;
    }

    public void setFatCatTmScore(double fatCatTmScore) {
        this.fatCatTmScore = fatCatTmScore;
    }

    public double getTmRmsd() {
        return tmRmsd;
    }

    public void setTmRmsd(double tmRmsd) {
        this.tmRmsd = tmRmsd;
    }

    public double getTmTmScore() {
        return tmTmScore;
    }

    public void setTmTmScore(double tmTmScore) {
        this.tmTmScore = tmTmScore;
    }

    public double getRmsd(AlignCriteria align) {

        double rmsd = 0.0;

        switch (align) {
            
            case CE:
                rmsd = this.ceRmsd;
                break;
            case FATCAT_FLEXIBLE:
                rmsd = this.fatCatRmsd;
                break;
            case TM_ALIGN:
                rmsd = this.tmRmsd;
                break;
            default:
                rmsd = 0.0;
        }

        return rmsd;
    }
    
    public double getTmScore(AlignCriteria align) {

        double tmScore = -1.0;

        switch (align) {
            
            case CE:
                tmScore = this.ceTmScore;
                break;
            case FATCAT_FLEXIBLE:
                tmScore = this.fatCatTmScore;
                break;
            case TM_ALIGN:
                tmScore = this.tmTmScore;
                break;
            default:
                tmScore = -1.0;
        }

        return tmScore;
    }

    @Override
    public String getValue() {
        String row = "(" 
            + version + ","
            + dbId1 + "," + dbId2 + ","
            + ceRmsd + "," + ceTmScore + ","
            + fatCatRmsd + "," + fatCatTmScore + ","
            + tmRmsd + "," + tmTmScore
            + ")";
        return row;
    }
}
