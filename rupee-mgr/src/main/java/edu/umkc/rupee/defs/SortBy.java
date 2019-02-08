package edu.umkc.rupee.defs;

public enum SortBy {

    RMSD(1, "RMSD", false),
    TM_SCORE(2, "TM-Score", true),
    SIMILARITY(3, "Similarity", true);

    private int id;
    private String name;
    private boolean descending;

    SortBy(int id, String name, boolean descending) {
        this.id = id;
        this.name = name;
        this.descending = descending;
    }
    
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isDescending() {
        return descending;
    }
    
    public static SortBy fromId(int id) {
        if (id == RMSD.getId()) {
            return RMSD;
        }
        else if (id == TM_SCORE.getId()) {
            return TM_SCORE;
        }
        else {
            return SIMILARITY;
        }
    }
}
