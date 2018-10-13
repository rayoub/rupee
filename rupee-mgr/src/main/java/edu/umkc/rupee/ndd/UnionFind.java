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
import java.util.Set;

import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.lib.Db;

public class UnionFind {

    private class Member {
        public String DbId;
        public String ParentDbId;
        public int Rank;
    }

    private final Map<String, Member> _setMembers = new HashMap<>();
    private final List<NddSet> _setRecords = new ArrayList<>();
    private final Map<String, Double> _pairSims = new HashMap<>();

    private final static double DEFAULT_SIMILARITY_THRESHOLD = 0.75;
    
    private double similarityThreshold;

    public UnionFind () {

        this.similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD;
    }
   
    public UnionFind (double similarityThreshold) {

        this.similarityThreshold = similarityThreshold;
    }

    public void go() throws SQLException {

        PGSimpleDataSource ds = Db.getDataSource();

        Connection conn;
        PreparedStatement stmt;

        conn = ds.getConnection();
        conn.setAutoCommit(false);

        stmt = conn.prepareStatement("SELECT db_id_1, db_id_2, similarity FROM scop_pair WHERE similarity >= ?;");
        stmt.setDouble(1, similarityThreshold);
        
        // first pass - make set operations
        
        System.out.println("Performing Make-Set Operations");
           
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {

            String dbId1 = rs.getString("db_id_1");
            String dbId2 = rs.getString("db_id_2");
            double similarity = rs.getDouble("similarity");

            // store for future use
            _pairSims.put(dbId1 + "_" + dbId2, similarity);
            
            // make set ops
            if (!_setMembers.containsKey(dbId1)) {
                Member member = new Member() {
                    {
                        DbId = dbId1;
                        ParentDbId = dbId1;
                        Rank = 0;
                    }
                };
                _setMembers.put(dbId1, member);
            }
            if (!_setMembers.containsKey(dbId2)) {
                Member member = new Member() {
                    {
                        DbId = dbId2;
                        ParentDbId = dbId2;
                        Rank = 0;
                    }
                };
                _setMembers.put(dbId2, member);
            }
        }
        rs.close();
        
        System.out.println("Done with Make-Set Operations");
        
        /*
            At this stage we have sets and set membership information. 
            Rank is used to implement the union by rank heuristic. 
        */
        
        System.out.println("Performing Union Operations");

        // second pass - union operations
        rs = stmt.executeQuery();
        while (rs.next()) {

            String dbId1 = rs.getString("db_id_1");
            String dbId2 = rs.getString("db_id_2");

            String pivotDbId1 = findSet(dbId1);
            String pivotDbId2 = findSet(dbId2);

            //this would imply they are already members of the same set         
            if (pivotDbId1.equals(pivotDbId2)) {
                continue;
            }

            Member pivotMember1 = _setMembers.get(pivotDbId1);
            Member pivotMember2 = _setMembers.get(pivotDbId2);

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
        rs.close();
       
        System.out.println("Done with Union Operations");

        // close connection 
        stmt.close();
        conn.close();        

        System.out.println("Assigning Similarities");
       
        // assign known similarities
        Set<String> dbIdSet = new HashSet<>();
        for (Entry<String, Member> entry : _setMembers.entrySet()) {

            String dbId = entry.getKey();
            String pivotDbId = findSet(dbId);
            
            NddSet record = new NddSet();
            record.setPivotDbId(pivotDbId);
            record.setMemberDbId(dbId);
            
            if(dbId.equals(pivotDbId)){
                record.setSimilaritiy(1.0);
            }
            else if(_pairSims.containsKey(dbId + "_" + pivotDbId)){
                record.setSimilaritiy(_pairSims.get(dbId + "_" + pivotDbId));
            }
            else if(_pairSims.containsKey(pivotDbId + "_" + dbId)){
                record.setSimilaritiy(_pairSims.get(pivotDbId + "_" + dbId));
            }
            else {
                record.setSimilaritiy(-1.0);
                dbIdSet.add(record.getPivotDbId());
                dbIdSet.add(record.getMemberDbId());
            }
            
            _setRecords.add(record);
        }      
        
        System.out.println("Done Assigning Scores");
    }
    
    public void saveSets() throws SQLException{

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
    }

    private String findSet(String dbId) {

        String parentDbId = _setMembers.get(dbId).ParentDbId;

        // path compression
        if (!dbId.equals(parentDbId)) {
            _setMembers.get(dbId).ParentDbId = findSet(parentDbId);
        }

        return _setMembers.get(dbId).ParentDbId;
    }
}
