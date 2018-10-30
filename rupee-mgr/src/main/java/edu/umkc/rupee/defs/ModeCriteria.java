package edu.umkc.rupee.defs;

public enum ModeCriteria {

    FAST(1, "Fast", 8000, 8000, 0),
    TOP_ALIGNED(2, "Top-Aligned", 40000, 2000, 400);

    private int id;
    private String name;
    private int lshCandidateCount;
    private int lcsCandidateCount;
    private int algCandidateCount;

    ModeCriteria(int id, String name, int lshCandidateCount, int lcsCandidateCount, int algCandidateCount) {
        this.id = id;
        this.name = name;
        this.lshCandidateCount = lshCandidateCount;
        this.lcsCandidateCount = lcsCandidateCount;
        this.algCandidateCount = algCandidateCount;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLshCandidateCount() {
        return lshCandidateCount;
    }

    public int getLcsCandidateCount() {
        return lcsCandidateCount;
    }

    public int getAlgCandidateCount() {
        return algCandidateCount;
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
