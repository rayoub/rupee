package edu.umkc.rupee.defs;

public enum SearchMode {

    FAST(1, "Fast", 40000),
    TOP_ALIGNED(2, "Top-Aligned", 40000);

    private int id;
    private String name;
    private int lshCandidateCount;

    SearchMode(int id, String name, int lshCandidateCount) {
        this.id = id;
        this.name = name;
        this.lshCandidateCount = lshCandidateCount;
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

    public static SearchMode fromId(int id) {
        if (id == FAST.getId()) {
            return FAST;
        }
        else {
            return TOP_ALIGNED;
        }
    }
}