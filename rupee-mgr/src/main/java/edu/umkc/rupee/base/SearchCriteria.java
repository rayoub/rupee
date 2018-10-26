package edu.umkc.rupee.base;

import edu.umkc.rupee.defs.DbTypeCriteria;
import edu.umkc.rupee.defs.ModeCriteria;
import edu.umkc.rupee.defs.SearchByCriteria;
import edu.umkc.rupee.defs.SortCriteria;

public class SearchCriteria {

    public DbTypeCriteria dbType;
    public SearchByCriteria searchBy;
    public DbTypeCriteria dbIdType;
    public String dbId;
    public int uploadId = -1;
    public int page;
    public int pageSize;
    public int limit;
    public ModeCriteria mode;
    public SortCriteria sort;
}

