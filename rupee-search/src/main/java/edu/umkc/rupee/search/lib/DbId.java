package edu.umkc.rupee.search.lib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.search.defs.DbType;

public class DbId {

    private static Pattern SCOP_PATTERN = Pattern.compile("d[1-9][a-z0-9]{3}[_a-z0-9\\.][_a-z1-9]", Pattern.CASE_INSENSITIVE);      // maxlen = 7
    private static Pattern CATH_PATTERN = Pattern.compile("[1-9][a-z0-9]{3}[a-z1-9][0-9]{2}", Pattern.CASE_INSENSITIVE);            // maxlen = 7
    private static Pattern CHAIN_PATTERN = Pattern.compile("[1-9][a-z0-9]{3}[a-z0-9]+", Pattern.CASE_INSENSITIVE);                  // maxlen = 12 (fudge)
    private static Pattern AFDB_PATTERN = Pattern.compile("AF\\-[a-z0-9\\-]+\\-model_v2", Pattern.CASE_INSENSITIVE);                // maxlen = 26
   
    private static boolean isScopId(String id) {

        Matcher m = SCOP_PATTERN.matcher(id);
        return m.matches();
    }

    private static boolean isCathId(String id) {

        Matcher m = CATH_PATTERN.matcher(id);
        return m.matches();
    } 
    
    private static boolean isChainId(String id) {

        Matcher m = CHAIN_PATTERN.matcher(id);
        return m.matches();
    }

    private static boolean isAfdbId(String id) {

        Matcher m = AFDB_PATTERN.matcher(id);
        return m.matches();
    }

    public static DbType getIdDbType(String id) {

        if (isScopId(id)) {
            return DbType.SCOP;
        }
        else if (isCathId(id)) {
            // CATH takes precedence over CHAIN
            return DbType.CATH;
        }
        else if (isChainId(id)) {
            return DbType.CHAIN;
        }
        else if (isAfdbId(id)) {
            return DbType.AFDB;
        }
        else {
            return DbType.INVALID;
        }
    }
    
    public static String getNormalizedId(String id) {

        String normalizedId = id;
        DbType dbType = getIdDbType(id);
    
        switch (dbType) {

            case SCOP:
                normalizedId = id.toLowerCase();
                break;
            case CATH:
                normalizedId = id.substring(0,4).toLowerCase() + id.substring(4,id.length());
                break;
            case CHAIN:
                normalizedId = id.substring(0,4).toLowerCase() + id.substring(4,id.length());
                break;
            case AFDB:
                normalizedId = id.substring(0,id.length() - "-model_v1".length()).toUpperCase() + "-model_v1";
                break;
            default:
                normalizedId = id;
        }

        return normalizedId;
    }
    
    public static String getAlternateId(String id) {
        
        String alternateId = id;
        DbType dbType = getIdDbType(id);
        String suffix = "";
    
        switch (dbType) {

            case CATH:
                suffix = id.substring(4, id.length());
                if (isStringLowerCase(suffix)) {
                    suffix = suffix.toUpperCase();
                }
                else {
                    suffix = suffix.toLowerCase();
                }
                alternateId = id.substring(0,4).toLowerCase() + suffix; 
                break;
            case CHAIN:
                suffix = id.substring(4, id.length());
                if (isStringLowerCase(suffix)) {
                    suffix = suffix.toUpperCase();
                }
                else {
                    suffix = suffix.toLowerCase();
                }
                alternateId = id.substring(0,4).toLowerCase() + suffix;
                break;
            default:
                alternateId = id;
        }

        return alternateId;
    }

    private static boolean isStringLowerCase(String str){
        
        // convert String to char array
        char[] charArray = str.toCharArray();
        
        for(int i = 0; i < charArray.length; i++){
            
            // if any character is not in lower case, return false
            if(!Character.isDigit(charArray[i]) && !Character.isLowerCase(charArray[i]))
                return false;
        }
        
        return true;
    }

    public static boolean doesIdExist(String id) {

        String normalizeId = getNormalizedId(id);
        DbType dbType = getIdDbType(id);

        if (dbType == DbType.INVALID) {
            return false; 
        }

        boolean exists = false;
        PGSimpleDataSource ds = Db.getDataSource();
        try {

            Connection conn = ds.getConnection();
            conn.setAutoCommit(true);
                
            PreparedStatement stmt = conn.prepareCall("SELECT db_id FROM " + dbType.getTableName() + "_hashes WHERE db_id = ?;");
            stmt.setString(1, normalizeId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                exists = true;
            }

            rs.close();
            stmt.close();
            conn.close();
        
        } catch (Exception e) {
            Logger.getLogger(DbId.class.getName()).log(Level.SEVERE, null, e);
        }

        return exists;
    }
}
