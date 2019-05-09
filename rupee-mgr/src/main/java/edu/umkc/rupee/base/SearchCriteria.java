package edu.umkc.rupee.base;

import java.util.EnumSet;

import edu.umkc.rupee.defs.DbType;
import edu.umkc.rupee.defs.SearchBy;
import edu.umkc.rupee.defs.SearchMode;
import edu.umkc.rupee.defs.SearchType;
import edu.umkc.rupee.defs.SortBy;

public class SearchCriteria {

    public DbType searchDbType;
    public SearchBy searchBy;
    public DbType idDbType;
    public String dbId;
    public int uploadId = -1;
    public int limit;
    public SearchMode searchMode;
    public EnumSet<SearchType> searchTypes;
    public SortBy sortBy;
}

