package edu.umkc.rupee.defs;

public enum AcrossType {

    CF(1, "Folds"),
    SF(2, "Superfamilies");
    
    private int id;
    private String name;

    AcrossType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static AcrossType fromId(int id) {
        if (id == CF.getId()) {
            return CF;
        }
        else {
            return SF;
        }
    }
}
