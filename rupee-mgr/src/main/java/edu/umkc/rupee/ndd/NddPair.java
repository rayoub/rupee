package edu.umkc.rupee.ndd;

import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;

public class NddPair extends PGobject {

    private int sid = 0;
    private String dbId1;
    private String dbId2;
    private double similarity;

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public String getDbId1() {
        return dbId1;
    }

    public void setDbId1(String dbId1) {
        this.dbId1 = dbId1;
    }

    public String getDbId2() {
        return dbId2;
    }

    public void setDbId2(String dbId2) {
        this.dbId2 = dbId2;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    @Override
    public void setValue(String s) {

        //remove parens and tokenize
        PGtokenizer t = new PGtokenizer(PGtokenizer.removePara(s), ',');

        sid = Integer.parseInt(t.getToken(1));
        dbId1 = t.getToken(2); 
        dbId2 = t.getToken(3);
        similarity = Double.parseDouble(t.getToken(4));
    }

    @Override
    public String getValue() {
        return "(" + sid + "," + dbId1 + "," + dbId2 + "," + similarity + ")";
    }
}
