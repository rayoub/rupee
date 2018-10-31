package edu.umkc.rupee.queue;

import java.util.List;

import edu.umkc.rupee.base.SearchRecord;

public class SearchQueueResult {

    private QueueItem queueItem;
    private List<SearchRecord> searchRecords;

    public SearchQueueResult() { }

    public SearchQueueResult(QueueItem queueItem, List<SearchRecord> searchRecords) {

        this.queueItem = queueItem;
        this.searchRecords = searchRecords;
    }

    public QueueItem getQueueItem() {
        return queueItem;
    }

    public void setQueueItem(QueueItem queueItem) {
        this.queueItem = queueItem;
    }

    public List<SearchRecord> getSearchRecords() {
        return searchRecords;
    }

    public void setSearchRecords(List<SearchRecord> searchRecords) {
        this.searchRecords = searchRecords;
    }
}
