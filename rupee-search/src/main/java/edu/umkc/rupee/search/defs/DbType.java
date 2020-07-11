package edu.umkc.rupee.search.defs;

import edu.umkc.rupee.search.lib.Constants;

public enum DbType {
   
    INVALID(-1, "Invalid", "invalid", ""),  
    DIR(0, "Directory", "dir", Constants.DIR_PATH),
    SCOP(1, "SCOPe", "scop", Constants.SCOP_PATH),
    CATH(2, "CATH", "cath", Constants.CATH_PATH),
    ECOD(3, "ECOD", "ecod", Constants.ECOD_PATH),
    CHAIN(4, "PDB Chains", "chain", Constants.CHAIN_PATH),
    UPLOAD(10, "Upload", "upload", Constants.UPLOAD_PATH);

    private int id;
    private String name;
    private String tableName;
    private String importPath;

    DbType(int id, String name, String tableName, String importPath) {
        this.id = id;
        this.name = name;
        this.tableName = tableName;
        this.importPath = importPath;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTableName() {
        return tableName;
    }

    public String getImportPath() {
        return importPath;
    }

    public static DbType fromId(int id) {
        if (id == DIR.getId()) {
            return DIR;
        }
        else if (id == SCOP.getId()) {
            return SCOP;
        }
        else if (id == CATH.getId()) {
            return CATH;
        }
        else if (id == ECOD.getId()) {
            return ECOD;
        }
        else if (id == CHAIN.getId()) {
            return CHAIN;
        }
        else if (id == UPLOAD.getId()) {
            return UPLOAD;
        }
        else {
            return INVALID;
        }
    }
}
