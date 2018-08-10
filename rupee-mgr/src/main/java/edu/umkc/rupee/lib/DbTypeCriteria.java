package edu.umkc.rupee.lib;

public enum DbTypeCriteria {
    
    SCOP(1, "Scop", Constants.SCOP_PATH),
    CATH(2, "Cath", Constants.CATH_PATH),
    ECOD(3, "Ecod", Constants.ECOD_PATH),
    CHAIN(4, "Chain", Constants.CHAIN_PATH);

    private int id;
    private String description;
    private String importPath;

    DbTypeCriteria(int id, String description, String importPath) {
        this.id = id;
        this.description = description;
        this.importPath = importPath;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getImportPath() {
        return importPath;
    }

    public static DbTypeCriteria fromId(int id) {
        if (id == SCOP.getId()) {
            return SCOP;
        }
        else if (id == CATH.getId()) {
            return CATH;
        }
        else if (id == ECOD.getId()) {
            return ECOD;
        }
        else {
            return CHAIN;
        }
    }
}
