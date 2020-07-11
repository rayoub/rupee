package edu.umkc.rupee.eval.lib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.ds.PGSimpleDataSource;

public class Benchmarks {

    public static List<String> get(String name) {

        List<String> dbIds = new ArrayList<>();

        PGSimpleDataSource ds = Db.getDataSource();

        try {

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);
       
            PreparedStatement stmt = conn.prepareCall("SELECT db_id FROM benchmark WHERE name = '" + name + "' ORDER BY db_id;");
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                String dbId = rs.getString("db_id");
                dbIds.add(dbId);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            Logger.getLogger(Benchmarks.class.getName()).log(Level.SEVERE, null, e);
        }

        return dbIds;
    }
    
    public static List<String> getSplit(String name, int splitCount, int splitIndex) {

        List<String> dbIds = new ArrayList<>();

        PGSimpleDataSource ds = Db.getDataSource();

        try {

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);
       
            PreparedStatement stmt = conn.prepareCall("SELECT db_id FROM get_benchmark_split(?,?,?);");
            stmt.setString(1, name);
            stmt.setInt(2, splitCount);
            stmt.setInt(3, splitIndex);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                String dbId = rs.getString("db_id");
                dbIds.add(dbId);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            Logger.getLogger(Benchmarks.class.getName()).log(Level.SEVERE, null, e);
        }

        return dbIds;
    }
}

