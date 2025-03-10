package edu.umkc.rupee.search.lib;

public class Constants {

    public final static String APP_NAME = "RUPEE";

    public final static String DB_NAME = "rupee";
   
    // *** LOCAL ONLY CONSTANTS  
    
    public final static String DB_USER = "postgres";
    public final static String DB_PASSWORD = "postgres";

    public final static String DATA_PATH = "D:/git/rupee/data/";

    public final static String CHAIN_PATH = DATA_PATH + "chain/";
    public final static String SCOP_PATH = DATA_PATH + "scop/";
    public final static String CATH_PATH = DATA_PATH + "cath/";
    public final static String AFDB_PATH = DATA_PATH + "afdb/";
    public final static String DIR_PATH = DATA_PATH + "dir/";
    
    public final static String UPLOAD_PATH = DATA_PATH + "upload/";
    
    public final static String CHAIN_PDB_PATH = CHAIN_PATH + "pdb/";
    public final static String SCOP_PDB_PATH = SCOP_PATH + "pdb/";
    public final static String CATH_PDB_PATH = CATH_PATH + "pdb/"; 
    public final static String AFDB_PDB_PATH = AFDB_PATH + "pdb/";

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
    public final static int SEARCH_SPLIT_COUNT = 12;
    public final static int PROCESSED_INCREMENT = 500;
}

