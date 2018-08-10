package edu.umkc.rupee.lib;

public enum SearchByCriteria {
    
    DB_ID(1),
    UPLOAD(2);

    private int id;

    SearchByCriteria(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
    
    public static SearchByCriteria fromId(int id) {
        if (id == DB_ID.getId()) {
            return DB_ID;
        }
        else {
            return UPLOAD;
        }
    }
}
