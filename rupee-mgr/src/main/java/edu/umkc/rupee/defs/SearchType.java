package edu.umkc.rupee.defs;

public enum SearchType {

    FULL_LENGTH(1, "Full-Length"),
    CONTAINED_IN(2, "Contained In"),
    CONTAINS(3, "Contains"),
    RMSD(4, "RMSD");

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
        else {
            return RMSD;
        }
    }
}
