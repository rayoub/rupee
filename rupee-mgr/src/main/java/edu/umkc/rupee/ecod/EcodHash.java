package edu.umkc.rupee.ecod;

import edu.umkc.rupee.base.Hash;
import edu.umkc.rupee.defs.DbTypeCriteria;

public class EcodHash extends Hash {

    public DbTypeCriteria getDbType() {

        return DbTypeCriteria.ECOD;
    }
}

