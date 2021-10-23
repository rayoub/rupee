package edu.umkc.rupee.search.lib;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.io.PDBFileReader;

import edu.umkc.rupee.search.defs.DbType;
import edu.umkc.rupee.tm.Kabsch;
import edu.umkc.rupee.tm.TmAlign;
import edu.umkc.rupee.tm.TmMode;
import edu.umkc.rupee.tm.TmResults;

public class Aligning {

    public static TmResults tmAlign(String dbId1, String dbId2, TmMode mode) {

        TmResults results = null;

        try {

            PDBFileReader reader = new PDBFileReader();
            reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);

            DbType dbType1 = DbId.getIdDbType(dbId1);
            DbType dbType2 = DbId.getIdDbType(dbId2);

            FileInputStream queryFile = new FileInputStream(dbType1.getImportPath() + dbId1 + ".pdb.gz");
            GZIPInputStream queryFileGz = new GZIPInputStream(queryFile);
            FileInputStream targetFile = new FileInputStream(dbType2.getImportPath() + dbId2 + ".pdb.gz");
            GZIPInputStream targetFileGz = new GZIPInputStream(targetFile);

            Structure queryStructure = reader.getStructure(queryFileGz);
            Structure targetStructure = reader.getStructure(targetFileGz);

            queryFileGz.close();
            queryFile.close();
            targetFileGz.close();
            targetFile.close();
            
            queryStructure.setName(dbId1);
            targetStructure.setName(dbId2);

            Kabsch kabsch = new Kabsch();
            TmAlign tm = new TmAlign(queryStructure, targetStructure, mode, kabsch);
            results = tm.align();

        } catch (IOException e) {
            Logger.getLogger(Aligning.class.getName()).log(Level.SEVERE, null, e);
        }

        return results;
    }

    public static TmResults tmAlign(int uploadId, String dbId, TmMode mode) {

        TmResults results = null;

        try {

            PDBFileReader reader = new PDBFileReader();
            reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);

            DbType dbType1 = DbType.UPLOAD; 
            DbType dbType2 = DbId.getIdDbType(dbId);

            FileInputStream queryFile = new FileInputStream(dbType1.getImportPath() + uploadId + ".pdb");
            FileInputStream targetFile = new FileInputStream(dbType2.getImportPath() + dbId + ".pdb.gz");
            GZIPInputStream targetFileGz = new GZIPInputStream(targetFile);

            Structure queryStructure = reader.getStructure(queryFile);
            Structure targetStructure = reader.getStructure(targetFileGz);
            
            queryFile.close();
            targetFileGz.close();
            targetFile.close();

            queryStructure.setName("upload");
            targetStructure.setName(dbId);

            Kabsch kabsch = new Kabsch();
            TmAlign tm = new TmAlign(queryStructure, targetStructure, mode, kabsch);
            results = tm.align();

        } catch (IOException e) {
            Logger.getLogger(Aligning.class.getName()).log(Level.SEVERE, null, e);
        }

        return results;
    }
}
