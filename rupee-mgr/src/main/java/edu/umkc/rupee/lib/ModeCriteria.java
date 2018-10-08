package edu.umkc.rupee.lib;

public enum ModeCriteria {

    REGULAR(40000),
    FAST(8000);

    private int maxCandidateCount;

    ModeCriteria(int maxCandidateCount) {
        this.maxCandidateCount = maxCandidateCount;
    }

    public int getMaxCandidateCount() {
        return maxCandidateCount;
    }

    public void setMaxCandidateCount(int maxCandidateCount) {
        this.maxCandidateCount = maxCandidateCount;
    }
}
