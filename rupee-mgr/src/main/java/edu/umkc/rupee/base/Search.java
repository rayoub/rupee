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
import edu.umkc.rupee.defs.SearchMode;
import edu.umkc.rupee.defs.SearchBy;
import edu.umkc.rupee.defs.SearchFrom;
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

    public abstract PreparedStatement getSearchStatement(SearchCriteria criteria, int bandIndex, Connection conn) throws SQLException;

    public abstract void augment(SearchRecord record, ResultSet rs) throws SQLException;

    public abstract SearchRecord getSearchRecord();

    // *********************************************************************
    // Instance Methods
    // *********************************************************************
    
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

        // size limit for top-aligned searches
        if (searchFrom == SearchFrom.WEB && criteria.searchMode == SearchMode.TOP_ALIGNED && grams.size() > 400) {
            
            Thread.sleep(2000); // sleeping corresponds to an event throttle on web page - looks nicer
            throw new UnsupportedOperationException("Query structures for immediate Top-Aligned searches must have fewer than 400 residues.");
        }

        final List<Integer> grams1 = grams;
        final Hashes hashes1 = hashes;

        if (hashes1 != null) {

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
                        int score = LCS.getLCSScoreFullLength(grams1, grams2); 
                        record.setSimilarity(score);
                    }
                });

            // sort lcs candidates
            records = records.stream()
                .sorted(Comparator.comparingDouble(SearchRecord::getSimilarity).reversed().thenComparing(SearchRecord::getSortKey))
                .collect(Collectors.toList());

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
                if (searchFrom == SearchFrom.SERVER) {
                    records.stream().forEach(record -> align(record, queryStructure, Mode.FAST));
                }
                else {
                    records.stream().parallel().forEach(record -> align(record, queryStructure, Mode.FAST));
                }

                // sort and filter for regular alignments
                records = records.stream()
                    .sorted(comparator)
                    .limit(alignmentFilter(Mode.REGULAR, grams1.size())) 
                    .collect(Collectors.toList());
                
                // regular alignments
                if (searchFrom == SearchFrom.SERVER) {
                    records.stream().forEach(record -> align(record, queryStructure, Mode.REGULAR));
                }
                else {
                    records.stream().parallel().forEach(record -> align(record, queryStructure, Mode.REGULAR));
                }

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

    private void align(SearchRecord record, Structure queryStructure, Mode mode) {

        try {
       
            FileInputStream targetFile = new FileInputStream(getDbType().getImportPath() + record.getDbId() + ".pdb.gz");
            GZIPInputStream targetFileGz = new GZIPInputStream(targetFile);

            Parser parser = new Parser(Integer.MAX_VALUE);
            Structure targetStructure = parser.parsePDBFile(targetFileGz);
     
            TMAlign tm = new TMAlign(mode);
            TMAlign.Results results = tm.align(queryStructure, targetStructure);

            record.setRmsd(results.getRmsd());
            record.setTmScore(results.getTmScoreAvg());
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

    private List<SearchRecord> searchBand(int bandIndex, SearchCriteria criteria, Hashes hashes1) {

        List<SearchRecord> records = new ArrayList<>();

        try {
   
            // *** LSH band matches
            
            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement stmt = getSearchStatement(criteria, bandIndex, conn);
            
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
