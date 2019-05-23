package edu.umkc.rupee.tm;

import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;
import org.biojava.nbio.structure.AminoAcid;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;

public class TMAlign {

    public static class Results {

        private int chainLength1;
        private int chainLength2;
        private int alignedLength;
        private double tmScoreQ;
        private double tmScoreT;
        private double tmScoreAvg;
        private double rmsd;
        private String output;

        public int getChainLength1() {
            return chainLength1;
        }

        public void setChainLength1(int chainLength1) {
            this.chainLength1 = chainLength1;
        }

        public int getChainLength2() {
            return chainLength2;
        }

        public void setChainLength2(int chainLength2) {
            this.chainLength2 = chainLength2;
        }

        public int getAlignedLength() {
            return alignedLength;
        }

        public void setAlignedLength(int alignedLength) {
            this.alignedLength = alignedLength;
        }

        public double getTmScoreQ() {
            return tmScoreQ;
        }

        public void setTmScoreQ(double tmScoreQ) {
            this.tmScoreQ = tmScoreQ;
        }

        public double getTmScoreT() {
            return tmScoreT;
        }

        public void setTmScoreT(double tmScoreT) {
            this.tmScoreT = tmScoreT;
        }

        public double getTmScoreAvg() {
            return tmScoreAvg;
        }

        public void setTmScoreAvg(double tmScoreAvg) {
            this.tmScoreAvg = tmScoreAvg;
        }

        public double getRmsd() {
            return rmsd;
        }

        public void setRmsd(double rmsd) {
            this.rmsd = rmsd;
        }

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }
    }

    /*
     * Layout
     * 
     * main alignment function
     *
     * parameters
     *  parameter_set4search
     *  parameter_set4final
     *
     * initial alignments
     *  get_initial
     *  get_initial_ss
     *  get_initial_ssplus
     *  get_initial5
     *  get_initial_fgt
     *
     * searching
     *  detailed_search
     *  detailed_search_standard
     *
     * scoring
     *  get_score_fast
     *  TMscore8_search
     *  TMscore8_search_standard
     *  score_fun8
     *  score_fun8_standard
     */

    private Mode mode;                              // regular, fast, super fast

    private double D0_MIN;                          // for d0
    private double Lnorm;                           // normalization length
    private double score_d8, d0, d0_search, dcu0;   // for TMscore search
    private double score[][];                       // for dynamic programming
    private boolean path[][];                       // for dynamic programming
    private double val[][];                         // for dynamic programming
    private int xlen, ylen, minlen;                 // length of proteins
    private double xa[][], ya[][];                  // for input coordinates xa[0...xlen-1][0..2] and ya[0...ylen-1][0..2]
                                                    // ya is regarded as native, superpose xa onto ya
    private double xtm[][], ytm[][];                // for storing alignment without gaps (i.e. densely)
    private double xt[][];                          // for saving the superposition coords of xa or xtm
    private char seqx[], seqy[];                    // for amino acid sequence
    private int secx[], secy[];                     // for secondary structure sequence
    private double r1[][], r2[][];                  // for Kabsch rotation
    private double t[];                             // Kabsch translation vector and rotation matrix
    private double u[][];

    public TMAlign() {

        this.mode = Mode.REGULAR;
    }

    public TMAlign(Mode mode) {
        
        this.mode = mode;
    }

    public Results align(Structure xstruct, Structure ystruct) { 

        // ********************************************************************************** //
        // * load data *
        // ********************************************************************************** //
       
        // chain names
        String xname = xstruct.getName();
        String yname = ystruct.getName();

        // get first chain in each structure
        Chain xchain = xstruct.getChains().get(0);
        Chain ychain = ystruct.getChains().get(0);

        // get groups of atoms per residue
        List<Group> xgroups = xchain.getAtomGroups().stream().filter(g -> g.hasAtom("CA")).collect(Collectors.toList());
        List<Group> ygroups = ychain.getAtomGroups().stream().filter(g -> g.hasAtom("CA")).collect(Collectors.toList());

        // get carbon alpha atoms per residue
        List<Atom> xatoms = xgroups.stream().map(g -> g.getAtom("CA")).collect(Collectors.toList());
        List<Atom> yatoms = ygroups.stream().map(g -> g.getAtom("CA")).collect(Collectors.toList());

        // get number of residues
        xlen = xatoms.size();
        ylen = yatoms.size();
        minlen = Math.min(xlen, ylen);

        // allocate storage
        score = new double[xlen + 1][ylen + 1];
        path = new boolean[xlen + 1][ylen + 1];
        val = new double[xlen + 1][ylen + 1];
        xtm = new double[minlen][3];
        ytm = new double[minlen][3];
        xt = new double[xlen][3];
        seqx = new char[xlen];
        seqy = new char[ylen];
        secx = new int[xlen];
        secy = new int[ylen];
        r1 = new double[minlen][3];
        r2 = new double[minlen][3];
        t = new double[3];
        u = new double[3][3];

        // get x atom coordinates
        xa = new double[xlen][3];
        for (int i = 0; i < xatoms.size(); i++) {

            Group g = xatoms.get(i).getGroup();
            if (g instanceof AminoAcid) {
                AminoAcid aa = (AminoAcid)g;
                seqx[i] = aa.getAminoType();
            }
            else {
                seqx[i] = 'X';
            }

            Atom atom = xatoms.get(i);
            xa[i][0] = atom.getX();
            xa[i][1] = atom.getY();
            xa[i][2] = atom.getZ();
        }

        // get y atom coordinates
        ya = new double[ylen][3];
        for (int i = 0; i < yatoms.size(); i++) {
            
            Group g = yatoms.get(i).getGroup();
            if (g instanceof AminoAcid) {
                AminoAcid aa = (AminoAcid)g;
                seqy[i] = aa.getAminoType();
            }
            else {
                seqy[i] = 'X';
            }

            Atom atom = yatoms.get(i);
            ya[i][0] = atom.getX();
            ya[i][1] = atom.getY();
            ya[i][2] = atom.getZ();
        }

        // ********************************************************************************** //
        // * parameter set *
        // ********************************************************************************** //

        // set: D0_MIN, Lnorm, d0, d0_search, score_d8
        parameter_set4search(xlen, ylen); 
       
        // set scoring method 
        int simplify_step = 40; 
        int score_sum_method = 8; 
       
        // temp storage for initial alignments
        int invmap[] = new int[ylen + 1];

        // store the best initial alignment
        int invmap0[] = new int[ylen + 1];
        
        double TM = 0;
        double TMmax = -1;
        for (int i = 0; i < ylen; i++) {
            invmap0[i] = -1;
        }

        double ddcc = 0.4;
        if (Lnorm <= 40)
            ddcc = 0.1; 
        double local_d0_search = d0_search;

        // ********************************************************************************** //
        // * get initial alignment with gapless threading *
        // ********************************************************************************** //

        get_initial(xa, ya, xlen, ylen, invmap0);
        TM = detailed_search(xa, ya, xlen, ylen, invmap0, t, u, simplify_step, score_sum_method, local_d0_search);

        if (TM > TMmax) {
            TMmax = TM;
        }
        TM = DP_iter(xa, ya, xlen, ylen, t, u, invmap, 0, 2, mode.getDpIterations(), local_d0_search);
        if (TM > TMmax) {
            TMmax = TM;
            for (int i = 0; i < ylen; i++) {
                invmap0[i] = invmap[i];
            }
        }

        // ********************************************************************************** //
        // * get initial alignment based on secondary structure *
        // ********************************************************************************** //
        
        get_initial_ss(xa, ya, xlen, ylen, invmap);
        TM = detailed_search(xa, ya, xlen, ylen, invmap, t, u, simplify_step, score_sum_method, local_d0_search);

        if (TM > TMmax) {
            TMmax = TM;
            for (int i = 0; i < ylen; i++) {
                invmap0[i] = invmap[i];
            }
        }
        if (TM > TMmax * 0.2) {
            TM = DP_iter(xa, ya, xlen, ylen, t, u, invmap, 0, 2, mode.getDpIterations(), local_d0_search);
            if (TM > TMmax) {
                TMmax = TM;
                for (int i = 0; i < ylen; i++) {
                    invmap0[i] = invmap[i];
                }
            }
        }
        
        // ********************************************************************************** //
        // * get initial alignment based on local superposition *
        // ********************************************************************************** //

        if (mode == Mode.REGULAR && get_initial5(xa, ya, xlen, ylen, invmap)) {

            TM = detailed_search(xa, ya, xlen, ylen, invmap, t, u, simplify_step, score_sum_method, local_d0_search);

            if (TM > TMmax) {
                TMmax = TM;
                for (int i = 0; i < ylen; i++) {
                    invmap0[i] = invmap[i];
                }
            }
            if (TM > TMmax * ddcc) {
                TM = DP_iter(xa, ya, xlen, ylen, t, u, invmap, 0, 2, 2, local_d0_search);
                if (TM > TMmax) {
                    TMmax = TM;
                    for (int i = 0; i < ylen; i++) {
                        invmap0[i] = invmap[i];
                    }
                }
            }
        }

        // ********************************************************************************** //
        // * get initial alignment based on previous alignment+secondary structure *
        // ********************************************************************************** //
        
        get_initial_ssplus(xa, ya, xlen, ylen, invmap0, invmap);
        TM = detailed_search(xa, ya, xlen, ylen, invmap, t, u, simplify_step, score_sum_method, local_d0_search);

        if (TM > TMmax) {
            TMmax = TM;
            for (int i = 0; i < ylen; i++) {
                invmap0[i] = invmap[i];
            }
        }
        if (TM > TMmax * ddcc) {
            TM = DP_iter(xa, ya, xlen, ylen, t, u, invmap, 0, 2, mode.getDpIterations(), local_d0_search);
            if (TM > TMmax) {
                TMmax = TM;
                for (int i = 0; i < ylen; i++) {
                    invmap0[i] = invmap[i];
                }
            }
        }

        // ********************************************************************************** //
        // * get initial alignment based on fragment gapless threading *
        // ********************************************************************************** //
        
        get_initial_fgt(xa, ya, xlen, ylen, invmap);
        TM = detailed_search(xa, ya, xlen, ylen, invmap, t, u, simplify_step, score_sum_method, local_d0_search);

        if (TM > TMmax) {
            TMmax = TM;
            for (int i = 0; i < ylen; i++) {
                invmap0[i] = invmap[i];
            }
        }
        if (TM > TMmax * ddcc) {
            TM = DP_iter(xa, ya, xlen, ylen, t, u, invmap, 1, 2, 2, local_d0_search);
            if (TM > TMmax) {
                TMmax = TM;
                for (int i = 0; i < ylen; i++) {
                    invmap0[i] = invmap[i];
                }
            }
        }

        // ********************************************************************************** //
        // * validate the final and best initial alignment *
        // ********************************************************************************** //
       
        // make sure at least one pair is aligned 
        boolean flag = false;
        for (int i = 0; i < ylen; i++) {
            if (invmap0[i] >= 0) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            throw new RuntimeException("no alignment bad result");
        }

        // ********************************************************************************** //
        // * Detailed TMscore search engine --> prepare for final TMscore *
        // ********************************************************************************** //
        
        // set scoring method 
        simplify_step = 1;
        score_sum_method = 8;

        TM = detailed_search_standard(xa, ya, xlen, ylen, invmap0, t, u, simplify_step, score_sum_method, local_d0_search, false);

        // select pairs with dis < d8 for final TMscore computation and output alignment
        int n_ali8, k = 0;
        int m1[], m2[];
        double d;
        m1 = new int[xlen]; // alignd index in x
        m2 = new int[ylen]; // alignd index in y
        Functions.do_rotation(xa, xt, xlen, t, u);
        k = 0;
        for (int j = 0; j < ylen; j++) {
            int i = invmap0[j];
            if (i >= 0)
            {
                // aligned
                d = Math.sqrt(Functions.dist(xt[i], ya[j]));
                if (d <= score_d8) {

                    m1[k] = i;
                    m2[k] = j;

                    // densely packed - not transformed
                    xtm[k][0] = xa[i][0];
                    xtm[k][1] = xa[i][1];
                    xtm[k][2] = xa[i][2];

                    ytm[k][0] = ya[j][0];
                    ytm[k][1] = ya[j][1];
                    ytm[k][2] = ya[j][2];

                    // densley packed - transformed
                    r1[k][0] = xt[i][0];
                    r1[k][1] = xt[i][1];
                    r1[k][2] = xt[i][2];

                    r2[k][0] = ya[j][0];
                    r2[k][1] = ya[j][1];
                    r2[k][2] = ya[j][2];

                    k++;
                }
            }
        }

        // alignment length
        n_ali8 = k;

        // minimize rmsd for the best rotation and translation matrices t and u
        MutableDouble rmsd0 = new MutableDouble(0.0);
        Kabsch.execute(r1, r2, n_ali8, 0, rmsd0, t, u); 
        rmsd0.setValue(Math.sqrt(rmsd0.getValue() / n_ali8));

        // ********************************************************************************* //
        // * Final TMscore *
        // ********************************************************************************* //
        
        MutableDouble rmsd = new MutableDouble(0.0);
        double t0[] = new double[3];
        double u0[][] = new double[3][3];
        double TM1, TM2, TM3; // confusing but TM2 is normalized by first structure and TM1 by second
        double d0_out=5.0;  
        double Lnorm_0 = ylen;

        // set score method 
        simplify_step = 1;
        score_sum_method = 0;
    
        //normalized by length of second structure
        parameter_set4final(Lnorm_0);
        double d0A=d0;
        local_d0_search = d0_search;
        TM1 = TMscore8_search(xtm, ytm, n_ali8, t0, u0, simplify_step, score_sum_method, rmsd, local_d0_search);
        
        // normalized by length of first structure
        parameter_set4final(xlen);
        double d0B=d0;
        local_d0_search = d0_search;
        TM2 = TMscore8_search(xtm, ytm, n_ali8, t, u, simplify_step, score_sum_method, rmsd, local_d0_search);
        
        // normalized by average length of structures
        Lnorm_0=(xlen+ylen)*0.5;
        parameter_set4final(Lnorm_0);
        double d0a=d0;
        local_d0_search = d0_search;
        TM3 = TMscore8_search(xtm, ytm, n_ali8, t, u, simplify_step, score_sum_method, rmsd, local_d0_search);
        
        // ********************************************************************************* //
        // * Output *
        // ********************************************************************************* //

        Results results = new Results();
        results.setChainLength1(xlen);
        results.setChainLength2(ylen);
        results.setAlignedLength(n_ali8);
        results.setTmScoreQ(TM2);
        results.setTmScoreT(TM1);
        results.setTmScoreAvg(TM3);
        results.setRmsd(rmsd0.getValue());

        if (this.mode == Mode.ALIGN_TEXT) {

            k = 0;
            d = 0.0;

            double seq_id;          
            int i, j;
            int ali_len = xlen + ylen;
            char[] seqM = new char[ali_len];
            char[] seqxA = new char[ali_len];
            char[] seqyA = new char[ali_len];
            
            Functions.do_rotation(xa, xt, xlen, t, u);

            seq_id=0;
            int kk=0, i_old=0, j_old=0;
            for(k=0; k<n_ali8; k++)
            {
                for(i=i_old; i<m1[k]; i++)
                {
                    //align x to gap
                    seqxA[kk]=seqx[i];
                    seqyA[kk]='-';
                    seqM[kk]=' ';
                    kk++;
                }

                for(j=j_old; j<m2[k]; j++)
                {
                    //align y to gap
                    seqxA[kk]='-';
                    seqyA[kk]=seqy[j];
                    seqM[kk]=' ';
                    kk++;
                }

                seqxA[kk]=seqx[m1[k]];
                seqyA[kk]=seqy[m2[k]];
                if(seqxA[kk]==seqyA[kk])
                {
                    seq_id++;
                }
                d = Math.sqrt(Functions.dist(xt[m1[k]], ya[m2[k]]));
                if(d < d0_out)
                {
                    seqM[kk]=':';
                }
                else
                {
                    seqM[kk]='.';
                }
                kk++;
                i_old=m1[k]+1;
                j_old=m2[k]+1;
            }

            //tail
            for(i=i_old; i<xlen; i++)
            {
                //align x to gap
                seqxA[kk]=seqx[i];
                seqyA[kk]='-';
                seqM[kk]=' ';                   
                kk++;
            }    
            for(j=j_old; j<ylen; j++)
            {
                //align y to gap
                seqxA[kk]='-';
                seqyA[kk]=seqy[j];
                seqM[kk]=' ';
                kk++;
            }
         
            seq_id = seq_id/( n_ali8+0.00000001); //what did by TMalign, but not reasonable, it should be n_ali8
        
            StringBuilder sb = new StringBuilder();
            Formatter formatter = new Formatter(sb, Locale.US);
            
            formatter.format("\nName of Chain_1: %s\n", xname); 
            formatter.format("Name of Chain_2: %s\n", yname);
            formatter.format("Length of Chain_1: %d residues\n", xlen);
            formatter.format("Length of Chain_2: %d residues\n\n", ylen);

            formatter.format("Aligned length= %d, RMSD= %6.2f, Seq_ID=n_identical/n_aligned= %4.3f\n", n_ali8, rmsd0.getValue(), seq_id); 
            formatter.format("TM-score= %6.5f (if normalized by length of Chain_1)\n", TM2, xlen, d0B);
            formatter.format("TM-score= %6.5f (if normalized by length of Chain_2)\n", TM1, ylen, d0A);
            
            double L_ave = (xlen + ylen) * 0.5;
            formatter.format("TM-score= %6.5f (if normalized by average length of chains)\n", TM3, L_ave, d0a);
            
            //output structure alignment
            formatter.format("\n(\":\" denotes residue pairs of d < %4.1f Angstrom, ", d0_out);
            formatter.format("\".\" denotes other aligned residues)\n");

            for (i = 0; i < ali_len; i = i + 120) {

                int from = i;
                int to = Math.min(ali_len, i + 120);

                formatter.format("%s\n", new String(Arrays.copyOfRange(seqxA, from, to)));
                formatter.format("%s\n", new String(Arrays.copyOfRange(seqM, from, to)));
                formatter.format("%s\n\n", new String(Arrays.copyOfRange(seqyA, from, to)));
            }

            formatter.close();

            results.setOutput(sb.toString());
        }
        else if (mode == Mode.ALIGN_3D) {

            int xlenreal = 0;
            for(Group g : xgroups) {
               
                if (g.hasAtom("N")) xlenreal++;
                xlenreal++; // for known CA
                if (g.hasAtom("C")) xlenreal++;
                if (g.hasAtom("O")) xlenreal++;
            }

            double[][] xa_all = new double[xlenreal][3];
            double[][] xt_all = new double[xlenreal][3];
            
            // iterate x atoms for xa_all
            int j = 0;
            for (int i = 0; i < xgroups.size(); i++) {
                
                Group group = xgroups.get(i);

                if (group.hasAtom("N")) {
                    
                    Atom n = group.getAtom("N");
                    xa_all[j][0] = n.getX();
                    xa_all[j][1] = n.getY();
                    xa_all[j][2] = n.getZ();
                    j++;
                }

                // for known CA
                Atom ca = group.getAtom("CA");
                xa_all[j][0] = ca.getX();
                xa_all[j][1] = ca.getY();
                xa_all[j][2] = ca.getZ();
                j++;

                if (group.hasAtom("C")) {

                    Atom c = group.getAtom("C");
                    xa_all[j][0] = c.getX();
                    xa_all[j][1] = c.getY();
                    xa_all[j][2] = c.getZ();
                    j++;
                }
                
                if (group.hasAtom("O")) {

                    Atom o = group.getAtom("O");
                    xa_all[j][0] = o.getX();
                    xa_all[j][1] = o.getY();
                    xa_all[j][2] = o.getZ();
                    j++;
                }
            }

            // transform xa_all into xt_all
            Functions.do_rotation(xa_all, xt_all, xlenreal, t, u);

            // set x atom coords
            j = 0;
            for (int i = 0; i < xgroups.size(); i++) {
                
                Group group = xgroups.get(i);

                if (group.hasAtom("N")) {
                    
                    Atom n = group.getAtom("N");
                    n.setX(xt_all[j][0]);
                    n.setY(xt_all[j][1]);
                    n.setZ(xt_all[j][2]);
                    j++;
                }

                // for known CA
                Atom ca = group.getAtom("CA");
                ca.setX(xt_all[j][0]);
                ca.setY(xt_all[j][1]);
                ca.setZ(xt_all[j][2]);
                j++;

                if (group.hasAtom("C")) {

                    Atom c = group.getAtom("C");
                    c.setX(xt_all[j][0]);
                    c.setY(xt_all[j][1]);
                    c.setZ(xt_all[j][2]);
                    j++;
                }
                
                if (group.hasAtom("O")) {

                    Atom o = group.getAtom("O");
                    o.setX(xt_all[j][0]);
                    o.setY(xt_all[j][1]);
                    o.setZ(xt_all[j][2]);
                    j++;
                }
            }

            StringBuilder sb = new StringBuilder();

            // iterate x atoms for chain A
            j = 0;
            for (int i = 0; i < xgroups.size(); i++) {
                
                Group group = xgroups.get(i);
                group.getChain().setName("A");
                
                if (group.hasAtom("N")) {
                    
                    Atom n = group.getAtom("N");
                    sb.append(n.toPDB());
                    j++;
                }

                // for known CA
                Atom ca = group.getAtom("CA");
                sb.append(ca.toPDB());
                j++;

                if (group.hasAtom("C")) {

                    Atom c = group.getAtom("C");
                    sb.append(c.toPDB());
                    j++;
                }
                
                if (group.hasAtom("O")) {

                    Atom o = group.getAtom("O");
                    sb.append(o.toPDB());
                    j++;
                }
            }

            // iterate y atoms for chain B
            j = 0;
            for (int i = 0; i < ygroups.size(); i++) {

                Group group = ygroups.get(i);
                group.getChain().setName("B");
                
                if (group.hasAtom("N")) {
                    
                    Atom n = group.getAtom("N");
                    sb.append(n.toPDB());
                    j++;
                }

                // for known CA
                Atom ca = group.getAtom("CA");
                sb.append(ca.toPDB());
                j++;

                if (group.hasAtom("C")) {

                    Atom c = group.getAtom("C");
                    sb.append(c.toPDB());
                    j++;
                }
                
                if (group.hasAtom("O")) {

                    Atom o = group.getAtom("O");
                    sb.append(o.toPDB());
                    j++;
                }
            }
            
            results.setOutput(sb.toString());
        }
        
        return results;
    }

    // **********************************************************************************
    // parameters
    // **********************************************************************************

    public void parameter_set4search(int xlen, int ylen) {
        // parameter initilization for searching: D0_MIN, Lnorm, d0, d0_search,
        // score_d8
        D0_MIN = 0.5;
        dcu0 = 4.25; // update 3.85-->4.25

        Lnorm = Math.min(xlen, ylen); // normaliz TMscore by this in
                                                // searching
        if (Lnorm <= 19) // update 15-->19
        {
            d0 = 0.168; // update 0.5-->0.168
        } else {
            d0 = (1.24 * Math.pow((Lnorm * 1.0 - 15), 1.0 / 3.0) - 1.8);
        }
        D0_MIN = d0 + 0.8; // this should be moved to above
        d0 = D0_MIN; // update: best for search

        d0_search = d0;
        if (d0_search > 8)
            d0_search = 8;
        if (d0_search < 4.5)
            d0_search = 4.5;

        score_d8 = 1.5 * Math.pow(Lnorm * 1.0, 0.3) + 3.5;
    }

    public void parameter_set4final(double len) {
        D0_MIN = 0.5;

        Lnorm = len; // normaliz TMscore by this in searching
        if (Lnorm <= 21) {
            d0 = 0.5;
        } else {
            d0 = (1.24 * Math.pow((Lnorm * 1.0 - 15), 1.0 / 3) - 1.8);
        }
        if (d0 < D0_MIN)
            d0 = D0_MIN;

        d0_search = d0;
        if (d0_search > 8)
            d0_search = 8;
        if (d0_search < 4.5)
            d0_search = 4.5;
    }

    // **********************************************************************************
    // initial alignments
    // **********************************************************************************
    
    // gapless threading to find initial alignment
    public double get_initial(double x[][], double y[][], int x_len, int y_len, int y2x[]) {

        // Output:
        // y2x: alignment of y to x (-1 indicates unaligned)

        int min_len = Math.min(x_len, y_len);
        if (min_len <= 5) {
            throw new RuntimeException("Sequence is too short <=5!");
        }

        // minimum size of fragment
        int min_ali = min_len / 2; 
        if (min_ali <= 5)
            min_ali = 5;

        int n1, n2;
        n1 = -y_len + min_ali;
        n2 = x_len - min_ali;

        int i, j, k, k_best;
        double tmscore, tmscore_max = -1;

        // slide seq y over seq x with minimum overlap of min_ali
        k_best = n1;
        for (k = n1; k <= n2; k++) {

            // get the map for current positions
            for (j = 0; j < y_len; j++) {
                i = j + k;
                if (i >= 0 && i < x_len) {
                    y2x[j] = i;
                } else {
                    y2x[j] = -1;
                }
            }

            // evaluate the initial alignments
            tmscore = get_score_fast(x, y, x_len, y_len, y2x);
            if (tmscore >= tmscore_max) {
                tmscore_max = tmscore;
                k_best = k;
            }
        }

        // extract the best map
        k = k_best;
        for (j = 0; j < y_len; j++) {
            i = j + k;
            if (i >= 0 && i < x_len) {
                y2x[j] = i;
            } else {
                y2x[j] = -1;
            }
        }

        return tmscore_max;
    }

    // secondary structure alignment to find initial alignment
    public void get_initial_ss(double x[][], double y[][], int x_len, int y_len, int y2x[]) {

        // Output:
        // y2x: alignment of y to x (-1 indicates unaligned)
        
        // assign secondary structures
        make_sec(x, x_len, secx);
        make_sec(y, y_len, secy);

        double gap_open = -1.0;
        NW.NWDP_TM(path, val, secx, secy, x_len, y_len, gap_open, y2x);
    }
    
    // 1->coil, 2->helix, 3->turn, 4->strand
    public void make_sec(double x[][], int len, int sec[]) {
        
        int j1, j2, j3, j4, j5;
        double d13, d14, d15, d24, d25, d35;
        for (int i = 0; i < len; i++) {
            sec[i] = 1;
            j1 = i - 2;
            j2 = i - 1;
            j3 = i;
            j4 = i + 1;
            j5 = i + 2;

            if (j1 >= 0 && j5 < len) {
                d13 = Math.sqrt(Functions.dist(x[j1], x[j3]));
                d14 = Math.sqrt(Functions.dist(x[j1], x[j4]));
                d15 = Math.sqrt(Functions.dist(x[j1], x[j5]));
                d24 = Math.sqrt(Functions.dist(x[j2], x[j4]));
                d25 = Math.sqrt(Functions.dist(x[j2], x[j5]));
                d35 = Math.sqrt(Functions.dist(x[j3], x[j5]));
                sec[i] = sec_str(d13, d14, d15, d24, d25, d35);
            }
        }
    }

    public int sec_str(double dis13, double dis14, double dis15, double dis24, double dis25, double dis35) {
        
        int s = 1;
        double delta = 2.1;
        if (Math.abs(dis15 - 6.37) < delta) {
            if (Math.abs(dis14 - 5.18) < delta) {
                if (Math.abs(dis25 - 5.18) < delta) {
                    if (Math.abs(dis13 - 5.45) < delta) {
                        if (Math.abs(dis24 - 5.45) < delta) {
                            if (Math.abs(dis35 - 5.45) < delta) {
                                s = 2; // helix
                                return s;
                            }
                        }
                    }
                }
            }
        }

        delta = 1.42;
        if (Math.abs(dis15 - 13) < delta) {
            if (Math.abs(dis14 - 10.4) < delta) {
                if (Math.abs(dis25 - 10.4) < delta) {
                    if (Math.abs(dis13 - 6.1) < delta) {
                        if (Math.abs(dis24 - 6.1) < delta) {
                            if (Math.abs(dis35 - 6.1) < delta) {
                                s = 4; // strand
                                return s;
                            }
                        }
                    }
                }
            }
        }

        if (dis15 < 8) {
            s = 3; // turn
        }

        return s;
    }
    
    // get initial alignment from secondary structure and previous alignments
    public void get_initial_ssplus(double x[][], double y[][], int x_len, int y_len, int y2x0[], int y2x[]) {
        
        // Output:
        // y2x: alignment of y to x (-1 indicates unaligned)

        // create score matrix for DP
        score_matrix_rmsd_sec(x, y, x_len, y_len, y2x0);

        double gap_open = -1.0;
        NW.NWDP_TM(score, path, val, x_len, y_len, gap_open, y2x);
    }

    public void score_matrix_rmsd_sec(double x[][], double y[][], int x_len, int y_len, int y2x[]) {

        double t[] = new double[3];
        double u[][] = new double[3][3];
        MutableDouble rmsd = new MutableDouble(0.0);
        double dij;
        double d01 = d0 + 1.5;
        if (d01 < D0_MIN)
            d01 = D0_MIN;
        double d02 = d01 * d01;

        double xx[] = new double[3];
        int i, k = 0;
        for (int j = 0; j < y_len; j++) {
            i = y2x[j];
            if (i >= 0) {
                r1[k][0] = x[i][0];
                r1[k][1] = x[i][1];
                r1[k][2] = x[i][2];

                r2[k][0] = y[j][0];
                r2[k][1] = y[j][1];
                r2[k][2] = y[j][2];

                k++;
            }
        }
        Kabsch.execute(r1, r2, k, 1, rmsd, t, u);

        for (int ii = 0; ii < x_len; ii++) {
            Functions.transform(t, u, x[ii], xx);
            for (int jj = 0; jj < y_len; jj++) {
                dij = Functions.dist(xx, y[jj]);
                if (secx[ii] == secy[jj]) {
                    score[ii + 1][jj + 1] = 1.0 / (1 + dij / d02) + 0.5;
                } else {
                    score[ii + 1][jj + 1] = 1.0 / (1 + dij / d02);
                }
            }
        }
    }
    
    // get initial alignment of local structure superposition
    public boolean get_initial5(double x[][], double y[][], int x_len, int y_len, int y2x[]) {
        
        // Output:
        // y2x: alignment of y to x (-1 indicates unaligned)
        
        double GL;
        MutableDouble rmsd = new MutableDouble(0.0);
        double t[] = new double[3];
        double u[][] = new double[3][3];

        double d01 = d0 + 1.5;
        if (d01 < D0_MIN)
            d01 = D0_MIN;
        double d02 = d01 * d01;

        double GLmax = 0;
        int aL = Math.min(x_len, y_len);
        int invmap[] = new int[y_len + 1];

        // jump on sequence1-------------->
        int n_jump1 = 0;
        if (x_len > 250)
            n_jump1 = 45;
        else if (x_len > 200)
            n_jump1 = 35;
        else if (x_len > 150)
            n_jump1 = 25;
        else
            n_jump1 = 15;
        if (n_jump1 > (x_len / 3))
            n_jump1 = x_len / 3;

        // jump on sequence2-------------->
        int n_jump2 = 0;
        if (y_len > 250)
            n_jump2 = 45;
        else if (y_len > 200)
            n_jump2 = 35;
        else if (y_len > 150)
            n_jump2 = 25;
        else
            n_jump2 = 15;
        if (n_jump2 > (y_len / 3))
            n_jump2 = y_len / 3;

        // fragment to superimpose-------------->
        int n_frag[] = { 20, 100 };
        if (n_frag[0] > (aL / 3))
            n_frag[0] = aL / 3;
        if (n_frag[1] > (aL / 2))
            n_frag[1] = aL / 2;

        // start superimpose search-------------->
        boolean flag = false;
        for (int i_frag = 0; i_frag < 2; i_frag++) {
            int m1 = x_len - n_frag[i_frag] + 1;
            int m2 = y_len - n_frag[i_frag] + 1;

            // for (int i = 1; i<m1; i = i + n_jump1) //index starts from 0,
            // different from FORTRAN
            // for debug
            for (int i = 0; i < m1; i = i + n_jump1) // index starts from 0,
                                                        // different from
                                                        // FORTRAN
            {
                // for (int j = 1; j<m2; j = j + n_jump2)
                for (int j = 0; j < m2; j = j + n_jump2) {
                    for (int k = 0; k < n_frag[i_frag]; k++) // fragment in y
                    {
                        r1[k][0] = x[k + i][0];
                        r1[k][1] = x[k + i][1];
                        r1[k][2] = x[k + i][2];

                        r2[k][0] = y[k + j][0];
                        r2[k][1] = y[k + j][1];
                        r2[k][2] = y[k + j][2];
                    }

                    // superpose the two structures and rotate it
                    Kabsch.execute(r1, r2, n_frag[i_frag], 1, rmsd, t, u);

                    double gap_open = 0.0;
                    NW.NWDP_TM(path, val, x, y, x_len, y_len, t, u, d02, gap_open, invmap);
                    GL = get_score_fast(x, y, x_len, y_len, invmap);
                    if (GL > GLmax) {
                        GLmax = GL;
                        for (int ii = 0; ii < y_len; ii++) {
                            y2x[ii] = invmap[ii];
                        }
                        flag = true;
                    }
                }
            }
        }

        return flag;
    }
    
    // perform fragment gapless threading to find the best initial alignment
    public double get_initial_fgt(double x[][], double y[][], int x_len, int y_len, int y2x[]) {

        // Output:
        // y2x: alignment of y to x (-1 indicates unaligned)
        
        int fra_min = 4; // minimum fragment for search
        int fra_min1 = fra_min - 1; // cutoff for shift, save time

        MutableInt xstart = new MutableInt(0);
        MutableInt ystart = new MutableInt(0);
        MutableInt xend = new MutableInt(0);
        MutableInt yend = new MutableInt(0);

        find_max_frag(x, x_len, xstart, xend);
        find_max_frag(y, y_len, ystart, yend);

        int Lx = xend.getValue() - xstart.getValue() + 1;
        int Ly = yend.getValue() - ystart.getValue() + 1;
        int ifr[], y2x_[];
        int L_fr = Math.min(Lx, Ly);
        ifr = new int[L_fr];
        y2x_ = new int[y_len + 1];

        // select what piece will be used (this may araise ansysmetry, but
        // only when L1=L2 and Lfr1=Lfr2 and L1 ne Lfr1
        // if L1=Lfr1 and L2=Lfr2 (normal proteins), it will be the same as
        // initial1

        if (Lx < Ly || (Lx == Ly && x_len <= y_len)) {
            for (int i = 0; i < L_fr; i++) {
                ifr[i] = xstart.getValue() + i;
            }
        } else if (Lx > Ly || (Lx == Ly && x_len > y_len)) {
            for (int i = 0; i < L_fr; i++) {
                ifr[i] = ystart.getValue() + i;
            }
        }

        int L0 = Math.min(x_len, y_len); // non-redundant to get_initial1
        if (L_fr == L0) {
            int n1 = (int) (L0 * 0.1); // my index starts from 0
            int n2 = (int) (L0 * 0.89);

            int j = 0;
            for (int i = n1; i <= n2; i++) {
                ifr[j] = ifr[i];
                j++;
            }
            L_fr = j;
        }

        // gapless threading for the extracted fragment
        double tmscore, tmscore_max = -1;

        if (Lx < Ly || (Lx == Ly && x_len <= y_len)) {
            int L1 = L_fr;
            int min_len = Math.min(L1, y_len);
            int min_ali = (int) (min_len / 2.5); // minimum size of considered
                                                    // fragment
            if (min_ali <= fra_min1)
                min_ali = fra_min1;
            int n1, n2;
            n1 = -y_len + min_ali;
            n2 = L1 - min_ali;

            int i, j, k;
            for (k = n1; k <= n2; k++) {
                // get the map
                for (j = 0; j < y_len; j++) {
                    i = j + k;
                    if (i >= 0 && i < L1) {
                        y2x_[j] = ifr[i];
                    } else {
                        y2x_[j] = -1;
                    }
                }

                // evaluate the map quickly in three iterations
                tmscore = get_score_fast(x, y, x_len, y_len, y2x_);

                if (tmscore >= tmscore_max) {
                    tmscore_max = tmscore;
                    for (j = 0; j < y_len; j++) {
                        y2x[j] = y2x_[j];
                    }
                }
            }
        } else {
            int L2 = L_fr;
            int min_len = Math.min(x_len, L2);
            int min_ali = (int) (min_len / 2.5); // minimum size of considered
                                                    // fragment
            if (min_ali <= fra_min1)
                min_ali = fra_min1;
            int n1, n2;
            n1 = -L2 + min_ali;
            n2 = x_len - min_ali;

            int i, j, k;

            for (k = n1; k <= n2; k++) {
                // get the map
                for (j = 0; j < y_len; j++) {
                    y2x_[j] = -1;
                }

                for (j = 0; j < L2; j++) {
                    i = j + k;
                    if (i >= 0 && i < x_len) {
                        y2x_[ifr[j]] = i;
                    }
                }

                // evaluate the map quickly in three iterations
                tmscore = get_score_fast(x, y, x_len, y_len, y2x_);
                if (tmscore >= tmscore_max) {
                    tmscore_max = tmscore;
                    for (j = 0; j < y_len; j++) {
                        y2x[j] = y2x_[j];
                    }
                }
            }
        }

        return tmscore_max;
    }
    
    public void find_max_frag(double x[][], int len, MutableInt start_max, MutableInt end_max) {
        int r_min, fra_min = 4; // minimum fragment for search
        double d;
        int start;
        int Lfr_max = 0, flag;

        r_min = (int) (len * 1.0 / 3.0); // minimum fragment, in case too small
                                            // protein
        if (r_min > fra_min)
            r_min = fra_min;

        int inc = 0;
        double dcu0_cut = dcu0 * dcu0;
        ;
        double dcu_cut = dcu0_cut;

        while (Lfr_max < r_min) {
            Lfr_max = 0;
            int j = 1; // number of residues at nf-fragment
            start = 0;
            for (int i = 1; i < len; i++) {
                d = Functions.dist(x[i - 1], x[i]);
                flag = 0;
                if (dcu_cut > dcu0_cut) {
                    if (d < dcu_cut) {
                        flag = 1;
                    }
                } else // if (resno[i] == (resno[i - 1] + 1)) // necessary??
                {
                    if (d < dcu_cut) {
                        flag = 1;
                    }
                }

                if (flag == 1) {
                    j++;

                    if (i == (len - 1)) {
                        if (j > Lfr_max) {
                            Lfr_max = j;
                            start_max.setValue(start);
                            end_max.setValue(i);
                        }
                        j = 1;
                    }
                } else {
                    if (j > Lfr_max) {
                        Lfr_max = j;
                        start_max.setValue(start);
                        end_max.setValue(i - 1);
                    }

                    j = 1;
                    start = i;
                }
            } // for i;

            if (Lfr_max < r_min) {
                inc++;
                double dinc = Math.pow(1.1, (double) inc) * dcu0;
                dcu_cut = dinc * dinc;
            }
        } // while <;
    }
    
    // **********************************************************************************
    // searching
    // **********************************************************************************

    public double DP_iter(
            double x[][], double y[][], int x_len, int y_len, 
            double t[], double u[][], int 
            invmap0[],
            int g1, int g2, 
            int iteration_max, double local_d0_search) {

        // Output
        // best alignment stored in invmap0

        double gap_open[] = { -0.6, 0 };
        MutableDouble rmsd = new MutableDouble(0.0);
        int invmap[] = new int[y_len + 1];

        int iteration, i, j, k;
        double tmscore, tmscore_max, tmscore_old = 0;
        int score_sum_method = 8;
        int simplify_step = 40;
        tmscore_max = -1;

        double d02 = d0 * d0;

        // try different gap open penalties
        for (int g = g1; g < g2; g++) {

            // iterate on DP algorithms
            for (iteration = 0; iteration < iteration_max; iteration++) {

                NW.NWDP_TM(path, val, x, y, x_len, y_len, t, u, d02, gap_open[g], invmap);

                k = 0;
                for (j = 0; j < y_len; j++) {
                    i = invmap[j];

                    if (i >= 0) {

                        // aligned
                        xtm[k][0] = x[i][0];
                        xtm[k][1] = x[i][1];
                        xtm[k][2] = x[i][2];

                        ytm[k][0] = y[j][0];
                        ytm[k][1] = y[j][1];
                        ytm[k][2] = y[j][2];

                        k++;
                    }
                }

                // k is the length of the alignment stored densely in xtm and ytm
                
                tmscore = TMscore8_search(xtm, ytm, k, t, u, simplify_step, score_sum_method, rmsd, local_d0_search);

                // update the best
                if (tmscore > tmscore_max) {
                    tmscore_max = tmscore;
                    for (i = 0; i < y_len; i++) {
                        invmap0[i] = invmap[i];
                    }
                }

                // test for convergence to break early
                if (iteration > 0) {
                    if (Math.abs(tmscore_old - tmscore) < 0.000001) {
                        break;
                    }
                }
                tmscore_old = tmscore;

            } // for iteration

        } // for gap open

        return tmscore_max;
    }

    // simplify_step: 1 or 40
    // score_sum_method:
    //      0 for score over all pairs
    //      8 for score over pairs with dist < score_d8
    
    public double detailed_search(
            double x[][], double y[][], int x_len, int y_len, 
            int invmap0[], 
            double t[], double u[][], 
            int simplify_step, int score_sum_method, 
            double local_d0_search) {

        // x is model, y is template, try to superpose onto y
        int i, j, k;
        MutableDouble rmsd = new MutableDouble(0.0);

        k = 0;
        for (i = 0; i < y_len; i++) {
            j = invmap0[i];
            if (j >= 0) {

                // aligned
                xtm[k][0] = x[j][0];
                xtm[k][1] = x[j][1];
                xtm[k][2] = x[j][2];

                ytm[k][0] = y[i][0];
                ytm[k][1] = y[i][1];
                ytm[k][2] = y[i][2];

                k++;
            }
        }

        // k is the length of the alignment stored densely in xtm, ytm

        // detailed search 40-->1
        return TMscore8_search(xtm, ytm, k, t, u, simplify_step, score_sum_method, rmsd, local_d0_search);
    }

    public double detailed_search_standard(
            double x[][], double y[][], int x_len, int y_len, 
            int invmap0[], 
            double t[], double u[][], 
            int simplify_step, int score_sum_method, 
            double local_d0_search, boolean bNormalize) {

        // x is model, y is template, try to superpose onto y
        int i, j, k;
        MutableDouble rmsd = new MutableDouble(0.0);

        k = 0;
        for (i = 0; i < y_len; i++) {
            j = invmap0[i];
            if (j >= 0) {

                // aligned
                xtm[k][0] = x[j][0];
                xtm[k][1] = x[j][1];
                xtm[k][2] = x[j][2];

                ytm[k][0] = y[i][0];
                ytm[k][1] = y[i][1];
                ytm[k][2] = y[i][2];

                k++;
            }
        }
        
        // k is the length of the alignment stored densely in xtm, ytm

        // detailed search 40-->1
        double tmscore = TMscore8_search_standard(xtm, ytm, k, t, u, simplify_step, score_sum_method, rmsd, local_d0_search);

        // to use standard_TMscore set bNormalize = true
        if (bNormalize)
            tmscore = tmscore * k / Lnorm;

        return tmscore;
    }

    // **********************************************************************************
    // scoring
    // **********************************************************************************
    
    // compute the score quickly in three iterations
    public double get_score_fast(double x[][], double y[][], int x_len, int y_len, int invmap[]) {
        
        MutableDouble rms = new MutableDouble(0.0);
        double tmscore, tmscore1, tmscore2;
        int i, j, k;

        k = 0;
        for (j = 0; j < y_len; j++) {
            i = invmap[j];
            if (i >= 0) {
                r1[k][0] = x[i][0];
                r1[k][1] = x[i][1];
                r1[k][2] = x[i][2];

                r2[k][0] = y[j][0];
                r2[k][1] = y[j][1];
                r2[k][2] = y[j][2];

                xtm[k][0] = x[i][0];
                xtm[k][1] = x[i][1];
                xtm[k][2] = x[i][2];

                ytm[k][0] = y[j][0];
                ytm[k][1] = y[j][1];
                ytm[k][2] = y[j][2];

                k++;
            } else if (i != -1) {
                throw new RuntimeException("Wrong map!");
            }
        }
        Kabsch.execute(r1, r2, k, 1, rms, t, u);

        // evaluate score
        double di;
        int len = k;
        double dis[] = new double[len];
        double d00 = d0_search;
        double d002 = d00 * d00;
        double d02 = d0 * d0;

        int n_ali = k;
        double xrot[] = new double[3];
        tmscore = 0;
        for (k = 0; k < n_ali; k++) {
            Functions.transform(t, u, xtm[k], xrot);
            di = Functions.dist(xrot, ytm[k]);
            dis[k] = di;
            tmscore += 1 / (1 + di / d02);
        }

        // second iteration
        double d002t = d002;
        while (true) {
            j = 0;
            for (k = 0; k < n_ali; k++) {
                if (dis[k] <= d002t) {
                    r1[j][0] = xtm[k][0];
                    r1[j][1] = xtm[k][1];
                    r1[j][2] = xtm[k][2];

                    r2[j][0] = ytm[k][0];
                    r2[j][1] = ytm[k][1];
                    r2[j][2] = ytm[k][2];

                    j++;
                }
            }
            // there are not enough feasible pairs, relieve the threshold
            if (j < 3 && n_ali > 3) {
                d002t += 0.5;
            } else {
                break;
            }
        }

        if (n_ali != j) {
            Kabsch.execute(r1, r2, j, 1, rms, t, u);
            tmscore1 = 0;
            for (k = 0; k < n_ali; k++) {
                Functions.transform(t, u, xtm[k], xrot);
                di = Functions.dist(xrot, ytm[k]);
                dis[k] = di;
                tmscore1 += 1 / (1 + di / d02);
            }

            // third iteration
            d002t = d002 + 1;

            while (true) {
                j = 0;
                for (k = 0; k < n_ali; k++) {
                    if (dis[k] <= d002t) {
                        r1[j][0] = xtm[k][0];
                        r1[j][1] = xtm[k][1];
                        r1[j][2] = xtm[k][2];

                        r2[j][0] = ytm[k][0];
                        r2[j][1] = ytm[k][1];
                        r2[j][2] = ytm[k][2];

                        j++;
                    }
                }
                // there are not enough feasible pairs, relieve the threshold
                if (j < 3 && n_ali > 3) {
                    d002t += 0.5;
                } else {
                    break;
                }
            }

            // evaluate the score
            Kabsch.execute(r1, r2, j, 1, rms, t, u);
            tmscore2 = 0;
            for (k = 0; k < n_ali; k++) {
                Functions.transform(t, u, xtm[k], xrot);
                di = Functions.dist(xrot, ytm[k]);
                tmscore2 += 1 / (1 + di / d02);
            }
        } else {
            tmscore1 = tmscore;
            tmscore2 = tmscore;
        }

        if (tmscore1 >= tmscore)
            tmscore = tmscore1;
        if (tmscore2 >= tmscore)
            tmscore = tmscore2;

        return tmscore; // no need to normalize this score because it will not
                        // be used for latter scoring
    }
    
    public double TMscore8_search(
            double xtm[][], double ytm[][], 
            int Lali, 
            double t0[], double u0[][],
            int simplify_step, int score_sum_method, 
            MutableDouble Rcomm, double local_d0_search) {

        int i, m;
        double score_max;
        MutableDouble score = new MutableDouble(0.0);
        MutableDouble rmsd = new MutableDouble(0.0);
        int kmax = Lali;
        int k_ali[] = new int[kmax];
        int ka, k;
        double t[] = new double[3];
        double u[][] = new double[3][3];
        double d;

        // iterative parameters
        int n_it = mode.getScoreIterations(); // maximum number of iterations
        int n_init_max = 6; // maximum number of different fragment length
        int L_ini[] = new int[n_init_max]; // fragment lengths, Lali, Lali/2,
                                            // Lali/4 ... 4
        int L_ini_min = 4;
        if (Lali < 4)
            L_ini_min = Lali;
        int n_init = 0, i_init;
        for (i = 0; i < n_init_max - 1; i++) {
            n_init++;
            L_ini[i] = (int) (Lali / Math.pow(2.0, (double) i));
            if (L_ini[i] <= L_ini_min) {
                L_ini[i] = L_ini_min;
                break;
            }
        }
        if (i == n_init_max - 1) {
            n_init++;
            L_ini[i] = L_ini_min;
        }

        score_max = -1;
        // find the maximum score starting from local structures superposition
        int i_ali[] = new int[kmax];
        int n_cut;
        int L_frag; // fragment length
        int iL_max; // maximum starting postion for the fragment

        for (i_init = 0; i_init < n_init; i_init++) {
            L_frag = L_ini[i_init];
            iL_max = Lali - L_frag;

            i = 0;
            while (true) {
                // extract the fragment starting from position i
                ka = 0;
                for (k = 0; k < L_frag; k++) {
                    int kk = k + i;
                    r1[k][0] = xtm[kk][0];
                    r1[k][1] = xtm[kk][1];
                    r1[k][2] = xtm[kk][2];

                    r2[k][0] = ytm[kk][0];
                    r2[k][1] = ytm[kk][1];
                    r2[k][2] = ytm[kk][2];

                    k_ali[ka] = kk;
                    ka++;
                }

                // extract rotation matrix based on the fragment
                Kabsch.execute(r1, r2, L_frag, 1, rmsd, t, u);
                if (simplify_step != 1)
                    Rcomm.setValue(0.0);
                Functions.do_rotation(xtm, xt, Lali, t, u);

                // get subsegment of this fragment
                d = local_d0_search - 1;
                n_cut = score_fun8(xt, ytm, Lali, d, i_ali, score, score_sum_method);
                if (score.getValue() > score_max) {
                    score_max = score.getValue();

                    // save the rotation matrix
                    for (k = 0; k < 3; k++) {
                        t0[k] = t[k];
                        u0[k][0] = u[k][0];
                        u0[k][1] = u[k][1];
                        u0[k][2] = u[k][2];
                    }
                }

                // try to extend the alignment iteratively
                d = local_d0_search + 1;
                for (int it = 0; it < n_it; it++) {
                    ka = 0;
                    for (k = 0; k < n_cut; k++) {
                        m = i_ali[k];
                        r1[k][0] = xtm[m][0];
                        r1[k][1] = xtm[m][1];
                        r1[k][2] = xtm[m][2];

                        r2[k][0] = ytm[m][0];
                        r2[k][1] = ytm[m][1];
                        r2[k][2] = ytm[m][2];

                        k_ali[ka] = m;
                        ka++;
                    }
                    // extract rotation matrix based on the fragment
                    Kabsch.execute(r1, r2, n_cut, 1, rmsd, t, u);
                    Functions.do_rotation(xtm, xt, Lali, t, u);
                    n_cut = score_fun8(xt, ytm, Lali, d, i_ali, score, score_sum_method);
                    if (score.getValue() > score_max) {
                        score_max = score.getValue();

                        // save the rotation matrix
                        for (k = 0; k < 3; k++) {
                            t0[k] = t[k];
                            u0[k][0] = u[k][0];
                            u0[k][1] = u[k][1];
                            u0[k][2] = u[k][2];
                        }
                    }

                    // check if it converges
                    if (n_cut == ka) {
                        for (k = 0; k < n_cut; k++) {
                            if (i_ali[k] != k_ali[k]) {
                                break;
                            }
                        }
                        if (k == n_cut) {
                            break; // stop iteration
                        }
                    }
                } // for iteration

                if (i < iL_max) {
                    i = i + simplify_step; // shift the fragment
                    if (i > iL_max)
                        i = iL_max; // do this to use the last missed fragment
                } else if (i >= iL_max) {
                    break;
                }
            } // while(1)
                // end of one fragment
        } // for(i_init
        return score_max;
    }

    public double TMscore8_search_standard(
            double xtm[][], double ytm[][], 
            int Lali, 
            double t0[], double u0[][],
            int simplify_step, int score_sum_method, 
            MutableDouble Rcomm, double local_d0_search) {

        MutableDouble score = new MutableDouble(0.0);
        MutableDouble rmsd = new MutableDouble(0.0);
        double t[] = new double[3];
        double u[][] = new double[3][3];
        int i, m;
        double d;

        // max number of iterations
        int n_it = mode.getScoreIterations(); 
        
        // fragment lengths
        int n_init_max = 6;                 // maximum number of fragments lengths
        int L_ini[] = new int[n_init_max];  // fragment lengths Lali, Lali/2, ...
        int L_ini_min = 4;
        if (Lali < 4)
            L_ini_min = Lali;
        int n_init = 0;
        for (i = 0; i < n_init_max - 1; i++) {
            n_init++;
            L_ini[i] = (int) (Lali / Math.pow(2.0, (double) i));
            if (L_ini[i] <= L_ini_min) {
                L_ini[i] = L_ini_min;
                break;
            }
        }
        if (i == n_init_max - 1) {
            n_init++;
            L_ini[i] = L_ini_min;
        }

        // find the maximum score starting from local structures superposition
        int kmax = Lali;
        int k_ali[] = new int[kmax];
        int ka, k;
        double score_max = -1;
        int i_ali[] = new int[kmax];
        int n_cut;
        int L_frag; // fragment length
        int iL_max; // maximum starting postion for the fragment

        int i_init;
        for (i_init = 0; i_init < n_init; i_init++) {
            L_frag = L_ini[i_init];
            iL_max = Lali - L_frag;

            i = 0;
            while (true) {
                // extract the fragment starting from position i
                ka = 0;
                for (k = 0; k < L_frag; k++) {
                    int kk = k + i;
                    r1[k][0] = xtm[kk][0];
                    r1[k][1] = xtm[kk][1];
                    r1[k][2] = xtm[kk][2];

                    r2[k][0] = ytm[kk][0];
                    r2[k][1] = ytm[kk][1];
                    r2[k][2] = ytm[kk][2];

                    k_ali[ka] = kk;
                    ka++;
                }
                // extract rotation matrix based on the fragment
                Kabsch.execute(r1, r2, L_frag, 1, rmsd, t, u);
                if (simplify_step != 1)
                    Rcomm.setValue(0.0);
                Functions.do_rotation(xtm, xt, Lali, t, u);

                // get subsegment of this fragment
                d = local_d0_search - 1;
                n_cut = score_fun8_standard(xt, ytm, Lali, d, i_ali, score, score_sum_method);

                if (score.getValue() > score_max) {
                    score_max = score.getValue();

                    // save the rotation matrix
                    for (k = 0; k < 3; k++) {
                        t0[k] = t[k];
                        u0[k][0] = u[k][0];
                        u0[k][1] = u[k][1];
                        u0[k][2] = u[k][2];
                    }
                }

                // try to extend the alignment iteratively
                d = local_d0_search + 1;
                for (int it = 0; it < n_it; it++) {
                    ka = 0;
                    for (k = 0; k < n_cut; k++) {
                        m = i_ali[k];
                        r1[k][0] = xtm[m][0];
                        r1[k][1] = xtm[m][1];
                        r1[k][2] = xtm[m][2];

                        r2[k][0] = ytm[m][0];
                        r2[k][1] = ytm[m][1];
                        r2[k][2] = ytm[m][2];

                        k_ali[ka] = m;
                        ka++;
                    }
                    // extract rotation matrix based on the fragment
                    Kabsch.execute(r1, r2, n_cut, 1, rmsd, t, u);
                    Functions.do_rotation(xtm, xt, Lali, t, u);
                    n_cut = score_fun8_standard(xt, ytm, Lali, d, i_ali, score, score_sum_method);
                    if (score.getValue() > score_max) {
                        score_max = score.getValue();

                        // save the rotation matrix
                        for (k = 0; k < 3; k++) {
                            t0[k] = t[k];
                            u0[k][0] = u[k][0];
                            u0[k][1] = u[k][1];
                            u0[k][2] = u[k][2];
                        }
                    }

                    // check if it converges
                    if (n_cut == ka) {
                        for (k = 0; k < n_cut; k++) {
                            if (i_ali[k] != k_ali[k]) {
                                break;
                            }
                        }
                        if (k == n_cut) {
                            break; // stop iteration
                        }
                    }
                } // for iteration

                if (i < iL_max) {
                    i = i + simplify_step; // shift the fragment
                    if (i > iL_max)
                        i = iL_max; // do this to use the last missed fragment
                } else if (i >= iL_max) {
                    break;
                }
            } // while(1)
                // end of one fragment
        } // for(i_init
        return score_max;
    }
    
    // 1, collect those residues with dis<d;
    // 2, calculate TMscore
    public int score_fun8(
            double xa[][], double ya[][],
            int n_ali, double d, int i_ali[], MutableDouble score1, int score_sum_method) {

        double score_sum = 0, di;
        double d_tmp = d * d;
        double d02 = d0 * d0;
        double score_d8_cut = score_d8 * score_d8;

        int i, n_cut, inc = 0;

        while (true) {
            n_cut = 0;
            score_sum = 0;
            for (i = 0; i < n_ali; i++) {
                di = Functions.dist(xa[i], ya[i]);
                if (di < d_tmp) {
                    i_ali[n_cut] = i;
                    n_cut++;
                }
                if (score_sum_method == 8) {
                    if (di <= score_d8_cut) {
                        score_sum += 1 / (1 + di / d02);
                    }
                } else {
                    score_sum += 1 / (1 + di / d02);
                }
            }
            // there are not enough feasible pairs, reliefe the threshold
            if (n_cut < 3 && n_ali > 3) {
                inc++;
                double dinc = (d + inc * 0.5);
                d_tmp = dinc * dinc;
            } else {
                break;
            }

        }

        score1.setValue(score_sum / Lnorm);

        return n_cut;
    }

    public int score_fun8_standard(
            double xa[][], double ya[][], 
            int n_ali, double d, int i_ali[], MutableDouble score1, int score_sum_method) {

        double score_sum = 0, di;
        double d_tmp = d * d;
        double d02 = d0 * d0;
        double score_d8_cut = score_d8 * score_d8;

        int i, n_cut, inc = 0;
        while (true) {
            n_cut = 0;
            score_sum = 0;
            for (i = 0; i < n_ali; i++) {
                di = Functions.dist(xa[i], ya[i]);
                if (di < d_tmp) {
                    i_ali[n_cut] = i;
                    n_cut++;
                }
                if (score_sum_method == 8) {
                    if (di <= score_d8_cut) {
                        score_sum += 1 / (1 + di / d02);
                    }
                } else {
                    score_sum += 1 / (1 + di / d02);
                }
            }
            // there are not enough feasible pairs, reliefe the threshold
            if (n_cut < 3 && n_ali > 3) {
                inc++;
                double dinc = (d + inc * 0.5);
                d_tmp = dinc * dinc;
            } else {
                break;
            }

        }

        score1.setValue(score_sum / n_ali);

        return n_cut;
    }
}


