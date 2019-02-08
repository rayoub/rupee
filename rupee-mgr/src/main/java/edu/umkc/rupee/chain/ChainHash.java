package edu.umkc.rupee.chain;

import edu.umkc.rupee.base.Hash;
import edu.umkc.rupee.defs.DbType;

public class ChainHash extends Hash {

    public DbType getDbType() {

        return DbType.CHAIN;
    }
}

