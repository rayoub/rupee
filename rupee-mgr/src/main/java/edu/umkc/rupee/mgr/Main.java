package edu.umkc.rupee.mgr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.umkc.rupee.base.SearchRecord;
import edu.umkc.rupee.cath.CathHash;
import edu.umkc.rupee.cath.CathImport;
import edu.umkc.rupee.cath.CathSearch;
import edu.umkc.rupee.cath.CathSearchCriteria;
import edu.umkc.rupee.cath.CathSearchRecord;
import edu.umkc.rupee.chain.ChainHash;
import edu.umkc.rupee.chain.ChainImport;
import edu.umkc.rupee.chain.ChainSearch;
import edu.umkc.rupee.chain.ChainSearchCriteria;
import edu.umkc.rupee.chain.ChainSearchRecord;
import edu.umkc.rupee.defs.AlignmentType;
import edu.umkc.rupee.defs.DbType;
import edu.umkc.rupee.defs.SearchBy;
import edu.umkc.rupee.defs.SearchFrom;
import edu.umkc.rupee.defs.SearchMode;
import edu.umkc.rupee.defs.SearchType;
import edu.umkc.rupee.defs.SortBy;
import edu.umkc.rupee.ecod.EcodHash;
import edu.umkc.rupee.ecod.EcodImport;
import edu.umkc.rupee.ecod.EcodSearch;
import edu.umkc.rupee.ecod.EcodSearchCriteria;
import edu.umkc.rupee.ecod.EcodSearchRecord;
import edu.umkc.rupee.lib.AlignRecord;
import edu.umkc.rupee.lib.Aligning;
import edu.umkc.rupee.lib.Constants;
import edu.umkc.rupee.lib.Db;
import edu.umkc.rupee.lib.DbId;
import edu.umkc.rupee.lib.Hashes;
import edu.umkc.rupee.lib.LCS;
import edu.umkc.rupee.lib.Similarity;
import edu.umkc.rupee.lib.Uploading;
import edu.umkc.rupee.scop.ScopHash;
import edu.umkc.rupee.scop.ScopImport;
import edu.umkc.rupee.scop.ScopSearch;
import edu.umkc.rupee.scop.ScopSearchCriteria;
import edu.umkc.rupee.scop.ScopSearchRecord;
import edu.umkc.rupee.tm.Mode;
import edu.umkc.rupee.tm.TMAlign;

public class Main {

    public static void main(String[] args) {

        Options options = new Options();
    
        OptionGroup group = new OptionGroup();

        group.addOption(Option.builder("i")
                .longOpt("import")
                .numberOfArgs(1)
                .argName("DB_TYPE")
                .build());
        group.addOption(Option.builder("h")
                .longOpt("hash")
                .numberOfArgs(1)
                .argName("DB_TYPE")
                .build());
        group.addOption(Option.builder("a")
                .longOpt("align")
                .numberOfArgs(3)
                .argName("DB_ID_1>,<DB_ID_2><ALIGN")
                .valueSeparator(',')
                .build());
        group.addOption(Option.builder("t")
                .longOpt("tm-align")
                .numberOfArgs(2)
                .argName("DB_ID_1>,<DB_ID_2")
                .valueSeparator(',')
                .build());
        group.addOption(Option.builder("f")
                .longOpt("lcs-fulllength")
                .numberOfArgs(2)
                .argName("DB_ID_1>,<DB_ID_2")
                .valueSeparator(',')
                .build());
        group.addOption(Option.builder("c")
                .longOpt("lcs-containment")
                .numberOfArgs(2)
                .argName("DB_ID_1>,<DB_ID_2")
                .valueSeparator(',')
                .build());
        group.addOption(Option.builder("s")
                .longOpt("search")
                .numberOfArgs(12)
                .argName("SEARCH_BY>,<DB_TYPE>,<DB_ID>,<LIMIT>,<REP1>,<REP2>,<REP3>,<DIFF1>,<DIFF2>,<DIFF3>,<SEARCH_MODE>,<SORT_BY")
                .valueSeparator(',')
                .build());
        group.addOption(Option.builder("u")
                .longOpt("upload")
                .numberOfArgs(1)
                .argName("FILE_PATH")
                .build());
        group.addOption(Option.builder("d")
                .longOpt("debug")
                .build());
        group.addOption(Option.builder("?")
                .longOpt("help")
                .build());

        group.setRequired(true);
        options.addOptionGroup(group);

        CommandLine line;

        try {
            CommandLineParser parser = new DefaultParser();
            line = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            option_help(options);
            return;
        }
       
        // allow for debug 
        // System.console().readLine("Press Enter Before Continuing> ");
        
        try {
            if (line.hasOption("i")) {
                option_i(line);
            } else if (line.hasOption("h")) {
                option_h(line);
            } else if (line.hasOption("a")) {
                option_a(line);
            } else if (line.hasOption("t")) {
                option_t(line);
            } else if (line.hasOption("f")) {
                option_f(line);
            } else if (line.hasOption("c")) {
                option_c(line);
            } else if (line.hasOption("s")) {
                option_s(line);
            } else if (line.hasOption("u")) {
                option_u(line);
            } else if (line.hasOption("d")) {
                option_d(line);
            } else if (line.hasOption("?")) {
                option_help(options);
            }
        }
        catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    private static void option_i(CommandLine line) {

        Set<String> dbTypeNames = new HashSet<>(Arrays.stream(DbType.values()).map(v -> v.name()).collect(Collectors.toList()));
        
        String[] args = line.getOptionValues("i");

        if (!dbTypeNames.contains(args[0])) {
            System.err.println("The <DB_TYPE> argument must be one of " + dbTypeNames.toString());
            return;
        }
        
        DbType dbType = DbType.valueOf(args[0]);

        if (dbType == DbType.CATH) {
            CathImport cathImport = new CathImport();
            cathImport.importGrams();
        }
        else if (dbType == DbType.SCOP) {
            ScopImport scopImport = new ScopImport();
            scopImport.importGrams();
        }
        else if (dbType == DbType.ECOD) {
            EcodImport ecodImport = new EcodImport();
            ecodImport.importGrams();
        }
        else { // CHAIN
            ChainImport chainImport = new ChainImport();
            chainImport.importGrams();
        }
        
        System.out.println("Done importing!");
    }
    
    private static void option_h(CommandLine line) {

        Set<String> dbTypeNames = new HashSet<>(Arrays.stream(DbType.values()).map(v -> v.name()).collect(Collectors.toList()));

        String[] args = line.getOptionValues("h");

        if (!dbTypeNames.contains(args[0])) {
            System.err.println("The <DB_TYPE> argument must be one of " + dbTypeNames.toString());
            return;
        }
        
        DbType dbType = DbType.valueOf(args[0]);

        if (dbType == DbType.CATH) {
            CathHash cathHash = new CathHash();
            cathHash.hash();
        }
        else if (dbType == DbType.SCOP) {
            ScopHash scopHash = new ScopHash();
            scopHash.hash();
        }
        else if (dbType == DbType.ECOD) {
            EcodHash ecodHash = new EcodHash();
            ecodHash.hash();
        }
        else { // CHAIN
            ChainHash chainHash = new ChainHash();
            chainHash.hash(); 
        }
        
        System.out.println("Done hashing!");
    }

    private static void option_a(CommandLine line) throws SQLException {

        Set<String> alignNames = new HashSet<>(Arrays.stream(AlignmentType.values()).map(v -> v.name()).collect(Collectors.toList()));
        
        String[] args = line.getOptionValues("a");
        
        String dbId1 = args[0];
        String dbId2 = args[1];

        if (!alignNames.contains(args[2])) {
            System.err.println("The <ALIGN> argument must be one of " + alignNames.toString());
            return;
        }

        AlignmentType align = AlignmentType.valueOf(args[2]);

        AlignRecord record = Aligning.align(dbId1, dbId2, align);

        System.out.println("\nAlgorithm:      " + record.algorithmName);
        System.out.println("RMSD:           " + record.afps.getTotalRmsdOpt());
        System.out.println("TM-Score:       " + record.afps.getTMScore());

        if (align == AlignmentType.CE || align == AlignmentType.CECP) { 
            System.out.println(record.afps.toCE(record.atoms1, record.atoms2));
        }
        else {
            System.out.println(record.afps.toFatcat(record.atoms1, record.atoms2));
        }
    }
    
    private static void option_t(CommandLine line) throws Exception {
            
        String[] args = line.getOptionValues("t");
        
        String dbId1 = args[0];
        String dbId2 = args[1];

        TMAlign.Results results = Aligning.tmAlign(dbId1, dbId2, Mode.ALIGN_TEXT);
        System.out.print(results.getOutput());
    }

    private static void option_f(CommandLine line) throws SQLException {
        
        String[] args = line.getOptionValues("f");
        
        String dbId1 = args[0];
        String dbId2 = args[1];

        DbType dbType1 = DbId.getIdDbType(dbId1);
        DbType dbType2 = DbId.getIdDbType(dbId2);

        Hashes hashes1 = Db.getHashes(dbId1, dbType1);
        Hashes hashes2 = Db.getHashes(dbId2, dbType2);

        List<Integer> grams1 = Db.getGrams(dbId1, dbType1);
        List<Integer> grams2 = Db.getGrams(dbId2, dbType2);

        if (hashes1 != null && hashes2 != null) {

            // band matches
            String bandMatches = Similarity.getBandMatches(hashes1.bandHashes, hashes2.bandHashes);

            // estimated and exact based on overlapping grams
            double estimated = Similarity.getEstimatedSimilarity(hashes1.minHashes, hashes2.minHashes);
            double exact = Similarity.getExactSimilarity(grams1, grams2);

            // lcs validated matching grams
            int score = LCS.getLCSScoreFullLength(grams1, grams2); 
          
            System.out.println(""); 
            System.out.println("Structure 1 Length:     " + grams1.size());
            System.out.println("Structure 2 Length:     " + grams2.size());
            System.out.println(""); 
            System.out.println("Band matches:           " + bandMatches);
            System.out.println(""); 
            System.out.println("Estimated Similarity:   " + estimated);
            System.out.println("Exact Similarity:       " + exact);
            System.out.println(""); 
            System.out.println("LCS Score:              " + score);

            System.out.println("LCS Alignment: \n");
            Map<Integer, String> codeMap = LCS.getCodeMap(grams1, grams2);
            LCS.printLCSFullLength(grams1, grams2, codeMap);
            System.out.println("");
        }
        else {

            if (hashes1 == null) {
                System.out.println(dbId1 + " does not exist.");
            }
            else {
                System.out.println(dbId2 + " does not exist.");
            }
        }
    }
    
    private static void option_c(CommandLine line) throws SQLException {
        
        String[] args = line.getOptionValues("c");
        
        String dbId1 = args[0];
        String dbId2 = args[1];

        DbType dbType1 = DbId.getIdDbType(dbId1);
        DbType dbType2 = DbId.getIdDbType(dbId2);

        Hashes hashes1 = Db.getHashes(dbId1, dbType1);
        Hashes hashes2 = Db.getHashes(dbId2, dbType2);

        List<Integer> grams1 = Db.getGrams(dbId1, dbType1);
        List<Integer> grams2 = Db.getGrams(dbId2, dbType2);

        if (hashes1 != null && hashes2 != null) {

            // band matches
            String bandMatches = Similarity.getBandMatches(hashes1.bandHashes, hashes2.bandHashes);

            // estimated and exact based on overlapping grams
            double estimated = Similarity.getEstimatedSimilarity(hashes1.minHashes, hashes2.minHashes);
            double exact = Similarity.getExactSimilarity(grams1, grams2);

            // lcs validated matching grams
            int score = LCS.getLCSScoreContainment(grams1, grams2); 
          
            System.out.println(""); 
            System.out.println("Structure 1 Length:     " + grams1.size());
            System.out.println("Structure 2 Length:     " + grams2.size());
            System.out.println(""); 
            System.out.println("Band matches:           " + bandMatches);
            System.out.println(""); 
            System.out.println("Estimated Similarity:   " + estimated);
            System.out.println("Exact Similarity:       " + exact);
            System.out.println(""); 
            System.out.println("LCS Score:              " + score);

            System.out.println("LCS Alignment: \n");
            Map<Integer, String> codeMap = LCS.getCodeMap(grams1, grams2);
            LCS.printLCSContainment(grams1, grams2, codeMap);
            System.out.println("");
        }
        else {

            if (hashes1 == null) {
                System.out.println(dbId1 + " does not exist.");
            }
            else {
                System.out.println(dbId2 + " does not exist.");
            }
        }
    }
    
    private static void option_s(CommandLine line) throws Exception {

        Set<String> searchByNames = new HashSet<>(Arrays.stream(SearchBy.values()).map(v -> v.name()).collect(Collectors.toList()));
        Set<String> dbTypeNames = new HashSet<>(Arrays.stream(DbType.values()).map(v -> v.name()).collect(Collectors.toList()));
        Set<String> searchModeNames = new HashSet<>(Arrays.stream(SearchMode.values()).map(v -> v.name()).collect(Collectors.toList()));
        Set<String> sortByNames = new HashSet<>(Arrays.stream(SortBy.values()).map(v -> v.name()).collect(Collectors.toList()));

        String[] args = line.getOptionValues("s");
       
        // types 
        if (!searchByNames.contains(args[0])) {
            System.err.println("The <SEARCH_BY> argument must be one of " + searchByNames.toString());
            return;
        }
        if (!dbTypeNames.contains(args[1])) {
            System.err.println("The <DB_TYPE> argument must be one of " + dbTypeNames.toString());
            return;
        }
       
        SearchBy searchBy = SearchBy.valueOf(args[0]); 
        DbType dbType = DbType.valueOf(args[1]);

        // id
        String id = args[2];
        DbType idDbType = DbId.getIdDbType(id);

        // limit
        int limit = tryParseInt(args[3]);
        if (limit == -1) {
            System.err.println("The <LIMIT> argument must be a positive integer.");
            return;
        }

        // booleans
        if (!args[4].equals("TRUE") && !args[4].equals("FALSE")) {
            System.err.println("The <REP1> argument must be TRUE or FALSE");
            return;
        }
        if (!args[5].equals("TRUE") && !args[5].equals("FALSE")) {
            System.err.println("The <REP2> argument must be TRUE or FALSE");
            return;
        }
        if (!args[6].equals("TRUE") && !args[6].equals("FALSE")) {
            System.err.println("The <REP3> argument must be TRUE or FALSE");
            return;
        }
        if (!args[7].equals("TRUE") && !args[7].equals("FALSE")) {
            System.err.println("The <DIFF1> argument must be TRUE or FALSE");
            return;
        }
        if (!args[8].equals("TRUE") && !args[8].equals("FALSE")) {
            System.err.println("The <DIFF2> argument must be TRUE or FALSE");
            return;
        }
        if (!args[9].equals("TRUE") && !args[9].equals("FALSE")) {
            System.err.println("The <DIFF3> argument must be TRUE or FALSE");
            return;
        }

        boolean rep1 = Boolean.parseBoolean(args[4]);
        boolean rep2 = Boolean.parseBoolean(args[5]);
        boolean rep3 = Boolean.parseBoolean(args[6]);
        boolean diff1 = Boolean.parseBoolean(args[7]);
        boolean diff2 = Boolean.parseBoolean(args[8]);
        boolean diff3 = Boolean.parseBoolean(args[9]);

        // search mode
        if (!searchModeNames.contains(args[10])) {
            System.err.println("The <SEARCH_MODE> argument must be one of " + searchModeNames.toString());
            return;
        }

        SearchMode searchMode = SearchMode.valueOf(args[10]);
       
        // sort by
        if (!sortByNames.contains(args[11])) {
            System.err.println("The <SORT_BY> argument must be one of " + sortByNames.toString());
            return;
        }

        SortBy sortBy = SortBy.valueOf(args[11]);

        // consistency rule
        if (searchMode == SearchMode.FAST) {
            sortBy = SortBy.SIMILARITY;
        }
              
        //*************************************************************
        //***  OUTPUT
        //*************************************************************

        SearchType searchType = SearchType.FULL_LENGTH;

        boolean verbose = false; 
        boolean timing = false;

        if (dbType == DbType.SCOP) { 

            ScopSearchCriteria criteria = new ScopSearchCriteria();
            
            criteria.searchBy = searchBy;
            criteria.idDbType = idDbType;
            criteria.searchDbType = dbType;
            if (criteria.searchBy == SearchBy.DB_ID) {
                criteria.dbId = id;
            }
            else {
                criteria.uploadId = Integer.parseInt(id);
            }

            criteria.limit = limit;
            criteria.searchType = searchType;
            criteria.searchMode = searchMode;
            criteria.sortBy = sortBy;
            criteria.differentFold = diff1;
            criteria.differentSuperfamily = diff2;
            criteria.differentFamily = diff3;

            ScopSearch scopSearch = new ScopSearch();

            long start = 0, stop = 0;
            if (timing) {
                start = System.currentTimeMillis(); 
            }
            List<SearchRecord> records = scopSearch.search(criteria, SearchFrom.CLI);
            if (timing) {
                stop = System.currentTimeMillis();
                System.out.println(criteria.dbId + "," + (stop - start));
                return;
            }
        
            for (SearchRecord baseRecord : records) {
           
                ScopSearchRecord record = (ScopSearchRecord) baseRecord;

                if (verbose) {

                    // verbose 
                    System.out.printf("%-10d %-10s %-24s %-24s %-24s %-16s %-10.2f %-10.2f %-10.2f\n", 
                        record.getN(),
                        record.getDbId(),
                        record.getCfDescription().substring(0,Math.min(record.getCfDescription().length(),23)),
                        record.getSfDescription().substring(0,Math.min(record.getSfDescription().length(),23)),
                        record.getFaDescription().substring(0,Math.min(record.getFaDescription().length(),23)),
                        record.getCl() + "." + record.getCf() + "." + record.getSf() + "." + record.getFa(), 
                        record.getSimilarity(),
                        record.getRmsd(),
                        record.getTmScore()
                    );
                }
                else {
           
                    // gathering results
                    System.out.printf("%d,%s,%s,%.4f,%.4f,%s\n",
                        record.getN(),
                        criteria.dbId,
                        record.getDbId(),
                        record.getRmsd(),
                        record.getTmScore(),
                        criteria.sortBy.name().toLowerCase()
                    );
                }
            }
        }
        else if (dbType == DbType.CATH) {
        
            CathSearchCriteria criteria = new CathSearchCriteria();

            criteria.searchBy = searchBy;
            criteria.idDbType = idDbType;
            criteria.searchDbType = dbType;
            if (criteria.searchBy == SearchBy.DB_ID) {
                criteria.dbId = id;
            }
            else {
                criteria.uploadId = Integer.parseInt(id);
            }

            criteria.limit = limit;
            criteria.searchType = searchType;
            criteria.searchMode = searchMode;
            criteria.sortBy = sortBy;
            criteria.topologyReps = rep1;
            criteria.superfamilyReps = rep2;
            criteria.s35Reps = rep3;
            criteria.differentTopology = diff1;
            criteria.differentSuperfamily = diff2;
            criteria.differentS35 = diff3;

            CathSearch cathSearch = new CathSearch();

            long start = 0, stop = 0;
            if (timing) {
                start = System.currentTimeMillis(); 
            }
            List<SearchRecord> records = cathSearch.search(criteria, SearchFrom.CLI);
            if (timing) {
                stop = System.currentTimeMillis();
                System.out.println(criteria.dbId + "," + (stop - start));
                return;
            }

            for (SearchRecord baseRecord : records) {
             
                CathSearchRecord record = (CathSearchRecord) baseRecord; 

                if (verbose) {

                    // verbose 
                    System.out.printf("%-10d %-10s %-24s %-24s %-24s %-16s %-16s %-10.2f %-10.2f %-10.2f\n", 
                        record.getN(),
                        record.getDbId(),
                        record.getADescription().substring(0,Math.min(record.getADescription().length(),23)),
                        record.getTDescription().substring(0,Math.min(record.getTDescription().length(),23)),
                        record.getHDescription().substring(0,Math.min(record.getHDescription().length(),23)),
                        record.getC() + "." + record.getA() + "." + record.getT() + "." + record.getH(), 
                        record.getS() + "." + record.getO() + "." + record.getL() + "." + record.getI() + "." + record.getD(),
                        record.getSimilarity(),
                        record.getRmsd(),
                        record.getTmScore()
                    );
                }
                else {
           
                    // gathering results
                    System.out.printf("%d,%s,%s,%.4f,%.4f,%s\n",
                        record.getN(),
                        criteria.dbId,
                        record.getDbId(),
                        record.getRmsd(),
                        record.getTmScore(),
                        criteria.sortBy.name().toLowerCase()
                    );
                }
            }
        }
        else if (dbType == DbType.ECOD) {

            EcodSearchCriteria criteria = new EcodSearchCriteria();
            
            criteria.searchBy = searchBy;
            criteria.idDbType = idDbType;
            criteria.searchDbType = dbType;
            if (criteria.searchBy == SearchBy.DB_ID) {
                criteria.dbId = id;
            }
            else {
                criteria.uploadId = Integer.parseInt(id);
            }

            criteria.limit = limit;
            criteria.searchType = searchType;
            criteria.searchMode = searchMode;
            criteria.sortBy = sortBy;
            criteria.differentH = diff1;
            criteria.differentT = diff2;
            criteria.differentF = diff3;

            EcodSearch ecodSearch = new EcodSearch();
            List<SearchRecord> records = ecodSearch.search(criteria, SearchFrom.CLI);
        
            for (SearchRecord baseRecord : records) {
           
                EcodSearchRecord record = (EcodSearchRecord) baseRecord;

                if (verbose) {

                    // verbose 
                    System.out.printf("%-10d %-10s %-24s %-24s %-24s %-16s %-10.2f %-10.2f %-10.2f\n", 
                        record.getN(),
                        record.getDbId(),
                        record.getHDescription().substring(0,Math.min(record.getHDescription().length(),23)),
                        record.getTDescription().substring(0,Math.min(record.getTDescription().length(),23)),
                        record.getFDescription().substring(0,Math.min(record.getFDescription().length(),23)),
                        record.getX() + "." + record.getH() + "." + record.getT() + (record.getF().isEmpty()?"":".") + record.getF(), 
                        record.getSimilarity(),
                        record.getRmsd(),
                        record.getTmScore()
                    );
                }
                else {
           
                    // gathering results
                    System.out.printf("%d,%s,%s,%.4f,%.4f,%s\n",
                        record.getN(),
                        criteria.dbId,
                        record.getDbId(),
                        record.getRmsd(),
                        record.getTmScore(),
                        criteria.sortBy.name().toLowerCase()
                    );
                }
            }
        }
        else { // CHAIN
            
            ChainSearchCriteria criteria = new ChainSearchCriteria();
            
            criteria.searchBy = searchBy;
            criteria.idDbType = idDbType;
            criteria.searchDbType = dbType;
            if (criteria.searchBy == SearchBy.DB_ID) {
                criteria.dbId = id;
            }
            else {
                criteria.uploadId = Integer.parseInt(id);
            }

            criteria.limit = limit;
            criteria.searchType = searchType;
            criteria.searchMode = searchMode;
            criteria.sortBy = sortBy;

            ChainSearch chainSearch = new ChainSearch();
            List<SearchRecord> records = chainSearch.search(criteria, SearchFrom.CLI);
        
            for (SearchRecord baseRecord : records) {
           
                ChainSearchRecord record = (ChainSearchRecord) baseRecord;

                if (verbose) {

                    // verbose 
                    System.out.printf("%-10d %-10s %-10.2f %-10.2f %-10.2f\n", 
                        record.getN(),
                        record.getDbId(),
                        record.getSimilarity(),
                        record.getRmsd(),
                        record.getTmScore()
                    );
                }
                else {
           
                    // gathering results
                    System.out.printf("%d,%s,%s,%.4f,%.4f,%s\n",
                        record.getN(),
                        criteria.dbId,
                        record.getDbId(),
                        record.getRmsd(),
                        record.getTmScore(),
                        criteria.sortBy.name().toLowerCase()
                    );
                }
            }
        }
    }
    
    private static void option_u(CommandLine line) throws IOException {
        
        String[] args = line.getOptionValues("u");
        
        String path = args[0];

        if (Files.notExists(Paths.get(path))) {
            System.out.println("File Not Found: " + path);
            return;
        }

        byte[] bytes = Files.readAllBytes(Paths.get(path));
        String content = new String(bytes);
        int uploadId = Uploading.upload(content);

        System.out.println("Upload Successful. Upload Id: " + uploadId);
    }

    private static void option_d(CommandLine line) throws Exception {

        /*
        List<Labels.Label> labels = Labels.getLabels("d2pf2a2", DbTypeCriteria.SCOP);
        for (Labels.Label label : labels) {
            System.out.println(label.getResidueNumber() + "," + label.getLabel());
        }
        */

        /*
        AlignResults.alignRupeeResults("scop_d360", "scop_v2_07", "tm_score", DbType.SCOP, 100);
        AlignResults.alignRupeeResults("scop_d360", "scop_v2_07", "rmsd", DbType.SCOP, 100);
        AlignResults.alignRupeeResults("scop_d360", "scop_v2_07", "similarity", DbType.SCOP, 100);
        AlignResults.alignMtmDomResults("scop_d360", "scop_v2_07", DbType.SCOP, 100);

        AlignResults.alignRupeeResults("cath_d99", "cath_v4_2_0", "tm_score", DbType.CATH, 100);
        AlignResults.alignRupeeResults("cath_d99", "cath_v4_2_0", "rmsd", DbType.CATH, 100);
        AlignResults.alignRupeeResults("cath_d99", "cath_v4_2_0", "similarity", DbType.CATH, 100);
        AlignResults.alignCathedralResults("cath_d99", "cath_v4_2_0", DbType.CATH, 100);
        */

        /*
        AlignResults.alignRupeeResults("scop_d62", "scop_v1_73", "tm_score", DbType.SCOP, 50);
        AlignResults.alignRupeeResults("scop_d62", "scop_v1_73", "rmsd", DbType.SCOP, 50);
        AlignResults.alignRupeeResults("scop_d62", "scop_v1_73", "similarity", DbType.SCOP, 50);
        AlignResults.alignSsmResults("scop_d62", "scop_v1_73", DbType.SCOP, 50);
        */
       
        /* 
        AlignResults.alignRupeeResults("scop_d50", "scop_v2_07", "similarity_1", DbType.SCOP, 100);
        AlignResults.alignRupeeResults("scop_d50", "scop_v2_07", "similarity_2", DbType.SCOP, 100);
        */
    }

    private static void option_help(Options options) {

        HelpFormatter formatter = getHelpFormatter("Usage: ");
        formatter.printHelp(Constants.APP_NAME, options);
    }

    private static HelpFormatter getHelpFormatter(String headerPrefix){

        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(new OptionComparator());
        formatter.setSyntaxPrefix(headerPrefix);
        formatter.setWidth(140);
        formatter.setLeftPadding(5);
        return formatter;
    }

    private static int tryParseInt(String value) {

        try {
            int val = Integer.parseInt(value);
            return val;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
