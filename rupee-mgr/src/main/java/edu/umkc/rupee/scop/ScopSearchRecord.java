package edu.umkc.rupee.scop;

import edu.umkc.rupee.base.SearchRecord;

public class ScopSearchRecord extends SearchRecord {

    private int sunid;
    private String cl;
    private int cf;
    private int sf;
    private int fa;
    private String cfDescription;
    private String sfDescription;
    private String faDescription;

    public ScopSearchRecord(String scopId, String pdbId, double similarity) {
        super(scopId, pdbId, similarity);
    }

    public int getSunid() {
        return sunid;
    }

    public void setSunid(int sunid) {
        this.sunid = sunid;
    }

    public String getCl() {
        return cl;
    }

    public void setCl(String cl) {
        this.cl = cl;
    }
    
    public int getCf() {
        return cf;
    }

    public void setCf(int fold) {
        this.cf = fold;
    }

    public int getSf() {
        return sf;
    }

    public void setSf(int superFamily) {
        this.sf = superFamily;
    }

    public int getFa() {
        return fa;
    }

    public void setFa(int family) {
        this.fa = family;
    }

    public String getCfDescription() {
        return cfDescription;
    }

    public void setCfDescription(String cfDescription) {
        this.cfDescription = cfDescription;
    }

    public String getSfDescription() {
        return sfDescription;
    }

    public void setSfDescription(String cfSfDescription) {
        this.sfDescription = cfSfDescription;
    }

    public String getFaDescription() {
        return faDescription;
    }

    public void setFaDescription(String cfSfFaDescription) {
        this.faDescription = cfSfFaDescription;
    }
}
