package edu.umkc.rupee.base;

import edu.umkc.rupee.lib.DbTypeCriteria;
import edu.umkc.rupee.lib.ModeCriteria;
import edu.umkc.rupee.lib.SearchByCriteria;
import edu.umkc.rupee.lib.SortCriteria;

public class SearchCriteria {

    public DbTypeCriteria dbType;
    public SearchByCriteria searchBy;
    public DbTypeCriteria dbIdType;
    public String dbId = "UPLOAD";
    public int uploadId = -1;
    public int page;
    public int pageSize;
    public int limit;
    public ModeCriteria mode;
    public SortCriteria sort;
}

