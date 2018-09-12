package edu.umkc.rupee.lib;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
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
    private static final int MAX_N = 100;

    private static AtomicInteger counter;

    static {
        counter = new AtomicInteger();
    }

    public static void alignRupeeResults(String benchmark, String version, DbTypeCriteria dbType) {

        List<String> dbIds = Benchmarks.get(benchmark);

        counter.set(0);
        dbIds.parallelStream().forEach(dbId -> alignRupeeResults(version, dbType, dbId));
    }

    public static void alignRupeeResults(String version, DbTypeCriteria dbType, String dbId) {

        try {

            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn
                    .prepareCall("SELECT * FROM rupee_result WHERE version = ? AND db_id_1 = ? AND n <= ? ORDER BY n;");

            stmt.setString(1, version);
            stmt.setString(2, dbId);
            stmt.setInt(3, MAX_N);

            ResultSet rs = stmt.executeQuery();

            PDBFileReader reader = new PDBFileReader();
            reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);

            FileInputStream queryFile = new FileInputStream(dbType.getImportPath() + dbId + ".pdb.gz");
            GZIPInputStream queryFileGz = new GZIPInputStream(queryFile);
            Structure queryStructure = reader.getStructure(queryFileGz);

            while (rs.next()) {

                String dbId2 = rs.getString("db_id_2");

                FileInputStream targetFile = new FileInputStream(dbType.getImportPath() + dbId2 + ".pdb.gz");
                GZIPInputStream targetFileGz = new GZIPInputStream(targetFile);
                Structure targetStructure = reader.getStructure(targetFileGz);

                AlignRecord ce = Aligning.align(queryStructure, targetStructure, AlignCriteria.CE);

                PreparedStatement updt = conn.prepareStatement("UPDATE rupee_result SET ce_rmsd = ?, ce_tm_score = ? "
                        + "WHERE version = ? AND db_id_1 = ? AND db_id_2 = ?;");

                updt.setDouble(1, ce.afps.getTotalRmsdOpt());
                updt.setDouble(2, ce.afps.getTMScore());
                updt.setString(3, version);
                updt.setString(4, dbId);
                updt.setString(5, dbId2);

                updt.execute();
            }

            conn.commit();

            rs.close();
            stmt.close();
            conn.close();

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

    public static void alignMtmDomResults(String benchmark, String version, DbTypeCriteria dbType) {

        List<String> dbIds = Benchmarks.get(benchmark);

        counter.set(0);
        dbIds.parallelStream().forEach(dbId -> alignMtmDomResults(version, dbType, dbId));
    }

    public static void alignMtmDomResults(String version, DbTypeCriteria dbType, String dbId) {

        try {

            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareCall(
                    "SELECT * FROM mtm_dom_result_matched WHERE version = ? AND db_id_1 = ? AND n <= ? AND ce_tm_score = -1 ORDER BY n;");

            stmt.setString(1, version);
            stmt.setString(2, dbId);
            stmt.setInt(3, MAX_N);

            ResultSet rs = stmt.executeQuery();

            PDBFileReader reader = new PDBFileReader();
            reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);

            FileInputStream queryFile = new FileInputStream(dbType.getImportPath() + dbId + ".pdb.gz");
            GZIPInputStream queryFileGz = new GZIPInputStream(queryFile);
            Structure queryStructure = reader.getStructure(queryFileGz);

            while (rs.next()) {

                String dbId2 = rs.getString("db_id_2");

                FileInputStream targetFile = new FileInputStream(dbType.getImportPath() + dbId2 + ".pdb.gz");
                GZIPInputStream targetFileGz = new GZIPInputStream(targetFile);
                Structure targetStructure = reader.getStructure(targetFileGz);

                AlignRecord ce = Aligning.align(queryStructure, targetStructure, AlignCriteria.CE);

                PreparedStatement updt = conn
                        .prepareStatement("UPDATE mtm_dom_result_matched SET ce_rmsd = ?, ce_tm_score = ? "
                                + "WHERE version = ? AND db_id_1 = ? AND db_id_2 = ?;");

                updt.setDouble(1, ce.afps.getTotalRmsdOpt());
                updt.setDouble(2, ce.afps.getTMScore());
                updt.setString(3, version);
                updt.setString(4, dbId);
                updt.setString(5, dbId2);

                updt.execute();
            }

            conn.commit();

            rs.close();
            stmt.close();
            conn.close();

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
