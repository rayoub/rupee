package edu.umkc.rupee.defs;

public enum SearchMode {

    FAST(1, "Fast"),
    TOP_ALIGNED(2, "Top-Aligned"),
    ALL_ALIGNED(3, "All-Aligned");

    private int id;
    private String name;

    SearchMode(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static SearchMode fromId(int id) {
        if (id == FAST.getId()) {
            return FAST;
        }
        else if (id == TOP_ALIGNED.getId()) {
            return TOP_ALIGNED;
        }
        else {
            return ALL_ALIGNED;
        }
    }
}
