package edu.umkc.rupee.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

import org.biojava.nbio.structure.Structure;
import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.bio.Parser;
import edu.umkc.rupee.defs.DbType;
import edu.umkc.rupee.defs.SearchBy;
import edu.umkc.rupee.defs.SearchFrom;
import edu.umkc.rupee.defs.SearchMode;
import edu.umkc.rupee.defs.SearchType;
import edu.umkc.rupee.defs.SortBy;
import edu.umkc.rupee.lib.Constants;
import edu.umkc.rupee.lib.Db;
import edu.umkc.rupee.lib.Hashes;
import edu.umkc.rupee.lib.LCS;
import edu.umkc.rupee.lib.Similarity;
import edu.umkc.rupee.tm.Mode;
import edu.umkc.rupee.tm.TMAlign;

public abstract class Search {

    // *********************************************************************
    // Abstract Methods 
    // *********************************************************************
    
    public abstract DbType getDbType();

    public abstract PreparedStatement getSplitSearchStatement(SearchCriteria criteria, int splitIndex, Connection conn) throws SQLException;

    public abstract PreparedStatement getBandSearchStatement(SearchCriteria criteria, int bandIndex, Connection conn) throws SQLException;

    public abstract void augment(SearchRecord record, ResultSet rs) throws SQLException;

    public abstract SearchRecord getSearchRecord();

    // *********************************************************************
    // Instance Methods
    // *********************************************************************
 
    @SuppressWarnings("unused") 
    public List<SearchRecord> search(SearchCriteria criteria, SearchFrom searchFrom) throws Exception {

        List<SearchRecord> records = new ArrayList<>();
        
        List<Integer> grams = null;
        Hashes hashes = null;

        if (criteria.searchBy == SearchBy.DB_ID) {
            
            grams = Db.getGrams(criteria.dbId, criteria.idDbType);
            hashes = Db.getHashes(criteria.dbId, criteria.idDbType);
        }
        else { // UPLOAD

            grams = Db.getUploadGrams(criteria.uploadId);
            hashes = Db.getUploadHashes(criteria.uploadId);
        }

        final List<Integer> grams1 = grams;
        final Hashes hashes1 = hashes;

        if (grams1 != null && hashes1 != null) {

            // if (criteria.searchTypes.contains(SearchType.FULL_LENGTH) && criteria.searchTypes.size() == 1) {
            if (false) {

                // parallel band match searches to gather lsh candidates
                records = IntStream.range(0, Constants.BAND_CHECK_COUNT).boxed().parallel()
                    .flatMap(bandIndex -> searchBand(bandIndex, criteria, hashes1).stream())
                    .sorted(Comparator.comparingDouble(SearchRecord::getSimilarity).reversed().thenComparing(SearchRecord::getSortKey))
                    .limit(criteria.searchMode.getLshCandidateCount()) 
                    .collect(Collectors.toList());
                
                // cache map of residue grams
                List<String> dbIds = records.stream().map(SearchRecord::getDbId).collect(Collectors.toList());
                Map<String, List<Integer>> map = Db.getGrams(dbIds, criteria.searchDbType);

                // parallel lcs algorithm
                records.stream()
                    .forEach(record -> {

                        if (map.containsKey(record.getDbId())) {
                            List<Integer> grams2 = map.get(record.getDbId());
                            double score = LCS.getLCSScoreFullLength(grams1, grams2); 
                            record.setSimilarity(score);
                        }
                    });

                // sort lcs candidates
                records = records.stream()
                    .sorted(Comparator.comparingDouble(SearchRecord::getSimilarity).reversed().thenComparing(SearchRecord::getSortKey))
                    .collect(Collectors.toList());

            }
            else { // 'contained in' or 'contains' selected

                // split grams and search in parallel on splits
                records = IntStream.range(0, Constants.SPLIT_COUNT).boxed().parallel()
                    .flatMap(splitIndex -> gramsSplit(splitIndex, criteria, grams1).stream())
                    .sorted(Comparator.comparingDouble(SearchRecord::getSimilarity).reversed().thenComparing(SearchRecord::getSortKey))
                    .limit(criteria.searchMode.getLshCandidateCount()) 
                    .collect(Collectors.toList());
            }

            // build comparator for final sorts
            Comparator<SearchRecord> comparator = getComparator(criteria);

            // if mode is TOP_ALIGNED
            if (criteria.searchMode == SearchMode.TOP_ALIGNED) {

                // *** parse query structure
        
                Parser parser = new Parser(Integer.MAX_VALUE);

                String fileName = "";
                Structure structure = null;
                if (criteria.searchBy == SearchBy.DB_ID) {

                    fileName = criteria.idDbType.getImportPath() + criteria.dbId + ".pdb.gz";
                    
                    FileInputStream queryFile = new FileInputStream(fileName);
                    GZIPInputStream queryFileGz = new GZIPInputStream(queryFile);

                    structure = parser.parsePDBFile(queryFileGz);
                }
                else { // UPLOAD
                    
                    fileName = Constants.UPLOAD_PATH + criteria.uploadId + ".pdb";
                    FileInputStream queryFile = new FileInputStream(fileName);

                    structure = parser.parsePDBFile(queryFile);
                }

                Structure queryStructure = structure;

                // *** perform alignments
                
                // filter for fast alignments 
                records = records.stream()
                    .limit(alignmentFilter(Mode.FAST, grams1.size()))
                    .collect(Collectors.toList());
                
                // fast alignments
                records.stream().parallel().forEach(record -> align(criteria, record, queryStructure, Mode.FAST));

                // sort and filter for regular alignments
                records = records.stream()
                    .sorted(comparator)
                    .limit(alignmentFilter(Mode.REGULAR, grams1.size())) 
                    .collect(Collectors.toList());
                
                // regular alignments
                records.stream().parallel().forEach(record -> align(criteria, record, queryStructure, Mode.REGULAR));

            } // end mode == TOP_ALIGNED

            // sort using comparator from above
            records = records.stream()
                    .sorted(comparator)
                    .limit(criteria.limit)
                    .collect(Collectors.toList());

            // query db id should be first
            if (criteria.searchBy == SearchBy.DB_ID) {
               
                int i; 
                for (i = 0; i < records.size(); i++) {
                    if (records.get(i).getDbId().equals(criteria.dbId)) {
                        break;
                    }
                }
                
                if (i != 0 && i < records.size()) {
                    
                    SearchRecord record = records.get(i);
                    records.remove(i);
                    records.add(0, record);
                    if (criteria.searchMode == SearchMode.TOP_ALIGNED) {
                        records.get(0).setRmsd(0.0);
                        records.get(0).setTmScore(1.0);
                    }
                }
            }

            // augment data set for output
            augment(records);
        }

        return records;
    }

    private int alignmentFilter(Mode mode, int gramCount) {

        if (mode == Mode.FAST) {

            if (gramCount <= 200) {
                return 8000; 
            }
            else if (gramCount <= 300) {
                return 6000;
            }
            else if (gramCount <= 400) {
                return 4000;
            }
            else if (gramCount <= 500) {
                return 2000;
            }
            else {
                return 1000;
            }
        }
        else { // mode == Mode.REGULAR
            
            return 400;
        }
    }

    private Comparator<SearchRecord> getComparator(SearchCriteria criteria) {

        Comparator<SearchRecord> comparator;

        if (criteria.sortBy == SortBy.SIMILARITY) {
            comparator = Comparator.comparingDouble(SearchRecord::getSimilarity);
        } 
        else if (criteria.sortBy == SortBy.RMSD) {
            comparator = Comparator.comparingDouble(SearchRecord::getRmsd);
        }
        else {
            comparator = Comparator.comparingDouble(SearchRecord::getTmScore);
        }
        if (criteria.sortBy.isDescending()) {
            comparator = comparator.reversed();
        }
        comparator = comparator.thenComparing(SearchRecord::getSortKey);

        return comparator;
    }

    private void align(SearchCriteria criteria, SearchRecord record, Structure queryStructure, Mode mode) {

        try {
       
            FileInputStream targetFile = new FileInputStream(getDbType().getImportPath() + record.getDbId() + ".pdb.gz");
            GZIPInputStream targetFileGz = new GZIPInputStream(targetFile);

            Parser parser = new Parser(Integer.MAX_VALUE);
            Structure targetStructure = parser.parsePDBFile(targetFileGz);
     
            TMAlign tm = new TMAlign(mode);
            TMAlign.Results results = tm.align(queryStructure, targetStructure);

            record.setRmsd(results.getRmsd());

            double maxScore = Integer.MIN_VALUE;
            SearchType searchType = SearchType.FULL_LENGTH;

            // take the max one regardless of initial max one from FAST
            if (criteria.searchTypes.contains(SearchType.FULL_LENGTH)) {
                if (results.getTmScoreAvg() > maxScore) {
                    maxScore = results.getTmScoreAvg();
                    searchType = SearchType.FULL_LENGTH;
                } 
            }
            if (criteria.searchTypes.contains(SearchType.CONTAINED_IN)) {
                if (results.getTmScoreQ() > maxScore) {
                    maxScore = results.getTmScoreQ();
                    searchType = SearchType.CONTAINED_IN;
                } 
            }
            if (criteria.searchTypes.contains(SearchType.CONTAINS)) {
                if (results.getTmScoreT() > maxScore) {
                    maxScore = results.getTmScoreT();
                    searchType = SearchType.CONTAINS;
                } 
            }

            record.setTmScore(maxScore);
            record.setSearchType(searchType);
        }
        catch (IOException e) {
            
            record.setRmsd(Double.MAX_VALUE);
            record.setTmScore(0);
        }
        catch (RuntimeException e) {
            
            record.setRmsd(Double.MAX_VALUE);
            record.setTmScore(0);
        }
    }

    private List<SearchRecord> gramsSplit(int splitIndex, SearchCriteria criteria, List<Integer> grams1) {

        List<SearchRecord> records = new ArrayList<>();

        try {
   
            // *** LCS matches
            
            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmt = getSplitSearchStatement(criteria, splitIndex, conn);
            
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {

                String dbId = rs.getString("db_id");
                String pdbId = rs.getString("pdb_id");
                String sortKey = rs.getString("sort_key");
                
                Integer[] grams2 = (Integer[])rs.getArray("grams").getArray();
                List<Integer> grams2AsList = Arrays.asList(grams2);
           
                double similarity = Integer.MIN_VALUE;
                SearchType searchType = SearchType.FULL_LENGTH;
            
                // take the max one based on filters selected
                if (criteria.searchTypes.size() == 3) {

                    double containedIn = Integer.MIN_VALUE;
                    double contains = Integer.MIN_VALUE;

                    if (grams2AsList.size() > Math.ceil(grams1.size() / 2.0)) {
                        containedIn = LCS.getLCSScoreContainment(grams1, grams2AsList);
                    } 
                    if (grams2AsList.size() > Math.ceil(grams1.size() / 3.0)) {
                        contains = LCS.getLCSScoreContainment(grams2AsList, grams1);
                    }

                    if (containedIn == contains) {

                        // this will always be the case if FULL_LENGTH wins for LCS
                        similarity = containedIn;
                        searchType = SearchType.FULL_LENGTH;
                    }
                    else if (containedIn > contains) {
                        similarity = containedIn;
                        searchType = SearchType.CONTAINED_IN;
                    }
                    else {
                        similarity = contains;
                        searchType = SearchType.CONTAINS;
                    }
                }
                else {
                    
                    double fullLength = Integer.MIN_VALUE;
                    double containedIn = Integer.MIN_VALUE;
                    double contains = Integer.MIN_VALUE;

                    if (criteria.searchTypes.contains(SearchType.FULL_LENGTH)) {
                        if (grams2AsList.size() > Math.ceil(grams1.size() / 2.0) && grams2AsList.size() < grams1.size() * 2) {
                            fullLength = LCS.getLCSScoreFullLength(grams1, grams2AsList);
                        }
                    }
                    if (criteria.searchTypes.contains(SearchType.CONTAINED_IN)) {
                        if (grams2AsList.size() > Math.ceil(grams1.size() / 2.0)) {
                            containedIn = LCS.getLCSScoreContainment(grams1, grams2AsList);
                        }
                    }
                    if (criteria.searchTypes.contains(SearchType.CONTAINS)) {
                        if (grams2AsList.size() > Math.ceil(grams1.size() / 3.0)) {
                            contains = LCS.getLCSScoreContainment(grams2AsList, grams1);
                        }
                    }

                    if (fullLength >= containedIn && fullLength >= contains) {
                        similarity = fullLength;
                        searchType = SearchType.FULL_LENGTH;
                    }
                    else if (containedIn >= contains) {
                        similarity = containedIn;
                        searchType = SearchType.CONTAINED_IN;
                    }
                    else {
                        similarity = contains;
                        searchType = SearchType.CONTAINS;
                    }
                }

                SearchRecord record = getSearchRecord();
                record.setDbId(dbId);
                record.setPdbId(pdbId);
                record.setSortKey(sortKey);
                record.setSearchType(searchType);
                record.setSimilarity(similarity);
                records.add(record);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, e);
        } 

        return records;
    }
    
    private List<SearchRecord> searchBand(int bandIndex, SearchCriteria criteria, Hashes hashes1) {

        List<SearchRecord> records = new ArrayList<>();

        try {
   
            // *** LSH band matches
            
            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmt = getBandSearchStatement(criteria, bandIndex, conn);
            
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {

                String dbId = rs.getString("db_id");
                String pdbId = rs.getString("pdb_id");
                String sortKey = rs.getString("sort_key");
                Integer[] minHashes = (Integer[])rs.getArray("min_hashes").getArray();
                Integer[] bandHashes = (Integer[])rs.getArray("band_hashes").getArray();
                
                if(!lowerBandMatch(hashes1.bandHashes, bandHashes, bandIndex)) {
                   
                    double similarity = Similarity.getEstimatedSimilarity(hashes1.minHashes, minHashes); 
                    if (similarity >= Constants.SIMILARITY_THRESHOLD) {

                        SearchRecord record = getSearchRecord();
                        record.setDbId(dbId);
                        record.setPdbId(pdbId);
                        record.setSortKey(sortKey);
                        record.setSimilarity(similarity);
                        record.setSearchType(SearchType.FULL_LENGTH);
                        records.add(record);
                    }
                }
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, e);
        } 

        return records;
    }

    private void augment(List<SearchRecord> bandRecords) {

        try {

            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn = ds.getConnection();
            conn.setAutoCommit(true);
       
            Object[] objDbIds = bandRecords.stream().map(record -> record.getDbId()).toArray();

            String[] dbIds = Arrays.copyOf(objDbIds, objDbIds.length, String[].class);

            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM get_" + getDbType().getTableName() + "_augmented_results(?);");
            stmt.setArray(1, conn.createArrayOf("VARCHAR", dbIds));

            ResultSet rs = stmt.executeQuery();
            
            int n = 1;

            while (rs.next()) {

                // WITH ORDINALITY clause will ensure they are ordered correctly

                SearchRecord record = bandRecords.get(n-1);

                record.setN(n++);

                augment(record, rs);
            }
            
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, e);
        } 
    }
    
    // *********************************************************************
    // Static Methods
    // *********************************************************************

    private static boolean lowerBandMatch(Integer[] bands1, Integer[] bands2, int bandIndex) {

        // use this function in case of distributed system to eliminate intermediate results up front

        boolean match = false; 
        for (int i = 0; i < bandIndex; i++) {
           if (bands1[i].equals(bands2[i])) {
                match = true;
                break;
           }
        }
        return match;
    }
}
