package edu.umkc.rupee.lib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umkc.rupee.defs.DbType;

public class DbId {

    private static Pattern SCOP_PATTERN = Pattern.compile("d[1-9][a-z0-9]{3}[_a-z0-9\\.][_a-z1-9]", Pattern.CASE_INSENSITIVE);     // maxlen = 7
    private static Pattern CATH_PATTERN = Pattern.compile("[1-9][a-z0-9]{3}[a-z1-9][0-9]{2}", Pattern.CASE_INSENSITIVE);    // maxlen = 7
    private static Pattern ECOD_PATTERN = Pattern.compile("e[1-9][a-z0-9]{3}[a-z1-9]+[0-9]+", Pattern.CASE_INSENSITIVE);    // maxlen = 12 (fudge)
    private static Pattern CHAIN_PATTERN = Pattern.compile("[1-9][a-z0-9]{3}[a-z1-9]", Pattern.CASE_INSENSITIVE);           // maxlen = 5
   
    public static boolean isScopId(String id) {

        Matcher m = SCOP_PATTERN.matcher(id);
        return m.matches();
    }

    public static boolean isCathId(String id) {

        Matcher m = CATH_PATTERN.matcher(id);
        return m.matches();
    } 
    
    public static boolean isEcodId(String id) {

        Matcher m = ECOD_PATTERN.matcher(id);
        return m.matches();
    } 

    public static boolean isChainId(String id) {

        Matcher m = CHAIN_PATTERN.matcher(id);
        return m.matches();
    }

    public static DbType getDbIdType(String id) {

        if (isScopId(id)) {
            return DbType.SCOP;
        }
        else if (isCathId(id)) {
            return DbType.CATH;
        }
        else if (isEcodId(id)) {
            return DbType.ECOD;
        }
        else {
            return DbType.CHAIN;
        }
    }
    
    public static String getNormalizedId(String id) {

        if (isScopId(id)) {
            return id.toLowerCase();
        }
        else if (isCathId(id)) {
            return id.substring(0,4).toLowerCase() + id.substring(4,id.length()).toUpperCase();
        }
        else if (isEcodId(id)) {
            return id.substring(0,5).toLowerCase() + id.substring(5,id.length()).toUpperCase();
        }
        else if (isChainId(id)) {
            return id.substring(0,4).toLowerCase() + id.substring(4,id.length()).toUpperCase();
        }
        else {
            return id;
        }
    }
}
