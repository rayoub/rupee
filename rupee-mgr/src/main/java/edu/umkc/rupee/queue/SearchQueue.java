package edu.umkc.rupee.queue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.postgresql.PGConnection;
import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.base.Search;
import edu.umkc.rupee.base.SearchCriteria;
import edu.umkc.rupee.base.SearchRecord;
import edu.umkc.rupee.cath.CathSearch;
import edu.umkc.rupee.cath.CathSearchCriteria;
import edu.umkc.rupee.chain.ChainSearch;
import edu.umkc.rupee.chain.ChainSearchCriteria;
import edu.umkc.rupee.defs.DbType;
import edu.umkc.rupee.defs.SearchMode;
import edu.umkc.rupee.defs.SearchBy;
import edu.umkc.rupee.defs.SearchFrom;
import edu.umkc.rupee.defs.SortBy;
import edu.umkc.rupee.ecod.EcodSearch;
import edu.umkc.rupee.ecod.EcodSearchCriteria;
import edu.umkc.rupee.lib.Db;
import edu.umkc.rupee.lib.DbId;
import edu.umkc.rupee.scop.ScopSearch;
import edu.umkc.rupee.scop.ScopSearchCriteria;

public class SearchQueue {

    // /api/queue/enqueue
    public static boolean enqueue(QueueItem item) throws SQLException {

        PGSimpleDataSource ds = Db.getDataSource();
        
        Connection conn = ds.getConnection();
        conn.setAutoCommit(true);

        PreparedStatement updt = conn.prepareStatement("SELECT insert_search_queue(?,?,?,?,?,?,?,?,?);");

        updt.setString(1, item.getUserId());
        updt.setInt(2, item.getDbType());
        updt.setInt(3, item.getSearchFilter());
        updt.setInt(4, item.getSearchBy());
        updt.setString(5, item.getDbId());
        updt.setInt(6, item.getUploadId());
        updt.setInt(7, item.getSortBy());
        updt.setInt(8, item.getMaxRecords());
        updt.setString(9, item.getStatus());

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

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_search_queue_by_user(?);");
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
    public static SearchQueueResult getSearch(int searchId, DbType dbType) throws SQLException {
        
        // *** open connection

        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn = ds.getConnection();
        conn.setAutoCommit(true);

        // *** get the queue item corresponding to search hash

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_search_queue(?);");
        stmt.setInt(1, searchId);

        QueueItem item = null;
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {

            item = new QueueItem(rs);
        }

        rs.close();
        stmt.close();

        // *** get search results corresponding to search hash

        Search search;
        if (dbType == DbType.SCOP) {
            search = new ScopSearch();
        }
        else if (dbType == DbType.CATH) {
            search = new CathSearch();
        }
        else if (dbType == DbType.ECOD) {
            search = new EcodSearch();
        }
        else {
            search = new ChainSearch();
        }

        List<SearchRecord> records = new ArrayList<SearchRecord>();

        PreparedStatement stmt2 = conn.prepareCall("SELECT * FROM get_search_result(?);");
        stmt2.setInt(1, searchId);

        ResultSet rs2 = stmt2.executeQuery();
        while(rs2.next()) {

            SearchRecord record = search.getSearchRecord();
            record.set(rs2);
            records.add(record);
        }

        rs2.close();
        stmt2.close();
        
        augment(records, dbType, search);
        
        // *** close connection

        conn.close();

        return new SearchQueueResult(item, records);
    }
    
    private static void augment(List<SearchRecord> records, DbType dbType, Search search) throws SQLException {

        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn = ds.getConnection();
        conn.setAutoCommit(true);
   
        Object[] objDbIds = records.stream().map(record -> record.getDbId()).toArray();

        String[] dbIds = Arrays.copyOf(objDbIds, objDbIds.length, String[].class);

        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM get_" + dbType.getTableName() + "_augmented_results(?);");
        stmt.setArray(1, conn.createArrayOf("VARCHAR", dbIds));

        ResultSet rs = stmt.executeQuery();
        
        int n = 0;

        while (rs.next()) {

            // WITH ORDINALITY clause will ensure they are ordered correctly

            SearchRecord record = records.get(n++);
            search.augment(record, rs);
        }
        
        rs.close();
        stmt.close();
        conn.close();
    }

    public static void search(QueueItem item) throws Exception {
        
        // *** open connection

        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn = ds.getConnection();
        conn.setAutoCommit(true);

        // *** build criteria - some duplicate code from SearchResource.java

        // specific criteria
        SearchCriteria criteria;
        Search search;
        if (item.getDbType() == DbType.SCOP.getId()) {
            ScopSearchCriteria scopCriteria = new ScopSearchCriteria();
            search = new ScopSearch();

            switch (item.getSearchFilter()) {
                case 1:
                    scopCriteria.differentFold = true;
                    break;
                case 2:
                    scopCriteria.differentSuperfamily = true;
                    break;
                case 3:
                    scopCriteria.differentFamily = true;
                    break;
            }

            criteria = scopCriteria;
        }
        else if (item.getDbType() == DbType.CATH.getId()) {
            CathSearchCriteria cathCriteria = new CathSearchCriteria();
            search = new CathSearch();

            switch (item.getSearchFilter()) {
                case 1:
                    cathCriteria.differentTopology = true;
                    break;
                case 2:
                    cathCriteria.differentSuperfamily = true;
                    break;
                case 3:
                    cathCriteria.differentS35 = true;
                    break;
                case 4:
                    cathCriteria.topologyReps = true;
                    break;
                case 5:
                    cathCriteria.superfamilyReps = true;
                    break;
                case 6:
                    cathCriteria.s35Reps = true;
                    break;
            }

            criteria = cathCriteria;
        }
        else if (item.getDbType() == DbType.ECOD.getId()) {
            EcodSearchCriteria ecodCriteria = new EcodSearchCriteria();
            search = new EcodSearch();
            
            switch (item.getSearchFilter()) {
                case 1:
                    ecodCriteria.differentH = true;
                    break;
                case 2:
                    ecodCriteria.differentT = true;
                    break;
                case 3:
                    ecodCriteria.differentF = true;
                    break;
            }

            criteria = ecodCriteria;
        }
        else {
            ChainSearchCriteria chainCriteria = new ChainSearchCriteria();
            search = new ChainSearch();

            criteria = chainCriteria;
        }
      
        // common criteria 
        criteria.limit = item.getMaxRecords();
        criteria.searchDbType = DbType.fromId(item.getDbType());
        criteria.searchBy = SearchBy.fromId(item.getSearchBy());
        criteria.searchMode = SearchMode.TOP_ALIGNED;
        criteria.sortBy = SortBy.fromId(item.getSortBy());

        // determine id type of our search or download
        if (criteria.searchBy == SearchBy.DB_ID) {
            criteria.idDbType = DbId.getDbIdType(item.getDbId());
            criteria.dbId = item.getDbId();        
        }
        else {
            criteria.idDbType = criteria.searchDbType;
            criteria.uploadId = item.getUploadId();
        }

        // *** execute the search and store results
   
        List<SearchRecord> records = search.search(criteria, SearchFrom.SERVER);
       
        // set search hash 
        records.stream().forEach(record -> record.setSearchId(item.getSearchId()));
            
        // save records
        PreparedStatement updt;

        ((PGConnection)conn).addDataType("search_result", SearchRecord.class);
        
        updt = conn.prepareStatement("SELECT insert_search_result(?);");

        SearchRecord a[] = new SearchRecord[records.size()];
        records.toArray(a);
        updt.setArray(1, conn.createArrayOf("search_result", a));
        updt.execute();
        
        updt.close();

        // *** close connection

        conn.close();
    }
}
