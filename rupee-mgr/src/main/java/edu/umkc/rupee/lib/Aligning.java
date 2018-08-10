package edu.umkc.rupee.lib;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.align.StructureAlignment;
import org.biojava.nbio.structure.align.StructureAlignmentFactory;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.util.AFPChainScorer;
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.io.PDBFileReader;

public class Aligning
{
    public static AlignRecord align(String dbId1, String dbId2, AlignCriteria align, DbTypeCriteria dbType) {

        AlignRecord record = null;

        try {

            PDBFileReader reader = new PDBFileReader();
            reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);

            FileInputStream queryFile = new FileInputStream(dbType.getImportPath() + dbId1 + ".pdb.gz");
            GZIPInputStream queryFileGz = new GZIPInputStream(queryFile);
            FileInputStream targetFile = new FileInputStream(dbType.getImportPath() + dbId2 + ".pdb.gz");
            GZIPInputStream targetFileGz = new GZIPInputStream(targetFile);

            Structure queryStructure = reader.getStructure(queryFileGz);
            Structure targetStructure = reader.getStructure(targetFileGz);

            record = align(queryStructure, targetStructure, align);

        } catch (IOException e) {
            Logger.getLogger(Aligning.class.getName()).log(Level.SEVERE, null, e);
        } catch (StructureException e) {
            Logger.getLogger(Aligning.class.getName()).log(Level.SEVERE, null, e);
        } 

        return record;
    }
    
    public static AlignRecord align(Structure queryStructure, Structure targetStructure, AlignCriteria align) 
        throws IOException, StructureException {

        Atom[] atoms1 = StructureTools.getAtomCAArray(queryStructure);
        Atom[] atoms2 = StructureTools.getAtomCAArray(targetStructure);

        StructureAlignment algorithm = StructureAlignmentFactory.getAlgorithm(align.getAlgorithmName());
        AFPChain afps = algorithm.align(atoms1, atoms2, align.getParams());
        
        afps.setName1(queryStructure.getName());
        afps.setName2(targetStructure.getName());

        afps.setTMScore(AFPChainScorer.getTMScore(afps, atoms1, atoms2));

        AlignRecord record = new AlignRecord();
        record.algorithmName = align.getAlgorithmName();
        record.afps = afps;
        record.atoms1 = atoms1;
        record.atoms2 = atoms2;

        return record;
    }
/*
    public static void batchAlignCathResults() {

        try {

            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);
            
            PreparedStatement stmt = conn.prepareCall(
                    "WITH results AS (SELECT ROW_NUMBER() OVER (ORDER BY ssap_score DESC) AS n, cath_id_1, cath_id_2 FROM cath_result) " + 
                    "SELECT * FROM results WHERE n <= ? ORDER BY n;");
            
            stmt.setInt(1, 50);

            ResultSet rs = stmt.executeQuery();

            PreparedStatement updt = conn.prepareStatement(
                    "UPDATE cath_result SET cecp_rmsd = ?, cecp_tm_score = ?, fatcat_rmsd = ?, fatcat_tm_score = ? " +
                    "WHERE cath_id_1 = ? AND cath_id_2 = ?;");

            PDBFileReader reader = new PDBFileReader();
            reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);

            while (rs.next()) {

                String cathId1 = rs.getString("cath_id_1");
                String cathId2 = rs.getString("cath_id_2");
                
                Structure queryStructure = reader.getStructure(Constants.PDB_PATH + cathId1 + ".pdb");
                Structure structure = reader.getStructure(Constants.PDB_PATH + cathId2 + ".pdb");
                
                AlignRecord cecp = Aligning.align(queryStructure, structure, AlignCriteria.CECP);
                AlignRecord fatcat = Aligning.align(queryStructure, structure, AlignCriteria.FATCAT_FLEXIBLE);

                updt.setDouble(1,cecp.afps.getTotalRmsdOpt());
                updt.setDouble(2,cecp.afps.getTMScore());
                updt.setDouble(3,fatcat.afps.getTotalRmsdOpt());
                updt.setDouble(4,fatcat.afps.getTMScore());
                updt.setString(5,cathId1);
                updt.setString(6,cathId2);

                updt.execute();
            }

            conn.commit();
            
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            Logger.getLogger(Aligning.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException e) {
            Logger.getLogger(Aligning.class.getName()).log(Level.SEVERE, null, e);
        } catch (StructureException e) {
            Logger.getLogger(Aligning.class.getName()).log(Level.SEVERE, null, e);
        } 
    }
    */
}
