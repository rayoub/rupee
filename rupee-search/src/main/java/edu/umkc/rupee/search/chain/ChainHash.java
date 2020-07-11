package edu.umkc.rupee.search.chain;

import edu.umkc.rupee.search.base.Hash;
import edu.umkc.rupee.search.defs.DbType;

public class ChainHash extends Hash {

    public DbType getDbType() {

        return DbType.CHAIN;
    }
}

