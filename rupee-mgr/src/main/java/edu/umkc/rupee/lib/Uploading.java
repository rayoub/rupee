package edu.umkc.rupee.lib;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.base.Hash;

public class Uploading {

    private final static Pattern ATOM_PATTERN = Pattern.compile("^(ATOM|HETATM)", Pattern.MULTILINE);
    private final static Pattern ENDMDL_PATTERN = Pattern.compile("^ENDMDL", Pattern.MULTILINE);
    private final static Pattern BREAK_PATTERN = Pattern.compile("\\R", Pattern.MULTILINE);

    public static int upload(String content) throws IOException {

        content = preProcess(content);

        Integer[] grams = getGrams(content);
        Hashes hashes = getHashes(grams);
      
        int uploadId = saveGrams(grams);
        saveHashes(uploadId, hashes);
        saveToDisk(uploadId, content);

        return uploadId;
    }

    private static String preProcess(String pdbContent) {
       
        // chop off additional models 
        Matcher endModelMatcher = ENDMDL_PATTERN.matcher(pdbContent);
        if(endModelMatcher.find()){
           pdbContent = pdbContent.substring(0,endModelMatcher.start()-1);
        }

        // split lines and filter
        List<String> lines = Arrays.stream(BREAK_PATTERN.split(pdbContent)).filter(ATOM_PATTERN.asPredicate()).collect(Collectors.toList());
    
        // first chain id
        char chainId = lines.get(0).charAt(21); 

        // filter lines again based on chain id and join
        pdbContent = lines.stream().filter(l -> l.charAt(21) == chainId).collect(Collectors.joining(System.lineSeparator()));

        return pdbContent;
    }

    private static Integer[] getGrams(String content) throws IOException {
        
        // get structure 
        PDBFileReader reader = new PDBFileReader();
        InputStream stream = new ByteArrayInputStream(content.getBytes());
        Structure structure = reader.getStructure(stream);

        // get grams
        Integer[] grams = Importing.parseStructure(structure).stream().filter(r -> r.getGram() > 0).map(r -> r.getGram()).toArray(Integer[]::new);

        return grams;
    }

    private static Hashes getHashes(Integer[] grams) {
      
        // get hashes
        Hashes hashes = Hash.getHashes(grams); 

        return hashes;
    }
    
    public static int saveGrams(Integer[] grams) {
        
        int id = -1;

        Connection conn = null;
        CallableStatement stmt = null;
        
        try {
            
            PGSimpleDataSource ds = Db.getDataSource();

            conn = ds.getConnection();
            conn.setAutoCommit(false);
        
            stmt = conn.prepareCall("{ ? = call insert_upload_grams(?) }");

            stmt.registerOutParameter(1, Types.INTEGER); 
            stmt.setArray(2, conn.createArrayOf("INTEGER", grams));

            stmt.execute();

            id = stmt.getInt(1);

            conn.commit();

            stmt.close();
            conn.close();
        
        } catch (SQLException ex) {
            Logger.getLogger(Uploading.class.getName()).log(Level.SEVERE, null, ex);
        }

        return id;
    }

    private static void saveHashes(int uploadId, Hashes hashes) {

        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            
            PGSimpleDataSource ds = Db.getDataSource();

            conn = ds.getConnection();
            conn.setAutoCommit(false);
            
            stmt = conn.prepareStatement("SELECT insert_upload_hashes(?, ?, ?);");

            stmt.setInt(1, uploadId);
            stmt.setArray(2, conn.createArrayOf("INTEGER", hashes.getMinHashes()));
            stmt.setArray(3, conn.createArrayOf("INTEGER", hashes.getBandHashes()));

            stmt.execute();

            conn.commit();
            
            stmt.close();
            conn.close();
        
        } catch (SQLException ex) {
            Logger.getLogger(Uploading.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void saveToDisk(int uploadId, String content) {
            
        Path p = Paths.get(Constants.UPLOAD_PATH, uploadId + ".pdb");
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            byte[] bytes = content.getBytes();
            out.write(bytes, 0, bytes.length);
        }
        catch (IOException ex) {
            Logger.getLogger(Uploading.class.getName()).log(Level.SEVERE, null, ex);
        }
    }        
}
