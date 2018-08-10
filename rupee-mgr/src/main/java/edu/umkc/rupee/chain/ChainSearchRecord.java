package edu.umkc.rupee.chain;

import edu.umkc.rupee.base.SearchRecord;

public class ChainSearchRecord extends SearchRecord {

    public ChainSearchRecord(String chainId, String pdbId, double similarity) {
        super(chainId, pdbId, similarity);
    }
}
