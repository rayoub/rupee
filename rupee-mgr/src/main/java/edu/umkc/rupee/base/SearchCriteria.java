package edu.umkc.rupee.base;

import edu.umkc.rupee.lib.AlignCriteria;
import edu.umkc.rupee.lib.DbTypeCriteria;
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
    public AlignCriteria align;
    public SortCriteria sort;
}

