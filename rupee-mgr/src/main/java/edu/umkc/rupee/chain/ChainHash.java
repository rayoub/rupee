package edu.umkc.rupee.chain;

import edu.umkc.rupee.base.Hash;
import edu.umkc.rupee.defs.DbTypeCriteria;

public class ChainHash extends Hash {

    public DbTypeCriteria getDbType() {

        return DbTypeCriteria.CHAIN;
    }
}

