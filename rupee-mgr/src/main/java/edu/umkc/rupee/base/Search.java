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
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.io.PDBFileReader;
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
import edu.umkc.rupee.lib.Grams;
import edu.umkc.rupee.lib.Hashes;
import edu.umkc.rupee.lib.LCS;
import edu.umkc.rupee.lib.Similarity;
import edu.umkc.rupee.tm.Kabsch;
import edu.umkc.rupee.tm.KabschTLS;
import edu.umkc.rupee.tm.TmAlign;
import edu.umkc.rupee.tm.TmMode;
import edu.umkc.rupee.tm.TmResults;

// ASSUMPTIONS: 
// 1. if search mode == FAST sort by must be SIMILARITY
// 2. if search mode != FAST and search type == RMSD sort by must be RMSD
// 3. if search mode != FAST and search type != RMSD sort by must be TM_SCORE

public abstract class Search {

    private static int INITIAL_FILTER = 40000;
    private static int FINAL_FILTER = 8000;

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
 
    public List<SearchRecord> search(SearchCriteria criteria, SearchFrom searchFrom) throws Exception {

        List<SearchRecord> records = new ArrayList<>();

        // enforce assumptions
        if (criteria.searchMode == SearchMode.FAST) {
            criteria.sortBy = SortBy.SIMILARITY;
        } 
        else if (criteria.searchType == SearchType.RMSD) {
            criteria.sortBy = SortBy.RMSD;
        }
        else if (criteria.searchType == SearchType.Q_SCORE) {
            criteria.sortBy = SortBy.Q_SCORE;
        }
        else if (criteria.searchType == SearchType.SSAP_SCORE) {
            criteria.sortBy = SortBy.SSAP_SCORE;
        }
        else { 
            criteria.sortBy = SortBy.TM_SCORE;
        }
       
        // limit
        criteria.limit = Math.min(criteria.limit, 1000); 

        Grams grams = null;
        Hashes hashes = null;
        if (criteria.searchBy == SearchBy.DB_ID) {
            
            grams = Db.getGrams(criteria.dbId, criteria.idDbType, true);
            hashes = Db.getHashes(criteria.dbId, criteria.idDbType);
        }
        else { // UPLOAD

            grams = Db.getUploadGrams(criteria.uploadId);
            hashes = Db.getUploadHashes(criteria.uploadId);
        }

        final Grams grams1 = grams;
        final Hashes hashes1 = hashes;

        if (grams1 != null && hashes1 != null) {

            if (criteria.searchMode != SearchMode.ALL_ALIGNED) {

                // TOP_ALIGNED and FAST

                // parallel band match searches to gather lsh candidates
                records = IntStream.range(0, Constants.BAND_CHECK_COUNT).boxed().parallel()
                    .flatMap(bandIndex -> searchBand(bandIndex, criteria, hashes1).stream())
                    .sorted(Comparator.comparingDouble(SearchRecord::getSimilarity).reversed().thenComparing(SearchRecord::getSortKey))
                    .limit(INITIAL_FILTER) 
                    .collect(Collectors.toList());
              
                // cache map of residue grams
                List<String> dbIds = records.stream().map(SearchRecord::getDbId).collect(Collectors.toList());
                Map<String, Grams> map = Db.getGrams(dbIds, criteria.searchDbType, false);

                // parallel lcs algorithm
                records.parallelStream()
                    .forEach(record -> {

                        if (map.containsKey(record.getDbId())) {
                            Grams grams2 = map.get(record.getDbId());
                            double score = LCS.getLCSScore(grams1.getGramsAsList(), grams2.getGramsAsList(), criteria.searchType);
                            record.setSimilarity(score);
                        }
                    });

                // sort lcs candidates
                records = records.stream()
                    .sorted(Comparator.comparingDouble(SearchRecord::getSimilarity).reversed().thenComparing(SearchRecord::getSortKey))
                    .limit(FINAL_FILTER) 
                    .collect(Collectors.toList());
            } 
            else {

                // initial filtering based on simple LCS plus tm-align on aligned descriptors
                records = IntStream.range(0, Constants.SEARCH_SPLIT_COUNT).boxed().parallel()
                    .flatMap(splitIndex -> gramsSplit(splitIndex, criteria, grams1).stream())
                    .sorted(Comparator.comparingDouble(SearchRecord::getSimilarity).reversed().thenComparing(SearchRecord::getSortKey))
                    .limit(FINAL_FILTER) 
                    .collect(Collectors.toList());
            }

            // build comparator for final sorts
            Comparator<SearchRecord> comparator = getComparator(criteria);

            // alignments
            if (criteria.searchMode == SearchMode.ALL_ALIGNED || criteria.searchMode == SearchMode.TOP_ALIGNED) {

                // *** parse query structure
       
                PDBFileReader reader = new PDBFileReader();
                reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);

                String fileName = "";
                Structure structure = null;
                if (criteria.searchBy == SearchBy.DB_ID) {

                    fileName = criteria.idDbType.getImportPath() + criteria.dbId + ".pdb.gz";
                    
                    FileInputStream queryFile = new FileInputStream(fileName);
                    GZIPInputStream queryFileGz = new GZIPInputStream(queryFile);

                    structure = reader.getStructure(queryFileGz);

                    queryFileGz.close();
                    queryFile.close();
                }
                else { // UPLOAD
                    
                    fileName = Constants.UPLOAD_PATH + criteria.uploadId + ".pdb";
                    FileInputStream queryFile = new FileInputStream(fileName);

                    structure = reader.getStructure(queryFile);
                    
                    queryFile.close();
                }

                Structure queryStructure = structure;

                // *** perform alignments
                
                // filter for fast alignments 
                records = records.stream()
                    .limit(alignmentFilter(TmMode.FAST, grams1.getLength()))
                    .collect(Collectors.toList());
                
                // fast alignments
                records.stream().parallel().forEach(record -> align(criteria, record, queryStructure, TmMode.FAST));

                // sort and filter for regular alignments
                records = records.stream()
                    .sorted(comparator)
                    .limit(criteria.limit) 
                    .collect(Collectors.toList());
                
                // regular alignments
                records.stream().parallel().forEach(record -> align(criteria, record, queryStructure, TmMode.REGULAR));

            } 

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

    private int alignmentFilter(TmMode mode, int gramCount) {

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

    private Comparator<SearchRecord> getComparator(SearchCriteria criteria) {

        Comparator<SearchRecord> comparator;

        if (criteria.sortBy == SortBy.SIMILARITY) {
            comparator = Comparator.comparingDouble(SearchRecord::getSimilarity);
        } 
        else if (criteria.sortBy == SortBy.RMSD) {
            comparator = Comparator.comparingDouble(SearchRecord::getRmsd);
        }
        else if (criteria.sortBy == SortBy.Q_SCORE) {
            comparator = Comparator.comparingDouble(SearchRecord::getQScore);
        }
        else if (criteria.sortBy == SortBy.SSAP_SCORE) {
            comparator = Comparator.comparingDouble(SearchRecord::getSsapScore);
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

    private void align(SearchCriteria criteria, SearchRecord record, Structure queryStructure, TmMode mode) {

        try {
       
            FileInputStream targetFile = new FileInputStream(getDbType().getImportPath() + record.getDbId() + ".pdb.gz");
            GZIPInputStream targetFileGz = new GZIPInputStream(targetFile);

            Parser parser = new Parser(); 

            Structure targetStructure = parser.parsePdbFile(targetFileGz);

            targetFileGz.close();
            targetFile.close();

            Kabsch kabsch = KabschTLS.get();     
            TmAlign tm = new TmAlign(queryStructure, targetStructure, mode, kabsch);
            TmResults results = tm.align();

            // always get these because they're there
            record.setRmsd(results.getRmsd());
            record.setQScore(results.getQScore());
            record.setSsapScore(results.getSsapScore());

            if (criteria.searchType == SearchType.FULL_LENGTH) {
                record.setTmScore(results.getTmScoreAvg());    
            }
            else if (criteria.searchType == SearchType.CONTAINED_IN) {
                record.setTmScore(results.getTmScoreQ());    
            }
            else if (criteria.searchType == SearchType.CONTAINS) {
                record.setTmScore(results.getTmScoreT());
            }
            else {

                // just get the average for other search types
                record.setTmScore(results.getTmScoreAvg());
            }
        }
        catch (IOException e) {
           
            record.setRmsd(Double.MAX_VALUE);
            record.setTmScore(-1);
        }
        catch (RuntimeException e) {
            
            record.setRmsd(Double.MAX_VALUE);
            record.setTmScore(-1);
        }
    }

    private List<SearchRecord> gramsSplit(int splitIndex, SearchCriteria criteria, Grams grams1) {

        List<SearchRecord> records = new ArrayList<>();

        try {
   
            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmt = getSplitSearchStatement(criteria, splitIndex, conn);
            stmt.setFetchSize(200);
            
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {

                String dbId = rs.getString("db_id");
                String pdbId = rs.getString("pdb_id");
                String sortKey = rs.getString("sort_key");

                Grams grams2 = Grams.fromResultSet(rs, true);                

                double similarity = Integer.MIN_VALUE;
                if (criteria.searchType == SearchType.FULL_LENGTH) {
                    if ((grams1.getLength() < Math.floorDiv(grams2.getLength(), 3)) || (grams2.getLength() < Math.floorDiv(grams1.getLength(), 3))) {
                        continue;
                    }
                    similarity = LCS.getLCSPlusScore(grams1, grams2, criteria.searchType);
                }            
                else if (criteria.searchType == SearchType.CONTAINED_IN) {
                    similarity = LCS.getLCSPlusScore(grams1, grams2, criteria.searchType);
                }
                else if (criteria.searchType == SearchType.CONTAINS) { 
                    if (grams2.getLength() < Math.floorDiv(grams1.getLength(), 3)) {
                        continue;
                    }
                    similarity = LCS.getLCSPlusScore(grams1, grams2, criteria.searchType);
                }
                else {

                    // no filtering for RMSD, Q-Score, and SSAP-Score
                    similarity = LCS.getLCSPlusScore(grams1, grams2, criteria.searchType);
                }

                SearchRecord record = getSearchRecord();
                record.setDbId(dbId);
                record.setPdbId(pdbId);
                record.setSortKey(sortKey);
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
            stmt.setFetchSize(200);
            
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
