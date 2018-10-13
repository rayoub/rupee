package edu.umkc.rupee.ndd;

import org.postgresql.util.PGobject;
import org.postgresql.util.PGtokenizer;

public class NddSet extends PGobject {

    private String pivotDbId;
    private String memberDbId;
    private double similaritiy;

    public String getPivotDbId() {
        return pivotDbId;
    }

    public void setPivotDbId(String pivotDbId) {
        this.pivotDbId = pivotDbId;
    }

    public String getMemberDbId() {
        return memberDbId;
    }

    public void setMemberDbId(String memberDbId) {
        this.memberDbId = memberDbId;
    }

    public double getSimilaritiy() {
        return similaritiy;
    }

    public void setSimilaritiy(double similarity) {
        this.similaritiy = similarity;
    }
    
    @Override
    public void setValue(String s) {

        //remove parens and tokenize
        PGtokenizer t = new PGtokenizer(PGtokenizer.removePara(s), ',');

        pivotDbId = t.getToken(0);
        memberDbId = t.getToken(1);
        similaritiy = Double.parseDouble(t.getToken(2));
    }

    @Override
    public String getValue() {
        return "(" + pivotDbId + "," + memberDbId + "," + similaritiy + ")";
    }
}
