package edu.umkc.rupee.core;

import java.util.List;
import java.util.Map;

public class Hasher {

    private final static long[] PRIMES = { 
        2654435761L, 2654435789L, 2654435803L, 2654435809L, 2654435813L, 2654435821L, 2654435827L, 2654435851L, 2654435909L, 2654435933L,
        2654435951L, 2654435999L, 2654436007L, 2654436011L, 2654436067L, 2654436073L, 2654436107L, 2654436181L, 2654436229L, 2654436241L,
        2654436283L, 2654436299L, 2654436353L, 2654436403L, 2654436413L, 2654436461L, 2654436469L, 2654436503L, 2654436563L, 2654436607L,
        2654436611L, 2654436641L, 2654436679L, 2654436691L, 2654436703L, 2654436709L, 2654436737L, 2654436749L, 2654436751L, 2654436761L,
        2654436773L, 2654436791L, 2654436833L, 2654436871L, 2654436881L, 2654436919L, 2654436929L, 2654436931L, 2654436947L, 2654436949L,
        2654436959L, 2654436977L, 2654437013L, 2654437057L, 2654437063L, 2654437067L, 2654437099L, 2654437109L, 2654437127L, 2654437139L,
        2654437157L, 2654437183L, 2654437229L, 2654437231L, 2654437243L, 2654437249L, 2654437267L, 2654437333L, 2654437363L, 2654437369L,
        2654437381L, 2654437391L, 2654437421L, 2654437441L, 2654437447L, 2654437469L, 2654437493L, 2654437507L, 2654437517L, 2654437543L,
        2654437561L, 2654437571L, 2654437573L, 2654437627L, 2654437633L, 2654437663L, 2654437711L, 2654437759L, 2654437763L, 2654437771L,
        2654437781L, 2654437787L, 2654437829L, 2654437853L, 2654437871L, 2654437901L, 2654437927L, 2654437957L, 2654437969L, 2654438009L
    };

    private final static int[] RANDOM_NUMBERS = { 
        995840969, 785116925, 452166487, 390228469, 726621655, 604536533, 661603350, 156273710, 830921069, 830180078,
        282105821, 704088338, 858465612, 823486316, 807381951, 135994276, 154893295, 123944097, 985458451, 906530239,
        946038320, 559199606, 301000995, 685755698, 190977004, 365787747, 929046885, 255539997, 852982752, 128055511,
        202909778, 450106276, 505707789, 898883261, 927444796, 777987187, 809599259, 367155117, 380458345, 417262965,
        885764173, 714945602, 838836910, 877354415, 515798617, 139232432, 478434320, 820393711, 976014690, 571627163,
        610707787, 736865463, 329668519, 462995319, 737292046, 130686225, 926524757, 687595451, 387866471, 588583481,
        666804491, 383487204, 155600354, 959178374, 515539378, 219331470, 275301860, 556176621, 731691377, 224278072,
        679247734, 259778991, 988318627, 500716386, 608736875, 602318888, 664727013, 261841274, 227332099, 874709394,
        500586336, 491330667, 797272747, 787624567, 878293342, 631101159, 408787173, 354806439, 258945344, 510002197,
        117203424, 940692191, 896841430, 510430869, 815152112, 825178102, 251076477, 877379191, 563452498, 894270271 
    };

    private int minHashCount;
    private int bandHashCount;

    public Hasher(int minHashCount, int bandHashCount) {

        this.minHashCount = minHashCount;
        this.bandHashCount = bandHashCount;
    }
   
    // for sets 
    public Integer[] getMinHashes(List<Integer> hashes) {

        Integer[] minHashes = new Integer[this.minHashCount];

        // initialize min-hashes
        for (int i = 0; i < minHashes.length; i++) {
            minHashes[i] = Integer.MAX_VALUE;
        }

        // iterate hashes and update min-hashes
        for (Integer hash : hashes) {

            updateMinHashes(minHashes, hash); 
        }

        return minHashes;
    }

    // for multisets (aka bags)
    public Integer[] getMinHashes(Map<Integer,Integer> hashMap, int DEC_POW_X) {
        
        Integer[] minHashes = new Integer[this.minHashCount];

        // initialize min-hashes
        for (int i = 0; i < minHashes.length; i++) {
            minHashes[i] = Integer.MAX_VALUE;
        }

        // iterate hashes and update min-hashes
        for (Integer hash : hashMap.keySet()) {

            // hashMap.get(hash) is the count index for that hash
            for (int i = 0; i < hashMap.get(hash); i++) {
                updateMinHashes(minHashes, hash + (i * DEC_POW_X)); 
            }
        }

        return minHashes;
    }

    private void updateMinHashes(Integer[] minHashes, Integer hash) {
        
        for (int i = 0; i < minHashes.length; i++) {         

            // throw exception if the long result overflows an int
            Integer minHash = Math.toIntExact((hash * PRIMES[i]) % RANDOM_NUMBERS[i]);
            
            minHashes[i] = Math.min(minHashes[i], minHash);
        }
    }
   
    public Integer[] getBandHashes(Integer[] minHashes) {

        Integer[] bandHashes = new Integer[this.bandHashCount];
        
        // initialize band hashes
        for (int i = 0; i < bandHashes.length; i++) {
            bandHashes[i] = 0;
        }

        // update band hashes
        for (int i = 0; i < this.minHashCount; i++) {
            bandHashes[i % bandHashes.length] += minHashes[i];
        }

        return bandHashes;
    }
}

