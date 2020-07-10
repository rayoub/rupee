package edu.umkc.rupee.base;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.defs.DbType;
import edu.umkc.rupee.lib.Constants;
import edu.umkc.rupee.lib.Db;
import edu.umkc.rupee.lib.FileUtils;
import edu.umkc.rupee.lib.Grams;
import edu.umkc.rupee.lib.Importing;
import edu.umkc.rupee.lib.Residue;

public abstract class Import {
    
    // *********************************************************************
    // Abstract Methods 
    // *********************************************************************
    
    public abstract DbType getDbType();

    // *********************************************************************
    // Instance Methods 
    // *********************************************************************

    public void importGrams() {

        IntStream.range(0, Constants.IMPORT_SPLIT_COUNT)
            .boxed()
            .parallel()
            .forEach(splitIndex -> importGramsSplit(splitIndex));
    }

    private void importGramsSplit(int splitIndex) {

        int processed = 0;

        PGSimpleDataSource ds = Db.getDataSource();

        try {

            Connection conn = ds.getConnection();
            conn.setAutoCommit(true);

            // *** get split

            DbType dbType = getDbType();

            System.out.println("Split: " + splitIndex + ", Getting " + dbType.getName() + " Ids to Import.");
                
            PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_" + dbType.getTableName() + "_split(?,?);");
            stmt.setInt(1, splitIndex);
            stmt.setInt(2, Constants.IMPORT_SPLIT_COUNT);
            
            ResultSet rs = stmt.executeQuery();

            System.out.println("Split: " + splitIndex + ", Got " + dbType.getName() + " Ids.");

            // *** iterate split
            
            while (rs.next()) {
               
                String dbId = "";
                String pdbId = "";

                try {
               
                    dbId = rs.getString("db_id");
                    pdbId = rs.getString("pdb_id");

                    String fileName = dbType.getImportPath() + dbId;
                    String fileNameWithExt = FileUtils.appendExt(fileName);
                    if (fileNameWithExt.isEmpty()) {
                        System.out.println("File not found for: " + dbId);
                        continue;
                    } 

                    PDBFileReader reader = new PDBFileReader();
                    reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);

                    InputStream inputStream = new FileInputStream(fileNameWithExt);
                    GZIPInputStream gzipInputStream = null; 

                    Structure structure = null;
                    if (fileNameWithExt.endsWith("gz")) {
                        gzipInputStream = new GZIPInputStream(inputStream);
                        structure = reader.getStructure(gzipInputStream); 
                    }
                    else {
                        structure = reader.getStructure(inputStream); 
                    }
                    structure.setPDBCode(pdbId);
                
                    List<Residue> residues = Importing.parseResidues(structure);
                    Grams grams = Grams.fromResidues(residues); 

                    saveGrams(dbId, grams, conn);

                    inputStream.close();
                    if (gzipInputStream != null) {
                        gzipInputStream.close();
                    }

                } catch (Exception e) {
                    Logger.getLogger(Import.class.getName()).log(Level.SEVERE, pdbId, e);
                }
                
                // output
                processed += 1;
                if (processed % Constants.PROCESSED_INCREMENT == 0) {
                    System.out.println("Split: " + splitIndex + ", Processed: "
                            + (Constants.PROCESSED_INCREMENT * (processed / Constants.PROCESSED_INCREMENT)));
                }
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void saveGrams(String dbId, Grams grams, Connection conn) throws SQLException {
        
        PreparedStatement updt = conn.prepareStatement("SELECT insert_" + getDbType().getTableName() + "_grams(?, ?, ?);");
        updt.setString(1, dbId);
        updt.setArray(2, conn.createArrayOf("INTEGER", grams.getGramsAsArray()));
        updt.setArray(3, conn.createArrayOf("NUMERIC", grams.getCoordsAsArray()));
        updt.execute();
        updt.close();
    }
}

