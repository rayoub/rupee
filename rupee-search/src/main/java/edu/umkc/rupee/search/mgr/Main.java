package edu.umkc.rupee.search.mgr;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.umkc.rupee.search.lib.ChainDefs;
import edu.umkc.rupee.search.lib.Constants;

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
        group.addOption(Option.builder("t")
                .longOpt("tm-align")
                .numberOfArgs(2)
                .argName("DB_ID_OR_PATH>,<DB_ID")
                .valueSeparator(',')
                .build());
        group.addOption(Option.builder("c")
                .longOpt("chain-defs")
                .numberOfArgs(1)
                .argName("VERSION")
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
        
        // header or not
        boolean printHeader = true; 
        boolean printMetaDataColumns = true;
        
        try {
            if (line.hasOption("i")) {
                OptionFunctions.option_i(line);
            } else if (line.hasOption("h")) {
                OptionFunctions.option_h(line);
            } else if (line.hasOption("s")) {
                OptionFunctions.option_s(line, printHeader, printMetaDataColumns);
            } else if (line.hasOption("u")) {
                OptionFunctions.option_u(line, printHeader, printMetaDataColumns);
            } else if (line.hasOption("t")) {
                OptionFunctions.option_t(line);
            } else if (line.hasOption("c")) {
                option_c(line);
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
    
    private static void option_c(CommandLine line) throws Exception {

        String[] args = line.getOptionValues("c");
        
        String version = args[0];
        
        ChainDefs.writePdbChains(version);
        ChainDefs.writeObsoleteChains(version);
    }

    private static void option_d(CommandLine line) throws Exception {

        System.out.println("Done Debugging");
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
