package edu.umkc.rupee.lib;

import java.io.FileInputStream;
import java.io.IOException;
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

    public static void alignRupeeResults(String benchmark, String version, String searchMode, DbType dbType, int maxN) {

        List<String> dbIds = Benchmarks.get(benchmark);
            
        String command = "SELECT db_id_2 FROM rupee_result WHERE version = ? AND search_mode = ? AND db_id_1 = ? AND n <= ? ORDER BY n;";

        counter.set(0);
        dbIds.parallelStream().forEach(dbId -> alignResults(command, version, searchMode, dbType, dbId, maxN));
    }

    public static void alignMtmResults(String benchmark, String version, DbType dbType, int maxN) {

        List<String> dbIds = Benchmarks.get(benchmark);
            
        String command = "SELECT db_id_2 FROM mtm_result_matched WHERE version = ? AND db_id_1 = ? AND n <= ? ORDER BY n;";

        counter.set(0);
        dbIds.parallelStream().forEach(dbId -> alignResults(command, version, "", dbType, dbId, maxN));
    }

    public static void alignCathedralResults(String benchmark, String version, DbType dbType, int maxN) {

        List<String> dbIds = Benchmarks.get(benchmark);

        String command = "SELECT db_id_2 FROM cathedral_result WHERE version = ? AND db_id_1 = ? AND n <= ? ORDER BY n;";

        counter.set(0);
        dbIds.parallelStream().forEach(dbId -> alignResults(command, version, "", dbType, dbId, maxN));
    }
    
    public static void alignSsmResults(String benchmark, String version, DbType dbType, int maxN) {

        List<String> dbIds = Benchmarks.get(benchmark);

        String command = "SELECT db_id_2 FROM ssm_result WHERE version = ? AND db_id_1 = ? AND n <= ? ORDER BY n;";

        counter.set(0);
        dbIds.parallelStream().forEach(dbId -> alignResults(command, version, "", dbType, dbId, maxN));
    }

    private static void alignResults(String command, String version, String searchMode, DbType dbType, String dbId, int maxN) {

        try {
            
            Map<String, AlignmentScores> map = Db.getAlignmentScores(version, dbId);
            List<AlignmentScores> scores = new ArrayList<>();

            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareCall(command);

            if (searchMode.isEmpty()) {

                stmt.setString(1, version);
                stmt.setString(2, dbId);
                stmt.setInt(3, maxN);
            }
            else {

                stmt.setString(1, version);
                stmt.setString(2, searchMode);
                stmt.setString(3, dbId);
                stmt.setInt(4, maxN);
            }

            ResultSet rs = stmt.executeQuery();

            PDBFileReader reader = new PDBFileReader();
            reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);

            Structure queryStructure = null;
            if (dbId.startsWith("T0")) {
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

                    FileInputStream targetFile = new FileInputStream(dbType.getImportPath() + dbId2 + ".pdb.gz");
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

                        score.setTmQRmsd(results.getRmsd());
                        score.setTmQTmScore(results.getTmScoreQ());
                        score.setTmAvgRmsd(results.getRmsd());
                        score.setTmAvgTmScore(results.getTmScoreAvg());
                    }
                    catch (RuntimeException e) {
                        System.out.println("error comparing: " + dbId + ", " + dbId2);
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
