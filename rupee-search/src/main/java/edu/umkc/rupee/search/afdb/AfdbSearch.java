package edu.umkc.rupee.search.afdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.umkc.rupee.search.base.Search;
import edu.umkc.rupee.search.base.SearchCriteria;
import edu.umkc.rupee.search.base.SearchRecord;
import edu.umkc.rupee.search.defs.DbType;
import edu.umkc.rupee.search.lib.Constants;

public class AfdbSearch extends Search {

    public DbType getDbType() {

        return DbType.AFDB;
    }

    public PreparedStatement getSplitSearchStatement(SearchCriteria criteria, int splitIndex, Connection conn)
            throws SQLException {
        
        AfdbSearchCriteria afdbCriteria = (AfdbSearchCriteria) criteria;

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_afdb_split_matches(?,?,?,?,?,?);");

        stmt.setInt(1, afdbCriteria.idDbType.getId());
        stmt.setString(2, afdbCriteria.dbId);
        stmt.setInt(3, afdbCriteria.uploadId);
        stmt.setInt(4, splitIndex);
        stmt.setInt(5, Constants.SEARCH_SPLIT_COUNT);
        if (afdbCriteria.proteomeId == null || afdbCriteria.proteomeId.isEmpty()) {
            stmt.setNull(6, java.sql.Types.VARCHAR);
        }
        else {
            stmt.setString(6, afdbCriteria.proteomeId); 
        }
        
        return stmt;
    }
    
    public PreparedStatement getBandSearchStatement(SearchCriteria criteria, int bandIndex, Connection conn)
            throws SQLException {

        AfdbSearchCriteria afdbCriteria = (AfdbSearchCriteria) criteria;

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_afdb_band_matches(?,?,?,?,?);");

        stmt.setInt(1, afdbCriteria.idDbType.getId());
        stmt.setString(2, afdbCriteria.dbId);
        stmt.setInt(3, afdbCriteria.uploadId);
        stmt.setInt(4, bandIndex + 1);
        if (afdbCriteria.proteomeId == null || afdbCriteria.proteomeId.isEmpty()) {
            stmt.setNull(5, java.sql.Types.VARCHAR);
        }
        else {
            stmt.setString(5, afdbCriteria.proteomeId); 
        }
        
        return stmt;
    }

    public void augment(SearchRecord record, ResultSet rs) throws SQLException {

        AfdbSearchRecord afdbRecord = (AfdbSearchRecord)record;

        afdbRecord.setProteomeId(rs.getString("proteome_id"));
        afdbRecord.setSpecies(rs.getString("species"));
        afdbRecord.setCommonName(rs.getString("common_name"));
    }

    public SearchRecord getSearchRecord() {

        return new AfdbSearchRecord();
    }
}
