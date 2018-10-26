package edu.umkc.rupee.queue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.base.SearchRecord;
import edu.umkc.rupee.lib.Db;

public class SearchQueue {

    // /api/queue/enqueue
    public static boolean enqueue(QueueItem item) throws SQLException {

        PGSimpleDataSource ds = Db.getDataSource();
        
        Connection conn = ds.getConnection();
        conn.setAutoCommit(true);

        PreparedStatement updt = conn.prepareStatement("SELECT insert_search_queue(?,?,?,?,?,?,?,?,?,?);");

        updt.setString(1, item.getUserId());
        updt.setString(2, item.getSearchHash());
        updt.setInt(3, item.getDbType());
        updt.setInt(4, item.getSearchFilter());
        updt.setInt(5, item.getSearchBy());
        updt.setString(6, item.getDbId());
        updt.setInt(7, item.getUploadId());
        updt.setInt(8, item.getSortBy());
        updt.setInt(9, item.getMaxRecords());
        updt.setString(10, item.getStatus());

        updt.execute();
        updt.close();
        conn.close();

        return true;
    }

    // /api/queue/list
    public static List<QueueItem> getQueue(String userId) throws SQLException {

        List<QueueItem> items = new ArrayList<QueueItem>();

        //try {

            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_search_queue(?);");
            stmt.setString(1, userId);

            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {

                QueueItem item = new QueueItem(rs);
                item.setUserId(userId);
                items.add(item);
            }

            rs.close();
            stmt.close();
            conn.close();
        //}
        //catch(SQLException e) {
        //    Logger.getLogger(SearchQueue.class.getName()).log(Level.SEVERE, null, e);
        //}

        return items;
    }
   
    // /api/queue/search 
    public static List<SearchRecord> getSearch(String hash) {


        // get the search corresponding to the hash
        // augment results as needed 
        // and provide results to client in the same manner as base search functionality

        return null;
    }
}
