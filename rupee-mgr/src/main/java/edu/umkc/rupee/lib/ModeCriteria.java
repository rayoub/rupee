package edu.umkc.rupee.lib;

public enum ModeCriteria {

    TOP_ALIGNED(40000, 2000, 400),
    FAST(8000, 8000, 0);

    private int lshCandidateCount;
    private int lcsCandidateCount;
    private int algCandidateCount;

    ModeCriteria(int lshCandidateCount, int lcsCandidateCount, int algCandidateCount) {
        this.lshCandidateCount = lshCandidateCount;
        this.lcsCandidateCount = lcsCandidateCount;
        this.algCandidateCount = algCandidateCount;
    }

    public int getLshCandidateCount() {
        return lshCandidateCount;
    }

    public void setLshCandidateCount(int maxCandidateCount) {
        this.lshCandidateCount = maxCandidateCount;
    }

    public int getLcsCandidateCount() {
        return lcsCandidateCount;
    }

    public void setLcsCandidateCount(int lcsCandidateCount) {
        this.lcsCandidateCount = lcsCandidateCount;
    }

    public int getAlgCandidateCount() {
        return algCandidateCount;
    }

    public void setAlgCandidateCount(int algCandidateCount) {
        this.algCandidateCount = algCandidateCount;
    }
}
