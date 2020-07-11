package edu.umkc.rupee.search.base;

import edu.umkc.rupee.search.defs.DbType;
import edu.umkc.rupee.search.defs.SearchBy;
import edu.umkc.rupee.search.defs.SearchMode;
import edu.umkc.rupee.search.defs.SearchType;
import edu.umkc.rupee.search.defs.SortBy;

public class SearchCriteria {

    public DbType searchDbType;
    public SearchBy searchBy;
    public DbType idDbType;
    public String dbId;
    public int uploadId = -1;
    public int limit;
    public SearchMode searchMode;
    public SearchType searchType;
    public SortBy sortBy;
}

