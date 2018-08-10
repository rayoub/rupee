package edu.umkc.rupee.cath;

import edu.umkc.rupee.base.Hash;
import edu.umkc.rupee.lib.DbTypeCriteria;

public class CathHash extends Hash {

    public DbTypeCriteria getDbType() {

        return DbTypeCriteria.CATH;
    }
}

