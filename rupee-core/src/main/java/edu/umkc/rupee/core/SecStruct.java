package edu.umkc.rupee.core;

import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.secstruc.SecStrucInfo;

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
            case "B":
                ss3 = "S";
                break;
            case "T":
            case "S":
            case "C":
                ss3 = "C";
                break;
            default:
                ss3 = "C";
        }
        return ss3;
    }    

    public static String toSs3MoreCoil(String ss8) {

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
                ss3 = "C";
        }
        return ss3;
    }    
    
    public static String toSs3LessCoil(String ss8) {

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
                ss3 = "C";
        }
        return ss3;
    }    
    
    public static String toSs8(Group g) {

        String ss8 = "";
        Object obj = g.getProperty(Group.SEC_STRUC);
        if (obj instanceof SecStrucInfo) {
           SecStrucInfo info = (SecStrucInfo)obj;
           ss8 = String.valueOf(info.getType().type).trim();
           if (ss8.isEmpty()) {
               ss8 = "C";
            }
        }
        return ss8;
    }
}
