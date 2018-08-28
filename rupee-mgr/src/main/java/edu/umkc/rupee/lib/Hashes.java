package edu.umkc.rupee.lib;

public class Hashes {

    public String dbId;
    public String setId;
    public Integer[] minHashes;
    public Integer[] bandHashes;
    public long exactHash;

    public String getDbId() {
        return dbId;
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public Integer[] getMinHashes() {
        return minHashes;
    }

    public void setMinHashes(Integer[] minHashes) {
        this.minHashes = minHashes;
    }

    public Integer[] getBandHashes() {
        return bandHashes;
    }

    public void setBandHashes(Integer[] bandHashes) {
        this.bandHashes = bandHashes;
    }

    public long getExactHash() {
        return exactHash;
    }

    public void setExactHash(long exactHash) {
        this.exactHash = exactHash;
    }
}

