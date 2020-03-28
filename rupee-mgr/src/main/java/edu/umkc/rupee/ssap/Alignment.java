package edu.umkc.rupee.ssap;

import java.util.ArrayList;
import java.util.List;

public class Alignment {

    public static int NULL_POS = -1;

    private List<Integer> _apositions = new ArrayList<>();
    private List<Integer> _bpositions = new ArrayList<>();

    public int getLength() {

        // they both should be the same
        return _apositions.size();
    }

    public void add(int apos, int bpos) {

        _apositions.add(apos);
        _bpositions.add(bpos);
    }

    public boolean hasBothPositions(int index) {
        
        return _apositions.get(index) != NULL_POS && _bpositions.get(index) != NULL_POS;
    }

    public int getAPosition(int index) {
        
        return _apositions.get(index);
    }
    
    public int getBPosition(int index) {
        
        return _bpositions.get(index);
    }

    public String toString() {
       
        StringBuilder builder = new StringBuilder(); 
        for (int i = 0; i < _apositions.size(); i++) {

            builder.append(String.format("%d %d\n", _apositions.get(i), _bpositions.get(i)));
        }
        return builder.toString();
    }
}
