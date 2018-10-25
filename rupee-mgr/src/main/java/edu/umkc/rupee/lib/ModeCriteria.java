package edu.umkc.rupee.lib;

public enum ModeCriteria {

    FAST(1,8000, 8000, 0),
    TOP_ALIGNED(2,40000, 2000, 400);

    private int id;
    private int lshCandidateCount;
    private int lcsCandidateCount;
    private int algCandidateCount;

    ModeCriteria(int id, int lshCandidateCount, int lcsCandidateCount, int algCandidateCount) {
        this.id = id;
        this.lshCandidateCount = lshCandidateCount;
        this.lcsCandidateCount = lcsCandidateCount;
        this.algCandidateCount = algCandidateCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public static ModeCriteria fromId(int id) {
        if (id == FAST.getId()) {
            return FAST;
        }
        else {
            return TOP_ALIGNED;
        }
    }
}
