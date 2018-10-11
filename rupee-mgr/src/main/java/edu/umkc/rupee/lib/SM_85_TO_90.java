package edu.umkc.rupee.lib;

public class SM_85_TO_90 {

    public static int DIM = 15;

    public static double pairCounts[][] = 
   /* 
    {

        //     1       2         3       4         5       6       7         8        9       10        11       12
        {  11515,      2,      590,      2,       23,      0,      0,      505,       5,     108,      778,      80},
        {      2,  51387,      603,      1,      172,      0,     10,      857,     755,     623,     2722,      34},
        {    590,    603,  6835644,   3038,     2276,      0,    414,    76914,    5460,   60172,   364620,    3528},
        {      2,      1,     3038,  16445,       89,      0,      0,     3728,      35,     156,     3370,      56},
        {     23,    172,     2276,     89,  4624774,    347,   3014,   184279,    2753,   14091,     6319,   27868},
        {      0,      0,        0,      0,      347,  14209,     73,      168,     458,     191,      439,       3},
        {      0,     10,      414,      0,     3014,     73,  96913,     1630,      93,    5042,      991,     439},
        {    505,    857,    76914,   3728,   184279,    168,   1630,  3781444,   34670,   97940,   115585,   34029},
        {      5,    755,     5460,     35,     2753,    458,     93,    34670,  228046,   11347,    44994,     397},
        {    108,    623,    60172,    156,    14091,    191,   5042,    97940,   11347,  830730,   132053,    4761},
        {    778,   2722,   364620,   3370,     6319,    439,    991,   115585,   44994,  132053,  1839890,    2663},
        {     80,     34,     3528,     56,    27868,      3,    439,    34029,     397,    4761,     2663,  139024}
    };
    */

    { 
        {  19741,      5,      788,      2,      35,      0,      0,     737,     24,     137,    279,    120,     643,     77,     86},
        {      5,  91636,      783,      1,     209,      0,     11,    1365,   1053,     779,    138,   2878,    1101,    211,     60},
        {    788,    783, 13426466,   4495,    2896,     31,    527,  142293,   8891,   99894,  26367,  38359,  615802,   1500,   5138},
        {      2,      1,     4495,  28173,     104,      0,      0,    5059,     51,     232,     75,    473,    1605,   2557,     43},
        {     35,    209,     2896,    104, 8607455,    445,   3464,  282509,   3952,   22022,   6127,   6980,   12046,   1605,  45604},
        {      0,      0,       31,      0,     445,  35226,     88,     225,    513,     107,      0,    136,      10,      0,     93},
        {      0,     11,      527,      0,    3464,     88, 166576,    2470,    126,    6752,     91,    403,     959,      8,    144},
        {    737,   1365,   142293,   5059,  282509,    225,   2470, 7107295,  61481,  176720,  30978,  62453,  132373,  10306,  61156},
        {     24,   1053,     8891,     51,    3952,    513,    126,   61481, 428528,   18669,   6683,  59106,   15401,   1271,    671},
        {    137,    779,    99894,    232,   22022,    107,   6752,  176720,  18669, 1519061,   8584,  27517,  192040,   2199,   7984},
        {    279,    138,    26367,     75,    6127,      0,     91,   30978,   6683,    8584, 313932,  11609,   77966,   3102,   1070},
        {    120,   2878,    38359,    473,    6980,    136,    403,   62453,  59106,   27517,  11609, 885712,  138669,  30933,   2780},
        {    643,   1101,   615802,   1605,   12046,     10,    959,  132373,  15401,  192040,  77966, 138669, 1923915,  16726,   3726},
        {     77,    211,     1500,   2557,    1605,      0,      8,   10306,   1271,    2199,   3102,  30933,   16726,  66075,    331},
        {     86,     60,     5138,     43,   45604,     93,    144,   61156,    671,    7984,   1070,   2780,    3726,    331, 250270}
    };

























    public static double descriptorCounts[];
    public static double descriptorProbs[];

    public static double totalPairCount;
    public static double totalDescriptorCount;
    
    public static double scores[][];

    static {

        double sum = 0;        
        for (int i = 0; i < DIM; i++) {
            for (int j = i; j < DIM; j++) {
                sum += 2.0 * pairCounts[i][j];
            }
        }

        totalPairCount = sum;
        totalDescriptorCount = sum / 2.0;

        descriptorCounts = new double[DIM];
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++) {
                descriptorCounts[i] += pairCounts[i][j];
            }
        }
        
        descriptorProbs = new double[DIM];
        for (int i = 0; i < DIM; i++) {
            descriptorProbs[i] = descriptorCounts[i] / totalPairCount;
        }

        scores = new double[DIM][DIM];
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++) {
                scores[i][j] = Math.round(Math.log((Math.max(1, pairCounts[i][j]) / totalPairCount) / (descriptorProbs[i] * descriptorProbs[j])));

                // cap extreme scores
                scores[i][j] = Math.min(5.0,scores[i][j]);
                scores[i][j] = Math.max(-5.0,scores[i][j]);
            }
        }

    }

    public static void printDescriptorCounts() {

        System.out.print("{ ");
        for (int i = 0; i < DIM; i++) {
            System.out.printf("%.0f", descriptorCounts[i]);
            if (i < DIM - 1) {
                System.out.print(", ");
            }
        }
        System.out.print("}\n");
    }
    
    public static void printDescriptorProbs() {

        System.out.print("{ ");
        for (int i = 0; i < DIM; i++) {
            System.out.printf("%1.4f", descriptorProbs[i]);
            if (i < DIM - 1) {
                System.out.print(", ");
            }
        }
        System.out.print("}\n");
    }

    public static void printScores() {

        for (int i = 0; i < DIM; i++) {
            System.out.print("{ ");
            for (int j = 0; j < DIM; j++) {
                System.out.printf("%3.0f", scores[i][j]);
                if (j < DIM - 1) {
                    System.out.print(", ");
                }
            }
            System.out.print("}\n");
        }
    }
}
