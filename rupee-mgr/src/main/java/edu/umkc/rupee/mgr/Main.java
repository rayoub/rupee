package edu.umkc.rupee.mgr;

import java.nio.file.Files;
import java.nio.file.Path;
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
import edu.umkc.rupee.base.SearchResults;
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
import edu.umkc.rupee.lib.Grams;
import edu.umkc.rupee.lib.Hashes;
import edu.umkc.rupee.lib.LCS;
import edu.umkc.rupee.lib.Similarity;
import edu.umkc.rupee.lib.Uploading;
import edu.umkc.rupee.scop.ScopHash;
import edu.umkc.rupee.scop.ScopImport;
import edu.umkc.rupee.scop.ScopSearch;
import edu.umkc.rupee.scop.ScopSearchCriteria;
import edu.umkc.rupee.scop.ScopSearchRecord;
import edu.umkc.rupee.tm.TmMode;
import edu.umkc.rupee.tm.TmResults;

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
                .argName("DB_ID_1>,<DB_ID_2>,<ALIGN_TYPE")
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
                .longOpt("search-dbid")
                .numberOfArgs(10)
                .argName("DB_TYPE>,<DB_ID>,<REP1>,<REP2>,<REP3>,<DIFF1>,<DIFF2>,<DIFF3>,<SEARCH_MODE>,<SEARCH_TYPE")
                .valueSeparator(',')
                .build());
        group.addOption(Option.builder("u")
                .longOpt("search-upload")
                .numberOfArgs(10)
                .argName("DB_TYPE>,<FILE_PATH>,<REP1>,<REP2>,<REP3>,<DIFF1>,<DIFF2>,<DIFF3>,<SEARCH_MODE>,<SEARCH_TYPE")
                .valueSeparator(',')
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

        // only for aligning with CE and FATCAT from CLI
        
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

        TmResults results = Aligning.tmAlign(dbId1, dbId2, TmMode.ALIGN_TEXT);
        System.out.print(results.getOutput());
    }

    private static void option_f(CommandLine line) throws SQLException {
        
        String[] args = line.getOptionValues("f");
        
        String dbId1 = args[0];
        String dbId2 = args[1];

        int uploadId1 = tryParseInt(dbId1);

        Hashes hashes1;
        Grams grams1;
        if (uploadId1 != -1) {

            hashes1 = Db.getUploadHashes(uploadId1);
            grams1 = Db.getUploadGrams(uploadId1);
        }
        else {

            DbType dbType1 = DbId.getIdDbType(dbId1);
            hashes1 = Db.getHashes(dbId1, dbType1);
            grams1 = Db.getGrams(dbId1, dbType1, true);
        }
        
        DbType dbType2 = DbId.getIdDbType(dbId2);
        Hashes hashes2 = Db.getHashes(dbId2, dbType2);
        Grams grams2 = Db.getGrams(dbId2, dbType2, true);

        if (hashes1 != null && hashes2 != null) {

            // band matches
            String bandMatches = Similarity.getBandMatches(hashes1.bandHashes, hashes2.bandHashes);

            // estimated and exact based on overlapping grams
            double estimated = Similarity.getEstimatedSimilarity(hashes1.minHashes, hashes2.minHashes);
            double exact = Similarity.getExactSimilarity(grams1.getGramsAsList(), grams2.getGramsAsList());

            // lcs validated matching grams
            double score = LCS.getLCSScore(grams1.getGramsAsList(), grams2.getGramsAsList(), SearchType.FULL_LENGTH); 

            // rough tm-score estimate
            double rough_tm = LCS.getLCSPlusScore(grams1, grams2, SearchType.FULL_LENGTH);
          
            System.out.println(""); 
            System.out.println("Structure 1 Length:     " + grams1.getLength());
            System.out.println("Structure 2 Length:     " + grams2.getLength());
            System.out.println(""); 
            System.out.println("Band matches:           " + bandMatches);
            System.out.println(""); 
            System.out.println("Estimated Similarity:   " + estimated);
            System.out.println("Exact Similarity:       " + exact);
            System.out.println(""); 
            System.out.println("LCS Score:              " + score);
            System.out.println("Tm-Score Estimate:      " + rough_tm);

            Map<Integer, String> codeMap = LCS.getCodeMap(grams1.getGramsAsList(), grams2.getGramsAsList());
            LCS.printLCSFullLength(grams1.getGramsAsList(), grams2.getGramsAsList(), codeMap);
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

        int uploadId1 = tryParseInt(dbId1);

        Hashes hashes1;
        Grams grams1;
        if (uploadId1 != -1) {

            hashes1 = Db.getUploadHashes(uploadId1);
            grams1 = Db.getUploadGrams(uploadId1);
        }
        else {

            DbType dbType1 = DbId.getIdDbType(dbId1);
            hashes1 = Db.getHashes(dbId1, dbType1);
            grams1 = Db.getGrams(dbId1, dbType1, true);
        }
        
        DbType dbType2 = DbId.getIdDbType(dbId2);
        Hashes hashes2 = Db.getHashes(dbId2, dbType2);
        Grams grams2 = Db.getGrams(dbId2, dbType2, true);

        if (hashes1 != null && hashes2 != null) {

            // lcs validated matching grams
            double score = LCS.getLCSScore(grams1.getGramsAsList(), grams2.getGramsAsList(), SearchType.CONTAINED_IN); 
            
            // rough tm-score estimate
            double rough_tm = LCS.getLCSPlusScore(grams1, grams2, SearchType.CONTAINED_IN);
          
            System.out.println(""); 
            System.out.println("Structure 1 Length:     " + grams1.getLength());
            System.out.println("Structure 2 Length:     " + grams2.getLength());
            System.out.println(""); 
            System.out.println("LCS Score:              " + score);
            System.out.println("Tm-Score Estimate:      " + rough_tm);

            Map<Integer, String> codeMap = LCS.getCodeMap(grams1.getGramsAsList(), grams2.getGramsAsList());
            LCS.printLCSContainment(grams1.getGramsAsList(), grams2.getGramsAsList(), codeMap);
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

        option_s_and_u("s", SearchBy.DB_ID, 400, line); 
    }
    
    private static void option_u(CommandLine line) throws Exception {
       
        option_s_and_u("u", SearchBy.UPLOAD, 400, line); 
    }
    
    private static void option_s_and_u(String option, SearchBy searchBy, int limit, CommandLine line) throws Exception {

        Set<String> dbTypeNames = new HashSet<>(Arrays.stream(DbType.values()).map(v -> v.name()).collect(Collectors.toList()));
        Set<String> searchModeNames = new HashSet<>(Arrays.stream(SearchMode.values()).map(v -> v.name()).collect(Collectors.toList()));
        Set<String> searchTypeNames = new HashSet<>(Arrays.stream(SearchType.values()).map(v -> v.name()).collect(Collectors.toList()));

        String[] args = line.getOptionValues(option);
       
        if (!dbTypeNames.contains(args[0])) {
            System.err.println("The <DB_TYPE> argument must be one of " + dbTypeNames.toString());
            return;
        }
       
        DbType dbType = DbType.valueOf(args[0]);

        // id or path
        int uploadId = -1;
        String idOrPath = args[1];
        DbType idDbType = DbType.INVALID;
        if (searchBy == SearchBy.DB_ID) {
            idDbType = DbId.getIdDbType(idOrPath);  
        }
        else { // searchBy == SearchBy.UPLOAD

            if (Files.notExists(Paths.get(idOrPath))) {
                System.out.println("File Not Found: " + idOrPath);
                return;
            }

            Path path = Paths.get(idOrPath);
            byte[] bytes = Files.readAllBytes(path);
            String content = new String(bytes);
            uploadId = Uploading.upload(content);

            idOrPath = path.getFileName().toString().replace(".pdb","");
        }

        // booleans
        if (!args[2].equals("TRUE") && !args[2].equals("FALSE")) {
            System.err.println("The <REP1> argument must be TRUE or FALSE");
            return;
        }
        if (!args[3].equals("TRUE") && !args[3].equals("FALSE")) {
            System.err.println("The <REP2> argument must be TRUE or FALSE");
            return;
        }
        if (!args[4].equals("TRUE") && !args[4].equals("FALSE")) {
            System.err.println("The <REP3> argument must be TRUE or FALSE");
            return;
        }
        if (!args[5].equals("TRUE") && !args[5].equals("FALSE")) {
            System.err.println("The <DIFF1> argument must be TRUE or FALSE");
            return;
        }
        if (!args[6].equals("TRUE") && !args[6].equals("FALSE")) {
            System.err.println("The <DIFF2> argument must be TRUE or FALSE");
            return;
        }
        if (!args[7].equals("TRUE") && !args[7].equals("FALSE")) {
            System.err.println("The <DIFF3> argument must be TRUE or FALSE");
            return;
        }

        boolean rep1 = Boolean.parseBoolean(args[2]);
        boolean rep2 = Boolean.parseBoolean(args[3]);
        boolean rep3 = Boolean.parseBoolean(args[4]);
        boolean diff1 = Boolean.parseBoolean(args[5]);
        boolean diff2 = Boolean.parseBoolean(args[6]);
        boolean diff3 = Boolean.parseBoolean(args[7]);

        // search mode
        if (!searchModeNames.contains(args[8])) {
            System.err.println("The <SEARCH_MODE> argument must be one of " + searchModeNames.toString());
            return;
        }

        SearchMode searchMode = SearchMode.valueOf(args[8]);

        // search type 
        if (!searchTypeNames.contains(args[9])) {
            System.err.println("The <SEARCH_TYPE> argument must be one of " + searchTypeNames.toString());
            return;
        }

        SearchType searchType = SearchType.valueOf(args[9]);

        // enforce assumptions
        SortBy sortBy;
        if (searchMode == SearchMode.FAST) {
            sortBy = SortBy.SIMILARITY;
        } 
        else if (searchType == SearchType.RMSD) {
            sortBy = SortBy.RMSD;
        }
        else if (searchType == SearchType.Q_SCORE) {
            sortBy = SortBy.Q_SCORE;
        }
        else if (searchType == SearchType.SSAP_SCORE) {
            sortBy = SortBy.SSAP_SCORE;
        }
        else {
            sortBy = SortBy.TM_SCORE;
        }
        
        //*************************************************************
        //***  OUTPUT
        //*************************************************************

        boolean printColumns = true;
        if (dbType == DbType.SCOP) { 

            ScopSearchCriteria criteria = new ScopSearchCriteria();
            
            criteria.searchBy = searchBy;
            criteria.idDbType = idDbType;
            criteria.searchDbType = dbType;
            criteria.dbId = idOrPath;
            if (criteria.searchBy == SearchBy.UPLOAD) {
                criteria.uploadId = uploadId;
            }

            criteria.limit = limit;
            criteria.searchType = searchType;
            criteria.searchMode = searchMode;
            criteria.sortBy = sortBy;
            criteria.differentFold = diff1;
            criteria.differentSuperfamily = diff2;
            criteria.differentFamily = diff3;

            ScopSearch scopSearch = new ScopSearch();

            SearchResults results = scopSearch.search(criteria);
            List<SearchRecord> records = results.getRecords();
      
            // column headers 
            if (printColumns) {
                String columns = "n,db_id_1,db_id_2,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                if (searchBy == SearchBy.UPLOAD) {
                    columns = "n,file_name,db_id,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                }
                System.out.println(columns);
            }   

            for (SearchRecord baseRecord : records) {
           
                ScopSearchRecord record = (ScopSearchRecord) baseRecord;

                // gathering results
                System.out.printf("%d,%s,%s,%.4f,%.4f,%.4f,%.4f,%s,%s\n",
                    record.getN(),
                    criteria.dbId,
                    record.getDbId(),
                    record.getRmsd(),
                    record.getTmScore(),
                    record.getQScore(),
                    record.getSsapScore(),
                    criteria.searchMode.name().toLowerCase(),
                    criteria.searchType.name().toLowerCase()
                );
            }
        }
        else if (dbType == DbType.CATH) {
        
            CathSearchCriteria criteria = new CathSearchCriteria();

            criteria.searchBy = searchBy;
            criteria.idDbType = idDbType;
            criteria.searchDbType = dbType;
            criteria.dbId = idOrPath;
            if (criteria.searchBy == SearchBy.UPLOAD) {
                criteria.uploadId = uploadId;
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

            SearchResults results = cathSearch.search(criteria);
            List<SearchRecord> records = results.getRecords();

            // column headers 
            if (printColumns) {
                String columns = "n,db_id_1,db_id_2,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                if (searchBy == SearchBy.UPLOAD) {
                    columns = "n,file_name,db_id,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                }
                System.out.println(columns);
            }   

            for (SearchRecord baseRecord : records) {
             
                CathSearchRecord record = (CathSearchRecord) baseRecord; 

                // gathering results
                System.out.printf("%d,%s,%s,%.4f,%.4f,%.4f,%.4f,%s,%s\n",
                    record.getN(),
                    criteria.dbId,
                    record.getDbId(),
                    record.getRmsd(),
                    record.getTmScore(),
                    record.getQScore(),
                    record.getSsapScore(),
                    criteria.searchMode.name().toLowerCase(),
                    criteria.searchType.name().toLowerCase()
                );
            }
        }
        else if (dbType == DbType.ECOD) {

            EcodSearchCriteria criteria = new EcodSearchCriteria();
            
            criteria.searchBy = searchBy;
            criteria.idDbType = idDbType;
            criteria.searchDbType = dbType;
            criteria.dbId = idOrPath;
            if (criteria.searchBy == SearchBy.UPLOAD) {
                criteria.uploadId = uploadId;
            }

            criteria.limit = limit;
            criteria.searchType = searchType;
            criteria.searchMode = searchMode;
            criteria.sortBy = sortBy;
            criteria.differentH = diff1;
            criteria.differentT = diff2;
            criteria.differentF = diff3;

            EcodSearch ecodSearch = new EcodSearch();
            SearchResults results = ecodSearch.search(criteria);
            List<SearchRecord> records = results.getRecords();

            // column headers 
            if (printColumns) {
                String columns = "n,db_id_1,db_id_2,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                if (searchBy == SearchBy.UPLOAD) {
                    columns = "n,file_name,db_id,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                }
                System.out.println(columns);
            }   
        
            for (SearchRecord baseRecord : records) {
           
                EcodSearchRecord record = (EcodSearchRecord) baseRecord;

                // gathering results
                System.out.printf("%d,%s,%s,%.4f,%.4f,%.4f,%.4f,%s,%s\n",
                    record.getN(),
                    criteria.dbId,
                    record.getDbId(),
                    record.getRmsd(),
                    record.getTmScore(),
                    record.getQScore(),
                    record.getSsapScore(),
                    criteria.searchMode.name().toLowerCase(),
                    criteria.searchType.name().toLowerCase()
                );
            }
        }
        else { // CHAIN
            
            ChainSearchCriteria criteria = new ChainSearchCriteria();
            
            criteria.searchBy = searchBy;
            criteria.idDbType = idDbType;
            criteria.searchDbType = dbType;
            criteria.dbId = idOrPath;
            if (criteria.searchBy == SearchBy.UPLOAD) {
                criteria.uploadId = uploadId;
            }

            criteria.limit = limit;
            criteria.searchType = searchType;
            criteria.searchMode = searchMode;
            criteria.sortBy = sortBy;

            ChainSearch chainSearch = new ChainSearch();
            SearchResults results = chainSearch.search(criteria);
            List<SearchRecord> records = results.getRecords();

            // column headers 
            if (printColumns) {
                String columns = "n,db_id_1,db_id_2,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                if (searchBy == SearchBy.UPLOAD) {
                    columns = "n,file_name,db_id,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                }
                System.out.println(columns);
            }   
        
            for (SearchRecord baseRecord : records) {
           
                ChainSearchRecord record = (ChainSearchRecord) baseRecord;

                // gathering results
                System.out.printf("%d,%s,%s,%.4f,%.4f,%.4f,%.4f,%s,%s\n",
                    record.getN(),
                    criteria.dbId,
                    record.getDbId(),
                    record.getRmsd(),
                    record.getTmScore(),
                    record.getQScore(),
                    record.getSsapScore(),
                    criteria.searchMode.name().toLowerCase(),
                    criteria.searchType.name().toLowerCase()
                );
            }
        }
    }

    private static void option_d(CommandLine line) throws Exception {
       
        /*
        ChainDefs.printChains("/home/ayoub/git/rupee/data/pdb/pdb");
        ChainDefs.printChains("/home/ayoub/git/rupee/data/pdb/obsolete");
        */

        /*
        // web site automation
        CathedralDriver driver = new CathedralDriver();

        driver.setUp();
        driver.doSearchBatch();
        driver.tearDown();
        */

        /*
        // gather alignment scores for running results
        AlignResults.alignRupeeResults("casp_d250", "casp_scop_v2_07", DbType.SCOP, 100);
        AlignResults.alignRupeeResults("scop_d360", "scop_v2_07", DbType.SCOP, 100);
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
