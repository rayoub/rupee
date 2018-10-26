package edu.umkc.rupee.defs;

public enum SortCriteria {

    RMSD(1, false),
    TM_SCORE(2, true),
    SIMILARITY(3, true);

    private int id;
    private boolean descending;

    SortCriteria(int id, boolean descending) {
        this.id = id;
        this.descending = descending;
    }
    
    public int getId() {
        return id;
    }

    public boolean isDescending() {
        return descending;
    }
    
    public static SortCriteria fromId(int id) {
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
