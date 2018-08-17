package edu.umkc.rupee.auto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.lib.Constants;
import edu.umkc.rupee.lib.Db;

public class Benchmarks {

    public static PGSimpleDataSource getDataSource() {

        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setDatabaseName(Constants.DB_NAME);
        ds.setUser(Constants.DB_USER);
        ds.setPassword(Constants.DB_PASSWORD);

        return ds;
    }

    public static List<String> getD193() {

        List<String> d193 = new ArrayList<>();

        PGSimpleDataSource ds = Db.getDataSource();

        try {

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);
       
            PreparedStatement stmt = conn.prepareCall("SELECT scop_id AS scop_id FROM benchmark_d193 ORDER BY scop_id;");
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                String scopId = rs.getString("scop_id");
                d193.add(scopId);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, e);
        }

        return d193;
    }
    
    public static List<String> getD500() {

        List<String> d500 = new ArrayList<>();

        PGSimpleDataSource ds = Db.getDataSource();

        try {

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);
       
            PreparedStatement stmt = conn.prepareCall("SELECT scop_id AS scop_id FROM benchmark_d500 ORDER BY scop_id;");
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                String scopId = rs.getString("scop_id");
                d500.add(scopId);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, e);
        }

        return d500;
    }
}

