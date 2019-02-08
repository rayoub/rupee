package edu.umkc.rupee.chain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.umkc.rupee.base.SearchContainment;
import edu.umkc.rupee.base.SearchCriteria;
import edu.umkc.rupee.base.SearchRecord;
import edu.umkc.rupee.defs.DbType;
import edu.umkc.rupee.lib.Constants;

public class ChainSearchContainment extends SearchContainment {

    public DbType getDbType() {

        return DbType.CHAIN;
    }

    public PreparedStatement getSearchStatement(SearchCriteria criteria, int splitIndex, Connection conn)
            throws SQLException {
        
        ChainSearchCriteria chainCriteria = (ChainSearchCriteria) criteria;

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_chain_split_matches(?,?,?,?,?);");

        stmt.setInt(1, chainCriteria.idDbType.getId());
        stmt.setString(2, chainCriteria.dbId);
        stmt.setInt(3, chainCriteria.uploadId);
        stmt.setInt(4, splitIndex);
        stmt.setInt(5, Constants.SPLIT_COUNT);

        return stmt;
    }

    public void augment(SearchRecord record, ResultSet rs) throws SQLException {
        
        // do nothing
    }

    public SearchRecord getSearchRecord() {

        return new ChainSearchRecord();
    }
}
