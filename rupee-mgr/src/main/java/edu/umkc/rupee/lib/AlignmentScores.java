package edu.umkc.rupee.lib;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.util.PGobject;

public class AlignmentScores extends PGobject {

    private String dbId1;
    private String dbId2;
    private double similarity;
    private double ceRmsd;
    private double ceTmScore;
    private double cecpRmsd;
    private double cecpTmScore;
    private double fatCatFlexibleRmsd;
    private double fatCatFlexibleTmScore;
    private double fatCatRigidRmsd; 
    private double fatCatRigidTmScore; 

    public AlignmentScores (String dbId1, String dbId2) {
        
        this.dbId1 = dbId1;
        this.dbId2 = dbId2;
    }

    public AlignmentScores (ResultSet rs) {
    
        try {

            this.dbId1 = rs.getString("db_id_1");
            this.dbId2 = rs.getString("db_id_2");
            this.similarity = rs.getDouble("similarity");
            this.ceRmsd = rs.getDouble("ce_rmsd");
            this.ceTmScore = rs.getDouble("ce_tm_score");
            this.cecpRmsd = rs.getDouble("cecp_rmsd");
            this.cecpTmScore = rs.getDouble("cecp_tm_score");
            this.fatCatFlexibleRmsd = rs.getDouble("fatcat_flexible_rmsd");
            this.fatCatFlexibleTmScore = rs.getDouble("fatcat_flexible_tm_score");
            this.fatCatRigidRmsd = rs.getDouble("fatcat_rigid_rmsd");
            this.fatCatRigidTmScore = rs.getDouble("fatcat_rigid_tm_score");

        } catch (SQLException e) {
            Logger.getLogger(AlignmentScores.class.getName()).log(Level.SEVERE, null, e);
        }
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

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
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

    public double getCecpRmsd() {
        return cecpRmsd;
    }

    public void setCecpRmsd(double cecpRmsd) {
        this.cecpRmsd = cecpRmsd;
    }

    public double getCecpTmScore() {
        return cecpTmScore;
    }

    public void setCecpTmScore(double cecpTmScore) {
        this.cecpTmScore = cecpTmScore;
    }

    public double getFatCatFlexibleRmsd() {
        return fatCatFlexibleRmsd;
    }

    public void setFatCatFlexibleRmsd(double fatCatFlexibleRmsd) {
        this.fatCatFlexibleRmsd = fatCatFlexibleRmsd;
    }

    public double getFatCatFlexibleTmScore() {
        return fatCatFlexibleTmScore;
    }

    public void setFatCatFlexibleTmScore(double fatCatFlexibleTmScore) {
        this.fatCatFlexibleTmScore = fatCatFlexibleTmScore;
    }

    public double getFatCatRigidRmsd() {
        return fatCatRigidRmsd;
    }

    public void setFatCatRigidRmsd(double fatCatRigidRmsd) {
        this.fatCatRigidRmsd = fatCatRigidRmsd;
    }

    public double getFatCatRigidTmScore() {
        return fatCatRigidTmScore;
    }

    public void setFatCatRigidTmScore(double fatCatRigidTmScore) {
        this.fatCatRigidTmScore = fatCatRigidTmScore;
    }

    public double getRmsd(AlignCriteria align) {

        double rmsd = -1.0;

        switch (align) {
            
            case CE:
                rmsd = this.ceRmsd;
                break;
            case CECP:
                rmsd = this.cecpRmsd;
                break;
            case FATCAT_FLEXIBLE:
                rmsd = this.fatCatFlexibleRmsd;
                break;
            case FATCAT_RIGID:
                rmsd = this.fatCatRigidRmsd;
                break;
            default:
                rmsd = -1.0;
        }

        return rmsd;
    }
    
    public double getTmScore(AlignCriteria align) {

        double rmsd = -1.0;

        switch (align) {
            
            case CE:
                rmsd = this.ceTmScore;
                break;
            case CECP:
                rmsd = this.cecpTmScore;
                break;
            case FATCAT_FLEXIBLE:
                rmsd = this.fatCatFlexibleTmScore;
                break;
            case FATCAT_RIGID:
                rmsd = this.fatCatRigidTmScore;
                break;
            default:
                rmsd = -1.0;
        }

        return rmsd;
    }

    @Override
    public String getValue() {
        String row = "(" 
            + dbId1 + "," + dbId2 + ","
            + similarity + ","
            + ceRmsd + "," + ceTmScore + ","
            + cecpRmsd + "," + cecpTmScore + ","
            + fatCatFlexibleRmsd + "," + fatCatFlexibleTmScore + ","
            + fatCatRigidRmsd + "," + fatCatRigidTmScore
            + ")";
        return row;
    }
}
