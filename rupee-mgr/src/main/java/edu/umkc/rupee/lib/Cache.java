package edu.umkc.rupee.lib;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.align.StructureAlignment;
import org.biojava.nbio.structure.align.StructureAlignmentFactory;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.util.AFPChainScorer;
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.postgresql.PGConnection;
import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.base.Search;
import edu.umkc.rupee.base.SearchCriteria;
import edu.umkc.rupee.base.SearchRecord;

public class Cache {
    
    // *********************************************************************
    // Instance Methods 
    // *********************************************************************
    
    public static void cacheAlignmentScores(String dbId, SearchCriteria criteria, Search search) {
        
        System.out.println("Caching alignment scores for: " + dbId);

        try {
            
            final List<Integer> grams1 = Db.getGrams(criteria.dbId, search.getDbType());
            Hashes hashes = Db.getHashes(criteria.dbId, search.getDbType());

            Map<String,AlignmentScores> cached = getCachedAlignmentScores(dbId);

            if (hashes != null) {

                // parallel band match searches to gather candidates
                List<SearchRecord> records = IntStream.range(0, Constants.BAND_CHECK_COUNT).boxed().parallel()
                    .flatMap(bandIndex -> search.searchBand(bandIndex, criteria, hashes).stream())
                    .sorted(Comparator.comparingDouble(SearchRecord::getSimilarity).reversed().thenComparing(SearchRecord::getDbId))
                    .limit(Constants.SIMILARITY_FILTER)
                    .collect(Collectors.toList());

                // cache map of residue grams
                List<String> dbIds = records.stream().map(SearchRecord::getDbId).collect(Collectors.toList());
                Map<String, List<Integer>> map = Db.getGrams(dbIds, search.getDbType());

                // parallel adjusted similarity
                records.parallelStream()
                    .forEach(record -> {

                        if (map.containsKey(record.getDbId())) {
                            List<Integer> grams2 = map.get(record.getDbId());
                            LCSResults results = LCS.getSemiGlobalLCS(grams1, grams2);
                            record.setSimilarity(Similarity.getAdjustedSimilarity(grams1, grams2, results));
                        }
                    });

                // sort with adjusted similarity and filter 
                records = records.stream()
                    .sorted(Comparator.comparingDouble(SearchRecord::getSimilarity).reversed().thenComparing(SearchRecord::getDbId))
                    .limit(criteria.limit)
                    .collect(Collectors.toList());

                // cache common objects for use by multiple threads 
                final PDBFileReader reader = new PDBFileReader();
                reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);
                    
                FileInputStream queryFile = new FileInputStream(search.getDbType().getImportPath() + criteria.dbId + ".pdb.gz");
                GZIPInputStream queryFileGz = new GZIPInputStream(queryFile);

                final Structure queryStructure = reader.getStructure(queryFileGz);
                final Atom[] queryAtoms = StructureTools.getAtomCAArray(queryStructure);

                // perform parallel alignments with candidates
                List<AlignmentScores> cache = records.stream().parallel()
                    .filter(record -> !cached.containsKey(record.getDbId()))
                    .map(record -> {
                   
                        AlignmentScores scores = new AlignmentScores(criteria.dbId, record.getDbId());
                        scores.setSimilarity(record.getSimilarity());

                        try {

                            FileInputStream targetFile = new FileInputStream(search.getDbType().getImportPath() + record.getDbId() + ".pdb.gz");
                            GZIPInputStream targetFileGz = new GZIPInputStream(targetFile);

                            Structure targetStructure = reader.getStructure(targetFileGz);
                            Atom[] targetAtoms = StructureTools.getAtomCAArray(targetStructure);
                           
                            StructureAlignment alg;
                            AFPChain afps;
                            
                            alg  = StructureAlignmentFactory.getAlgorithm(AlignCriteria.CE.getAlgorithmName());
                            afps = alg.align(queryAtoms, targetAtoms, AlignCriteria.CE.getParams());
                            afps.setTMScore(AFPChainScorer.getTMScore(afps, queryAtoms, targetAtoms));

                            scores.setCeRmsd(afps.getTotalRmsdOpt());
                            scores.setCeTmScore(afps.getTMScore());
                            
                            alg  = StructureAlignmentFactory.getAlgorithm(AlignCriteria.CECP.getAlgorithmName());
                            afps = alg.align(queryAtoms, targetAtoms, AlignCriteria.CECP.getParams());
                            afps.setTMScore(AFPChainScorer.getTMScore(afps, queryAtoms, targetAtoms));

                            scores.setCecpRmsd(afps.getTotalRmsdOpt());
                            scores.setCecpTmScore(afps.getTMScore());
                            
                            alg  = StructureAlignmentFactory.getAlgorithm(AlignCriteria.FATCAT_FLEXIBLE.getAlgorithmName());
                            afps = alg.align(queryAtoms, targetAtoms, AlignCriteria.FATCAT_FLEXIBLE.getParams());
                            afps.setTMScore(AFPChainScorer.getTMScore(afps, queryAtoms, targetAtoms));

                            scores.setFatCatFlexibleRmsd(afps.getTotalRmsdOpt());
                            scores.setFatCatFlexibleTmScore(afps.getTMScore());
                            
                            alg  = StructureAlignmentFactory.getAlgorithm(AlignCriteria.FATCAT_RIGID.getAlgorithmName());
                            afps = alg.align(queryAtoms, targetAtoms, AlignCriteria.FATCAT_RIGID.getParams());
                            afps.setTMScore(AFPChainScorer.getTMScore(afps, queryAtoms, targetAtoms));

                            scores.setFatCatRigidRmsd(afps.getTotalRmsdOpt());
                            scores.setFatCatRigidTmScore(afps.getTMScore());

                        }
                        catch (Exception e) {
                            scores = null;
                        }

                        return scores;
                    })
                .filter(scores -> scores != null).collect(Collectors.toList());

                if (cache.size() > 0) {
                    saveAlignmentScores(criteria.dbId, cache);
                }

            } // if (hashes != null) {

        } catch (Exception e) {
            Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public static void saveAlignmentScores(String dbId, List<AlignmentScores> cache) {

        PGSimpleDataSource ds = Db.getDataSource();

        try {

            Connection conn = ds.getConnection();
            conn.setAutoCommit(true);

            ((PGConnection) conn).addDataType("alignment_scores", AlignmentScores.class);

            PreparedStatement updt = conn.prepareStatement("SELECT insert_alignment_scores(?,?);");

            AlignmentScores a[] = new AlignmentScores[cache.size()];
            cache.toArray(a);
            updt.setString(1, dbId);
            updt.setArray(2, conn.createArrayOf("alignment_scores", a));

            updt.execute();

            updt.close();
            conn.close();
        
        } catch (SQLException e) {
            Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static Map<String,AlignmentScores> getCachedAlignmentScores(String dbId) {

        Map<String,AlignmentScores> cache = new HashMap<>();

        PGSimpleDataSource ds = Db.getDataSource();

        try {
       
            Connection conn = ds.getConnection();
            conn.setAutoCommit(true);

            ((PGConnection) conn).addDataType("alignment_scores", AlignmentScores.class);

            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM get_alignment_scores(?);");
            stmt.setString(1, dbId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
              
                AlignmentScores scores = new AlignmentScores(rs);
                cache.put(scores.getDbId2(), scores); 
            }

            rs.close();
            stmt.close();
            conn.close();
        
        } catch (SQLException e) {
            Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, e);
        }

        return cache;
    }
}
