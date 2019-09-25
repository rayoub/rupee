package edu.umkc.rupee.base;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.defs.DbType;
import edu.umkc.rupee.lib.Constants;
import edu.umkc.rupee.lib.Db;
import edu.umkc.rupee.lib.Hashes;
import edu.umkc.rupee.lib.Hashing;

public abstract class Hash {

    // *********************************************************************
    // Abstract Methods 
    // *********************************************************************

    public abstract DbType getDbType();

    // *********************************************************************
    // Instance Methods 
    // *********************************************************************

    public void hash() {
            
        IntStream.range(0, Constants.IMPORT_SPLIT_COUNT)
            .boxed()
            .parallel()
            .forEach(splitIndex -> hashSplit(splitIndex));
    }

    public void hashSplit(int splitIndex) {
        
        int processed = 0;

        PGSimpleDataSource ds = Db.getDataSource();

        try {

            Connection conn = ds.getConnection();
            conn.setAutoCommit(true);

            // *** get split
            
            DbType dbType = getDbType();

            System.out.println("Split: " + splitIndex + ", Getting " + dbType.getName() + " Ids to Import.");
                
            PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_" + dbType.getTableName() + "_grams_split(?,?);");
            stmt.setInt(1, splitIndex);
            stmt.setInt(2, Constants.IMPORT_SPLIT_COUNT);
            
            ResultSet rs = stmt.executeQuery();

            System.out.println("Split: " + splitIndex + ", Got " + dbType.getName() + " Ids.");

            // *** iterate split
    
            while (rs.next()) {

                String dbId = rs.getString("db_id");
                Integer[] grams = (Integer[])rs.getArray("grams").getArray();

                if (grams.length >= Constants.MIN_GRAM_COUNT) {

                    Hashes hashes = getHashes(grams);

                    PreparedStatement updt = conn.prepareStatement("SELECT insert_" + getDbType().getTableName() + "_hashes(?, ?, ?);");
                    updt.setString(1, dbId);
                    updt.setArray(2, conn.createArrayOf("INTEGER", hashes.getMinHashes()));
                    updt.setArray(3, conn.createArrayOf("INTEGER", hashes.getBandHashes()));

                    updt.execute();
                    updt.close();
                }

                // output
                processed += 1;
                if (processed % Constants.PROCESSED_INCREMENT == 0) {
                    System.out.println("Split: " + splitIndex + ", Processed: "
                            + (Constants.PROCESSED_INCREMENT * (processed / Constants.PROCESSED_INCREMENT)));
                }

            } // iterate pdb ids

            rs.close();
            stmt.close();
            conn.close();
        
        } catch (SQLException e) {
            Logger.getLogger(Hash.class.getName()).log(Level.SEVERE, null, e);
        } catch (NullPointerException e) {
            Logger.getLogger(Hash.class.getName()).log(Level.WARNING, null, e);
        }
    }
    
    // *********************************************************************
    // Static Methods 
    // *********************************************************************
    
    public static Hashes getHashes(Integer[] grams) {

        Map<Integer, Integer> gramMap = new HashMap<>();
        for(int i = 0; i < grams.length; i++) {

            Integer gram = grams[i];                        

            if (gramMap.containsKey(gram)) {
                gramMap.replace(gram, gramMap.get(gram) + 1);
            } else {
                gramMap.put(gram, 1);
            }
        }

        Integer minHashes[] = Hashing.getMinHashes(gramMap);
        Integer bandHashes[] = Hashing.getBandHashes(minHashes);

        Hashes hashes = new Hashes();
        hashes.setMinHashes(minHashes);
        hashes.setBandHashes(bandHashes);

        return hashes;
    }
}

