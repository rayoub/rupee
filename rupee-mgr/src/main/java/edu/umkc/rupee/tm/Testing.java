package edu.umkc.rupee.tm;

import java.util.List;

import edu.umkc.rupee.lib.Aligning;
import edu.umkc.rupee.lib.AlignmentScores;
import edu.umkc.rupee.lib.Db;

public class Testing {

    public static void test() {

        List<AlignmentScores> list = Db.getAlignmentScores("scop_v2_07");

        list.stream().limit(1000).forEach(s -> {

            TmAlign.Results results = Aligning.tmAlign(s.getDbId1(), s.getDbId2(), TmMode.REGULAR);

            test(s, s.getTmAvgRmsd(), results.getRmsd());
            test(s, s.getTmAvgTmScore(), results.getTmScoreAvg());
            test(s, s.getTmQTmScore(), results.getTmScoreQ());
        });
    }

    public static void test(AlignmentScores s, double score1, double score2) {

        double diff = Math.abs(score1 - score2);
        if (diff > 0.00001) {
            System.out.println("Inconsistent");
            System.out.println(s.getDbId1() + ", " + s.getDbId2());
            System.out.println(score1 + " != " + score2 + " (" + diff + ")");
        }
    }
}
