package edu.umkc.rupee.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Similarity {

    public static double getEstimatedSimilarity(Integer[] minHashes1, Integer[] minHashes2) {

        // assuming array lengths are equal
      
        int similarity = 0;
        for (int i = 0; i < minHashes1.length; i++) {

            if (minHashes1[i].equals(minHashes2[i])) {
                similarity++;
            }
        }

        return ((double)similarity) / minHashes1.length;
    }

    public static double getExactSimilarity(List<Integer> grams1, List<Integer> grams2) {

        // get maps of gram counts
    
        Map<Integer, Integer> map1 = new HashMap<>();
        for (Integer gram : grams1) {

            if (map1.containsKey(gram)) {
                map1.replace(gram, map1.get(gram) + 1);
            } else {
                map1.put(gram, 1);
            }
        }
        
        Map<Integer, Integer> map2 = new HashMap<>();
        for (Integer gram : grams2) {

            if (map2.containsKey(gram)) {
                map2.replace(gram, map2.get(gram) + 1);
            } else {
                map2.put(gram, 1);
            }
        }

        // create sets from multisets
       
        Set<Integer> set1 = new HashSet<Integer>(); 
        for (Entry<Integer,Integer> entry : map1.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                set1.add(entry.getKey() + (i * Constants.DEC_POW_7));
            }    
        }
        
        Set<Integer> set2 = new HashSet<Integer>(); 
        for (Entry<Integer,Integer> entry : map2.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                set2.add(entry.getKey() + (i * Constants.DEC_POW_7));
            }    
        }

        // union
        
        Set<Integer> union = new HashSet<>(set1);
        union.addAll(set2);

        // intersection
        
        Set<Integer> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        return ((double) intersection.size()) / ((double) union.size());
    }

    public static double getAdjustedSimilarity(List<Integer> grams1, List<Integer> grams2, int lcsLength) {

        // get maps of gram counts
    
        Map<Integer, Integer> map1 = new HashMap<>();
        for (Integer gram : grams1) {
            if (map1.containsKey(gram)) {
                map1.replace(gram, map1.get(gram) + 1);
            } else {
                map1.put(gram, 1);
            }
        }
        
        Map<Integer, Integer> map2 = new HashMap<>();
        for (Integer gram : grams2) {
            if (map2.containsKey(gram)) {
                map2.replace(gram, map2.get(gram) + 1);
            } else {
                map2.put(gram, 1);
            }
        }

        // create sets from multisets
       
        Set<Integer> set1 = new HashSet<Integer>(); 
        for (Entry<Integer,Integer> entry : map1.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                set1.add(entry.getKey() + (i * Constants.DEC_POW_7));
            }    
        }
        
        Set<Integer> set2 = new HashSet<Integer>(); 
        for (Entry<Integer,Integer> entry : map2.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                set2.add(entry.getKey() + (i * Constants.DEC_POW_7));
            }    
        }

        // union
        
        Set<Integer> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return ((double) lcsLength) / ((double) union.size());
    }

    public static double getAdjustedSimilarity(List<Integer> grams1, List<Integer> grams2, LCSResults results) {

        // get maps of gram counts
    
        Map<Integer, Integer> map1 = new HashMap<>();
        for (int i = results.iMin - 1; i < results.iMax; i++) {
            int gram = grams1.get(i);
            if (map1.containsKey(gram)) {
                map1.replace(gram, map1.get(gram) + 1);
            } else {
                map1.put(gram, 1);
            }
        }
        
        Map<Integer, Integer> map2 = new HashMap<>();
        for (int j = results.jMin - 1; j < results.jMax; j++) {
            int gram = grams2.get(j);
            if (map2.containsKey(gram)) {
                map2.replace(gram, map2.get(gram) + 1);
            } else {
                map2.put(gram, 1);
            }
        }

        // create sets from multisets
       
        Set<Integer> set1 = new HashSet<Integer>(); 
        for (Entry<Integer,Integer> entry : map1.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                set1.add(entry.getKey() + (i * Constants.DEC_POW_7));
            }    
        }
        
        Set<Integer> set2 = new HashSet<Integer>(); 
        for (Entry<Integer,Integer> entry : map2.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                set2.add(entry.getKey() + (i * Constants.DEC_POW_7));
            }    
        }

        // union
        
        Set<Integer> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return ((double) results.matchCount) / ((double) union.size());
    }
}
