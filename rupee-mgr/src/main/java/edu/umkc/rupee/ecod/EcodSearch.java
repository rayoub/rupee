package edu.umkc.rupee.ecod;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.umkc.rupee.base.Search;
import edu.umkc.rupee.base.SearchCriteria;
import edu.umkc.rupee.base.SearchRecord;
import edu.umkc.rupee.lib.DbTypeCriteria;

public class EcodSearch extends Search {

    public DbTypeCriteria getDbType() {

        return DbTypeCriteria.ECOD;
    }
    
    public PreparedStatement getSearchStatement(SearchCriteria criteria, int bandIndex, Connection conn)
            throws SQLException {
        
        EcodSearchCriteria ecodCriteria = (EcodSearchCriteria) criteria;

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_ecod_band_matches(?,?,?,?,?,?,?);");

        stmt.setInt(1, ecodCriteria.dbIdType.getId());
        stmt.setString(2, ecodCriteria.dbId);
        stmt.setInt(3, ecodCriteria.uploadId);
        stmt.setInt(4, bandIndex + 1);
        stmt.setBoolean(5, ecodCriteria.differentH);
        stmt.setBoolean(6, ecodCriteria.differentT);
        stmt.setBoolean(7, ecodCriteria.differentF);

        return stmt;
    }

    public void augment(SearchRecord record, ResultSet rs) throws SQLException {

        EcodSearchRecord ecodRecord = (EcodSearchRecord)record;

        ecodRecord.setX(rs.getString("x"));
        ecodRecord.setH(rs.getString("h"));
        ecodRecord.setT(rs.getString("t"));
        ecodRecord.setF(rs.getString("f"));
        ecodRecord.setArchitecture(rs.getString("architecture"));
        ecodRecord.setXDescription(rs.getString("x_description"));
        ecodRecord.setHDescription(rs.getString("h_description"));
        ecodRecord.setTDescription(rs.getString("t_description"));
        ecodRecord.setFDescription(rs.getString("f_description"));
    }

    public SearchRecord getSearchRecord() {

        return new EcodSearchRecord();
    }
}
