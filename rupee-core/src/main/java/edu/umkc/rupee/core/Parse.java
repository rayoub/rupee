package edu.umkc.rupee.core;

public class Parse {

    public static int tryParseInt(String value) {

        try {
            int val = Integer.parseInt(value);
            return val;
        } catch (NumberFormatException e) {
            return -1;
        }
    }       
}
