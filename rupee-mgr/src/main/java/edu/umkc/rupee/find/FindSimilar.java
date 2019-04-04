package edu.umkc.rupee.find;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.base.SearchRecord;
import edu.umkc.rupee.defs.AcrossType;
import edu.umkc.rupee.defs.DbType;
import edu.umkc.rupee.defs.SearchBy;
import edu.umkc.rupee.defs.SearchFrom;
import edu.umkc.rupee.defs.SearchMode;
import edu.umkc.rupee.defs.SearchType;
import edu.umkc.rupee.defs.SortBy;
import edu.umkc.rupee.lib.Aligning;
import edu.umkc.rupee.lib.Db;
import edu.umkc.rupee.scop.ScopSearch;
import edu.umkc.rupee.scop.ScopSearchCriteria;
import edu.umkc.rupee.tm.Mode;
import edu.umkc.rupee.tm.TMAlign;

public class FindSimilar {

    private static double SIMILARITY_THRESHOLD = 0.50;
    private static int RESULTS_DEPTH = 10;

    public static void searchAcross(SearchType searchType, AcrossType acrossType) {

        PGSimpleDataSource ds = Db.getDataSource();

        // grab domains from the database
        List<FsDomain> domains = getDomains();

        // iterate domains
        for (FsDomain domain : domains) {

            List<SearchRecord> records = searchAcross(domain.ScopId, searchType, acrossType);

            for (int i = 0; i < Math.min(RESULTS_DEPTH, records.size()); i++) {

                SearchRecord record = records.get(i);
                if (!domain.ScopId.equals(record.getDbId())) {

                    try {

                        TMAlign.Results results = Aligning.tmAlign(domain.ScopId, record.getDbId(), Mode.REGULAR);
                        if (getTmScore(searchType, results) > SIMILARITY_THRESHOLD) {

                            try {

                                Connection conn = ds.getConnection();
                                conn.setAutoCommit(true);

                                PreparedStatement updt = conn.prepareStatement(
                                        "INSERT INTO fs_sims (scop_id_1, scop_id_2, search_type, across_type, rmsd, tm_score) VALUES (?,?,?,?,?,?);"
                                        );

                                updt.setString(1, domain.ScopId);
                                updt.setString(2, record.getDbId());
                                updt.setString(3, searchType.toString());
                                updt.setString(4, acrossType.toString());
                                updt.setDouble(5, results.getRmsd());
                                updt.setDouble(6, getTmScore(searchType, results));

                                updt.execute();
                                updt.close();

                            } catch (SQLException e) {
                                Logger.getLogger(FindSimilar.class.getName()).log(Level.SEVERE, null, e);
                            }
                        }
                    }
                    catch (Exception e) {
                        Logger.getLogger(FindSimilar.class.getName()).log(Level.SEVERE, domain.ScopId + " - " + record.getDbId(), e);
                    }
                }
            }
        }
    }

    private static List<SearchRecord> searchAcross(String scopId, SearchType searchType, AcrossType acrossType) {

        ScopSearchCriteria criteria = new ScopSearchCriteria();
        
        criteria.searchBy = SearchBy.DB_ID;
        criteria.idDbType = DbType.SCOP;
        criteria.searchDbType = DbType.SCOP;
        criteria.dbId = scopId;
        criteria.limit = 100;
        criteria.searchType = searchType;
        criteria.searchMode = SearchMode.FAST;
        criteria.sortBy =  SortBy.SIMILARITY;
        criteria.differentFold = (acrossType == AcrossType.CF ? true : false);
        criteria.differentSuperfamily = (acrossType == AcrossType.SF ? true : false);
        criteria.differentFamily = false;

        ScopSearch scopSearch = new ScopSearch();

        List<SearchRecord> records = new ArrayList<SearchRecord>(); 
        try {

            records = scopSearch.search(criteria, SearchFrom.WEB);
        
        } catch (Exception e) {

            Logger.getLogger(FindSimilar.class.getName()).log(Level.SEVERE, null, e);
        }

        return records;
    }

    private static double getTmScore(SearchType searchType, TMAlign.Results results) {

        if (searchType == SearchType.FULL_LENGTH) 
            return results.getTmScoreAvg();
        else if (searchType == SearchType.CONTAINED_IN) 
            return results.getTmScoreQ();
        else  // SearchType.CONTAINS
            return results.getTmScoreT();
    }
    
    private static List<FsDomain> getDomains() {

        List<FsDomain> domains = new ArrayList<>();

        PGSimpleDataSource ds = Db.getDataSource();

        try {

            Connection conn = ds.getConnection();
       
            PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_fs_domains();");
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                FsDomain domain = new FsDomain();

                domain.ScopId = rs.getString("scop_id");
                domain.PdbId = rs.getString("pdb_id");
                domain.Cl = rs.getString("cl");
                domain.Cf = rs.getInt("cf");
                domain.Sf = rs.getInt("sf");
                domain.Fa = rs.getInt("fa");

                domains.add(domain);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            Logger.getLogger(FindSimilar.class.getName()).log(Level.SEVERE, null, e);
        }

        return domains;
    }
    
    public static List<FsSim> getSims(SearchType searchType, AcrossType acrossType) {

        List<FsSim> sims = new ArrayList<>();

        PGSimpleDataSource ds = Db.getDataSource();

        try {

            Connection conn = ds.getConnection();
       
            PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_fs_sims(?,?);");
            stmt.setString(1, searchType.toString());
            stmt.setString(2, acrossType.toString());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                FsSim sim = new FsSim();

                sim.N = rs.getInt("n");

                sim.ScopId1 = rs.getString("scop_id_1");
                sim.Sunid1 = rs.getInt("sunid_1");
                sim.ClCf1 = rs.getString("cl_cf_1");
                sim.CfDescr1 = rs.getString("cf_descr_1");
                sim.ClCfSf1 = rs.getString("cl_cf_sf_1");
                sim.SfDescr1 = rs.getString("sf_descr_1");
                
                sim.ScopId2 = rs.getString("scop_id_2");
                sim.Sunid2 = rs.getInt("sunid_2");
                sim.ClCf2 = rs.getString("cl_cf_2");
                sim.CfDescr2 = rs.getString("cf_descr_2");
                sim.ClCfSf2 = rs.getString("cl_cf_sf_2");
                sim.SfDescr2 = rs.getString("sf_descr_2");
                
                sim.TmScore = rs.getDouble("tm_score");

                sims.add(sim);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            Logger.getLogger(FindSimilar.class.getName()).log(Level.SEVERE, null, e);
        }

        return sims;
    }
}
