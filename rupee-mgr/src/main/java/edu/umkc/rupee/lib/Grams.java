package edu.umkc.rupee.lib;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Grams {

    private Integer[] gramsAsArray;
    private List<Integer> gramsAsList;
    private Float[] coordsAsArray;
    private List<Float> coordsAsList;

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

    public Float[] getCoordsAsArray() {
        return coordsAsArray;
    }

    public void setCoordsAsArray(Float[] coordsAsArray) {
        this.coordsAsArray = coordsAsArray;
    }

    public List<Float> getCoordsAsList() {
        return coordsAsList;
    }

    public void setCoordsAsList(List<Float> coordsAsList) {
        this.coordsAsList = coordsAsList;
    }

    public int getLength() {
        return this.gramsAsArray.length;
    }

    public static Grams fromResidues(List<Residue> residues) {

        Grams grams = new Grams();

        List<Residue> filtered = residues.stream().filter(r -> r.getGram() > 0).collect(Collectors.toList());
        Integer[] gramsAsArray = filtered.stream().map(r -> r.getGram()).toArray(Integer[]::new);

        grams.setGramsAsArray(gramsAsArray);
        grams.setGramsAsList(Arrays.asList(gramsAsArray));

        int i = 0;
        Float[] coordsAsArray = new Float[filtered.size() * 3]; 
        for (Residue residue : filtered) {
            coordsAsArray[i] = residue.getX();
            coordsAsArray[i + 1] = residue.getY();
            coordsAsArray[i + 2] = residue.getZ();
            i += 3;
        }

        grams.setCoordsAsArray(coordsAsArray);
        grams.setCoordsAsList(Arrays.asList(coordsAsArray));

        return grams;
    }
    
    public static Grams fromResultSet(ResultSet rs) throws SQLException {

        Grams grams = new Grams();

        Integer[] gramsAsArray = (Integer[])rs.getArray("grams").getArray();
        grams.setGramsAsArray(gramsAsArray);
        grams.setGramsAsList(Arrays.asList(gramsAsArray));
       
        Float[] coordsAsArray = (Float[])rs.getArray("coords").getArray();
        grams.setCoordsAsArray(coordsAsArray);
        grams.setCoordsAsList(Arrays.asList(coordsAsArray));

        return grams;
    }
}

