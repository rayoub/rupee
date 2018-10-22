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
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.postgresql.ds.PGSimpleDataSource;

public class AlignResults
{
    private static AtomicInteger counter;

    static {
        counter = new AtomicInteger();
    }

    public static void alignRupeeResults(String benchmark, String version, String sort, DbTypeCriteria dbType, int maxN) {

        List<String> dbIds = Benchmarks.get(benchmark);
            
        String command = "SELECT db_id_2 FROM rupee_result WHERE version = ? AND sort = ? AND db_id_1 = ? AND n <= ? ORDER BY n;";

        counter.set(0);
        dbIds.parallelStream().forEach(dbId -> alignResults(command, version, sort, dbType, dbId, maxN));
    }

    public static void alignMtmDomResults(String benchmark, String version, DbTypeCriteria dbType, int maxN) {

        List<String> dbIds = Benchmarks.get(benchmark);
            
        String command = "SELECT db_id_2 FROM mtm_dom_result_matched WHERE version = ? AND db_id_1 = ? AND n <= ? ORDER BY n;";

        counter.set(0);
        dbIds.parallelStream().forEach(dbId -> alignResults(command, version, "", dbType, dbId, maxN));
    }

    public static void alignCathedralResults(String benchmark, String version, DbTypeCriteria dbType, int maxN) {

        List<String> dbIds = Benchmarks.get(benchmark);

        String command = "SELECT db_id_2 FROM cathedral_result WHERE version = ? AND db_id_1 = ? AND n <= ? ORDER BY n;";

        counter.set(0);
        dbIds.parallelStream().forEach(dbId -> alignResults(command, version, "", dbType, dbId, maxN));
    }
    
    public static void alignSsmResults(String benchmark, String version, DbTypeCriteria dbType, int maxN) {

        List<String> dbIds = Benchmarks.get(benchmark);

        String command = "SELECT db_id_2 FROM ssm_result WHERE version = ? AND db_id_1 = ? AND n <= ? ORDER BY n;";

        counter.set(0);
        dbIds.parallelStream().forEach(dbId -> alignResults(command, version, "", dbType, dbId, maxN));
    }

    private static void alignResults(String command, String version, String sort, DbTypeCriteria dbType, String dbId, int maxN) {

        try {
            
            Map<String, AlignmentScores> map = Db.getAlignmentScores(version, dbId);
            List<AlignmentScores> scores = new ArrayList<>();

            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareCall(command);

            if (sort.isEmpty()) {

                stmt.setString(1, version);
                stmt.setString(2, dbId);
                stmt.setInt(3, maxN);
            }
            else {

                stmt.setString(1, version);
                stmt.setString(2, sort);
                stmt.setString(3, dbId);
                stmt.setInt(4, maxN);
            }

            ResultSet rs = stmt.executeQuery();

            PDBFileReader reader = new PDBFileReader();
            reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);

            FileInputStream queryFile = new FileInputStream(dbType.getImportPath() + dbId + ".pdb.gz");
            GZIPInputStream queryFileGz = new GZIPInputStream(queryFile);
            Structure queryStructure = reader.getStructure(queryFileGz);

            while (rs.next()) {

                String dbId2 = rs.getString("db_id_2");
                
                if (!map.containsKey(dbId2)) {

                    FileInputStream targetFile = new FileInputStream(dbType.getImportPath() + dbId2 + ".pdb.gz");
                    GZIPInputStream targetFileGz = new GZIPInputStream(targetFile);
                    Structure targetStructure = reader.getStructure(targetFileGz);

                    AlignRecord ce = Aligning.align(queryStructure, targetStructure, AlignCriteria.CE);
                    AlignRecord fatcat = Aligning.align(queryStructure, targetStructure, AlignCriteria.FATCAT_FLEXIBLE);

                    AlignmentScores score = new AlignmentScores();

                    score.setVersion(version);
                    score.setDbId1(dbId);
                    score.setDbId2(dbId2);
                    score.setCeRmsd(ce.afps.getTotalRmsdOpt());
                    score.setCeTmScore(ce.afps.getTMScore());
                    score.setFatCatRmsd(fatcat.afps.getTotalRmsdOpt());
                    score.setFatCatTmScore(fatcat.afps.getTMScore());

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
        } catch (StructureException e) {
            Logger.getLogger(Aligning.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
