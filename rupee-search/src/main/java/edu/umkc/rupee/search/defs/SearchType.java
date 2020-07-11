package edu.umkc.rupee.search.defs;

public enum SearchType {

    FULL_LENGTH(1, "Full-Length"),
    CONTAINED_IN(2, "Contained In"),
    CONTAINS(3, "Contains"),
    RMSD(4, "RMSD"),
    Q_SCORE(5, "Q-Score"),
    SSAP_SCORE(6, "SSAP-Score");

    private int id;
    private String name;

    SearchType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static SearchType fromId(int id) {
        if (id == FULL_LENGTH.getId()) {
            return FULL_LENGTH;
        }
        else if (id == CONTAINED_IN.getId()) {
            return CONTAINED_IN;
        }
        else if (id == CONTAINS.getId()) {
            return CONTAINS;
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
