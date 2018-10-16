package edu.umkc.rupee.ndd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.lib.Db;

public class UnionFind {

    private class Member {
        public String DbId;
        public String ParentDbId;
        public int Rank;
    }

    private List<NddSet> _setRecords = new ArrayList<>();

    private final static double MIN_SIMILARITY_THRESHOLD = 0.70;

    public void iterate() {

        List<NddPair> pairs = getPairs();
     
        for (double threshold = 0.95; threshold >= MIN_SIMILARITY_THRESHOLD; threshold = threshold - 0.01) {

            double efThreshold = threshold;
           
            List<NddPair> filteredPairs = pairs.stream()
                .filter(p -> p.getSimilarity() >= efThreshold)
                .collect(Collectors.toList());

            while (filteredPairs.size() > 0) {

                System.out.println("Filtered Pair Count = " + filteredPairs.size() + " at Threshold = " + threshold); 

                pairs = iteration(pairs, filteredPairs);

                filteredPairs = pairs.stream()
                    .filter(p -> p.getSimilarity() >= efThreshold)
                    .collect(Collectors.toList());
            } 
        }

        saveSets();
    }

    private List<NddPair> iteration(List<NddPair> pairs, List<NddPair> filteredPairs) {
       
        Map<String, Member> setMembers = new HashMap<>();
        Map<String, Double> pairSims = new HashMap<>();

        boolean start;
        
        // first pass - make set operations
        start = true; 
        for (NddPair pair : filteredPairs) {

            if (start) {
                System.out.println("Performing Make-Set Operations");
                start = false;
            }

            // store for future use
            pairSims.put(pair.getDbId1() + "_" + pair.getDbId2(), pair.getSimilarity());
            
            // make set ops
            if (!setMembers.containsKey(pair.getDbId1())) {
                Member member = new Member() {
                    {
                        DbId = pair.getDbId1();
                        ParentDbId = pair.getDbId1();
                        Rank = 0;
                    }
                };
                setMembers.put(pair.getDbId1(), member);
            }
            if (!setMembers.containsKey(pair.getDbId2())) {
                Member member = new Member() {
                    {
                        DbId = pair.getDbId2();
                        ParentDbId = pair.getDbId2();
                        Rank = 0;
                    }
                };
                setMembers.put(pair.getDbId2(), member);
            }
        }
        
        System.out.println("Done with Make-Set Operations");
        
        /*
            At this stage we have sets and set membership information. 
            Rank is used to implement the union by rank heuristic. 
        */
        
        // second pass - union operations
        start = true;
        for (NddPair pair : filteredPairs) {

            if (start) {
                System.out.println("Performing Union Operations");
                start = false;
            }
            
            String pivotDbId1 = findSet(pair.getDbId1(), setMembers);
            String pivotDbId2 = findSet(pair.getDbId2(), setMembers);

            //this would imply they are already members of the same set         
            if (pivotDbId1.equals(pivotDbId2)) {
                continue;
            }

            Member pivotMember1 = setMembers.get(pivotDbId1);
            Member pivotMember2 = setMembers.get(pivotDbId2);

            //union by rank
            if (pivotMember1.Rank > pivotMember2.Rank) {
                pivotMember2.ParentDbId = pivotDbId1;
            } else {
                pivotMember1.ParentDbId = pivotDbId2;

                // if ranks were equal we have increased the depth of the tree by 1
                if (pivotMember1.Rank == pivotMember2.Rank) {
                    pivotMember2.Rank++;
                }
            }
        }
       
        System.out.println("Done with Union Operations");

        System.out.println("Getting Set Records");
       
        // extract set records
        for (Entry<String, Member> entry : setMembers.entrySet()) {

            String dbId = entry.getKey();
            String pivotDbId = findSet(dbId, setMembers);
            
            NddSet record = new NddSet();
            record.setPivotDbId(pivotDbId);
            record.setMemberDbId(dbId);
            
            if(dbId.equals(pivotDbId)){
                record.setSimilaritiy(1.0);
            }
            else if(pairSims.containsKey(dbId + "_" + pivotDbId)){
                record.setSimilaritiy(pairSims.get(dbId + "_" + pivotDbId));
            }
            else if(pairSims.containsKey(pivotDbId + "_" + dbId)){
                record.setSimilaritiy(pairSims.get(pivotDbId + "_" + dbId));
            }
            else {
                continue;
            }
            
            _setRecords.add(record);
        }      

        System.out.println("Done Getting Set Records");
        
        System.out.println("Set Member Count = " + _setRecords.size());
        
        // filter pairs and return
        HashSet<String> dbIds = _setRecords.stream().map(s -> s.getMemberDbId()).collect(Collectors.toCollection(HashSet::new)); 
        return pairs.stream()
            .filter(p -> !dbIds.contains(p.getDbId1()) && !dbIds.contains(p.getDbId2()))
            .collect(Collectors.toList());
    }

    private List<NddPair> getPairs() {

        List<NddPair> pairs = new ArrayList<>();

        try {

            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn;
            PreparedStatement stmt;

            conn = ds.getConnection();
            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(
                    "SELECT p.db_id_1, p.db_id_2, p.similarity " + 
                    "FROM scop_pair p " + 
                    "WHERE p.similarity >= ?; " 
                );
            stmt.setDouble(1, MIN_SIMILARITY_THRESHOLD);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                
                NddPair pair = new NddPair();

                pair.setDbId1(rs.getString("db_id_1"));
                pair.setDbId2(rs.getString("db_id_2"));
                pair.setSimilarity(rs.getDouble("similarity"));

                pairs.add(pair);
            }
                    
        } catch (SQLException e) {
            Logger.getLogger(UnionFind.class.getName()).log(Level.SEVERE, null, e);
        }
        
        return pairs;    
    }
   
    private void saveSets() {

        try {

            PGSimpleDataSource ds = Db.getDataSource();

            Connection conn;
            PreparedStatement updt;

            conn = ds.getConnection();
            conn.setAutoCommit(true);        
            
            updt = conn.prepareStatement("SELECT insert_scop_sets(?);"); 

            NddSet a[] = new NddSet[_setRecords.size()];
            _setRecords.toArray(a);
            updt.setArray(1, conn.createArrayOf("scop_set", a));
            updt.execute();

            updt.close();
            conn.close();        
                    
        } catch (SQLException e) {
            Logger.getLogger(UnionFind.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private String findSet(String dbId, Map<String, Member> setMembers) {

        String parentDbId = setMembers.get(dbId).ParentDbId;

        // path compression
        if (!dbId.equals(parentDbId)) {
            setMembers.get(dbId).ParentDbId = findSet(parentDbId, setMembers);
        }

        return setMembers.get(dbId).ParentDbId;
    }
}
