package edu.umkc.rupee.lib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.PGConnection;
import org.postgresql.ds.PGSimpleDataSource;

public class Db {

    public static PGSimpleDataSource getDataSource() {

        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setDatabaseName(Constants.DB_NAME);
        ds.setUser(Constants.DB_USER);
        ds.setPassword(Constants.DB_PASSWORD);

        return ds;
    }

    // *********************************************************************
    // Getting Grams
    // *********************************************************************

    public static List<Integer> getGrams(String dbId, DbTypeCriteria dbType) throws SQLException {

        List<Integer> grams = new ArrayList<>();

        List<String> dbIds = new ArrayList<>();
        dbIds.add(dbId);
        Map<String, List<Integer>> map = getGrams(dbIds, dbType);
        if (map.containsKey(dbId)) {
            grams = map.get(dbId);
        }

        return grams;
    }

    public static Map<String, List<Integer>> getGrams(List<String> dbIds, DbTypeCriteria dbType) throws SQLException {

        Map<String, List<Integer>> map = new HashMap<>();

        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
   
        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_" + dbType.getDescription().toLowerCase() + "_grams(?);");
        
        Object[] objDbIds = dbIds.toArray();
        String[] stringDbIds = Arrays.copyOf(objDbIds, objDbIds.length, String[].class);
        stmt.setArray(1, conn.createArrayOf("VARCHAR", stringDbIds));
        
        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {

            String dbId = rs.getString("db_id");
            Integer[] grams = (Integer[])rs.getArray("grams").getArray();
          
            if (grams != null && grams.length > 0) {
                map.put(dbId, Arrays.asList(grams));
            } 
        }

        rs.close();
        stmt.close();
        conn.close();

        return map;
    }

    public static List<Integer> getUploadGrams(int uploadId) throws SQLException {

        List<Integer> grams = null;

        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
   
        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_upload_grams(?);");
       
        stmt.setInt(1, uploadId);
        
        ResultSet rs = stmt.executeQuery();
        if(rs.next()) {

            Integer[] grams1 = (Integer[])rs.getArray("grams").getArray();
            if (grams1 != null && grams1.length > 0) {
                grams = Arrays.asList(grams1);
            }
        }

        rs.close();
        stmt.close();
        conn.close();

        return grams;
    }
    
    // *********************************************************************
    // Getting Hashes
    // *********************************************************************

    public static Hashes getHashes(String dbId, DbTypeCriteria dbType) throws SQLException {

        Hashes hashes = null;

        List<String> dbIds = new ArrayList<>();
        dbIds.add(dbId);
        Map<String, Hashes> map = getHashes(dbIds, dbType);
        if (map.containsKey(dbId)) {
            hashes = map.get(dbId);
        }

        return hashes;
    }
    
    public static Map<String,Hashes> getHashes(List<String> dbIds, DbTypeCriteria dbType) throws SQLException {

        Map<String, Hashes> map = new HashMap<>();

        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_" + dbType.getDescription().toLowerCase() + "_hashes(?);");
        
        Object[] objDbIds = dbIds.toArray();
        String[] stringDbIds = Arrays.copyOf(objDbIds, objDbIds.length, String[].class);
        stmt.setArray(1, conn.createArrayOf("VARCHAR", stringDbIds));

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {

            Hashes hashes = new Hashes();
            hashes.dbId = rs.getString("db_id");
            hashes.minHashes = (Integer[])rs.getArray("min_hashes").getArray();
            hashes.bandHashes = (Integer[])rs.getArray("band_hashes").getArray();

            map.put(hashes.getDbId(), hashes);
        }

        rs.close();
        stmt.close();
        conn.close();

        return map;
    }
    
    public static Hashes getUploadHashes(int uploadId) throws SQLException {

        Hashes hashes = null;

        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_upload_hashes(?);");

        stmt.setInt(1, uploadId);

        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            hashes = new Hashes();
            hashes.minHashes = (Integer[])rs.getArray("min_hashes").getArray();
            hashes.bandHashes = (Integer[])rs.getArray("band_hashes").getArray();
        }

        rs.close();
        stmt.close();
        conn.close();

        return hashes;
    }
    
    // *********************************************************************
    // Getting 
    // *********************************************************************
    
    public static Map<String, AlignmentScores> getAlignmentScores(String dbId) {

        Map<String, AlignmentScores> map = new HashMap<>();

        try {

            PGSimpleDataSource ds = Db.getDataSource();
            Connection conn = ds.getConnection();

            PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_alignment_scores(?);");
            stmt.setString(1, dbId);

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

    // *********************************************************************
    // Setting
    // *********************************************************************

    public static void saveLogs(List<Log> logs, Connection conn) throws SQLException {

        ((PGConnection) conn).addDataType("log", Log.class);

        PreparedStatement updt = conn.prepareStatement("SELECT insert_logs(?);");

        Log a[] = new Log[logs.size()];
        logs.toArray(a);
        updt.setArray(1, conn.createArrayOf("log", a));

        updt.execute();
        updt.close();
    }
    
    public static void saveAlignmentScores(String dbId, List<AlignmentScores> scores) {

        try {

            PGSimpleDataSource ds = Db.getDataSource();
            Connection conn = ds.getConnection();
            conn.setAutoCommit(true);

            ((PGConnection) conn).addDataType("alignment_scores", AlignmentScores.class);

            PreparedStatement updt = conn.prepareStatement("SELECT insert_alignment_scores(?, ?);");

            updt.setString(1, dbId);

            AlignmentScores a[] = new AlignmentScores[scores.size()];
            scores.toArray(a);
            updt.setArray(2, conn.createArrayOf("alignment_scores", a));

            updt.execute();
            updt.close();
        
        } catch (SQLException e) {
            Logger.getLogger(Db.class.getName()).log(Level.WARNING, null, e);
        }
    }
}

