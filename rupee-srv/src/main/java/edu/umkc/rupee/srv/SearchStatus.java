package edu.umkc.rupee.srv;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.lib.Db;

public class SearchStatus {

    public static void updateStatus(int searchId, String status) {

        PGSimpleDataSource ds = Db.getDataSource();

        try {

            Connection conn = ds.getConnection();
            conn.setAutoCommit(true);

            String sql = "";
            if (status.equals("queued")) {
                sql = "UPDATE search_queue SET status = ? WHERE search_id = ? AND status = 'pending';";
            }
            else {
                sql = "UPDATE search_queue SET status = ? WHERE search_id = ?;";
            }

            PreparedStatement stmt = conn.prepareCall(sql);
            stmt.setString(1, status);
            stmt.setInt(2, searchId);

            stmt.execute();

        } catch (SQLException e) {
            Logger.getLogger(SearchStatus.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
