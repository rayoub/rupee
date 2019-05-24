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
            double diff = Math.abs(s.getTmAvgTmScore() - results.getTmScoreAvg());
            if (diff > 0.0) {
                System.out.println("Inconsistent");
                System.out.println(s.getDbId1() + ", " + s.getDbId2());
                System.out.println(s.getTmAvgTmScore() + " != " + results.getTmScoreAvg() + " (" + diff + ")");
            }
        });
    }
}
