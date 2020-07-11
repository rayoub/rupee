package edu.umkc.rupee.search.lib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.search.defs.DbType;

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

    public static Grams getGrams(String dbId, DbType dbType, boolean storeCoords) throws SQLException {

        Grams grams = null;

        List<String> dbIds = new ArrayList<>();
        dbIds.add(dbId);
        Map<String, Grams> map = getGrams(dbIds, dbType, storeCoords);
        if (map.containsKey(dbId)) {
            grams = map.get(dbId);
        }

        return grams;
    }

    public static Map<String, Grams> getGrams(List<String> dbIds, DbType dbType, boolean storeCoords) throws SQLException {

        Map<String, Grams> map = new HashMap<>();

        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
   
        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_" + dbType.getTableName() + "_grams(?);");
        stmt.setFetchSize(200);
        
        Object[] objDbIds = dbIds.toArray();
        String[] stringDbIds = Arrays.copyOf(objDbIds, objDbIds.length, String[].class);
        stmt.setArray(1, conn.createArrayOf("VARCHAR", stringDbIds));
        
        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {

            String dbId = rs.getString("db_id");
            Grams grams = Grams.fromResultSet(rs, storeCoords);
            map.put(dbId, grams);
        }

        rs.close();
        stmt.close();
        conn.close();

        return map;
    }

    public static Grams getUploadGrams(int uploadId) throws SQLException {

        Grams grams = null;

        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);
   
        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_upload_grams(?);");
       
        stmt.setInt(1, uploadId);
        
        ResultSet rs = stmt.executeQuery();
        if(rs.next()) {
            grams = Grams.fromResultSet(rs, true);
        }

        rs.close();
        stmt.close();
        conn.close();

        return grams;
    }
    
    // *********************************************************************
    // Getting Hashes
    // *********************************************************************

    public static Hashes getHashes(String dbId, DbType dbType) throws SQLException {

        Hashes hashes = null;

        List<String> dbIds = new ArrayList<>();
        dbIds.add(dbId);
        Map<String, Hashes> map = getHashes(dbIds, dbType);
        if (map.containsKey(dbId)) {
            hashes = map.get(dbId);
        }

        return hashes;
    }
    
    public static Map<String,Hashes> getHashes(List<String> dbIds, DbType dbType) throws SQLException {

        Map<String, Hashes> map = new HashMap<>();

        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_" + dbType.getTableName() + "_hashes(?);");
        
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
}

