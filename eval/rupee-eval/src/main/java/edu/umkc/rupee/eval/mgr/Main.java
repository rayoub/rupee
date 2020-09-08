package edu.umkc.rupee.eval.mgr;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
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

import edu.umkc.rupee.core.Parse;
import edu.umkc.rupee.eval.defs.AlignmentType;
import edu.umkc.rupee.eval.lib.AlignRecord;
import edu.umkc.rupee.eval.lib.Aligning;
import edu.umkc.rupee.eval.lib.Constants;
import edu.umkc.rupee.search.defs.DbType;
import edu.umkc.rupee.search.defs.SearchType;
import edu.umkc.rupee.search.lib.Db;
import edu.umkc.rupee.search.lib.Grams;
import edu.umkc.rupee.search.lib.Hashes;
import edu.umkc.rupee.search.lib.LCS;
import edu.umkc.rupee.search.lib.Similarity;
import edu.umkc.rupee.search.mgr.OptionFunctions;

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
                .argName("ALIGN_TYPE>,<DB_ID_1>,<DB_ID_2")
                .valueSeparator(',')
                .build());
        group.addOption(Option.builder("f")
                .longOpt("lcs-fulllength")
                .numberOfArgs(3)
                .argName("DB_TYPE>,<DB_ID_1>,<DB_ID_2")
                .valueSeparator(',')
                .build());
        group.addOption(Option.builder("c")
                .longOpt("lcs-containment")
                .numberOfArgs(3)
                .argName("DB_TYPE>,<DB_ID_1>,<DB_ID_2")
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
       
        // columns or not
        boolean printColumns = false;

        try {
            if (line.hasOption("i")) {
                OptionFunctions.option_i(line);
            } else if (line.hasOption("h")) {
                OptionFunctions.option_h(line);
            } else if (line.hasOption("a")) {
                option_a(line);
            } else if (line.hasOption("f")) {
                option_f(line);
            } else if (line.hasOption("c")) {
                option_c(line);
            } else if (line.hasOption("s")) {
                OptionFunctions.option_s(line, printColumns);
            } else if (line.hasOption("u")) {
                OptionFunctions.option_u(line, printColumns);
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

    private static void option_a(CommandLine line) throws SQLException {

        // only for aligning with CE and FATCAT from CLI
        
        Set<String> alignNames = new HashSet<>(Arrays.stream(AlignmentType.values()).map(v -> v.name()).collect(Collectors.toList()));
        
        String[] args = line.getOptionValues("a");

        if (!alignNames.contains(args[0])) {
            System.err.println("The <ALIGN> argument must be one of " + alignNames.toString());
            return;
        }
        
        AlignmentType align = AlignmentType.valueOf(args[0]);
        
        String dbId1 = args[1];
        String dbId2 = args[2];

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

    private static void option_f(CommandLine line) throws SQLException {
        
        Set<String> dbTypeNames = new HashSet<>(Arrays.stream(DbType.values()).map(v -> v.name()).collect(Collectors.toList()));

        String[] args = line.getOptionValues("f");
       
        if (!dbTypeNames.contains(args[0])) {
            System.err.println("The <DB_TYPE> argument must be one of " + dbTypeNames.toString());
            return;
        }
       
        DbType dbType = DbType.valueOf(args[0]);
        
        String dbId1 = args[1];
        String dbId2 = args[2];

        int uploadId1 = Parse.tryParseInt(dbId1); 
        Hashes hashes1;
        Grams grams1;
        if (uploadId1 != -1) {

            hashes1 = Db.getUploadHashes(uploadId1);
            grams1 = Db.getUploadGrams(uploadId1);
        }
        else {

            hashes1 = Db.getHashes(dbId1, dbType);
            grams1 = Db.getGrams(dbId1, dbType, true);
        }
        
        Hashes hashes2 = Db.getHashes(dbId2, dbType);
        Grams grams2 = Db.getGrams(dbId2, dbType, true);

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
        
        Set<String> dbTypeNames = new HashSet<>(Arrays.stream(DbType.values()).map(v -> v.name()).collect(Collectors.toList()));
        
        String[] args = line.getOptionValues("c");
       
        if (!dbTypeNames.contains(args[0])) {
            System.err.println("The <DB_TYPE> argument must be one of " + dbTypeNames.toString());
            return;
        }
       
        DbType dbType = DbType.valueOf(args[0]);
        
        String dbId1 = args[1];
        String dbId2 = args[2];

        int uploadId1 = Parse.tryParseInt(dbId1);

        Hashes hashes1;
        Grams grams1;
        if (uploadId1 != -1) {

            hashes1 = Db.getUploadHashes(uploadId1);
            grams1 = Db.getUploadGrams(uploadId1);
        }
        else {

            hashes1 = Db.getHashes(dbId1, dbType);
            grams1 = Db.getGrams(dbId1, dbType, true);
        }
        
        Hashes hashes2 = Db.getHashes(dbId2, dbType);
        Grams grams2 = Db.getGrams(dbId2, dbType, true);

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

    private static void option_d(CommandLine line) throws Exception {

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
}
