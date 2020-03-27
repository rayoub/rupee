package edu.umkc.rupee.ssap;


public class Alignment {

    private int _length = 0;
    private int[] _apositions = new int[_length];
    private int[] _bpositions = new int[_length];

    public int getLength() {

        return 0;
    }

    public boolean hasBothPositions(int index) {
        
        return _apositions[index] != 0 && _bpositions[index] != 0;
    }

    public int getAPositionOffset1(int index) {
        
        return _apositions[index] + 1;
    }
    
    public int getBPositionOffset1(int index) {
        
        return _bpositions[index] + 1;
    }
}
