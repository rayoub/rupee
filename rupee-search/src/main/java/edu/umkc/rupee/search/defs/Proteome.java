package edu.umkc.rupee.search.defs;

public enum Proteome {
    
    ALL(0,"All Proteomes"),
    UP000006548(1,"Arabidopsis thaliana"),
    UP000001940(2,"Caenorhabditis elegans"),
    UP000000559(3,"Candida albicans"),
    UP000000437(4,"Danio rerio"),
    UP000002195(5,"Dictyostelium discoideum"),
    UP000000803(6,"Drosophila melanogaster"),
    UP000000625(7,"Escherichia coli"),
    UP000008827(8,"Glycine max"),
    UP000005640(9,"Homo sapiens"),
    UP000008153(10,"Leishmania infantum"),
    UP000000805(11,"Methanocaldococcus jannaschii"),
    UP000000589(12,"Mus musculus"),
    UP000001584(13,"Mycobacterium tuberculosis"),
    UP000059680(14,"Oryza sativa"),
    UP000001450(15,"Plasmodium falciparum"),
    UP000002494(16,"Rattus norvegicus"),
    UP000002311(17,"Saccharomyces cerevisiae"),
    UP000002485(18,"Schizosaccharomyces pombe"),
    UP000008816(19,"Staphylococcus aureus"),
    UP000002296(20,"Trypanosoma cruzi"),
    UP000007305(21,"Zea mays");

    private int id;
    private String name;

    Proteome(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public static Proteome fromId(int id) {
        
        if (id == ALL.getId()) {
            return ALL;
        }
        else if (id == UP000006548.getId()) {
            return UP000006548;
        }
        else if (id == UP000001940.getId()) {
            return UP000001940;
        }
        else if (id == UP000000559.getId()) {
            return UP000000559;
        }
        else if (id == UP000000437.getId()) {
            return UP000000437;
        }
        else if (id == UP000002195.getId()) {
            return UP000002195;
        }
        else if (id == UP000000803.getId()) {
            return UP000000803;
        }
        else if (id == UP000000625.getId()) {
            return UP000000625;
        }
        else if (id == UP000008827.getId()) {
            return UP000008827;
        }
        else if (id == UP000005640.getId()) {
            return UP000005640;
        }
        else if (id == UP000008153.getId()) {
            return UP000008153;
        }
        else if (id == UP000000805.getId()) {
            return UP000000805;
        }
        else if (id == UP000000589.getId()) {
            return UP000000589;
        }
        else if (id == UP000001584.getId()) {
            return UP000001584;
        }
        else if (id == UP000059680.getId()) {
            return UP000059680;
        }
        else if (id == UP000001450.getId()) {
            return UP000001450;
        }
        else if (id == UP000002494.getId()) {
            return UP000002494;
        }
        else if (id == UP000002311.getId()) {
            return UP000002311;
        }
        else if (id == UP000002485.getId()) {
            return UP000002485;
        }
        else if (id == UP000008816.getId()) {
            return UP000008816;
        }
        else if (id == UP000002296.getId()) {
            return UP000002296;
        }
        else {
            return UP000007305;
        }
    }
}
