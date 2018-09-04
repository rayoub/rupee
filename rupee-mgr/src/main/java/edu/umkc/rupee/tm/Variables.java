package edu.umkc.rupee.tm;

public class Variables {

    // task all others may just be file io variables
    // the trick is to initialize the below

    public static double D0_MIN;                            //for d0
    public static double Lnorm;                             //normalization length
    public static double score_d8, d0, d0_search, dcu0;     //for TMscore search
    public static double score[][];                         // Input score table for dynamic programming
    public static boolean path[][];                         // for dynamic programming
    public static double val[][];                           // for dynamic programming
    public static int xlen, ylen, minlen;                   //length of proteins
    public static double xa[][], ya[][];                        //for input vectors xa[0...xlen-1][0..2], ya[0...ylen-1][0..2]
                                                            //in general, ya is regarded as native structure --> superpose xa onto ya
    public static double xtm[][], ytm[][];                  //for TMscore search engine
    public static double xt[][];                            //for saving the superposed version of r_1 or xtm
    public static int    secx[], secy[];                    //for the secondary structure 
    public static double r1[][], r2[][];                    //for Kabsch rotation 
    public static double t[];                               //Kabsch translation vector and rotation matrix
    public static double u[][];
}
