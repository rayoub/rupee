package edu.umkc.rupee.queue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.base.Search;
import edu.umkc.rupee.base.SearchRecord;
import edu.umkc.rupee.cath.CathSearch;
import edu.umkc.rupee.chain.ChainSearch;
import edu.umkc.rupee.defs.DbTypeCriteria;
import edu.umkc.rupee.ecod.EcodSearch;
import edu.umkc.rupee.lib.Db;
import edu.umkc.rupee.scop.ScopSearch;

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

        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_search_queue(?);");
        stmt.setString(1, userId);

        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {

            QueueItem item = new QueueItem(rs);
            items.add(item);
        }

        rs.close();
        stmt.close();
        conn.close();

        return items;
    }
   
    // /api/queue/search 
    public static List<SearchRecord> getSearch(String searchHash, DbTypeCriteria dbType) throws SQLException {
        
        Search search;
        if (dbType == DbTypeCriteria.SCOP) {
            search = new ScopSearch();
        }
        else if (dbType == DbTypeCriteria.CATH) {
            search = new CathSearch();
        }
        else if (dbType == DbTypeCriteria.ECOD) {
            search = new EcodSearch();
        }
        else {
            search = new ChainSearch();
        }

        List<SearchRecord> records = new ArrayList<SearchRecord>();

        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn = ds.getConnection();
        conn.setAutoCommit(false);

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_search_result(?);");
        stmt.setString(1, searchHash);

        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {

            SearchRecord record = search.getSearchRecord();
            record.set(rs);
            records.add(record);
        }

        rs.close();
        stmt.close();
        conn.close();

        augment(records, dbType, search);

        return records;
    }
    
    private static void augment(List<SearchRecord> records, DbTypeCriteria dbType, Search search) throws SQLException {

        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn = ds.getConnection();
        conn.setAutoCommit(true);
   
        Object[] objDbIds = records.stream().map(record -> record.getDbId()).toArray();

        String[] dbIds = Arrays.copyOf(objDbIds, objDbIds.length, String[].class);

        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM get_" + dbType.getTableName() + "_augmented_results(?);");
        stmt.setArray(1, conn.createArrayOf("VARCHAR", dbIds));

        ResultSet rs = stmt.executeQuery();
        
        int n = 1;

        while (rs.next()) {

            // WITH ORDINALITY clause will ensure they are ordered correctly

            SearchRecord record = records.get(n-1);
            search.augment(record, rs);
        }
        
        rs.close();
        stmt.close();
        conn.close();
    }
}
