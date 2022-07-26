package edu.umkc.rupee.search.defs;

public enum Proteome {
    ALL(0,"All Species"),
    UP000001631(1,"Ajellomyces capsulatus"),
    UP000006548(2,"Arabidopsis thaliana (Arabidopsis)"),
    UP000006672(3,"Brugia malayi"),
    UP000001940(4,"Caenorhabditis elegans (Nematode worm)"),
    UP000000799(5,"Campylobacter jejuni"),
    UP000000559(6,"Candida albicans"),
    UP000094526(7,"Cladophialophora carrionii"),
    UP000000437(8,"Danio rerio (Zebrafish)"),
    UP000002195(9,"Dictyostelium discoideum"),
    UP000274756(10,"Dracunculus medinensis"),
    UP000000803(11,"Drosophila melanogaster (Fruit fly)"),
    UP000325664(12,"Enterococcus faecium"),
    UP000000625(13,"Escherichia coli"),
    UP000053029(14,"Fonsecaea pedrosoi"),
    UP000008827(15,"Glycine max (Soybean)"),
    UP000000579(16,"Haemophilus influenzae"),
    UP000000429(17,"Helicobacter pylori"),
    UP000005640(18,"Homo sapiens (Human)"),
    UP000007841(19,"Klebsiella pneumoniae"),
    UP000008153(20,"Leishmania infantum"),
    UP000078237(21,"Madurella mycetomatis"),
    UP000000805(22,"Methanocaldococcus jannaschii"),
    UP000000589(23,"Mus musculus (Mouse)"),
    UP000000806(24,"Mycobacterium leprae"),
    UP000001584(25,"Mycobacterium tuberculosis"),
    UP000020681(26,"Mycobacterium ulcerans"),
    UP000000535(27,"Neisseria gonorrhoeae"),
    UP000006304(28,"Nocardia brasiliensis"),
    UP000024404(29,"Onchocerca volvulus"),
    UP000059680(30,"Oryza sativa (Asian rice)"),
    UP000002059(31,"Paracoccidioides lutzii"),
    UP000001450(32,"Plasmodium falciparum"),
    UP000002438(33,"Pseudomonas aeruginosa"),
    UP000002494(34,"Rattus norvegicus (Rat)"),
    UP000002311(35,"Saccharomyces cerevisiae (Budding yeast)"),
    UP000001014(36,"Salmonella typhimurium"),
    UP000008854(37,"Schistosoma mansoni"),
    UP000002485(38,"Schizosaccharomyces pombe (Fission yeast)"),
    UP000002716(39,"Shigella dysenteriae"),
    UP000018087(40,"Sporothrix schenckii"),
    UP000008816(41,"Staphylococcus aureus"),
    UP000000586(42,"Streptococcus pneumoniae"),
    UP000035681(43,"Strongyloides stercoralis"),
    UP000030665(44,"Trichuris trichiura"),
    UP000008524(45,"Trypanosoma brucei"),
    UP000002296(46,"Trypanosoma cruzi"),
    UP000270924(47,"Wuchereria bancrofti"),
    UP000007305(48,"Zea mays (Maize)");

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
