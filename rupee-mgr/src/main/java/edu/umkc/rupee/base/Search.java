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

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.align.StructureAlignment;
import org.biojava.nbio.structure.align.StructureAlignmentFactory;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.util.AFPChainScorer;
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.lib.AlignCriteria;
import edu.umkc.rupee.lib.Constants;
import edu.umkc.rupee.lib.Db;
import edu.umkc.rupee.lib.DbTypeCriteria;
import edu.umkc.rupee.lib.Hashes;
import edu.umkc.rupee.lib.LCS;
import edu.umkc.rupee.lib.LCSResults;
import edu.umkc.rupee.lib.SearchByCriteria;
import edu.umkc.rupee.lib.Similarity;
import edu.umkc.rupee.lib.SortCriteria;

public abstract class Search {
    
    // *********************************************************************
    // Abstract Methods 
    // *********************************************************************
    
    public abstract DbTypeCriteria getDbType();

    public abstract PreparedStatement getSearchStatement(SearchCriteria criteria, int bandIndex, Connection conn) throws SQLException;

    public abstract void augment(SearchRecord record, ResultSet rs) throws SQLException;

    public abstract SearchRecord getSearchRecord(String dbId, String pdbId, String sortKey, double similarity);

    // *********************************************************************
    // Instance Methods
    // *********************************************************************
    
    public List<SearchRecord> search(SearchCriteria criteria) {

        List<SearchRecord> records = new ArrayList<>();

        try {
            List<Integer> grams = null;
            Hashes hashes = null;

            if (criteria.searchBy == SearchByCriteria.DB_ID) {
                
                grams = Db.getGrams(criteria.dbId, criteria.dbIdType);
                hashes = Db.getHashes(criteria.dbId, criteria.dbIdType);
            }
            else { // UPLOAD

                grams = Db.getUploadGrams(criteria.uploadId);
                hashes = Db.getUploadHashes(criteria.uploadId);
            }

            final List<Integer> grams1 = grams;
            final Hashes hashes1 = hashes;

            if (hashes1 != null) {

                // parallel band match searches to gather candidates
                records = IntStream.range(0, Constants.BAND_CHECK_COUNT).boxed().parallel()
                    .flatMap(bandIndex -> searchBand(bandIndex, criteria, hashes1).stream())
                    .sorted(Comparator.comparingDouble(SearchRecord::getSimilarity).reversed().thenComparing(SearchRecord::getSortKey))
                    .limit(Constants.SIMILARITY_FILTER) 
                    .collect(Collectors.toList());

                // cache map of residue grams
                List<String> dbIds = records.stream().map(SearchRecord::getDbId2).collect(Collectors.toList());
                Map<String, List<Integer>> map = Db.getGrams(dbIds, criteria.dbType);

                // parallel adjusted similarity
                records.parallelStream()
                    .forEach(record -> {

                        if (map.containsKey(record.getDbId2())) {
                            List<Integer> grams2 = map.get(record.getDbId2());
                            LCSResults results = LCS.getSemiGlobalLCS(grams1, grams2);
                            record.setSimilarity(Similarity.getAdjustedSimilarity(grams1, grams2, results));
                        }
                    });

                // sort with adjusted similarity and filter 
                records = records.stream()
                    .sorted(Comparator.comparingDouble(SearchRecord::getSimilarity).reversed().thenComparing(SearchRecord::getSortKey))
                    .limit(criteria.limit)
                    .collect(Collectors.toList());

                // assign similarity ranks for reference
                for (int i = 0; i < records.size(); i++) {
                    records.get(i).setSimilarityRank(i+1);
                }

                // if an external alignment algorithm is requested
                if (criteria.align != AlignCriteria.NONE) {

                    // cache common objects for use by multiple threads 
                    final PDBFileReader reader = new PDBFileReader();
                    reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);

                    String fileName = "";
                    Structure structure = null;
                    if (criteria.searchBy == SearchByCriteria.DB_ID) {

                        fileName = criteria.dbIdType.getImportPath() + criteria.dbId + ".pdb.gz";
                        
                        FileInputStream queryFile = new FileInputStream(fileName);
                        GZIPInputStream queryFileGz = new GZIPInputStream(queryFile);

                        structure = reader.getStructure(queryFileGz);
                    }
                    else { // UPLOAD
                        
                        fileName = Constants.UPLOAD_PATH + criteria.uploadId + ".pdb";
                        FileInputStream queryFile = new FileInputStream(fileName);

                        structure = reader.getStructure(queryFile);
                    }

                    final Structure queryStructure = structure;
                    final Atom[] queryAtoms = StructureTools.getAtomCAArray(queryStructure);

                    // perform parallel alignments with candidates
                    records.stream().parallel().forEach(record -> {
                        
                        try {
                         
                            StructureAlignment alg = StructureAlignmentFactory.getAlgorithm(criteria.align.getAlgorithmName());

                            FileInputStream targetFile = new FileInputStream(getDbType().getImportPath() + record.getDbId2() + ".pdb.gz");
                            GZIPInputStream targetFileGz = new GZIPInputStream(targetFile);

                            Structure targetStructure = reader.getStructure(targetFileGz);
                            Atom[] targetAtoms = StructureTools.getAtomCAArray(targetStructure);
                       
                            AFPChain afps = alg.align(queryAtoms, targetAtoms, criteria.align.getParams());
                            afps.setTMScore(AFPChainScorer.getTMScore(afps, queryAtoms, targetAtoms));

                            record.setRmsd(afps.getTotalRmsdOpt());
                            record.setTmScore(afps.getTMScore());
                        }
                        catch (IOException e) {
                            Logger.getLogger(Search.class.getName()).log(Level.INFO, null, e);
                        }
                        catch (StructureException e) {
                            Logger.getLogger(Search.class.getName()).log(Level.INFO, null, e);
                        }
                    });
                }

                // get the total record count
                int recordCount = records.size();

                // build comparator based on sort criteria
                Comparator<SearchRecord> comparator;
                if (criteria.sort == SortCriteria.SIMILARITY) {
                    comparator = Comparator.comparingDouble(SearchRecord::getSimilarity);
                } 
                else if (criteria.sort == SortCriteria.RMSD) {
                    comparator = Comparator.comparingDouble(SearchRecord::getRmsd);
                }
                else {
                    comparator = Comparator.comparingDouble(SearchRecord::getTmScore);
                }
                if (criteria.sort.isDescending()) {
                    comparator = comparator.reversed();
                }
                comparator = comparator.thenComparing(SearchRecord::getSortKey);

                // sort using comparator from above
                records = records.stream()
                        .sorted(comparator)
                        .skip((criteria.page - 1) * criteria.pageSize)
                        .limit(criteria.pageSize)
                        .collect(Collectors.toList());

                // augment data set for output
                augment(records, criteria.dbId, recordCount);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, e);
        }

        return records;
    }

    public List<SearchRecord> searchBand(int bandIndex, SearchCriteria criteria, Hashes hashes) {

        List<SearchRecord> records = new ArrayList<>();

        try {
   
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
                
                if(!lowerBandMatch(hashes.bandHashes, bandHashes, bandIndex)) {
                   
                    double similarity = Similarity.getEstimatedSimilarity(hashes.minHashes, minHashes); 
                    if (similarity >= Constants.SIMILARITY_THRESHOLD) {
                        records.add(getSearchRecord(dbId, pdbId, sortKey, similarity));
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

    public void augment(List<SearchRecord> bandRecords, String dbId, int recordCount) {

        try {

            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn = ds.getConnection();
            conn.setAutoCommit(true);
       
            Object[] objDbIds = bandRecords.stream().map(record -> record.getDbId2()).toArray();

            String[] dbIds = Arrays.copyOf(objDbIds, objDbIds.length, String[].class);

            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM get_" + getDbType().getDescription().toLowerCase() + "_augmented_results(?);");
            stmt.setArray(1, conn.createArrayOf("VARCHAR", dbIds));

            ResultSet rs = stmt.executeQuery();
            
            int n = 1;

            while (rs.next()) {

                // WITH ORDINALITY clause will ensure they are ordered correctly

                SearchRecord record = bandRecords.get(n-1);

                record.setN(n++);
                record.setRecordCount(recordCount);
                record.setDbId1(dbId);

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

    public static boolean lowerBandMatch(Integer[] bands1, Integer[] bands2, int bandIndex) {

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
