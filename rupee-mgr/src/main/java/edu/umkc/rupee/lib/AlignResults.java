package edu.umkc.rupee.lib;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.defs.AlignmentType;
import edu.umkc.rupee.defs.DbType;
import edu.umkc.rupee.tm.Kabsch;
import edu.umkc.rupee.tm.TmAlign;
import edu.umkc.rupee.tm.TmMode;
import edu.umkc.rupee.tm.TmResults;

public class AlignResults
{
    private static AtomicInteger counter;

    static {
        counter = new AtomicInteger();
    }
   
    public static void alignRupeeVsMtmResults() {

        String benchmark = "casp_d250";
        String version = "casp_chain_v01_01_2020";
        DbType dbType = DbType.CHAIN;
        int maxN = 100;

        alignRupeeResults(benchmark, version, dbType, maxN, "all_aligned", "rmsd");
        alignRupeeResults(benchmark, version, dbType, maxN, "top_aligned", "rmsd");
        alignRupeeResults(benchmark, version, dbType, maxN, "all_aligned", "contained_in");
        alignRupeeResults(benchmark, version, dbType, maxN, "top_aligned", "contained_in");
        
        alignMtmResults(benchmark, version, dbType, maxN); 
    }

    public static void alignRupeeVsCathedralResults() {

        String benchmark = "casp_d250";
        String version = "casp_cath_v4_2_0";
        DbType dbType = DbType.CATH;
        int maxN = 100;

        alignRupeeResults(benchmark, version, dbType, maxN, "all_aligned", "rmsd");
        alignRupeeResults(benchmark, version, dbType, maxN, "top_aligned", "rmsd");
        alignRupeeResults(benchmark, version, dbType, maxN, "all_aligned", "ssap_score");
        alignRupeeResults(benchmark, version, dbType, maxN, "top_aligned", "ssap_score");
        alignRupeeResults(benchmark, version, dbType, maxN, "all_aligned", "full_length");
        alignRupeeResults(benchmark, version, dbType, maxN, "top_aligned", "full_length");
        
        alignCathedralResults(benchmark, version, dbType, maxN); 
    }
    
    public static void alignRupeeVsSsmResults() {

        String benchmark = "casp_d250";
        String version = "casp_scop_v1_73";
        DbType dbType = DbType.SCOP;
        int maxN = 100;

        alignRupeeResults(benchmark, version, dbType, maxN, "all_aligned", "rmsd");
        alignRupeeResults(benchmark, version, dbType, maxN, "top_aligned", "rmsd");
        alignRupeeResults(benchmark, version, dbType, maxN, "all_aligned", "q_score");
        alignRupeeResults(benchmark, version, dbType, maxN, "top_aligned", "q_score");
        alignRupeeResults(benchmark, version, dbType, maxN, "all_aligned", "full_length");
        alignRupeeResults(benchmark, version, dbType, maxN, "top_aligned", "full_length");
        
        alignSsmResults(benchmark, version, dbType, maxN, "rmsd"); 
        alignSsmResults(benchmark, version, dbType, maxN, "q_score"); 
    }
   
    // RUPEE is already done for this vs. mTM above
    public static void alignVastResults() {

        String benchmark = "casp_vast_d55";
        String version = "casp_chain_v01_01_2020";
        DbType dbType = DbType.CHAIN;
        int maxN = 50;
        
        alignVastResults(benchmark, version, dbType, maxN); 
    }

    private static void alignRupeeResults(String benchmark, String version, DbType dbType, int maxN, String searchMode, String searchType) {

        List<String> dbIds = Benchmarks.get(benchmark);
            
        String command = "SELECT db_id_2 FROM rupee_result WHERE version = ? AND db_id_1 = ? AND n <= ? AND search_mode = ? AND search_type = ? ORDER BY n;";

        counter.set(0);
        dbIds.parallelStream().forEach(dbId -> alignResults(command, version, dbType, dbId, maxN, searchMode, searchType));
    }

    private static void alignMtmResults(String benchmark, String version, DbType dbType, int maxN) {

        List<String> dbIds = Benchmarks.get(benchmark);
            
        String command = "SELECT db_id_2 FROM mtm_result WHERE version = ? AND db_id_1 = ? AND n <= ? ORDER BY n;";

        counter.set(0);
        dbIds.parallelStream().forEach(dbId -> alignResults(command, version, dbType, dbId, maxN, "", ""));
    }

    private static void alignCathedralResults(String benchmark, String version, DbType dbType, int maxN) {

        List<String> dbIds = Benchmarks.get(benchmark);

        String command = "SELECT db_id_2 FROM cathedral_result WHERE version = ? AND db_id_1 = ? AND n <= ? ORDER BY n;";

        counter.set(0);
        dbIds.parallelStream().forEach(dbId -> alignResults(command, version, dbType, dbId, maxN, "", ""));
    }
    
    private static void alignSsmResults(String benchmark, String version, DbType dbType, int maxN, String searchType) {

        List<String> dbIds = Benchmarks.get(benchmark);

        String command = "SELECT db_id_2 FROM ssm_result WHERE version = ? AND db_id_1 = ? AND n <= ? AND search_type = ? ORDER BY n;";

        counter.set(0);
        dbIds.parallelStream().forEach(dbId -> alignResults(command, version, dbType, dbId, maxN, "", searchType));
    }

    private static void alignVastResults(String benchmark, String version, DbType dbType, int maxN) {

        List<String> dbIds = Benchmarks.get(benchmark);

        String command = "SELECT db_id_2 FROM vast_result WHERE version = ? AND db_id_1 = ? AND n <= ? ORDER BY n;";

        counter.set(0);
        dbIds.parallelStream().forEach(dbId -> alignResults(command, version, dbType, dbId, maxN, "", ""));
    }

    private static void alignResults(String command, String version, DbType dbType, String dbId, int maxN, String searchMode, String searchType) {

        try {
            
            Map<String, AlignmentScores> map = Db.getAlignmentScores(version, dbId);
            List<AlignmentScores> scores = new ArrayList<>();

            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareCall(command);

            stmt.setString(1, version);
            stmt.setString(2, dbId);
            stmt.setInt(3, maxN);
            if (!searchMode.isEmpty()) { // RUPEE
                stmt.setString(4, searchMode);
                stmt.setString(5, searchType);
            }
            else if (!searchType.isEmpty()) { // SSM
                stmt.setString(4, searchType);
            }

            ResultSet rs = stmt.executeQuery();

            PDBFileReader reader = new PDBFileReader();
            reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);

            Structure queryStructure = null;
            if (version.startsWith("casp")) {
                String path = Constants.CASP_PATH + dbId + ".pdb";
                FileInputStream queryFile = new FileInputStream(path);
                queryStructure = reader.getStructure(queryFile);
            }
            else {
                String path = dbType.getImportPath() + dbId + ".pdb.gz";
                FileInputStream queryFile = new FileInputStream(path);
                GZIPInputStream queryFileGz = new GZIPInputStream(queryFile);
                queryStructure = reader.getStructure(queryFileGz);
            }

            while (rs.next()) {

                String dbId2 = rs.getString("db_id_2");
            
                if (!map.containsKey(dbId2)) {

                    String path = dbType.getImportPath() + dbId2 + ".pdb.gz";
                    if (!Files.exists(Paths.get(path))) {
                        continue;
                    }
                    FileInputStream targetFile = new FileInputStream(path);
                    GZIPInputStream targetFileGz = new GZIPInputStream(targetFile);
                    Structure targetStructure = reader.getStructure(targetFileGz);
                    
                    // gather alignment scores 
                    AlignmentScores score = new AlignmentScores();
                    score.setVersion(version);
                    score.setDbId1(dbId);
                    score.setDbId2(dbId2);

                    // perform tm-align alignment
                    try {
                        Kabsch kabsch = new Kabsch();
                        TmAlign tm = new TmAlign(queryStructure, targetStructure, TmMode.REGULAR, kabsch);
                        TmResults results = tm.align();

                        score.setTmQTmScore(results.getTmScoreQ());
                        score.setTmAvgTmScore(results.getTmScoreAvg());
                        score.setTmRmsd(results.getRmsd());
                        score.setTmQScore(results.getQScore());
                    }
                    catch (RuntimeException e) {
                        System.out.println("TM-align error comparing: " + dbId + ", " + dbId2);
                        System.out.println(e.getMessage());
                    }

                    // perform CE alignments
                    try {

                        AlignRecord result = Aligning.align(queryStructure, targetStructure, AlignmentType.CE);
                        score.setCeRmsd(result.afps.getTotalRmsdOpt());
                    }
                    catch (Exception e) {
                        System.out.println("CE error comparing: " + dbId + ", " + dbId2);
                        System.out.println(e.getMessage());
                    }
                    
                    // perform FATCAT RIGID alignments
                    try {

                        AlignRecord result = Aligning.align(queryStructure, targetStructure, AlignmentType.FATCAT_RIGID);
                        score.setFatcatRigidRmsd(result.afps.getTotalRmsdOpt());
                    }
                    catch (Exception e) {
                        System.out.println("FATCAT RIGID error comparing: " + dbId + ", " + dbId2);
                        System.out.println(e.getMessage());
                    }

                    scores.add(score);
                }
            }

            rs.close();
            stmt.close();
            conn.close();

            if (scores.size() > 0) {
                Db.saveAlignmentScores(version, dbId, scores);
            }

            int count = counter.incrementAndGet();

            System.out.println("Processed Count: " + count);

        } catch (SQLException e) {
            Logger.getLogger(Aligning.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException e) {
            Logger.getLogger(Aligning.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
