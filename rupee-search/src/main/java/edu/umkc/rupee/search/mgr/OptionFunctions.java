package edu.umkc.rupee.search.mgr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;

import edu.umkc.rupee.search.afdb.AfdbHash;
import edu.umkc.rupee.search.afdb.AfdbImport;
import edu.umkc.rupee.search.afdb.AfdbSearch;
import edu.umkc.rupee.search.afdb.AfdbSearchCriteria;
import edu.umkc.rupee.search.afdb.AfdbSearchRecord;
import edu.umkc.rupee.search.base.SearchRecord;
import edu.umkc.rupee.search.base.SearchResults;
import edu.umkc.rupee.search.cath.CathHash;
import edu.umkc.rupee.search.cath.CathImport;
import edu.umkc.rupee.search.cath.CathSearch;
import edu.umkc.rupee.search.cath.CathSearchCriteria;
import edu.umkc.rupee.search.cath.CathSearchRecord;
import edu.umkc.rupee.search.chain.ChainHash;
import edu.umkc.rupee.search.chain.ChainImport;
import edu.umkc.rupee.search.chain.ChainSearch;
import edu.umkc.rupee.search.chain.ChainSearchCriteria;
import edu.umkc.rupee.search.chain.ChainSearchRecord;
import edu.umkc.rupee.search.defs.DbType;
import edu.umkc.rupee.search.defs.SearchBy;
import edu.umkc.rupee.search.defs.SearchMode;
import edu.umkc.rupee.search.defs.SearchType;
import edu.umkc.rupee.search.defs.SortBy;
import edu.umkc.rupee.search.dir.DirHash;
import edu.umkc.rupee.search.dir.DirImport;
import edu.umkc.rupee.search.dir.DirInit;
import edu.umkc.rupee.search.dir.DirSearch;
import edu.umkc.rupee.search.dir.DirSearchCriteria;
import edu.umkc.rupee.search.dir.DirSearchRecord;
import edu.umkc.rupee.search.ecod.EcodHash;
import edu.umkc.rupee.search.ecod.EcodImport;
import edu.umkc.rupee.search.ecod.EcodSearch;
import edu.umkc.rupee.search.ecod.EcodSearchCriteria;
import edu.umkc.rupee.search.ecod.EcodSearchRecord;
import edu.umkc.rupee.search.lib.Aligning;
import edu.umkc.rupee.search.lib.DbId;
import edu.umkc.rupee.search.lib.Uploading;
import edu.umkc.rupee.search.scop.ScopHash;
import edu.umkc.rupee.search.scop.ScopImport;
import edu.umkc.rupee.search.scop.ScopSearch;
import edu.umkc.rupee.search.scop.ScopSearchCriteria;
import edu.umkc.rupee.search.scop.ScopSearchRecord;
import edu.umkc.rupee.tm.TmMode;
import edu.umkc.rupee.tm.TmResults;

public class OptionFunctions {

    public static void option_i(CommandLine line) throws Exception {

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
        else if (dbType == DbType.CHAIN) { 
            ChainImport chainImport = new ChainImport();
            chainImport.importGrams();
        }
        else if (dbType == DbType.AFDB) {
            AfdbImport afdbImport = new AfdbImport();
            afdbImport.importGrams();;
        }
        else { // DIR

            // first initialize dir tables
            DirInit.init();

            DirImport dirImport = new DirImport();
            dirImport.importGrams();
        }
        
        System.out.println("Done importing!");
    }
    
    public static void option_h(CommandLine line) {

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
        else if (dbType == DbType.CHAIN) { 
            ChainHash chainHash = new ChainHash();
            chainHash.hash(); 
        }
        else if (dbType == DbType.AFDB) {
            AfdbHash afdbHash = new AfdbHash();
            afdbHash.hash();
        }
        else { // DIR
            DirHash dirHash = new DirHash();
            dirHash.hash();
        }
        
        System.out.println("Done hashing!");
    }
    
    public static void option_s(CommandLine line, boolean printHeader, boolean printMetaDataColumns) throws Exception {

        OptionFunctions.option_s_and_u("s", SearchBy.DB_ID, 400, line, printHeader, printMetaDataColumns); 
    }
    
    public static void option_u(CommandLine line, boolean printHeader, boolean printMetaDataColumns) throws Exception {
       
        OptionFunctions.option_s_and_u("u", SearchBy.UPLOAD, 400, line, printHeader, printMetaDataColumns); 
    }
    
    private static void option_s_and_u(String option, SearchBy searchBy, int limit, CommandLine line, boolean printHeader, boolean printMetaDataColumns) throws Exception {

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
            
            if (dbType == DbType.DIR) {

                // if we are searching DIR the id has to be DIR
                idDbType = DbType.DIR;
            }
            else {
                idDbType = DbId.getIdDbType(idOrPath);  
            }
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
        
        // enforce some sorts based on search type 
        SortBy sortBy;
        if (searchType == SearchType.RMSD) {
            sortBy = SortBy.RMSD;
        }
        else {
            sortBy = SortBy.TM_SCORE;
        }
        
        //*************************************************************
        //***  OUTPUT
        //*************************************************************

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
      
            // print header
            if (printHeader) {
                String header = "n,db_id_1,db_id_2,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                if (searchBy == SearchBy.UPLOAD) {
                    header = "n,file_name,db_id,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                }
                if (printMetaDataColumns) {
                    header += ",cl,cf,sf,fa,fold,superfamily";
                }
                System.out.println(header);
            }   

            for (SearchRecord baseRecord : records) {
           
                ScopSearchRecord record = (ScopSearchRecord) baseRecord;

                String data = String.format("%d,%s,%s,%.4f,%.4f,%s,%s",
                    record.getN(),
                    criteria.dbId,
                    record.getDbId(),
                    record.getRmsd(),
                    record.getTmScore(),
                    criteria.searchMode.name().toLowerCase(),
                    criteria.searchType.name().toLowerCase()
                );

                if (printMetaDataColumns) {
                    data += String.format(",%s,%s,%s,%s,%s,%s",
                        record.getCl(), 
                        record.getCf(),
                        record.getSf(),
                        record.getFa(),
                        record.getCfDescription(),
                        record.getSfDescription()
                    );
                }

                System.out.println(data);
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

            // print headers 
            if (printHeader) {
                String header = "n,db_id_1,db_id_2,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                if (searchBy == SearchBy.UPLOAD) {
                    header = "n,file_name,db_id,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                }
                if (printMetaDataColumns) {
                    header += ",c,a,t,h,s35,topology,superfamily";
                }
                System.out.println(header);
            }   

            for (SearchRecord baseRecord : records) {
             
                CathSearchRecord record = (CathSearchRecord) baseRecord; 

                String data = String.format("%d,%s,%s,%.4f,%.4f,%s,%s",
                    record.getN(),
                    criteria.dbId,
                    record.getDbId(),
                    record.getRmsd(),
                    record.getTmScore(),
                    criteria.searchMode.name().toLowerCase(),
                    criteria.searchType.name().toLowerCase()
                );

                if (printMetaDataColumns) {
                    data += String.format(",%s,%s,%s,%s,%s,%s,%s",
                        record.getC(),
                        record.getA(),
                        record.getT(),
                        record.getH(),
                        record.getS(),
                        record.getTDescription(),
                        record.getHDescription()
                    );
                }

                System.out.println(data);
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

            // print headers 
            if (printHeader) {
                String header = "n,db_id_1,db_id_2,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                if (searchBy == SearchBy.UPLOAD) {
                    header = "n,file_name,db_id,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                }
                System.out.println(header);
            }   
        
            for (SearchRecord baseRecord : records) {
           
                EcodSearchRecord record = (EcodSearchRecord) baseRecord;

                // gathering results
                System.out.printf("%d,%s,%s,%.4f,%.4f,%s,%s\n",
                    record.getN(),
                    criteria.dbId,
                    record.getDbId(),
                    record.getRmsd(),
                    record.getTmScore(),
                    criteria.searchMode.name().toLowerCase(),
                    criteria.searchType.name().toLowerCase()
                );
            }
        }
        else if (dbType == DbType.CHAIN) { // CHAIN
            
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

            // print headers 
            if (printHeader) {
                String header = "n,db_id_1,db_id_2,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                if (searchBy == SearchBy.UPLOAD) {
                    header = "n,file_name,db_id,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                }
                System.out.println(header);
            }   
        
            for (SearchRecord baseRecord : records) {
           
                ChainSearchRecord record = (ChainSearchRecord) baseRecord;

                // gathering results
                System.out.printf("%d,%s,%s,%.4f,%.4f,%s,%s\n",
                    record.getN(),
                    criteria.dbId,
                    record.getDbId(),
                    record.getRmsd(),
                    record.getTmScore(),
                    criteria.searchMode.name().toLowerCase(),
                    criteria.searchType.name().toLowerCase()
                );
            }
        }
        else if (dbType == DbType.AFDB) { // AFDB
        
            AfdbSearchCriteria criteria = new AfdbSearchCriteria();
            
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

            AfdbSearch afdbSearch = new AfdbSearch();
            SearchResults results = afdbSearch.search(criteria);
            List<SearchRecord> records = results.getRecords();

            // print headers 
            if (printHeader) {
                String header = "n,db_id_1,db_id_2,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                if (searchBy == SearchBy.UPLOAD) {
                    header = "n,file_name,db_id,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                }
                System.out.println(header);
            }   
        
            for (SearchRecord baseRecord : records) {
           
                AfdbSearchRecord record = (AfdbSearchRecord) baseRecord;

                // gathering results
                System.out.printf("%d,%s,%s,%.4f,%.4f,%s,%s\n",
                    record.getN(),
                    criteria.dbId,
                    record.getDbId(),
                    record.getRmsd(),
                    record.getTmScore(),
                    criteria.searchMode.name().toLowerCase(),
                    criteria.searchType.name().toLowerCase()
                );
            }
        }
        else { // DIR
            
            DirSearchCriteria criteria = new DirSearchCriteria();
            
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

            DirSearch dirSearch = new DirSearch();
            SearchResults results = dirSearch.search(criteria);
            List<SearchRecord> records = results.getRecords();

            // print headers 
            if (printHeader) {
                String header = "n,db_id_1,db_id_2,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                if (searchBy == SearchBy.UPLOAD) {
                    header = "n,file_name,db_id,rmsd,tm_score,q_score,ssap_score,search_mode,search_type";
                }
                System.out.println(header);
            }   
        
            for (SearchRecord baseRecord : records) {
           
                DirSearchRecord record = (DirSearchRecord) baseRecord;

                // gathering results
                System.out.printf("%d,%s,%s,%.4f,%.4f,%s,%s\n",
                    record.getN(),
                    criteria.dbId,
                    record.getDbId(),
                    record.getRmsd(),
                    record.getTmScore(),
                    criteria.searchMode.name().toLowerCase(),
                    criteria.searchType.name().toLowerCase()
                );
            }
        }
    }
    
    public static void option_t(CommandLine line) throws IOException {

        String[] args = line.getOptionValues("t");
      
        String idOrPath = args[0];
        String dbId = args[1];

        int uploadId = -1;
       
        // *** db id or path
        
        DbType idDbType1 = DbId.getIdDbType(idOrPath);
        if (idDbType1 != DbType.INVALID) {

            // we got a db id
            idOrPath = DbId.getNormalizedId(idOrPath);
        }
        else if (Files.exists(Paths.get(idOrPath))) {

            // we got a file
            idDbType1 = DbType.INVALID;
            
            Path path = Paths.get(idOrPath);
            byte[] bytes = Files.readAllBytes(path);
            String content = new String(bytes);
            uploadId = Uploading.upload(content);
        } 
        else {

            System.out.println("Either 'File Not Found' or\n'Bad DB_ID' or\n'DB_TYPE DIR is not supported' for first parameter.");
            return;
        }
       
        // *** db id
        
        DbType idDbType2 = DbId.getIdDbType(dbId);
        if (idDbType2 != DbType.INVALID) {

            // we got a db id
            dbId = DbId.getNormalizedId(dbId);
        }
        else {

            // assume DIR type
            System.out.println("Either 'Bad DB_ID' or\n'DB_TYPE DIR is not supported' for second parameter.");
            return;
        }

        // perform alignment
        TmResults results = null;
        if (uploadId != -1) {

            results = Aligning.tmAlign(uploadId, dbId, TmMode.ALIGN_TEXT);
        }
        else {

            results = Aligning.tmAlign(idOrPath, dbId, TmMode.ALIGN_TEXT);
        }

        System.out.println(results.getOutput());
    }
}

