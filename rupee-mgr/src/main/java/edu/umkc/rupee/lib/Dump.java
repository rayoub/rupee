package edu.umkc.rupee.lib;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.defs.DbType;
import edu.umkc.rupee.tm.TmAlign;
import edu.umkc.rupee.tm.TmResults;

public class Dump {

    public static void debugMtmResults() {

        try {
            
            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);
            
            String command = "SELECT db_id_1, db_id_2, mtm_tm_score FROM mtm_result_matched WHERE version = 'casp_chain_v06_26_2019' AND n = 1 ORDER BY db_id_1;";
            PreparedStatement stmt = conn.prepareCall(command);
            
            PDBFileReader reader = new PDBFileReader();
            reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                String dbId1 = rs.getString("db_id_1");
                String dbId2 = rs.getString("db_id_2");
                double mtmScore = rs.getDouble("mtm_tm_score");
                    
                FileInputStream file1 = new FileInputStream(Constants.CASP_PATH + dbId1 + ".pdb");
                Structure structure1 = reader.getStructure(file1);

                FileInputStream file2 = new FileInputStream(DbType.CHAIN.getImportPath() + dbId2 + ".pdb.gz");
                GZIPInputStream fileGz2 = new GZIPInputStream(file2);
                Structure structure2 = reader.getStructure(fileGz2);
                    
                // perform tm-align alignment
                try {
                    TmAlign tm = new TmAlign(structure1, structure2);
                    TmResults results = tm.align();

                    if (Math.abs(mtmScore - results.getTmScoreQ()) > 0.1) {
                        System.out.println(dbId1 + ", " + dbId2);
                        System.out.println(mtmScore + ", \t" + results.getTmScoreQ() + dbId1 + ", " + dbId2);
                    }
                }
                catch (RuntimeException e) {
                    System.out.println("error comparing: " + dbId1 + ", " + dbId2);
                }
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            Logger.getLogger(Aligning.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException e) {
            Logger.getLogger(Aligning.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
