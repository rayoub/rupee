package edu.umkc.rupee.lib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DbId {

    private static Pattern SCOP_PATTERN = Pattern.compile("d[a-z0-9]{4}[a-z][_1-9]", Pattern.CASE_INSENSITIVE);
    private static Pattern CATH_PATTERN = Pattern.compile("[a-z0-9]{4}[a-z][0-9]{2}", Pattern.CASE_INSENSITIVE);
    private static Pattern ECOD_PATTERN = Pattern.compile("e[a-z0-9]{4}[a-z]+[0-9]+", Pattern.CASE_INSENSITIVE);
    private static Pattern CHAIN_PATTERN = Pattern.compile("[a-z0-9]{4}[a-z]", Pattern.CASE_INSENSITIVE); 
   
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

    public static DbTypeCriteria getDbIdType(String id) {

        if (isScopId(id)) {
            return DbTypeCriteria.SCOP;
        }
        else if (isCathId(id)) {
            return DbTypeCriteria.CATH;
        }
        else if (isEcodId(id)) {
            return DbTypeCriteria.ECOD;
        }
        else {
            return DbTypeCriteria.CHAIN;
        }
    }
}
