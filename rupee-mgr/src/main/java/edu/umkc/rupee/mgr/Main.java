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

import edu.umkc.rupee.base.Search;
import edu.umkc.rupee.base.SearchCriteria;
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
import edu.umkc.rupee.ecod.EcodHash;
import edu.umkc.rupee.ecod.EcodImport;
import edu.umkc.rupee.ecod.EcodSearch;
import edu.umkc.rupee.ecod.EcodSearchCriteria;
import edu.umkc.rupee.ecod.EcodSearchRecord;
import edu.umkc.rupee.lib.AlignCriteria;
import edu.umkc.rupee.lib.AlignRecord;
import edu.umkc.rupee.lib.Aligning;
import edu.umkc.rupee.lib.Cache;
import edu.umkc.rupee.lib.Constants;
import edu.umkc.rupee.lib.Db;
import edu.umkc.rupee.lib.DbTypeCriteria;
import edu.umkc.rupee.lib.Hashes;
import edu.umkc.rupee.lib.DbId;
import edu.umkc.rupee.lib.LCS;
import edu.umkc.rupee.lib.LCSResults;
import edu.umkc.rupee.lib.SearchByCriteria;
import edu.umkc.rupee.lib.Similarity;
import edu.umkc.rupee.lib.SortCriteria;
import edu.umkc.rupee.lib.Uploading;
import edu.umkc.rupee.scop.ScopHash;
import edu.umkc.rupee.scop.ScopImport;
import edu.umkc.rupee.scop.ScopSearch;
import edu.umkc.rupee.scop.ScopSearchCriteria;
import edu.umkc.rupee.scop.ScopSearchRecord;

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
                .numberOfArgs(4)
                .argName("ID_TYPE><DB_ID_1>,<DB_ID_2><ALIGN")
                .valueSeparator(',')
                .build());
        group.addOption(Option.builder("l")
                .longOpt("lcs")
                .numberOfArgs(3)
                .argName("ID_TYPE>,<DB_ID_1>,<DB_ID_2")
                .valueSeparator(',')
                .build());
        group.addOption(Option.builder("s")
                .longOpt("search")
                .numberOfArgs(13)
                .argName("SEARCH_BY><ID_TYPE>,<DB_TYPE>,<DB_ID>,<LIMIT>,<REP1>,<REP2>,<REP3>,<DIFF1>,<DIFF2><DIFF3><ALIGN>,<SORT")
                .valueSeparator(',')
                .build());
        group.addOption(Option.builder("u")
                .longOpt("upload")
                .numberOfArgs(1)
                .argName("FILE_PATH")
                .build());
        group.addOption(Option.builder("c")
                .longOpt("cache")
                .numberOfArgs(2)
                .argName("DB_TYPE>,<DB_ID")
                .valueSeparator(',')
                .build());
        group.addOption(Option.builder("t")
                .longOpt("test")
                .numberOfArgs(1)
                .argName("TEST_ARG")
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

        try {
            if (line.hasOption("i")) {
                option_i(line);
            } else if (line.hasOption("h")) {
                option_h(line);
            } else if (line.hasOption("a")) {
                option_a(line);
            } else if (line.hasOption("l")) {
                option_l(line);
            } else if (line.hasOption("s")) {
                option_s(line);
            } else if (line.hasOption("u")) {
                option_u(line);
            } else if (line.hasOption("c")) {
                option_c(line);
            } else if (line.hasOption("t")) {
                option_t(line);
            } else if (line.hasOption("?")) {
                option_help(options);
            }
        }
        catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    private static void option_i(CommandLine line) {

        Set<String> dbTypeNames = new HashSet<>(Arrays.stream(DbTypeCriteria.values()).map(v -> v.name()).collect(Collectors.toList()));
        
        String[] args = line.getOptionValues("i");

        if (!dbTypeNames.contains(args[0])) {
            System.err.println("The <DB_TYPE> argument must be one of " + dbTypeNames.toString());
            return;
        }
        
        DbTypeCriteria dbType = DbTypeCriteria.valueOf(args[0]);

        if (dbType == DbTypeCriteria.CATH) {
            CathImport cathImport = new CathImport();
            cathImport.importGrams();
        }
        else if (dbType == DbTypeCriteria.SCOP) {
            ScopImport scopImport = new ScopImport();
            scopImport.importGrams();
        }
        else if (dbType == DbTypeCriteria.ECOD) {
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

        Set<String> dbTypeNames = new HashSet<>(Arrays.stream(DbTypeCriteria.values()).map(v -> v.name()).collect(Collectors.toList()));

        String[] args = line.getOptionValues("h");

        if (!dbTypeNames.contains(args[0])) {
            System.err.println("The <DB_TYPE> argument must be one of " + dbTypeNames.toString());
            return;
        }
        
        DbTypeCriteria dbType = DbTypeCriteria.valueOf(args[0]);

        if (dbType == DbTypeCriteria.CATH) {
            CathHash cathHash = new CathHash();
            cathHash.hash();
        }
        else if (dbType == DbTypeCriteria.SCOP) {
            ScopHash scopHash = new ScopHash();
            scopHash.hash();
        }
        else if (dbType == DbTypeCriteria.ECOD) {
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

        Set<String> searchIdTypeNames = new HashSet<>(Arrays.stream(DbTypeCriteria.values()).map(v -> v.name()).collect(Collectors.toList()));
        Set<String> alignNames = new HashSet<>(Arrays.stream(AlignCriteria.values()).map(v -> v.name()).collect(Collectors.toList()));
        
        String[] args = line.getOptionValues("a");
        
        String dbId1 = args[1];
        String dbId2 = args[2];
        
        if (!searchIdTypeNames.contains(args[0])) {
            System.err.println("The <ID_TYPE> argument must be one of " + searchIdTypeNames.toString());
            return;
        }

        if (!alignNames.contains(args[3])) {
            System.err.println("The <ALIGN> argument must be one of " + alignNames.toString());
            return;
        }
        
        DbTypeCriteria searchIdType = DbTypeCriteria.valueOf(args[0]);
        AlignCriteria align = AlignCriteria.valueOf(args[3]);
        AlignRecord record = Aligning.align(dbId1, dbId2, align, searchIdType);

        System.out.println("\nAlgorithm:      " + record.algorithmName);
        System.out.println("RMSD:           " + record.afps.getTotalRmsdOpt());
        System.out.println("TM-Score:       " + record.afps.getTMScore());

        if (align == AlignCriteria.CE || align == AlignCriteria.CECP) { 
            System.out.println(record.afps.toCE(record.atoms1, record.atoms2));
        }
        else {
            System.out.println(record.afps.toFatcat(record.atoms1, record.atoms2));
        }
    }

    private static void option_l(CommandLine line) throws SQLException {
        
        Set<String> searchIdTypeNames = new HashSet<>(Arrays.stream(DbTypeCriteria.values()).map(v -> v.name()).collect(Collectors.toList()));

        String[] args = line.getOptionValues("l");
        
        String dbId1 = args[1];
        String dbId2 = args[2];
        
        if (!searchIdTypeNames.contains(args[0])) {
            System.err.println("The <ID_TYPE> argument must be one of " + searchIdTypeNames.toString());
            return;
        }

        DbTypeCriteria dbType = DbTypeCriteria.valueOf(args[0]);

        Hashes hashes1 = Db.getHashes(dbId1, dbType);
        Hashes hashes2 = Db.getHashes(dbId2, dbType);

        List<Integer> grams1 = Db.getGrams(dbId1, dbType);
        List<Integer> grams2 = Db.getGrams(dbId2, dbType);

        if (hashes1 != null && hashes2 != null) {

            // estimated and exact based on overlapping grams
            double estimated = Similarity.getEstimatedSimilarity(hashes1.minHashes, hashes2.minHashes);
            double exact = Similarity.getExactSimilarity(grams1, grams2);

            // lcs validated matching grams
            int lcsLength = LCS.getLCSLength(grams1, grams2); 
            double lcsSimilarity = Similarity.getAdjustedSimilarity(grams1, grams2, lcsLength);
           
            // semi-global adjustment
            LCSResults results = LCS.getSemiGlobalLCS(grams1,grams2);
            double semiGlobal = Similarity.getAdjustedSimilarity(grams1, grams2, results);
          
            System.out.println(""); 
            System.out.println("Structure 1 Length:     " + grams1.size());
            System.out.println("Structure 2 Length:     " + grams2.size());
            System.out.println(""); 
            System.out.println("Estimated Similarity:   " + estimated);
            System.out.println("Exact Similarity:       " + exact);
            System.out.println(""); 
            System.out.println("LCS Length:             " + lcsLength);
            System.out.println("LCS Similarity:         " + lcsSimilarity);
            System.out.println("SemiGlobal Similarity:  " + semiGlobal + "\n");

            Map<Integer, String> codeMap = LCS.getCodeMap(grams1, grams2);

            System.out.println("LCS Alignment: \n");
            LCS.printLCS(grams1, grams2, codeMap);
            System.out.println("");

            System.out.println("Semi-Global LCS Alignment: \n");
            LCS.printSemiGlobalLCS(grams1, grams2, codeMap);
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
    
    private static void option_s(CommandLine line) {

        Set<String> searchTypeNames = new HashSet<>(Arrays.stream(SearchByCriteria.values()).map(v -> v.name()).collect(Collectors.toList()));
        Set<String> dbTypeNames = new HashSet<>(Arrays.stream(DbTypeCriteria.values()).map(v -> v.name()).collect(Collectors.toList()));
        Set<String> alignNames = new HashSet<>(Arrays.stream(AlignCriteria.values()).map(v -> v.name()).collect(Collectors.toList()));
        Set<String> sortNames = new HashSet<>(Arrays.stream(SortCriteria.values()).map(v -> v.name()).collect(Collectors.toList()));

        String[] args = line.getOptionValues("s");
       
        // types 
        if (!searchTypeNames.contains(args[0])) {
            System.err.println("The <SEARCH_BY> argument must be one of " + searchTypeNames.toString());
            return;
        }
        if (!dbTypeNames.contains(args[1])) {
            System.err.println("The <ID_TYPE> argument must be one of " + dbTypeNames.toString());
            return;
        }
        if (!dbTypeNames.contains(args[2])) {
            System.err.println("The <DB_TYPE> argument must be one of " + dbTypeNames.toString());
            return;
        }
       
        SearchByCriteria searchType = SearchByCriteria.valueOf(args[0]); 
        DbTypeCriteria dbIdType = DbTypeCriteria.valueOf(args[1]);
        DbTypeCriteria dbType = DbTypeCriteria.valueOf(args[2]);

        // id
        String id = args[3];

        // limit
        int limit = tryParseInt(args[4]);

        if (limit == -1) {
            System.err.println("The <LIMIT> argument must be a positive integer.");
            return;
        }

        // booleans
        if (!args[5].equals("TRUE") && !args[5].equals("FALSE")) {
            System.err.println("The <REP1> argument must be TRUE or FALSE");
            return;
        }
        if (!args[6].equals("TRUE") && !args[6].equals("FALSE")) {
            System.err.println("The <REP2> argument must be TRUE or FALSE");
            return;
        }
        if (!args[7].equals("TRUE") && !args[7].equals("FALSE")) {
            System.err.println("The <REP3> argument must be TRUE or FALSE");
            return;
        }
        if (!args[8].equals("TRUE") && !args[8].equals("FALSE")) {
            System.err.println("The <DIFF1> argument must be TRUE or FALSE");
            return;
        }
        if (!args[9].equals("TRUE") && !args[9].equals("FALSE")) {
            System.err.println("The <DIFF2> argument must be TRUE or FALSE");
            return;
        }
        if (!args[10].equals("TRUE") && !args[10].equals("FALSE")) {
            System.err.println("The <DIFF3> argument must be TRUE or FALSE");
            return;
        }

        boolean rep1 = Boolean.parseBoolean(args[5]);
        boolean rep2 = Boolean.parseBoolean(args[6]);
        boolean rep3 = Boolean.parseBoolean(args[7]);
        boolean diff1 = Boolean.parseBoolean(args[8]);
        boolean diff2 = Boolean.parseBoolean(args[9]);
        boolean diff3 = Boolean.parseBoolean(args[10]);
       
        // alignment and sorting 
        if (!alignNames.contains(args[11])) {
            System.err.println("The <ALIGN> argument must be one of " + alignNames.toString());
            return;
        }
        if (!sortNames.contains(args[12])) {
            System.err.println("The <SORT> argument must be one of " + sortNames.toString());
            return;
        }

        AlignCriteria align = AlignCriteria.valueOf(args[11]);
        SortCriteria sort = SortCriteria.valueOf(args[12]);

        // consistency rule
        if (align == AlignCriteria.NONE) {
            sort = SortCriteria.SIMILARITY;
        }
              
        //*************************************************************
        //***  OUTPUT
        //*************************************************************

        boolean verbose = true; 

        if (dbType == DbTypeCriteria.CATH) {
        
            CathSearchCriteria criteria = new CathSearchCriteria();

            criteria.searchBy = searchType;
            criteria.dbIdType = dbIdType;
            criteria.dbType = dbType;
            if (criteria.searchBy == SearchByCriteria.DB_ID) {
                criteria.dbId = id;
            }
            else {
                criteria.uploadId = Integer.parseInt(id);
            }

            criteria.page = 1;
            criteria.pageSize = limit;
            criteria.limit = limit;
            criteria.topologyReps = rep1;
            criteria.superfamilyReps = rep2;
            criteria.s35Reps = rep3;
            criteria.differentTopology = diff1;
            criteria.differentSuperfamily = diff2;
            criteria.differentS35 = diff3;
            criteria.align = align;
            criteria.sort = sort;

            CathSearch cathSearch = new CathSearch();
            List<SearchRecord> records = cathSearch.search(criteria);

            for (SearchRecord baseRecord : records) {
             
                CathSearchRecord record = (CathSearchRecord) baseRecord; 

                if (verbose) {


                    // verbose 
                    System.out.printf("%-10d %-10s %-24s %-24s %-24s %-16s %-16s %-10d %-10.2f %-10.2f %-10.2f\n", 
                        record.getN(),
                        record.getDbId2(),
                        record.getADescription().substring(0,Math.min(record.getADescription().length(),23)),
                        record.getTDescription().substring(0,Math.min(record.getTDescription().length(),23)),
                        record.getHDescription().substring(0,Math.min(record.getHDescription().length(),23)),
                        record.getC() + "." + record.getA() + "." + record.getT() + "." + record.getH(), 
                        record.getS() + "." + record.getO() + "." + record.getL() + "." + record.getI() + "." + record.getD(),
                        record.getSimilarityRank(),
                        record.getSimilarity(),
                        record.getRmsd(),
                        record.getTmScore()
                    );
                }
                else {
           
                    // gathering results
                    System.out.printf("%-10d %-10s %-10s %-10.2f %-10.2f %-10.2f\n", 
                        record.getN(),
                        record.getDbId1(),
                        record.getDbId2(),
                        record.getSimilarity(),
                        record.getRmsd(),
                        record.getTmScore()
                    );
                }
            }
        }
        else if (dbType == DbTypeCriteria.SCOP) { 

            ScopSearchCriteria criteria = new ScopSearchCriteria();
            
            criteria.searchBy = searchType;
            criteria.dbIdType = dbIdType;
            criteria.dbType = dbType;
            if (criteria.searchBy == SearchByCriteria.DB_ID) {
                criteria.dbId = id;
            }
            else {
                criteria.uploadId = Integer.parseInt(id);
            }

            criteria.page = 1;
            criteria.pageSize = limit;
            criteria.limit = limit;
            criteria.align = align;
            criteria.sort = sort;
            criteria.differentFold = diff1;
            criteria.differentSuperfamily = diff2;
            criteria.differentFamily = diff3;

            ScopSearch scopSearch = new ScopSearch();
            List<SearchRecord> records = scopSearch.search(criteria);
        
            for (SearchRecord baseRecord : records) {
           
                ScopSearchRecord record = (ScopSearchRecord) baseRecord;

                if (verbose) {

                    // verbose 
                    System.out.printf("%-10d %-10s %-24s %-24s %-24s %-16s %-10d %-10.2f %-10.2f %-10.2f\n", 
                        record.getN(),
                        record.getDbId2(),
                        record.getCfDescription().substring(0,Math.min(record.getCfDescription().length(),23)),
                        record.getSfDescription().substring(0,Math.min(record.getSfDescription().length(),23)),
                        record.getFaDescription().substring(0,Math.min(record.getFaDescription().length(),23)),
                        record.getCl() + "." + record.getCf() + "." + record.getSf() + "." + record.getFa(), 
                        record.getSimilarityRank(),
                        record.getSimilarity(),
                        record.getRmsd(),
                        record.getTmScore()
                    );
                }
                else {
           
                    // gathering results
                    System.out.printf("%-10d %-10s %-10s %-10.2f %-10.2f %-10.2f\n", 
                        record.getN(),
                        record.getDbId1(),
                        record.getDbId2(),
                        record.getSimilarity(),
                        record.getRmsd(),
                        record.getTmScore()
                    );
                }
            }
        }
        else if (dbType == DbTypeCriteria.ECOD) {

            EcodSearchCriteria criteria = new EcodSearchCriteria();
            
            criteria.searchBy = searchType;
            criteria.dbIdType = dbIdType;
            criteria.dbType = dbType;
            if (criteria.searchBy == SearchByCriteria.DB_ID) {
                criteria.dbId = id;
            }
            else {
                criteria.uploadId = Integer.parseInt(id);
            }

            criteria.page = 1;
            criteria.pageSize = limit;
            criteria.limit = limit;
            criteria.align = align;
            criteria.sort = sort;
            criteria.differentH = diff1;
            criteria.differentT = diff2;
            criteria.differentF = diff3;

            EcodSearch ecodSearch = new EcodSearch();
            List<SearchRecord> records = ecodSearch.search(criteria);
        
            for (SearchRecord baseRecord : records) {
           
                EcodSearchRecord record = (EcodSearchRecord) baseRecord;

                if (verbose) {

                    // verbose 
                    System.out.printf("%-10d %-10s %-24s %-24s %-24s %-16s %-10d %-10.2f %-10.2f %-10.2f\n", 
                        record.getN(),
                        record.getDbId2(),
                        record.getHDescription().substring(0,Math.min(record.getHDescription().length(),23)),
                        record.getTDescription().substring(0,Math.min(record.getTDescription().length(),23)),
                        record.getFDescription().substring(0,Math.min(record.getFDescription().length(),23)),
                        record.getX() + "." + record.getH() + "." + record.getT() + (record.getF().isEmpty()?"":".") + record.getF(), 
                        record.getSimilarityRank(),
                        record.getSimilarity(),
                        record.getRmsd(),
                        record.getTmScore()
                    );
                }
                else {
           
                    // gathering results
                    System.out.printf("%-10d %-10s %-10s %-10.2f %-10.2f %-10.2f\n", 
                        record.getN(),
                        record.getDbId1(),
                        record.getDbId2(),
                        record.getSimilarity(),
                        record.getRmsd(),
                        record.getTmScore()
                    );
                }
            }
        }
        else { // CHAIN
            
            ChainSearchCriteria criteria = new ChainSearchCriteria();
            
            criteria.searchBy = searchType;
            criteria.dbIdType = dbIdType;
            criteria.dbType = dbType;
            if (criteria.searchBy == SearchByCriteria.DB_ID) {
                criteria.dbId = id;
            }
            else {
                criteria.uploadId = Integer.parseInt(id);
            }

            criteria.page = 1;
            criteria.pageSize = limit;
            criteria.limit = limit;
            criteria.align = align;
            criteria.sort = sort;

            ChainSearch chainSearch = new ChainSearch();
            List<SearchRecord> records = chainSearch.search(criteria);
        
            for (SearchRecord baseRecord : records) {
           
                ChainSearchRecord record = (ChainSearchRecord) baseRecord;

                if (verbose) {

                    // verbose 
                    System.out.printf("%-10d %-10s %-10d %-10.2f %-10.2f %-10.2f\n", 
                        record.getN(),
                        record.getDbId2(),
                        record.getSimilarityRank(),
                        record.getSimilarity(),
                        record.getRmsd(),
                        record.getTmScore()
                    );
                }
                else {
           
                    // gathering results
                    System.out.printf("%-10d %-10s %-10s %-10.2f %-10.2f %-10.2f\n", 
                        record.getN(),
                        record.getDbId1(),
                        record.getDbId2(),
                        record.getSimilarity(),
                        record.getRmsd(),
                        record.getTmScore()
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
    
    private static void option_c(CommandLine line) {
        
        Set<String> dbTypeNames = new HashSet<>(Arrays.stream(DbTypeCriteria.values()).map(v -> v.name()).collect(Collectors.toList()));
        
        String[] args = line.getOptionValues("c");
        
        String dbId = args[1];
        
        if (!dbTypeNames.contains(args[0])) {
            System.err.println("The <DB_TYPE> argument must be one of " + dbTypeNames.toString());
            return;
        }
        
        DbTypeCriteria dbType = DbTypeCriteria.valueOf(args[0]);

        SearchCriteria criteria;
        Search search;

        if (dbType == DbTypeCriteria.CATH) {
            CathSearchCriteria cathCriteria = new CathSearchCriteria();
            cathCriteria.dbId = dbId;
            cathCriteria.limit = 400;

            criteria = cathCriteria;
            search = new CathSearch();
        }
        else if (dbType == DbTypeCriteria.SCOP) { 
            ScopSearchCriteria scopCriteria = new ScopSearchCriteria();
            scopCriteria.dbId = dbId;
            scopCriteria.limit = 400;

            criteria = scopCriteria;
            search = new ScopSearch();
        }
        else if (dbType == DbTypeCriteria.ECOD) {
            EcodSearchCriteria ecodCriteria = new EcodSearchCriteria();
            ecodCriteria.dbId = dbId;
            ecodCriteria.limit = 400;

            criteria = ecodCriteria;
            search = new ScopSearch();
        }
        else { // CHAIN
            ChainSearchCriteria chainCriteria = new ChainSearchCriteria();
            chainCriteria.dbId = dbId;
            chainCriteria.limit = 400;

            criteria = chainCriteria;
            search = new ChainSearch();
        }

        Cache.cacheAlignmentScores(dbId, criteria, search);
    }

    private static void option_t(CommandLine line) {

        String[] args = line.getOptionValues("t");

        if (DbId.isScopId(args[0])) {
            System.out.println("It is a SCOP ID");
        }
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
