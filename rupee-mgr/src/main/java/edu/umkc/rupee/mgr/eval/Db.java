package edu.umkc.rupee.mgr.eval;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.PGConnection;
import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.mgr.lib.Constants;

public class Db {

    public static PGSimpleDataSource getDataSource() {

        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setDatabaseName(Constants.DB_NAME);
        ds.setUser(Constants.DB_USER);
        ds.setPassword(Constants.DB_PASSWORD);

        return ds;
    }
    
    public static List<AlignmentScores> getAlignmentScores(String version) {

        List<AlignmentScores> list = new ArrayList<>();

        try {

            PGSimpleDataSource ds = Db.getDataSource();
            Connection conn = ds.getConnection();

            PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_alignment_scores(?);");
            stmt.setString(1, version);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                AlignmentScores scores = new AlignmentScores(rs);
                list.add(scores);
            }

            rs.close();
            stmt.close();
            conn.close();
        
        } catch (SQLException e) {
            Logger.getLogger(Db.class.getName()).log(Level.WARNING, null, e);
        }

        return list;
    }
    
    public static Map<String, AlignmentScores> getAlignmentScores(String version, String dbId) {

        Map<String, AlignmentScores> map = new HashMap<>();

        try {

            PGSimpleDataSource ds = Db.getDataSource();
            Connection conn = ds.getConnection();

            PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_alignment_scores(?, ?);");
            stmt.setString(1, version);
            stmt.setString(2, dbId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                AlignmentScores scores = new AlignmentScores(rs);
                map.put(scores.getDbId2(), scores);
            }

            rs.close();
            stmt.close();
            conn.close();
        
        } catch (SQLException e) {
            Logger.getLogger(Db.class.getName()).log(Level.WARNING, null, e);
        }

        return map;
    }

    public static void saveAlignmentScores(String version, String dbId, List<AlignmentScores> scores) {

        try {

            PGSimpleDataSource ds = Db.getDataSource();
            Connection conn = ds.getConnection();
            conn.setAutoCommit(true);

            ((PGConnection) conn).addDataType("alignment_scores", AlignmentScores.class);

            PreparedStatement updt = conn.prepareStatement("SELECT insert_alignment_scores(?, ?, ?);");

            updt.setString(1, version);
            updt.setString(2, dbId);

            AlignmentScores a[] = new AlignmentScores[scores.size()];
            scores.toArray(a);
            updt.setArray(3, conn.createArrayOf("alignment_scores", a));

            updt.execute();
            updt.close();
        
        } catch (SQLException e) {
            Logger.getLogger(Db.class.getName()).log(Level.WARNING, null, e);
        }
    }
}

