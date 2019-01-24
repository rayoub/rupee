package edu.umkc.rupee.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.nbio.structure.AminoAcid;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.biojava.nbio.structure.secstruc.SecStrucCalc;
import org.biojava.nbio.structure.secstruc.SecStrucInfo;

import edu.umkc.rupee.base.Import;
import edu.umkc.rupee.defs.DbTypeCriteria;

public class Labels {

    public static class Label {

        private int residueNumber;
        private int label;

        public Label (int residueNumber, int descriptor) {
            this.residueNumber = residueNumber;
            this.label = descriptor;
        }

        public int getResidueNumber() {
            return residueNumber;
        }

        public void setResidueNumber(int residueNumber) {
            this.residueNumber = residueNumber;
        }

        public int getLabel() {
            return label;
        }

        public void setLabel(int descriptor) {
            this.label = descriptor;
        }
    }

    public static List<Label> getLabels(String dbId, DbTypeCriteria dbType) {

        List<Label> labels = null;

        try {

            // get file name
            String fileName = dbType.getImportPath() + dbId + ".pdb.gz";

            // read file into structure
            PDBFileReader reader = new PDBFileReader();
            reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);
            Structure structure = reader.getStructure(fileName);

            // get labels
            labels = parseStructure(structure);
        
        } catch (IOException e) {
            Logger.getLogger(Labels.class.getName()).log(Level.SEVERE, dbId, e);
        }

        return labels;
    }

    public static List<Label> parseStructure(Structure structure) {

        List<Label> labels = new ArrayList<>();

        SecStrucCalc ssCalc = new SecStrucCalc();
            
        // assign secondary structure
        try {
            ssCalc.calculate(structure, true);
        } catch (StructureException e) {
            // do nothing
        }

        // iterate chains
        List<Chain> chains = structure.getChains();
        for(Chain chain : chains) {
        
            // iterate residues (i.e. groups of atoms)
            List<Group> groups = chain.getAtomGroups();
            for (int i = 1; i < groups.size() - 1; i++) {

                Group g1 = groups.get(i - 1);
                Group g2 = groups.get(i);
                Group g3 = groups.get(i + 1);
               
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
                        sse = "Helix";
                        break;
                    case "T":
                        sse = "Turn";
                        break;
                    case "E":
                        sse = "Strand";
                        break;
                    case "B":
                        sse = "Bridge";
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

                int residueNumber = g2.getResidueNumber().getSeqNum();
                int label = Import.calculateRegion(phi, psi, sse);

                labels.add(new Label(residueNumber, label));
            }
            
        } // iterating chains

        return labels;
    }
}

