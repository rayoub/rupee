package edu.umkc.rupee.lib;

public class SM_96_90 {

    public static int DIM = 12;

    public static double pairCounts[][] = {

        //     1          2          3          4          5          6          7          8          9         10         11         12
        {  12827,         0,       119,         0,        11,         0,         0,       286,         0,        27,       654,        20},   // 1
        {      0,     59222,        88,         1,         0,         0,         0,        52,       500,        53,      2128,         1},   // 2 
        {    119,        88,  27543447,       605,        42,         0,        34,      9646,       499,     34667,    387630,       144},   // 3  
        {      0,         1,       605,     70415,        53,         0,         0,      2316,         9,        44,      4637,         0},   // 4 
        {     11,         0,        42,        53,  20363076,       209,      1323,    171450,        91,      3598,      1823,     13433},   // 5 
        {      0,         0,         0,         0,       209,    133040,         2,        40,       278,         0,        19,         0},   // 6 
        {      0,         0,        34,         0,      1323,         2,    452833,       135,         5,     16569,       155,         3},   // 7 
        {    286,        52,      9646,      2316,    171450,        40,       135,  16258092,     28110,    136803,     52601,     54521},   // 8 
        {      0,       500,       499,         9,        91,       278,         5,     28110,   1268428,      4699,     40407,       399},   // 9 
        {     27,        53,     34667,        44,      3598,         0,     16569,    136803,      4699,   4043158,    224623,      1163},   // 10 
        {    654,      2128,    387630,      4637,      1823,        19,       155,     52601,     40407,    224623,   8273147,      3920},   // 11 
        {     20,         1,       144,         0,     13433,         0,         3,     54521,       399,      1163,      3920,    769129}    // 12
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
