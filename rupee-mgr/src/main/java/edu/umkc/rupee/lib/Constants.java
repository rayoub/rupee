package edu.umkc.rupee.lib;

public class Constants {

    public final static String APP_NAME = "RUPEE";

    public final static String DB_NAME = "rupee";
   
    public final static String DB_USER = "ayoub";
    public final static String DB_PASSWORD = "ayoub";

    //public final static String DB_USER = "ec2-user";
    //public final static String DB_PASSWORD = "ec2-user";
    
    public final static String SCOP_PATH = "/home/ayoub/git/rupee/data/scop/pdb/";
    public final static String CATH_PATH = "/home/ayoub/git/rupee/data/cath/pdb/";
    public final static String ECOD_PATH = "/home/ayoub/git/rupee/data/ecod/pdb/";
    public final static String CHAIN_PATH = "/home/ayoub/git/rupee/data/chain/pdb/";
    public final static String UPLOAD_PATH = "/home/ayoub/git/rupee/data/upload/";

    //public final static String SCOP_PATH = "/home/ec2-user/data/scop/pdb/";
    //public final static String CATH_PATH = "/home/ec2-user/data/cath/pdb/";
    //public final static String ECOD_PATH = "/home/ec2-user/data/ecod/pdb/";
    //public final static String CHAIN_PATH = "/home/ec2-user/data/chain/pdb/";
    //public final static String UPLOAD_PATH = "/home/ec2-user/data/upload/";

    public final static String PDB_PATH = "/home/ayoub/git/rupee/data/pdb/pdb/";
    public final static String OBSOLETE_PATH = "/home/ayoub/git/rupee/data/pdb/obsolete/";
    
    public final static String DOWNLOAD_PATH = "/home/ayoub/Downloads/";
    public final static String SSM_PATH = "/home/ayoub/git/rupee/results/ssm/temp/";
    public final static String MTM_PATH = "/home/ayoub/git/rupee/results/mtm/temp/";
    public final static String CATHEDRAL_PATH = "/home/ayoub/git/rupee/results/cathedral/temp/";
    
    public final static String CASP_PATH = "/home/ayoub/git/rupee/data/casp/eu_preds/";
    
    public final static int MIN_GRAM_COUNT = 10;
    public final static int MIN_HASH_COUNT = 99;
    public final static int BAND_HASH_COUNT = 33;
    public final static int BAND_CHECK_COUNT = BAND_HASH_COUNT;
    
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

    public final static int FETCH_SIZE = 1000;

    public final static int NULL_INT = -9999;
}

