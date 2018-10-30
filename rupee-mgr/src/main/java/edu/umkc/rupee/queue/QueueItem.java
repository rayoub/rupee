package edu.umkc.rupee.queue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class QueueItem {

    private String userId;
    private String searchHash;
    private int dbType;
    private int searchFilter;
    private int searchBy;
    private String dbId;
    private int uploadId;
    private int sortBy;
    private int maxRecords;
    private String status;
    private Timestamp insertedOn;

    public QueueItem() { }

    public QueueItem(ResultSet rs) throws SQLException {

        this.userId = rs.getString("user_id");
        this.searchHash = rs.getString("search_hash");
        this.dbType = rs.getInt("db_type");
        this.searchFilter = rs.getInt("search_filter");
        this.searchBy = rs.getInt("search_by");
        this.dbId = rs.getString("db_id");
        this.uploadId = rs.getInt("upload_id");
        this.sortBy = rs.getInt("sort_by");
        this.maxRecords = rs.getInt("max_records");
        this.status = rs.getString("status");
        this.insertedOn = (Timestamp)rs.getObject("inserted_on");
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSearchHash() {
        return searchHash;
    }

    public void setSearchHash(String searchHash) {
        this.searchHash = searchHash;
    }

    public int getDbType() {
        return dbType;
    }

    public void setDbType(int dbType) {
        this.dbType = dbType;
    }

    public int getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(int searchFilter) {
        this.searchFilter = searchFilter;
    }

    public int getSearchBy() {
        return searchBy;
    }

    public void setSearchBy(int searchBy) {
        this.searchBy = searchBy;
    }

    public String getDbId() {
        return dbId;
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }

    public int getUploadId() {
        return uploadId;
    }

    public void setUploadId(int uploadId) {
        this.uploadId = uploadId;
    }

    public int getSortBy() {
        return sortBy;
    }

    public void setSortBy(int sortBy) {
        this.sortBy = sortBy;
    }

    public int getMaxRecords() {
        return maxRecords;
    }

    public void setMaxRecords(int maxRecords) {
        this.maxRecords = maxRecords;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getInsertedOn() {
        return insertedOn;
    }

    public void setInsertedOn(Timestamp insertedOn) {
        this.insertedOn = insertedOn;
    }
}
