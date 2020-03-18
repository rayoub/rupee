package edu.umkc.rupee.lib;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.util.PGobject;

import edu.umkc.rupee.defs.AlignmentType;

public class AlignmentScores extends PGobject {

    private String version;
    private String dbId1;
    private String dbId2;
    private double tmQTmScore = -1.0;
    private double tmAvgTmScore = -1.0;
    private double tmRmsd = 0.0;

    public AlignmentScores() { }

    public AlignmentScores(ResultSet rs) throws SQLException {
  
        this.version = rs.getString("version"); 
        this.dbId1 = rs.getString("db_id_1");
        this.dbId2 = rs.getString("db_id_2");
        this.tmQTmScore = rs.getDouble("tm_q_tm_score");
        this.tmAvgTmScore = rs.getDouble("tm_avg_tm_score");
        this.tmRmsd = rs.getDouble("tm_rmsd");
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

    public double getTmQTmScore() {
        return tmQTmScore;
    }

    public void setTmQTmScore(double tmTmScore) {
        this.tmQTmScore = tmTmScore;
    }

    public double getTmAvgTmScore() {
        return tmAvgTmScore;
    }

    public void setTmAvgTmScore(double tmAvgTmScore) {
        this.tmAvgTmScore = tmAvgTmScore;
    }

    public double getTmRmsd() {
        return tmRmsd;
    }

    public void setTmRmsd(double tmRmsd) {
        this.tmRmsd = tmRmsd;
    }

    public double getRmsd(AlignmentType align) {

        double rmsd = 0.0;

        switch (align) {
            case TM_Q_ALIGN:
                rmsd = this.tmRmsd;
                break;
            case TM_AVG_ALIGN:
                rmsd = this.tmRmsd;
                break;
            default:
                rmsd = 0.0;
        }

        return rmsd;
    }
    
    public double getTmScore(AlignmentType align) {

        double tmScore = -1.0;

        switch (align) {
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
            + tmQTmScore + "," + tmAvgTmScore + ","
            + tmRmsd 
            + ")";
        return row;
    }
}
