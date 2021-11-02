package edu.umkc.rupee.search.afdb;

import edu.umkc.rupee.search.base.SearchRecord;

public class AfdbSearchRecord extends SearchRecord {

    private String proteomeId;
    private String uniprotId;
    private String species;
    private String commonName;

    public String getProteomeId() {
        return proteomeId;
    }

    public void setProteomeId(String proteomeId) {
        this.proteomeId = proteomeId;
    }
    
    public String getUniprotId() {
        return uniprotId;
    }

    public void setUniprotId(String uniprotId) {
        this.uniprotId = uniprotId;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }
}
