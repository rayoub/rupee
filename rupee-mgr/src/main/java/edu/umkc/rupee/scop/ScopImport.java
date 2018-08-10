package edu.umkc.rupee.scop;

import edu.umkc.rupee.base.Import;
import edu.umkc.rupee.lib.DbTypeCriteria;

public class ScopImport extends Import {

    public DbTypeCriteria getDbType() {

        return DbTypeCriteria.SCOP;
    }
}
