package edu.umkc.rupee.search.lib;

import java.util.ArrayList;
import java.util.List;

import org.biojava.nbio.structure.AminoAcid;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.secstruc.SecStrucCalc;

import edu.umkc.rupee.core.Descriptor;
import edu.umkc.rupee.core.SecStruct;

public class Importing {

    public static List<Residue> parseResidues(Structure structure) {

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
                String ss8 = SecStruct.toSs8(g2); 
                if (ss8.isEmpty()) {
                    ss8 = "C";
                }

                // map to 3-state 
                String ss3 = SecStruct.toSs3LessCoil(ss8);

                // get coordinates
                Atom ca = g2.getAtom("CA");
                float x = (float) ca.getX();
                float y = (float) ca.getY();
                float z = (float) ca.getZ();
                
                // calculate torsion angles
                double phi = Descriptor.NULL_ANGLE;
                double psi = Descriptor.NULL_ANGLE;
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
                residue.setChainName(chain.getId());
                residue.setAtomNumber(g2.getAtom("CA").getPDBserial());
                residue.setResidueNumber(g2.getResidueNumber().getSeqNum());
                residue.setInsertCode(String.valueOf(g2.getResidueNumber().getInsCode()));
                residue.setResidueCode(residueCode);
                residue.setSs8(ss8);
                residue.setSs3(ss3);
                residue.setX(x);
                residue.setY(y);
                residue.setZ(z);
                residue.setPhi(phi);
                residue.setPsi(psi);
                residue.setDescriptor(Descriptor.toDescriptor(phi,psi,ss3));
                residue.setBreakBefore(breakBefore);
                residue.setBreakAfter(breakAfter);

                residues.add(residue);
            }
            
            // *** run position encoding by region

            List<Residue> run = new ArrayList<>();
            int lastDescr = Integer.MIN_VALUE; 

            for (Residue residue : residues) {

                // run end
                if (residue.getDescriptor() != lastDescr && lastDescr != Integer.MIN_VALUE) {
                    
                    setRunFactors(run);
                    run.clear();
                    run = new ArrayList<>();
                }

                // accumulate run grams
                run.add(residue);
                lastDescr = residue.getDescriptor();
            }

            // process last run
            if (!run.isEmpty()) {
                setRunFactors(run); 
                run.clear();
            }
            
            // *** calculate gram hashes
    
            for (int i = 1; i < residues.size() - 1; i++) {

                Residue residue1 = residues.get(i - 1);
                Residue residue2 = residues.get(i);
                Residue residue3 = residues.get(i + 1);

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
                    residue2.setGram(gram);
                }
            }

        } // iterating chains

        return residues;
    }

    public static void setRunFactors(List<Residue> residues) {

        // only runs of regular secondary structures receive run factors
        if (residues.get(0).isHelix() || residues.get(0).isStrand()) {
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
}
