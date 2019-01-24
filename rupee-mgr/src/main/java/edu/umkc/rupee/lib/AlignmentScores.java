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
    private double tmQRmsd = 0.0;
    private double tmQTmScore = -1.0;
    private double tmAvgRmsd = 0.0;
    private double tmAvgTmScore = -1.0;

    public AlignmentScores() { }

    public AlignmentScores(ResultSet rs) throws SQLException {
  
        this.version = rs.getString("version"); 
        this.dbId1 = rs.getString("db_id_1");
        this.dbId2 = rs.getString("db_id_2");
        this.ceRmsd = rs.getDouble("ce_rmsd");
        this.ceTmScore = rs.getDouble("ce_tm_score");
        this.fatCatRmsd = rs.getDouble("fatcat_rmsd");
        this.fatCatTmScore = rs.getDouble("fatcat_tm_score");
        this.tmQRmsd = rs.getDouble("tm_q_rmsd");
        this.tmQTmScore = rs.getDouble("tm_q_tm_score");
        this.tmAvgRmsd = rs.getDouble("tm_avg_rmsd");
        this.tmAvgTmScore = rs.getDouble("tm_avg_tm_score");
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

    public double getTmQRmsd() {
        return tmQRmsd;
    }

    public void setTmQRmsd(double tmRmsd) {
        this.tmQRmsd = tmRmsd;
    }

    public double getTmQTmScore() {
        return tmQTmScore;
    }

    public void setTmQTmScore(double tmTmScore) {
        this.tmQTmScore = tmTmScore;
    }

    public double getTmAvgRmsd() {
        return tmAvgRmsd;
    }

    public void setTmAvgRmsd(double tmAvgRmsd) {
        this.tmAvgRmsd = tmAvgRmsd;
    }

    public double getTmAvgTmScore() {
        return tmAvgTmScore;
    }

    public void setTmAvgTmScore(double tmAvgTmScore) {
        this.tmAvgTmScore = tmAvgTmScore;
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
            case TM_Q_ALIGN:
                rmsd = this.tmQRmsd;
                break;
            case TM_AVG_ALIGN:
                rmsd = this.tmAvgRmsd;
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
            case TM_Q_ALIGN:
                tmScore = this.tmQTmScore;
                break;
            case TM_AVG_ALIGN:
                tmScore = this.tmAvgTmScore;
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
            + tmQRmsd + "," + tmQTmScore + ","
            + tmAvgRmsd + "," + tmAvgTmScore
            + ")";
        return row;
    }
}
