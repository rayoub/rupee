package edu.umkc.rupee.lib;

public class Hashes {

    public String dbId;
    public Integer[] minHashes;
    public Integer[] bandHashes;

    public Hashes() {}

    public Hashes(Integer[] minHashes, Integer[] bandHashes) {
        this.dbId = "";
        this.minHashes = minHashes;
        this.bandHashes = bandHashes;
    }

    public Hashes(String dbId, Integer[] minHashes, Integer[] bandHashes) {
        this.dbId = dbId;
        this.minHashes = minHashes;
        this.bandHashes = bandHashes;
    }

    public String getDbId() {
        return dbId;
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
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
}

