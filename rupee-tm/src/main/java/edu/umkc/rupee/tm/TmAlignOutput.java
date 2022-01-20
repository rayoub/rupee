package edu.umkc.rupee.tm;

import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.secstruc.SecStrucCalc;
import org.biojava.nbio.structure.secstruc.SecStrucInfo;

public class TmAlignOutput {

    public static String alignTextOutput(TmResult results) {

        int k = 0;
        double d = 0.0;
        double d0_out = 5.0;

        double seq_id;
        int i, j;
        int ali_len = results.get_xlen() + results.get_ylen();
        char[] seqM = new char[ali_len];
        char[] seqxA = new char[ali_len];
        char[] seqyA = new char[ali_len];

        seq_id = 0;
        int kk = 0, i_old = 0, j_old = 0;
        for (k = 0; k < results.get_alignlen(); k++) {
            for (i = i_old; i < results.get_m1()[k]; i++) {
                // align x to gap
                seqxA[kk] = results.get_seqx()[i];
                seqyA[kk] = '-';
                seqM[kk] = ' ';
                kk++;
            }

            for (j = j_old; j < results.get_m2()[k]; j++) {
                // align y to gap
                seqxA[kk] = '-';
                seqyA[kk] = results.get_seqy()[j];
                seqM[kk] = ' ';
                kk++;
            }

            seqxA[kk] = results.get_seqx()[results.get_m1()[k]];
            seqyA[kk] = results.get_seqy()[results.get_m2()[k]];
            if (seqxA[kk] == seqyA[kk]) {
                seq_id++;
            }
            d = Math.sqrt(Functions.dist(results.get_xt()[results.get_m1()[k]], results.get_ya()[results.get_m2()[k]]));
            if (d < d0_out) {
                seqM[kk] = ':';
            } else {
                seqM[kk] = '.';
            }
            kk++;
            i_old = results.get_m1()[k] + 1;
            j_old = results.get_m2()[k] + 1;
        }

        // tail
        for (i = i_old; i < results.get_xlen(); i++) {
            // align x to gap
            seqxA[kk] = results.get_seqx()[i];
            seqyA[kk] = '-';
            seqM[kk] = ' ';
            kk++;
        }
        for (j = j_old; j < results.get_ylen(); j++) {
            // align y to gap
            seqxA[kk] = '-';
            seqyA[kk] = results.get_seqy()[j];
            seqM[kk] = ' ';
            kk++;
        }

        seq_id = seq_id / (results.get_alignlen() + 0.00000001); // what did by TMalign, but not reasonable, it
                                                                     // should be n_ali8
        
                                                                     StringBuilder sb = new StringBuilder();
        try (Formatter formatter = new Formatter(sb, Locale.US)) {

            formatter.format("\nName of Chain_1: %s\n", results.get_xname());
            formatter.format("Name of Chain_2: %s\n", results.get_yname());
            formatter.format("Length of Chain_1: %d residues\n", results.get_xlen());
            formatter.format("Length of Chain_2: %d residues\n\n", results.get_ylen());

            formatter.format("Aligned length= %d, RMSD= %6.2f, Seq_ID=n_identical/n_aligned= %4.3f\n",
                    results.get_alignlen(), results.get_rmsd(), seq_id);
            formatter.format("TM-score= %6.5f (if normalized by length of Chain_1)\n", results.get_tmq());
            formatter.format("TM-score= %6.5f (if normalized by length of Chain_2)\n", results.get_tmt());

            formatter.format("TM-score= %6.5f (if normalized by average length of chains)\n", results.get_tmavg());

            // output structure alignment
            formatter.format("\n(\":\" denotes residue pairs of d < %4.1f Angstrom, ", d0_out);
            formatter.format("\".\" denotes other aligned residues)\n");

            for (i = 0; i < ali_len; i = i + 120) {

                int from = i;
                int to = Math.min(ali_len, i + 120);

                formatter.format("%s\n", new String(Arrays.copyOfRange(seqxA, from, to)));
                formatter.format("%s\n", new String(Arrays.copyOfRange(seqM, from, to)));
                formatter.format("%s\n\n", new String(Arrays.copyOfRange(seqyA, from, to)));
            }
        }

        return sb.toString();
    }

    public static String align3dOutput(TmResult results) {

        // assign secondary structure
        SecStrucCalc xssCalc = new SecStrucCalc();
        try {
            xssCalc.calculate(results.get_xstruct(), true);
        } catch (StructureException e) {
            // do nothing
        }
        SecStrucCalc yssCalc = new SecStrucCalc();
        try {
            yssCalc.calculate(results.get_ystruct(), true);
        } catch (StructureException e) {
            // do nothing
        }

        // superimpose x coords onto y coords
        translate(results.get_xgroups(), results.get_t(), results.get_u());

        StringBuilder sb = new StringBuilder();

        // set sec structs
        writeHelicesToPdb(sb, results.get_xgroups(), "A");
        writeHelicesToPdb(sb, results.get_ygroups(), "B");
        writeStrandsToPdb(sb, results.get_xgroups(), "A");
        writeStrandsToPdb(sb, results.get_ygroups(), "B");

        writeChainToPdb(sb, results.get_xgroups(), "A");
        writeChainToPdb(sb, results.get_ygroups(), "B");
       
        return sb.toString(); 
    }

    // **************************************************************************************************************************************************
    // *** WRITE TO PDB
    // **************************************************************************************************************************************************

    private static void writeHelicesToPdb(StringBuilder sb, List<Group> groups, String chainId) {

        // iterate helices
        boolean lastWasHelix = false;
        int lastResidue = Integer.MIN_VALUE;
        for (int i = 0; i < groups.size(); i++) {

            Group group = groups.get(i);
            String ss = get3state(group);
            if (ss.equals("HELIX")) {

                if (!lastWasHelix) {

                    sb.append("HELIX");
                    sb.append(spaces(14));
                    sb.append(chainId);
                    sb.append(String.format("%5d", group.getResidueNumber().getSeqNum()));
                    sb.append(spaces(8));
                }
                lastWasHelix = true;
            }
            else {

                if (lastWasHelix) {
                    sb.append(String.format("%4d", lastResidue));
                    sb.append("\n");
                }
                lastWasHelix = false;
            }
            lastResidue = group.getResidueNumber().getSeqNum();
        }
    }

    private static void writeStrandsToPdb(StringBuilder sb, List<Group> groups, String chainId) {

        // iterate helices
        boolean lastWasStrand = false;
        int lastResidue = Integer.MIN_VALUE;
        for (int i = 0; i < groups.size(); i++) {

            Group group = groups.get(i);
            String ss = get3state(group);
            if (ss.equals("STRAND")) {

                if (!lastWasStrand) {

                    sb.append("SHEET");
                    sb.append(spaces(16));
                    sb.append(chainId);
                    sb.append(String.format("%4d", group.getResidueNumber().getSeqNum()));
                    sb.append(spaces(7));
                }
                lastWasStrand = true;
            }
            else {

                if (lastWasStrand) {
                    sb.append(String.format("%4d", lastResidue));
                    sb.append("\n");
                }
                lastWasStrand = false;
            }
            lastResidue = group.getResidueNumber().getSeqNum();
        }
    }

    private static void writeChainToPdb(StringBuilder sb, List<Group> groups, String chainId) {
      
        // iterate x atoms for chain A
        for (int i = 0; i < groups.size(); i++) {
            
            Group group = groups.get(i);
            group.getChain().setName(chainId);
            
            if (group.hasAtom("N")) {
                
                Atom n = group.getAtom("N");
                sb.append(n.toPDB());
            }

            // for known CA
            Atom ca = group.getAtom("CA");
            sb.append(ca.toPDB());

            if (group.hasAtom("C")) {

                Atom c = group.getAtom("C");
                sb.append(c.toPDB());
            }
            
            if (group.hasAtom("O")) {

                Atom o = group.getAtom("O");
                sb.append(o.toPDB());
            }
        }

        // chain termination
        sb.append("TER\n");
    }

    // **************************************************************************************************************************************************
    // *** HELPERS
    // **************************************************************************************************************************************************

    private static void translate(List<Group> groups, double[] t, double[][] u) {

        // count atoms
        int xlen = 0;
        for(Group g : groups) {
           
            if (g.hasAtom("N")) xlen++;
            xlen++; // for known CA
            if (g.hasAtom("C")) xlen++;
            if (g.hasAtom("O")) xlen++;
        }

        double[][] xa = new double[xlen][3];
        double[][] xt = new double[xlen][3];
        
        // pack atoms
        int j = 0;
        for (int i = 0; i < groups.size(); i++) {
            
            Group group = groups.get(i);

            if (group.hasAtom("N")) {
                
                Atom n = group.getAtom("N");
                xa[j][0] = n.getX();
                xa[j][1] = n.getY();
                xa[j][2] = n.getZ();
                j++;
            }

            // for known CA
            Atom ca = group.getAtom("CA");
            xa[j][0] = ca.getX();
            xa[j][1] = ca.getY();
            xa[j][2] = ca.getZ();
            j++;

            if (group.hasAtom("C")) {

                Atom c = group.getAtom("C");
                xa[j][0] = c.getX();
                xa[j][1] = c.getY();
                xa[j][2] = c.getZ();
                j++;
            }
            
            if (group.hasAtom("O")) {

                Atom o = group.getAtom("O");
                xa[j][0] = o.getX();
                xa[j][1] = o.getY();
                xa[j][2] = o.getZ();
                j++;
            }
        }

        // transform coords
        Functions.do_rotation(xa, xt, xlen, t, u);

        // assign transformed coords back
        j = 0;
        for (int i = 0; i < groups.size(); i++) {
            
            Group group = groups.get(i);

            if (group.hasAtom("N")) {
                
                Atom n = group.getAtom("N");
                n.setX(xt[j][0]);
                n.setY(xt[j][1]);
                n.setZ(xt[j][2]);
                j++;
            }

            // for known CA
            Atom ca = group.getAtom("CA");
            ca.setX(xt[j][0]);
            ca.setY(xt[j][1]);
            ca.setZ(xt[j][2]);
            j++;

            if (group.hasAtom("C")) {

                Atom c = group.getAtom("C");
                c.setX(xt[j][0]);
                c.setY(xt[j][1]);
                c.setZ(xt[j][2]);
                j++;
            }
            
            if (group.hasAtom("O")) {

                Atom o = group.getAtom("O");
                o.setX(xt[j][0]);
                o.setY(xt[j][1]);
                o.setZ(xt[j][2]);
                j++;
            }
        }
    }

    private static String get3state(Group g) {

        String ss8 = "";
        Object obj = g.getProperty(Group.SEC_STRUC);
        if (obj instanceof SecStrucInfo) {
           SecStrucInfo info = (SecStrucInfo)obj;
           ss8 = String.valueOf(info.getType().type).trim();
        }
        return map8state(ss8);
    }

    private static String map8state(String ss8) {

        String ss3;
        switch(ss8) {
            case "G":
            case "H":
            case "I":
                ss3 = "HELIX";
                break;
            case "E":
                ss3 = "STRAND";
                break;
            default:
                ss3 = "LOOP";
        }
        return ss3;
    }

    private static String spaces(int n) {
        
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < n; i++) {
            sb.append(" "); 
        }
        return sb.toString();
    }
}
