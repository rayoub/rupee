package edu.umkc.rupee.search.defs;

public enum SortBy {

    SIMILARITY(1, "Similarity", true),
    RMSD(2, "RMSD", false),
    TM_SCORE(3, "TM-Score", true),
    Q_SCORE(4, "Q-Score", true),
    SSAP_SCORE(5, "SSAP-Score", true);

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
        if (id == SIMILARITY.getId()) {
            return SIMILARITY;
        }
        else if (id == TM_SCORE.getId()) {
            return TM_SCORE;
        }
        else if (id == RMSD.getId()) {
            return RMSD;
        }
        else if (id == Q_SCORE.getId()) {
            return Q_SCORE;
        }
        else {
            return SSAP_SCORE;
        }
    }
}
