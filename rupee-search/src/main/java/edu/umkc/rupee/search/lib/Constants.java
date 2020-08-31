package edu.umkc.rupee.search.lib;

public class Constants {

    public final static String APP_NAME = "RUPEE";

    public final static String DB_NAME = "rupee";
   
    // *** LOCAL ONLY CONSTANTS  
    
    public final static String DB_USER = "ayoub";
    public final static String DB_PASSWORD = "ayoub";

    public final static String DATA_PATH = "/home/ayoub/git/rupee/data/";
    
    // *** SERVER ONLY CONSTANTS  
    
    //public final static String DB_USER = "ec2-user";
    //public final static String DB_PASSWORD = "ec2-user";
    //
    //public final static String DATA_PATH = "/home/ec2-user/data/";
  
    // *** END 

    public final static String CHAIN_PATH = DATA_PATH + "chain/";
    public final static String SCOP_PATH = DATA_PATH + "scop/";
    public final static String CATH_PATH = DATA_PATH + "cath/";
    public final static String ECOD_PATH = DATA_PATH + "ecod/";
    
    public final static String CHAIN_PDB_PATH = CHAIN_PATH + "pdb/";
    public final static String SCOP_PDB_PATH = SCOP_PATH + "pdb/";
    public final static String CATH_PDB_PATH = CATH_PATH + "pdb/"; 
    public final static String ECOD_PDB_PATH = ECOD_PATH + "pdb/";

    public final static String UPLOAD_PATH = DATA_PATH + "upload/";

    public final static String DIR_PATH = "";

    public final static String PDB_PATH = DATA_PATH + "pdb/";
    public final static String PDB_PDB_PATH = PDB_PATH + "pdb/";
    public final static String PDB_OBSOLETE_PATH = PDB_PATH + "obsolete/";
    public final static String PDB_BUNDLE_PATH = PDB_PATH + "chopped/"; 
    
    public final static int MIN_GRAM_COUNT = 10;
    public final static int MIN_HASH_COUNT = 99;
    public final static int BAND_HASH_COUNT = 33;
    
    public final static double SIMILARITY_THRESHOLD = 0.10;

    public final static int DEC_POW_1 = 10;
    public final static int DEC_POW_2 = DEC_POW_1 * DEC_POW_1;
    public final static int DEC_POW_3 = DEC_POW_1 * DEC_POW_2;
    public final static int DEC_POW_4 = DEC_POW_1 * DEC_POW_3;
    public final static int DEC_POW_5 = DEC_POW_1 * DEC_POW_4;
    public final static int DEC_POW_6 = DEC_POW_1 * DEC_POW_5;

    public final static int IMPORT_SPLIT_COUNT = 8;
    public final static int SEARCH_SPLIT_COUNT = 8;
    public final static int PROCESSED_INCREMENT = 500;
}

