package edu.umkc.rupee.lib;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class Grams {

    private Integer[] gramsAsArray;
    private List<Integer> gramsAsList;

    public Integer[] getGramsAsArray() {
        return gramsAsArray;
    }

    public void setGramsAsArray(Integer[] gramsAsArray) {
        this.gramsAsArray = gramsAsArray;
    }

    public List<Integer> getGramsAsList() {
        return gramsAsList;
    }

    public void setGramsAsList(List<Integer> gramsAsList) {
        this.gramsAsList = gramsAsList;
    }

    public int getLength() {
        return this.gramsAsArray.length;
    }

    public static Grams fromResidues(List<Residue> residues) {

        Grams grams = new Grams();

        Integer[] gramsAsArray = residues.stream().filter(r -> r.getGram() > 0).map(r -> r.getGram()).toArray(Integer[]::new);
        grams.setGramsAsArray(gramsAsArray);
        grams.setGramsAsList(Arrays.asList(gramsAsArray));

        return grams;
    }
    
    public static Grams fromResultSet(ResultSet rs) throws SQLException {

        Grams grams = new Grams();

        Integer[] gramsAsArray = (Integer[])rs.getArray("grams").getArray();
        grams.setGramsAsArray(gramsAsArray);
        grams.setGramsAsList(Arrays.asList(gramsAsArray));

        return grams;
    }
}

