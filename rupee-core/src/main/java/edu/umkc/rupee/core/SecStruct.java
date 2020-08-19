package edu.umkc.rupee.core;

public class SecStruct {

    public static String toSs3(String ss8) {

        String ss3;
        switch(ss8) {
            case "G":
            case "H":
            case "I":
                ss3 = "H";
                break;
            case "E":
                ss3 = "S";
                break;
            case "B":
            case "T":
            case "S":
            case "C":
                ss3 = "C";
                break;
            default:
                ss3 = "";
        }
        return ss3;
    }    
    
    public static String toSs3Alt(String ss8) {

        String ss3;
        switch(ss8) {
            case "G":
            case "H":
            case "I":
            case "T":
                ss3 = "H";
                break;
            case "E":
            case "B":
                ss3 = "S";
                break;
            case "S":
            case "C":
                ss3 = "C";
                break;
            default:
                ss3 = "";
        }
        return ss3;
    }    
}
