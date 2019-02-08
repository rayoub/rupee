package edu.umkc.rupee.cath;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.umkc.rupee.base.Search;
import edu.umkc.rupee.base.SearchCriteria;
import edu.umkc.rupee.base.SearchRecord;
import edu.umkc.rupee.defs.DbType;

public class CathSearch extends Search {

    public DbType getDbType() {

        return DbType.CATH;
    }

    public PreparedStatement getSearchStatement(SearchCriteria criteria, int bandIndex, Connection conn)
            throws SQLException {
        
        CathSearchCriteria cathCriteria = (CathSearchCriteria) criteria;

        PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_cath_band_matches(?,?,?,?,?,?,?,?,?,?);");

        stmt.setInt(1, cathCriteria.idDbType.getId());
        stmt.setString(2, cathCriteria.dbId);
        stmt.setInt(3, cathCriteria.uploadId);
        stmt.setInt(4, bandIndex + 1);
        stmt.setBoolean(5, cathCriteria.topologyReps);
        stmt.setBoolean(6, cathCriteria.superfamilyReps);
        stmt.setBoolean(7, cathCriteria.s35Reps);
        stmt.setBoolean(8, cathCriteria.differentTopology);
        stmt.setBoolean(9, cathCriteria.differentSuperfamily);
        stmt.setBoolean(10, cathCriteria.differentS35);

        return stmt;
    }

    public void augment(SearchRecord record, ResultSet rs) throws SQLException {

        CathSearchRecord cathRecord = (CathSearchRecord)record;

        cathRecord.setC(rs.getString("c"));
        cathRecord.setA(rs.getString("a"));
        cathRecord.setT(rs.getString("t"));
        cathRecord.setH(rs.getString("h"));
        cathRecord.setS(rs.getString("s"));
        cathRecord.setO(rs.getString("o"));
        cathRecord.setL(rs.getString("l"));
        cathRecord.setI(rs.getString("i"));
        cathRecord.setD(rs.getString("d"));
        cathRecord.setADescription(rs.getString("a_description"));
        cathRecord.setTDescription(rs.getString("t_description"));
        cathRecord.setHDescription(rs.getString("h_description"));
    }

    public SearchRecord getSearchRecord() {

        return new CathSearchRecord();
    }
}
