package edu.umkc.rupee.scop;

import edu.umkc.rupee.base.Hash;
import edu.umkc.rupee.lib.DbTypeCriteria;

public class ScopHash extends Hash {

    public DbTypeCriteria getDbType() {

        return DbTypeCriteria.SCOP;
    }
}

