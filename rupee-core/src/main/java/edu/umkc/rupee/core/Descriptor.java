package edu.umkc.rupee.core;

public class Descriptor {

    public static double NULL_ANGLE = 360.0;

    public static int toDescriptor(double phi, double psi, String ss3) {

        if (phi == NULL_ANGLE || psi == NULL_ANGLE) 
            return toDefaultDescriptor(ss3);

        // helix 0, 1, 2, 3
        if (ss3.equals("H")) {
            return toHelixDescriptor(phi, psi);
        }

        // strand 4, 5, 6
        else if (ss3.equals("S")) {
            return toStrandDescriptor(phi, psi);
        }
     
        // loop 7, 8, 9
        else {
            return toLoopDescriptor(phi, psi);
        }
    }

    public static int toDefaultDescriptor(String ss3) {
        
        // helix 0, 1, 2, 3
        if (ss3.equals("H")) {
            return 2;
        }

        // strand 4, 5, 6
        else if (ss3.equals("S")) {
            return 4;
        }
      
        // loop 7, 8, 9
        else {  
            return 7;
        }
    }

    public static int toHelixDescriptor(double phi, double psi) {

        int descr = -1;

        if (psi >= -180 && psi < -135) {
            if (phi >= 0 && phi < 180) {
                descr = 3;
            }
            else {
                descr = 0; 
            }
        }
        else if (psi >= -135 && psi < -90) {
            if (phi >= 0 && phi < 180) {
                descr = 3;
            }
            else {
                descr = 2;
            }
        }
        else if (psi >= -90 && psi < 90) {
            if (phi >= 0 && phi < 180) {
                descr = 1;
            }
            else {
                descr = 2;
            }
        }
        else if (psi >= 90 && psi < 180) {
            if (phi >= 0 && phi < 180) {
                descr = 3;
            }
            else {
                descr = 0;
            }
        }
        
        return descr;
    }

    public static int toStrandDescriptor(double phi, double psi) {

        int descr = -1;

        if (psi >= -180 && psi < -100) {
            descr = 4;
        }
        else if (psi >= -100 && psi < -90) {
            if (phi >= 0 && phi < 180) {
                descr = 4;
            }
            else {
                descr = 6;
            }
        }
        else if (psi >= -90 && psi < 40) {
            if (phi >= 0 && phi < 180) {
                descr = 5;
            }
            else {
                descr = 6;
            }
        }
        else if (psi >= 40 && psi < 90) {
            if (phi >= 0 && phi < 180) {
                descr = 5;
            }
            else {
                descr = 4;
            }
        }
        else if (psi >= 90 && psi < 180) {
            descr = 4;
        }

        return descr;
    }
    
    public static int toLoopDescriptor(double phi, double psi) {

        int descr = -1;

        if (psi >= -180 && psi < -100) {
            descr = 7;
        }
        else if (psi >= -100 && psi < -90) {
            if (phi >= 0 && phi < 180) {
                descr = 7;
            }
            else {
                descr = 9;
            }
        }
        else if (psi >= -90 && psi < 40) {
            if (phi >= 0 && phi < 180) {
                descr = 8;
            }
            else {
                descr = 9;
            }
        }
        else if (psi >= 40 && psi < 90) {
            if (phi >= 0 && phi < 180) {
                descr = 8;
            }
            else {
                descr = 7;
            }
        }
        else if (psi >= 90 && psi < 180) {
            descr = 7;
        }

        return descr;
    }

    public static boolean isHelix(int descr) {

        if (descr >= 0 && descr <= 3) {
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean isStrand(int descr) {

        if (descr >= 4 && descr <= 6) { 
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean isLoop(int descr) {

        if (descr >= 7 && descr <= 9) {
            return true;
        }
        else {
            return false;
        }
    }

    public static String toSs3(int descr) {

        if (isHelix(descr)) {
            return "H";
        }
        else if (isStrand(descr)) {
            return "S";
        }
        else  {
            return "C";
        }
    }
}

