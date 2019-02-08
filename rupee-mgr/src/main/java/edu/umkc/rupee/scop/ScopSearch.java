package edu.umkc.rupee.scop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.umkc.rupee.base.Search;
import edu.umkc.rupee.base.SearchCriteria;
import edu.umkc.rupee.base.SearchRecord;
import edu.umkc.rupee.defs.DbType;

public class ScopSearch extends Search {

    public DbType getDbType() {

        return DbType.SCOP;
    }
    
    public PreparedStatement getSearchStatement(SearchCriteria criteria, int bandIndex, Connection conn)
            throws SQLException {
        
        ScopSearchCriteria scopCriteria = (ScopSearchCriteria) criteria;

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_scop_band_matches(?,?,?,?,?,?,?);");

        stmt.setInt(1, scopCriteria.idDbType.getId());
        stmt.setString(2, scopCriteria.dbId);
        stmt.setInt(3, scopCriteria.uploadId);
        stmt.setInt(4, bandIndex + 1);
        stmt.setBoolean(5, scopCriteria.differentFold);
        stmt.setBoolean(6, scopCriteria.differentSuperfamily);
        stmt.setBoolean(7, scopCriteria.differentFamily);

        return stmt;
    }

    public void augment(SearchRecord record, ResultSet rs) throws SQLException {

        ScopSearchRecord scopRecord = (ScopSearchRecord)record;

        scopRecord.setSunid(rs.getInt("sunid"));
        scopRecord.setCl(rs.getString("cl"));
        scopRecord.setCf(rs.getInt("cf"));
        scopRecord.setSf(rs.getInt("sf"));
        scopRecord.setFa(rs.getInt("fa"));
        scopRecord.setCfDescription(rs.getString("cf_description"));
        scopRecord.setSfDescription(rs.getString("sf_description"));
        scopRecord.setFaDescription(rs.getString("fa_description"));
    }

    public SearchRecord getSearchRecord() {

        return new ScopSearchRecord();
    }
}
