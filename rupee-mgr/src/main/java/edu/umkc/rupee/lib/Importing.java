package edu.umkc.rupee.lib;

import java.util.ArrayList;
import java.util.List;

import org.biojava.nbio.structure.AminoAcid;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.secstruc.SecStrucCalc;
import org.biojava.nbio.structure.secstruc.SecStrucInfo;

public class Importing {

    public static List<Residue> parseStructure(Structure structure) {

        List<Residue> residues = new ArrayList<>();
            
        // assign secondary structure
        SecStrucCalc ssCalc = new SecStrucCalc();
        try {
            ssCalc.calculate(structure, true);
        } catch (StructureException e) {
            // do nothing
        }

        // iterate chains
        List<Chain> chains = structure.getChains();
        for(Chain chain : chains) {
        
            // *** gather residues

            // iterate residues (i.e. groups of atoms)
            List<Group> groups = chain.getAtomGroups();
            for (int i = 1; i < groups.size() - 1; i++) {

                Group g1 = groups.get(i - 1);
                Group g2 = groups.get(i);
                Group g3 = groups.get(i + 1);
               
                String residueCode = g2.getChemComp().getOne_letter_code();

                // we need the carbon alpha
                if (!g2.hasAtom("CA")) {
                    continue;
                }
                
                // get secondary structure assignment
                String ssa = "C";
                Object obj = g2.getProperty(Group.SEC_STRUC);
                if (obj instanceof SecStrucInfo) {
                   SecStrucInfo info = (SecStrucInfo)obj;
                   ssa = String.valueOf(info.getType().type).trim();
                   if (ssa.isEmpty()) {
                       ssa = "C";
                    }
                }

                // map to extension coding
                String sse;
                switch(ssa) {
                    case "G":
                    case "H":
                    case "I":
                    case "T":
                        sse = "Helix";
                        break;
                    case "E":
                    case "B":
                        sse = "Strand";
                        break;
                    case "S":
                    case "C":
                        sse = "Loop";
                        break;
                    default:
                        sse = "Loop";
                }
                
                // calculate torsion angles
                double phi = 360.0;
                double psi = 360.0;
                boolean breakBefore = false;
                boolean breakAfter = false;
                try {
                    if (g1 instanceof AminoAcid && g2 instanceof AminoAcid && g3 instanceof AminoAcid) {
                        AminoAcid a1 = (AminoAcid) g1;
                        AminoAcid a2 = (AminoAcid) g2;
                        AminoAcid a3 = (AminoAcid) g3;
                       
                        // check connectivity
                        breakBefore = !Calc.isConnected(a1,a2);
                        breakAfter = !Calc.isConnected(a2,a3);
                        if (!breakBefore && !breakAfter) {
                            phi = Calc.getPhi(a1,a2);
                            psi = Calc.getPsi(a2,a3);
                        }
                    }
                } catch (StructureException e) {
                    // do nothing
                }

                Residue residue = new Residue();

                residue.setPdbId(structure.getPDBCode());
                residue.setChainId(chain.getId());
                residue.setAtomNumber(g2.getAtom("CA").getPDBserial());
                residue.setResidueNumber(g2.getResidueNumber().getSeqNum());
                residue.setResidueCode(String.valueOf(g2.getResidueNumber().getInsCode()));
                residue.setResidueCode(residueCode);
                residue.setSSA(ssa);
                residue.setSSE(sse);
                residue.setPhi(phi);
                residue.setPsi(psi);
                residue.setDescriptor(calculateRegion(phi,psi,sse));
                residue.setBreakBefore(breakBefore);
                residue.setBreakAfter(breakAfter);

                residues.add(residue);
            }
            
            // *** run position encoding by region

            List<Residue> run = new ArrayList<>();
            int lastRegion = Integer.MIN_VALUE; 

            for (Residue residue : residues) {

                // run end
                if (residue.getDescriptor() != lastRegion && lastRegion != Integer.MIN_VALUE) {
                    
                    setRunFactors(run);
                    run.clear();
                    run = new ArrayList<>();
                }

                // accumulate run grams
                run.add(residue);
                lastRegion = residue.getDescriptor();
            }

            // process last run
            if (!run.isEmpty()) {
                setRunFactors(run); 
                run.clear();
            }
            
            // *** calculate gram hashes
    
            for (int i = 0; i < residues.size() - 2; i++) {

                Residue residue1 = residues.get(i);
                Residue residue2 = residues.get(i + 1);
                Residue residue3 = residues.get(i + 2);

                // good window
                if (!(
                        residue1.getBreakAfter() || residue2.getBreakAfter() || 
                        residue2.getBreakBefore() || residue3.getBreakBefore()
                    )) {

                    // abc
                    int gram = 
                        residue1.getDescriptor() * Constants.DEC_POW_2 + 
                        residue2.getDescriptor() * Constants.DEC_POW_1 +
                        residue3.getDescriptor();
                   
                    // rr abc
                    int runFactor = residue1.getRunFactor() % 100;
                    gram = gram + runFactor * Constants.DEC_POW_3; 

                    // set hashes
                    residue1.setGram(gram);
                }
            }

        } // iterating chains

        return residues;
    }

    public static void setRunFactors(List<Residue> residues) {

        // only runs of regular secondary structures receive run factors
        if (residues.get(0).isHelix() || residues.get(0).isStrand() || residues.get(0).isTurn()) {
            int test = Math.floorDiv(residues.size(), 2);
            for (int i = 0; i < residues.size(); i++) {
                if (i >= test) {
                    residues.get(i).setRunFactor(residues.size() - 1 - i);
                } else {
                    residues.get(i).setRunFactor(i);
                }
            }
        }
    }
    
    public static int calculateRegion(double phi, double psi, String sse) {

        if (phi == 360 || psi == 360) 
            return calculateDefaultRegion(sse);
        
        // helix 0, 1, 2, 3
        else if (sse.equals("Helix")) {
            return calculateHelixRegion(phi, psi);
        }

        // strand 4, 5, 6
        else if (sse.equals("Strand")) {
            return calculateStrandRegion(phi, psi);
        }
     
        // loop 7, 8, 9
        else { 
            return calculateLoopRegion(phi, psi);
        }
    }

    public static int calculateDefaultRegion(String sse) {
        
        // helix 0, 1, 2, 3
        if (sse.equals("Helix")) {
            return 2;
        }

        // strand 4, 5, 6
        else if (sse.equals("Strand")) {
            return 4;
        }
      
        // loop 7, 8, 9
        else {  
            return 7;
        }
    }

    public static int calculateHelixRegion(double phi, double psi) {

        int region = -1;

        if (psi >= -180 && psi < -135) {
            if (phi >= 0 && phi < 180) {
                region = 3;
            }
            else {
                region = 0; 
            }
        }
        else if (psi >= -135 && psi < -75) {
            if (phi >= 0 && phi < 180) {
                region = 3;
            }
            else {
                region = 2;
            }
        }
        else if (psi >= -75 && psi < 90) {
            if (phi >= 0 && phi < 180) {
                region = 1;
            }
            else {
                region = 2;
            }
        }
        else if (psi >= 90 && psi < 120) {
            if (phi >= 0 && phi < 180) {
                region = 1;
            }
            else {
                region = 0;
            }
        }
        else if (psi >= 120 && psi < 180) {
            if (phi >= 0 && phi < 180) {
                region = 3;
            }
            else {
                region = 0;
            }
        }
        
        return region;
    }

    public static int calculateStrandRegion(double phi, double psi) {

        int region = -1;

        if (psi >= -180 && psi < -110) {
            region = 4;
        }
        else if (psi >= -110 && psi < -60) {
            if (phi >= 0 && phi < 180) {
                region = 4;
            }
            else {
                region = 6;
            }
        }
        else if (psi >= -60 && psi < 60) {
            if (phi >= 0 && phi < 180) {
                region = 5;
            }
            else {
                region = 6;
            }
        }
        else if (psi >= 60 && psi < 90) {
            if (phi >= 0 && phi < 180) {
                region = 5;
            }
            else {
                region = 4;
            }
        }
        else if (psi >= 90 && psi < 180) {
            region = 4;
        }

        return region;
    }
    
    public static int calculateLoopRegion(double phi, double psi) {

        int region = -1;

        if (psi >= -180 && psi < -100) {
            region = 7;
        }
        else if (psi >= -100 && psi < -90) {
            if (phi >= 0 && phi < 180) {
                region = 7;
            }
            else {
                region = 9;
            }
        }
        else if (psi >= -90 && psi < 60) {
            if (phi >= 0 && phi < 180) {
                region = 8;
            }
            else {
                region = 9;
            }
        }
        else if (psi >= 60 && psi < 90) {
            if (phi >= 0 && phi < 180) {
                region = 8;
            }
            else {
                region = 7;
            }
        }
        else if (psi >= 90 && psi < 180) {
            region = 7;
        }
            
        return region;
    }
}
