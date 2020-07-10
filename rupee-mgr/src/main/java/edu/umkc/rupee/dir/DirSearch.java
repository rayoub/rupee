package edu.umkc.rupee.dir;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.umkc.rupee.base.Search;
import edu.umkc.rupee.base.SearchCriteria;
import edu.umkc.rupee.base.SearchRecord;
import edu.umkc.rupee.defs.DbType;
import edu.umkc.rupee.lib.Constants;

public class DirSearch extends Search {

    public DbType getDbType() {

        return DbType.DIR;
    }

    public PreparedStatement getSplitSearchStatement(SearchCriteria criteria, int splitIndex, Connection conn)
            throws SQLException {
        
        DirSearchCriteria dirCriteria = (DirSearchCriteria) criteria;

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_dir_split_matches(?,?,?,?,?);");

        stmt.setInt(1, dirCriteria.idDbType.getId());
        stmt.setString(2, dirCriteria.dbId);
        stmt.setInt(3, dirCriteria.uploadId);
        stmt.setInt(4, splitIndex);
        stmt.setInt(5, Constants.SEARCH_SPLIT_COUNT);

        return stmt;
    }

    public PreparedStatement getBandSearchStatement(SearchCriteria criteria, int bandIndex, Connection conn)
            throws SQLException {
        
        DirSearchCriteria dirCriteria = (DirSearchCriteria) criteria;

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_dir_band_matches(?,?,?,?);");

        stmt.setInt(1, dirCriteria.idDbType.getId());
        stmt.setString(2, dirCriteria.dbId);
        stmt.setInt(3, dirCriteria.uploadId);
        stmt.setInt(4, bandIndex + 1);

        return stmt;
    }

    public void augment(SearchRecord record, ResultSet rs) throws SQLException {
        
        // do nothing
    }

    public SearchRecord getSearchRecord() {

        return new DirSearchRecord();
    }
}
