package edu.umkc.rupee.eval.auto;

public class VastResults {

    public String ResultsRmsd = "";
    public String ResultsVastScore = "";

    public boolean isEmpty() {

        if (ResultsRmsd.isEmpty() || ResultsVastScore.isEmpty()) {
            return true;
        }
        return false;
    }
}
