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
import edu.umkc.rupee.defs.DbTypeCriteria;
import edu.umkc.rupee.defs.ModeCriteria;
import edu.umkc.rupee.defs.SearchByCriteria;
import edu.umkc.rupee.defs.SearchFrom;
import edu.umkc.rupee.defs.SortCriteria;
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

    public static void search(String searchHash) throws Exception {
        
        // *** open connection

        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn = ds.getConnection();
        conn.setAutoCommit(true);

        // *** get the queue item corresponding to search hash

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_search_queue_by_hash(?);");
        stmt.setString(1, searchHash);

        QueueItem item = null;
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {

            item = new QueueItem(rs);
        }

        rs.close();
        stmt.close();

        // *** build criteria - some duplicate code from SearchResource.java

        // specific criteria
        SearchCriteria criteria;
        Search search;
        if (item.getDbType() == DbTypeCriteria.SCOP.getId()) {
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
        else if (item.getDbType() == DbTypeCriteria.CATH.getId()) {
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
        else if (item.getDbType() == DbTypeCriteria.ECOD.getId()) {
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
        criteria.dbType = DbTypeCriteria.fromId(item.getDbType());
        criteria.searchBy = SearchByCriteria.fromId(item.getSearchBy());
        criteria.mode = ModeCriteria.TOP_ALIGNED;
        criteria.sort = SortCriteria.fromId(item.getSortBy());

        // determine id type of our search or download
        if (criteria.searchBy == SearchByCriteria.DB_ID) {
            criteria.dbIdType = DbId.getDbIdType(item.getDbId());
            criteria.dbId = item.getDbId();        
        }
        else {
            criteria.dbIdType = criteria.dbType;
            criteria.uploadId = item.getUploadId();
        }

        // *** execute the search and store results
   
        List<SearchRecord> records = search.search(criteria, SearchFrom.SERVER);
       
        // set search hash 
        records.stream().forEach(record -> record.setSearchHash(searchHash));
            
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
