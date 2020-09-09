package edu.umkc.rupee.eval.lib;

public class Constants {

    public final static String APP_NAME = "RUPEE";

    public final static String DB_NAME = "rupee";
   
    public final static String DB_USER = "ayoub";
    public final static String DB_PASSWORD = "ayoub";
   
    public final static String HOME_DIR = "/home/ayoub/";
    public final static String RESULTS_DIR = HOME_DIR + "git/rupee/eval/results/";
    public final static String DATA_DIR = HOME_DIR + "git/rupee/data/";

    public final static String DOWNLOAD_PATH = HOME_DIR + "Downloads/";

    public final static String SSM_PATH = RESULTS_DIR + "ssm/temp/";
    public final static String MTM_PATH = RESULTS_DIR + "mtm/temp/";
    public final static String CATHEDRAL_PATH = RESULTS_DIR + "cathedral/temp/";
    public final static String VAST_PATH = RESULTS_DIR + "vast/";
    public final static String VAST_PATH_RMSD 
        = VAST_PATH + "casp_d250_casp_chain_v08_28_2020_rmsd/";
    public final static String VAST_PATH_VAST_SCORE 
        = VAST_PATH + "casp_d250_casp_chain_v08_28_2020_full_length/";
    
    public final static String CASP_PATH = DATA_DIR + "casp/eu_preds/";
}

