package edu.umkc.rupee.base;

import java.util.List;

public class SearchResults {
   
    private List<SearchRecord> records; 
    private boolean suggestAlternate;

    public List<SearchRecord> getRecords() {
        return records;
    }

    public void setRecords(List<SearchRecord> records) {
        this.records = records;
    }

    public boolean getSuggestAlternate() {
        return suggestAlternate;
    }

    public void setSuggestAlternate(boolean suggestAlternate) {
        this.suggestAlternate = suggestAlternate;
    }
}
