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

public class TmAlign {

    // globals 
    private static double DIST_CUT = 4.25;

    // privates
    private TmMode _mode;                           // regular, fast, ...

    // structure privates
    private String _xname;
    private String _yname;
    private Chain _xchain;
    private Chain _ychain;
    private List<Group> _xgroups;
    private List<Group> _ygroups;
    private List<Atom> _xatoms;
    private List<Atom> _yatoms;

    // scratch privates 
    private double _score[][];                      // for dynamic programming
    private boolean _path[][];                      // for dynamic programming
    private double _val[][];                        // for dynamic programming
    private int _xlen, _ylen, _minlen;              // length of proteins
    private double _xa[][], _ya[][];                // for input coordinates
    private double _xtm[][], _ytm[][];              // for packing alignment without gaps 
    private double _xt[][];                         // for saving the superposition coords of xa or xtm
    private char _seqx[], _seqy[];                  // for amino acid sequence
    private int _secx[], _secy[];                   // for secondary structure sequence
    private double _r1[][], _r2[][];                // for Kabsch rotation
    private double _t[];                            // Kabsch translation vector and rotation matrix
    private double _u[][];

    public TmAlign(Structure xstruct, Structure ystruct) {

        // default mode
        _mode = TmMode.REGULAR;

        // chain names
        _xname = xstruct.getName();
        _yname = ystruct.getName();

        // get first chain in each structure
        _xchain = xstruct.getChains().get(0);
        _ychain = ystruct.getChains().get(0);

        // get groups of atoms per residue
        _xgroups = _xchain.getAtomGroups().stream().filter(g -> !g.isHetAtomInFile() && g.hasAtom("CA")).collect(Collectors.toList());
        _ygroups = _ychain.getAtomGroups().stream().filter(g -> !g.isHetAtomInFile() && g.hasAtom("CA")).collect(Collectors.toList());

        // get carbon alpha atoms per residue
        _xatoms = _xgroups.stream().map(g -> g.getAtom("CA")).collect(Collectors.toList());
        _yatoms = _ygroups.stream().map(g -> g.getAtom("CA")).collect(Collectors.toList());

        // get number of residues
        _xlen = _xatoms.size();
        _ylen = _yatoms.size();
        _minlen = Math.min(_xlen, _ylen);

        // allocate storage
        _score = new double[_xlen + 1][_ylen + 1];
        _path = new boolean[_xlen + 1][_ylen + 1];
        _val = new double[_xlen + 1][_ylen + 1];
        _xtm = new double[_minlen][3];
        _ytm = new double[_minlen][3];
        _xt = new double[_xlen][3];
        _seqx = new char[_xlen];
        _seqy = new char[_ylen];
        _secx = new int[_xlen];
        _secy = new int[_ylen];
        _r1 = new double[_minlen][3];
        _r2 = new double[_minlen][3];
        _t = new double[3];
        _u = new double[3][3];

        // get x atom coordinates
        _xa = new double[_xlen][3];
        for (int i = 0; i < _xatoms.size(); i++) {

            Group g = _xatoms.get(i).getGroup();
            if (g instanceof AminoAcid) {
                AminoAcid aa = (AminoAcid)g;
                _seqx[i] = aa.getAminoType();
            }
            else {
                _seqx[i] = 'X';
            }

            Atom atom = _xatoms.get(i);
            _xa[i][0] = atom.getX();
            _xa[i][1] = atom.getY();
            _xa[i][2] = atom.getZ();
        }

        // get y atom coordinates
        _ya = new double[_ylen][3];
        for (int i = 0; i < _yatoms.size(); i++) {
            
            Group g = _yatoms.get(i).getGroup();
            if (g instanceof AminoAcid) {
                AminoAcid aa = (AminoAcid)g;
                _seqy[i] = aa.getAminoType();
            }
            else {
                _seqy[i] = 'X';
            }

            Atom atom = _yatoms.get(i);
            _ya[i][0] = atom.getX();
            _ya[i][1] = atom.getY();
            _ya[i][2] = atom.getZ();
        }
    }

    public TmAlign(Structure xstruct, Structure ystruct, TmMode mode) {
        
        this(xstruct, ystruct);
        this._mode = mode;
    }

    public TmAlign(int xlen, int ylen) {

        // default mode 
        _mode = TmMode.REGULAR; 

        // get number of residues
        _xlen = xlen;
        _ylen = ylen;
        _minlen = Math.min(_xlen, _ylen);

        // allocate storage
        _score = new double[_xlen + 1][_ylen + 1];
        _path = new boolean[_xlen + 1][_ylen + 1];
        _val = new double[_xlen + 1][_ylen + 1];
        _xtm = new double[_minlen][3];
        _ytm = new double[_minlen][3];
        _xt = new double[_xlen][3];
        _seqx = new char[_xlen];
        _seqy = new char[_ylen];
        _secx = new int[_xlen];
        _secy = new int[_ylen];
        _r1 = new double[_minlen][3];
        _r2 = new double[_minlen][3];
        _t = new double[3];
        _u = new double[3][3];
    }

    public TmAlign(int xlen, int ylen, TmMode mode) {
        
        this(xlen, ylen);
        this._mode = mode;
    }

    public TmResults align() { 

        // ********************************************************************************** //
        // * initialization *
        // ********************************************************************************** //

        // set d0 terms and normalization term
        Parameters params = Parameters.getSearchParameters(_xlen, _ylen);
       
        // set scoring method 
        int simplify_step = 40; 
        int score_sum_method = 8; 
       
        // temp storage for initial alignments
        int invmap[] = new int[_ylen + 1];

        // store the best initial alignment
        int invmap_best[] = new int[_ylen + 1];
        for (int i = 0; i < _ylen; i++) {
            invmap_best[i] = -1;
        }
        
        double tm = 0;
        double max_tm = -1;

        double percent_of_max = 0.4;
        if (params.getNormalizeBy() <= 40)
            percent_of_max = 0.1; 

        // ********************************************************************************** //
        // * get initial alignment with gapless threading *
        // ********************************************************************************** //

        get_initial(_xa, _ya, _xlen, _ylen, invmap_best, params);
        tm = detailed_search_wrapper(_xa, _ya, _xlen, _ylen, invmap_best, _t, _u, simplify_step, score_sum_method, false, params);

        if (tm > max_tm) {
            max_tm = tm;
        }
        tm = dp_iteration(_xa, _ya, _xlen, _ylen, _t, _u, invmap, _mode.getDpIterations(), false, params);
        if (tm > max_tm) {
            max_tm = tm;
            for (int i = 0; i < _ylen; i++) {
                invmap_best[i] = invmap[i];
            }
        }

        // ********************************************************************************** //
        // * get initial alignment based on secondary structure *
        // ********************************************************************************** //
        
        get_initial_ss(_xa, _ya, _xlen, _ylen, invmap);
        tm = detailed_search_wrapper(_xa, _ya, _xlen, _ylen, invmap, _t, _u, simplify_step, score_sum_method, false, params);

        if (tm > max_tm) {
            max_tm = tm;
            for (int i = 0; i < _ylen; i++) {
                invmap_best[i] = invmap[i];
            }
        }
        if (tm > max_tm * 0.2) {
            tm = dp_iteration(_xa, _ya, _xlen, _ylen, _t, _u, invmap, _mode.getDpIterations(), false, params);
            if (tm > max_tm) {
                max_tm = tm;
                for (int i = 0; i < _ylen; i++) {
                    invmap_best[i] = invmap[i];
                }
            }
        }
        
        // ********************************************************************************** //
        // * get initial alignment based on local superposition *
        // ********************************************************************************** //

        if (_mode == TmMode.REGULAR && get_initial5(_xa, _ya, _xlen, _ylen, invmap, params)) {

            tm = detailed_search_wrapper(_xa, _ya, _xlen, _ylen, invmap, _t, _u, simplify_step, score_sum_method, false, params);

            if (tm > max_tm) {
                max_tm = tm;
                for (int i = 0; i < _ylen; i++) {
                    invmap_best[i] = invmap[i];
                }
            }
            if (tm > max_tm * percent_of_max) {
                tm = dp_iteration(_xa, _ya, _xlen, _ylen, _t, _u, invmap, 2, false, params);
                if (tm > max_tm) {
                    max_tm = tm;
                    for (int i = 0; i < _ylen; i++) {
                        invmap_best[i] = invmap[i];
                    }
                }
            }
        }

        // ********************************************************************************** //
        // * get initial alignment based on previous alignment+secondary structure *
        // ********************************************************************************** //
        
        get_initial_ssplus(_xa, _ya, _xlen, _ylen, invmap_best, invmap, params);
        tm = detailed_search_wrapper(_xa, _ya, _xlen, _ylen, invmap, _t, _u, simplify_step, score_sum_method, false, params);

        if (tm > max_tm) {
            max_tm = tm;
            for (int i = 0; i < _ylen; i++) {
                invmap_best[i] = invmap[i];
            }
        }
        if (tm > max_tm * percent_of_max) {
            tm = dp_iteration(_xa, _ya, _xlen, _ylen, _t, _u, invmap, _mode.getDpIterations(), false, params);
            if (tm > max_tm) {
                max_tm = tm;
                for (int i = 0; i < _ylen; i++) {
                    invmap_best[i] = invmap[i];
                }
            }
        }

        // ********************************************************************************** //
        // * get initial alignment based on fragment gapless threading *
        // ********************************************************************************** //
        
        get_initial_fgt(_xa, _ya, _xlen, _ylen, invmap, params);
        tm = detailed_search_wrapper(_xa, _ya, _xlen, _ylen, invmap, _t, _u, simplify_step, score_sum_method, false, params);

        if (tm > max_tm) {
            max_tm = tm;
            for (int i = 0; i < _ylen; i++) {
                invmap_best[i] = invmap[i];
            }
        }
        if (tm > max_tm * percent_of_max) {
            tm = dp_iteration(_xa, _ya, _xlen, _ylen, _t, _u, invmap, 2, true, params);
            if (tm > max_tm) {
                max_tm = tm;
                for (int i = 0; i < _ylen; i++) {
                    invmap_best[i] = invmap[i];
                }
            }
        }

        // ********************************************************************************** //
        // * validate the final and best initial alignment *
        // ********************************************************************************** //
       
        // make sure at least one pair is aligned 
        boolean flag = false;
        for (int i = 0; i < _ylen; i++) {
            if (invmap_best[i] >= 0) {
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

        tm = detailed_search_wrapper(_xa, _ya, _xlen, _ylen, invmap_best, _t, _u, simplify_step, score_sum_method, true, params);

        // select pairs with dis < d8 for final TMscore computation and output alignment
        int align_len, k = 0;
        int m1[], m2[];
        double d;
        m1 = new int[_xlen]; // alignd index in x
        m2 = new int[_ylen]; // alignd index in y
        Functions.do_rotation(_xa, _xt, _xlen, _t, _u);
        k = 0;
        for (int j = 0; j < _ylen; j++) {
            int i = invmap_best[j];
            if (i >= 0)
            {
                // aligned
                d = Math.sqrt(Functions.dist(_xt[i], _ya[j]));
                if (d <= params.getScoreD8()) {

                    m1[k] = i;
                    m2[k] = j;

                    // densely packed - not transformed
                    _xtm[k][0] = _xa[i][0];
                    _xtm[k][1] = _xa[i][1];
                    _xtm[k][2] = _xa[i][2];

                    _ytm[k][0] = _ya[j][0];
                    _ytm[k][1] = _ya[j][1];
                    _ytm[k][2] = _ya[j][2];

                    // densley packed - transformed
                    _r1[k][0] = _xt[i][0];
                    _r1[k][1] = _xt[i][1];
                    _r1[k][2] = _xt[i][2];

                    _r2[k][0] = _ya[j][0];
                    _r2[k][1] = _ya[j][1];
                    _r2[k][2] = _ya[j][2];

                    k++;
                }
            }
        }

        // alignment length
        align_len = k;

        // minimize rmsd for the best rotation and translation matrices t and u
        double rmsd = Kabsch.execute(_r1, _r2, align_len, 0, _t, _u); 
        rmsd = Math.sqrt(rmsd / (double) align_len);

        // ********************************************************************************* //
        // * Final TMscore *
        // ********************************************************************************* //
        
        double tmQ, tmT, tmAvg; 
        double d0_out = 5.0;  

        // set score method 
        simplify_step = 1;
        score_sum_method = 0;
    
        // normalized by length of first structure
        params = Parameters.getFinalParameters(_xlen, _ylen, _xlen);
        tmQ = detailed_search(_xtm, _ytm, align_len, _t, _u, simplify_step, score_sum_method, false, params);

        //normalized by length of second structure
        params = Parameters.getFinalParameters(_xlen, _ylen, _ylen);
        tmT = detailed_search(_xtm, _ytm, align_len, _t, _u, simplify_step, score_sum_method, false, params);
        
        // normalized by average length of structures
        params = Parameters.getFinalParameters(_xlen, _ylen, (_xlen + _ylen) * 0.5);
        tmAvg = detailed_search(_xtm, _ytm, align_len, _t, _u, simplify_step, score_sum_method, false, params);
        
        // ********************************************************************************* //
        // * Output *
        // ********************************************************************************* //

        TmResults results = new TmResults();
        results.setChainLength1(_xlen);
        results.setChainLength2(_ylen);
        results.setAlignedLength(align_len);
        results.setTmScoreQ(tmQ);
        results.setTmScoreT(tmT);
        results.setTmScoreAvg(tmAvg);
        results.setRmsd(rmsd);

        if (this._mode == TmMode.ALIGN_TEXT) {

            k = 0;
            d = 0.0;

            double seq_id;          
            int i, j;
            int ali_len = _xlen + _ylen;
            char[] seqM = new char[ali_len];
            char[] seqxA = new char[ali_len];
            char[] seqyA = new char[ali_len];
            
            Functions.do_rotation(_xa, _xt, _xlen, _t, _u);

            seq_id=0;
            int kk=0, i_old=0, j_old=0;
            for(k=0; k<align_len; k++)
            {
                for(i=i_old; i<m1[k]; i++)
                {
                    //align x to gap
                    seqxA[kk]=_seqx[i];
                    seqyA[kk]='-';
                    seqM[kk]=' ';
                    kk++;
                }

                for(j=j_old; j<m2[k]; j++)
                {
                    //align y to gap
                    seqxA[kk]='-';
                    seqyA[kk]=_seqy[j];
                    seqM[kk]=' ';
                    kk++;
                }

                seqxA[kk]=_seqx[m1[k]];
                seqyA[kk]=_seqy[m2[k]];
                if(seqxA[kk]==seqyA[kk])
                {
                    seq_id++;
                }
                d = Math.sqrt(Functions.dist(_xt[m1[k]], _ya[m2[k]]));
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
            for(i=i_old; i<_xlen; i++)
            {
                //align x to gap
                seqxA[kk]=_seqx[i];
                seqyA[kk]='-';
                seqM[kk]=' ';                   
                kk++;
            }    
            for(j=j_old; j<_ylen; j++)
            {
                //align y to gap
                seqxA[kk]='-';
                seqyA[kk]=_seqy[j];
                seqM[kk]=' ';
                kk++;
            }
         
            seq_id = seq_id/( align_len+0.00000001); //what did by TMalign, but not reasonable, it should be n_ali8
        
            StringBuilder sb = new StringBuilder();
            Formatter formatter = new Formatter(sb, Locale.US);
            
            formatter.format("\nName of Chain_1: %s\n", _xname); 
            formatter.format("Name of Chain_2: %s\n", _yname);
            formatter.format("Length of Chain_1: %d residues\n", _xlen);
            formatter.format("Length of Chain_2: %d residues\n\n", _ylen);

            formatter.format("Aligned length= %d, RMSD= %6.2f, Seq_ID=n_identical/n_aligned= %4.3f\n", align_len, rmsd, seq_id); 
            formatter.format("TM-score= %6.5f (if normalized by length of Chain_1)\n", tmQ);
            formatter.format("TM-score= %6.5f (if normalized by length of Chain_2)\n", tmT);
            
            formatter.format("TM-score= %6.5f (if normalized by average length of chains)\n", tmAvg);
            
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
        else if (_mode == TmMode.ALIGN_3D) {

            int xlenreal = 0;
            for(Group g : _xgroups) {
               
                if (g.hasAtom("N")) xlenreal++;
                xlenreal++; // for known CA
                if (g.hasAtom("C")) xlenreal++;
                if (g.hasAtom("O")) xlenreal++;
            }

            double[][] xa_all = new double[xlenreal][3];
            double[][] xt_all = new double[xlenreal][3];
            
            // iterate x atoms for xa_all
            int j = 0;
            for (int i = 0; i < _xgroups.size(); i++) {
                
                Group group = _xgroups.get(i);

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
            Functions.do_rotation(xa_all, xt_all, xlenreal, _t, _u);

            // set x atom coords
            j = 0;
            for (int i = 0; i < _xgroups.size(); i++) {
                
                Group group = _xgroups.get(i);

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
            for (int i = 0; i < _xgroups.size(); i++) {
                
                Group group = _xgroups.get(i);
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
            for (int i = 0; i < _ygroups.size(); i++) {

                Group group = _ygroups.get(i);
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
    // initial alignments
    // **********************************************************************************
    
    public double get_initial(double xa[][], double ya[][], int xlen, int ylen, int invmap[], Parameters params) {

        // Output:
        // y2x: alignment of y to x (-1 indicates unaligned)

        int min_len = Math.min(xlen, ylen);
        if (min_len <= 5) {
            throw new RuntimeException("Sequence is too short <=5!");
        }

        // minimum size of fragment
        int min_ali = min_len / 2; 
        if (min_ali <= 5)
            min_ali = 5;

        int n1, n2;
        n1 = -ylen + min_ali;
        n2 = xlen - min_ali;

        int i, j, k, k_best;
        double tmscore, tmscore_max = -1;

        // slide seq y over seq x with minimum overlap of min_ali
        k_best = n1;
        for (k = n1; k <= n2; k++) {

            // get the map for current positions
            for (j = 0; j < ylen; j++) {
                i = j + k;
                if (i >= 0 && i < xlen) {
                    invmap[j] = i;
                } else {
                    invmap[j] = -1;
                }
            }

            // evaluate the initial alignments
            tmscore = fast_search(xa, ya, xlen, ylen, invmap, params);
            if (tmscore >= tmscore_max) {
                tmscore_max = tmscore;
                k_best = k;
            }
        }

        // extract the best map
        k = k_best;
        for (j = 0; j < ylen; j++) {
            i = j + k;
            if (i >= 0 && i < xlen) {
                invmap[j] = i;
            } else {
                invmap[j] = -1;
            }
        }

        return tmscore_max;
    }

    // get initial alignment from secondary structure 
    public void get_initial_ss(double xa[][], double ya[][], int xlen, int ylen, int invmap[]) {

        // assign secondary structures
        make_sec(xa, xlen, _secx);
        make_sec(ya, ylen, _secy);

        double gap_open = -1.0;
        NW.dp_ss(_path, _val, _secx, _secy, xlen, ylen, gap_open, invmap);
    }
    
    // get initial alignment from secondary structure plus previous alignments
    public void get_initial_ssplus(double xa[][], double ya[][], int xlen, int ylen, 
            int invmap_best[], int invmap[], 
            Parameters params) {
        
        // create score matrix for best initial alignment so far
        score_matrix_rmsd_sec(xa, ya, xlen, ylen, invmap_best, params);

        double gap_open = -1.0;
        NW.dp_score(_score, _path, _val, xlen, ylen, gap_open, invmap);
    }
    
    // 1->coil, 2->helix, 3->turn, 4->strand
    public void make_sec(double a[][], int len, int sec[]) {
        
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
                d13 = Math.sqrt(Functions.dist(a[j1], a[j3]));
                d14 = Math.sqrt(Functions.dist(a[j1], a[j4]));
                d15 = Math.sqrt(Functions.dist(a[j1], a[j5]));
                d24 = Math.sqrt(Functions.dist(a[j2], a[j4]));
                d25 = Math.sqrt(Functions.dist(a[j2], a[j5]));
                d35 = Math.sqrt(Functions.dist(a[j3], a[j5]));
                sec[i] = sec_str(d13, d14, d15, d24, d25, d35);
            }
        }
    }

    public int sec_str(double dis13, double dis14, double dis15, double dis24, double dis25, double dis35) {
        
        int s = 1; // coil

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

    public void score_matrix_rmsd_sec(double xa[][], double ya[][], int xlen, int ylen, int invmap[], Parameters params) {

        double t[] = new double[3];
        double u[][] = new double[3][3];
        double dij;
        double d01 = params.getD0() + 1.5;
        double d02 = d01 * d01;

        double xx[] = new double[3];
        int i, k = 0;
        for (int j = 0; j < ylen; j++) {
            i = invmap[j];
            if (i >= 0) {
                _r1[k][0] = xa[i][0];
                _r1[k][1] = xa[i][1];
                _r1[k][2] = xa[i][2];

                _r2[k][0] = ya[j][0];
                _r2[k][1] = ya[j][1];
                _r2[k][2] = ya[j][2];

                k++;
            }
        }
        Kabsch.execute(_r1, _r2, k, 1, t, u);

        for (int ii = 0; ii < xlen; ii++) {
            Functions.transform(t, u, xa[ii], xx);
            for (int jj = 0; jj < ylen; jj++) {
                dij = Functions.dist(xx, ya[jj]);
                if (_secx[ii] == _secy[jj]) {
                    _score[ii + 1][jj + 1] = 1.0 / (1 + dij / d02) + 0.5;
                } else {
                    _score[ii + 1][jj + 1] = 1.0 / (1 + dij / d02);
                }
            }
        }
    }
    
    public boolean get_initial5(double xa[][], double ya[][], int xlen, int ylen, int invmap[], Parameters params) {
        
        double GL;
        double t[] = new double[3];
        double u[][] = new double[3][3];

        double d01 = params.getD0() + 1.5;
        double d02 = d01 * d01;

        double GLmax = 0;
        int aL = Math.min(xlen, ylen);
        int invmap_local[] = new int[ylen + 1];

        // jump on sequence1-------------->
        int n_jump1 = 0;
        if (xlen > 250)
            n_jump1 = 45;
        else if (xlen > 200)
            n_jump1 = 35;
        else if (xlen > 150)
            n_jump1 = 25;
        else
            n_jump1 = 15;
        if (n_jump1 > (xlen / 3))
            n_jump1 = xlen / 3;

        // jump on sequence2-------------->
        int n_jump2 = 0;
        if (ylen > 250)
            n_jump2 = 45;
        else if (ylen > 200)
            n_jump2 = 35;
        else if (ylen > 150)
            n_jump2 = 25;
        else
            n_jump2 = 15;
        if (n_jump2 > (ylen / 3))
            n_jump2 = ylen / 3;

        // fragment to superimpose-------------->
        int n_frag[] = { 20, 100 };
        if (n_frag[0] > (aL / 3))
            n_frag[0] = aL / 3;
        if (n_frag[1] > (aL / 2))
            n_frag[1] = aL / 2;

        // start superimpose search-------------->
        boolean flag = false;
        for (int i_frag = 0; i_frag < 2; i_frag++) {
            int m1 = xlen - n_frag[i_frag] + 1;
            int m2 = ylen - n_frag[i_frag] + 1;

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
                        _r1[k][0] = xa[k + i][0];
                        _r1[k][1] = xa[k + i][1];
                        _r1[k][2] = xa[k + i][2];

                        _r2[k][0] = ya[k + j][0];
                        _r2[k][1] = ya[k + j][1];
                        _r2[k][2] = ya[k + j][2];
                    }

                    // superpose the two structures and rotate it
                    Kabsch.execute(_r1, _r2, n_frag[i_frag], 1, t, u);

                    double gap_open = 0.0;
                    NW.dp_dist(_path, _val, xa, ya, xlen, ylen, t, u, d02, gap_open, invmap_local);
                    GL = fast_search(xa, ya, xlen, ylen, invmap_local, params);
                    if (GL > GLmax) {
                        GLmax = GL;
                        for (int ii = 0; ii < ylen; ii++) {
                            invmap[ii] = invmap_local[ii];
                        }
                        flag = true;
                    }
                }
            }
        }

        return flag;
    }
    
    public double get_initial_fgt(double xa[][], double ya[][], int xlen, int ylen, int invmap[], Parameters params) {

        int fra_min = 4; // minimum fragment for search
        int fra_min1 = fra_min - 1; // cutoff for shift, save time

        MutableInt xstart = new MutableInt(0);
        MutableInt ystart = new MutableInt(0);
        MutableInt xend = new MutableInt(0);
        MutableInt yend = new MutableInt(0);

        find_max_frag(xa, xlen, xstart, xend);
        find_max_frag(ya, ylen, ystart, yend);

        int Lx = xend.getValue() - xstart.getValue() + 1;
        int Ly = yend.getValue() - ystart.getValue() + 1;
        int ifr[], invmap_local[];
        int L_fr = Math.min(Lx, Ly);
        ifr = new int[L_fr];
        invmap_local = new int[ylen + 1];

        // select what piece will be used (this may araise ansysmetry, but
        // only when L1=L2 and Lfr1=Lfr2 and L1 ne Lfr1
        // if L1=Lfr1 and L2=Lfr2 (normal proteins), it will be the same as
        // initial1

        if (Lx < Ly || (Lx == Ly && xlen <= ylen)) {
            for (int i = 0; i < L_fr; i++) {
                ifr[i] = xstart.getValue() + i;
            }
        } else if (Lx > Ly || (Lx == Ly && xlen > ylen)) {
            for (int i = 0; i < L_fr; i++) {
                ifr[i] = ystart.getValue() + i;
            }
        }

        int L0 = Math.min(xlen, ylen); // non-redundant to get_initial1
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

        if (Lx < Ly || (Lx == Ly && xlen <= ylen)) {
            int L1 = L_fr;
            int min_len = Math.min(L1, ylen);
            int min_ali = (int) (min_len / 2.5); // minimum size of considered
                                                    // fragment
            if (min_ali <= fra_min1)
                min_ali = fra_min1;
            int n1, n2;
            n1 = -ylen + min_ali;
            n2 = L1 - min_ali;

            int i, j, k;
            for (k = n1; k <= n2; k++) {
                // get the map
                for (j = 0; j < ylen; j++) {
                    i = j + k;
                    if (i >= 0 && i < L1) {
                        invmap_local[j] = ifr[i];
                    } else {
                        invmap_local[j] = -1;
                    }
                }

                // evaluate the map quickly in three iterations
                tmscore = fast_search(xa, ya, xlen, ylen, invmap_local, params);

                if (tmscore >= tmscore_max) {
                    tmscore_max = tmscore;
                    for (j = 0; j < ylen; j++) {
                        invmap[j] = invmap_local[j];
                    }
                }
            }
        } else {
            int L2 = L_fr;
            int min_len = Math.min(xlen, L2);
            int min_ali = (int) (min_len / 2.5); // minimum size of considered
                                                    // fragment
            if (min_ali <= fra_min1)
                min_ali = fra_min1;
            int n1, n2;
            n1 = -L2 + min_ali;
            n2 = xlen - min_ali;

            int i, j, k;

            for (k = n1; k <= n2; k++) {
                // get the map
                for (j = 0; j < ylen; j++) {
                    invmap_local[j] = -1;
                }

                for (j = 0; j < L2; j++) {
                    i = j + k;
                    if (i >= 0 && i < xlen) {
                        invmap_local[ifr[j]] = i;
                    }
                }

                // evaluate the map quickly in three iterations
                tmscore = fast_search(xa, ya, xlen, ylen, invmap_local, params);
                if (tmscore >= tmscore_max) {
                    tmscore_max = tmscore;
                    for (j = 0; j < ylen; j++) {
                        invmap[j] = invmap_local[j];
                    }
                }
            }
        }

        return tmscore_max;
    }
    
    public void find_max_frag(double a[][], int len, MutableInt start_max, MutableInt end_max) {
        
        int r_min, fra_min = 4; // minimum fragment for search
        double d;
        int start;
        int Lfr_max = 0, flag;

        r_min = (int) (len * 1.0 / 3.0); // minimum fragment, in case too small
                                            // protein
        if (r_min > fra_min)
            r_min = fra_min;

        int inc = 0;
        double dcu0_cut = DIST_CUT * DIST_CUT;
        ;
        double dcu_cut = dcu0_cut;

        while (Lfr_max < r_min) {
            Lfr_max = 0;
            int j = 1; // number of residues at nf-fragment
            start = 0;
            for (int i = 1; i < len; i++) {
                d = Functions.dist(a[i - 1], a[i]);
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
                double dinc = Math.pow(1.1, (double) inc) * DIST_CUT;
                dcu_cut = dinc * dinc;
            }
        } // while <;
    }
    
    // **********************************************************************************
    // dynamic programming iteration
    // **********************************************************************************

    public double dp_iteration(
            double xa[][], double ya[][], 
            int xlen, int ylen, 
            double t[], double u[][], 
            int invmap[],
            int iteration_max,
            boolean gapless,
            Parameters params) {

        int invmap_local[] = new int[ylen + 1];

        int iteration, i, j, k;
        double tmscore, tmscore_max, tmscore_old = 0;
        int score_sum_method = 8;
        int simplify_step = 40;
        tmscore_max = -1;

        int g1 = 0; 
        int g2 = 2;
        if (gapless) 
            g1 = 1;
        double gap_open[] = { -0.6, 0 };

        // try different gap open penalties
        for (int g = g1; g < g2; g++) {

            // iterate on NW dp algorithm
            for (iteration = 0; iteration < iteration_max; iteration++) {

                NW.dp_dist(_path, _val, xa, ya, xlen, ylen, t, u, params.getD02(), gap_open[g], invmap_local);

                k = 0;
                for (j = 0; j < ylen; j++) {

                    i = invmap_local[j];
                    if (i >= 0) {

                        // pack alignment
                        _xtm[k][0] = xa[i][0];
                        _xtm[k][1] = xa[i][1];
                        _xtm[k][2] = xa[i][2];

                        _ytm[k][0] = ya[j][0];
                        _ytm[k][1] = ya[j][1];
                        _ytm[k][2] = ya[j][2];

                        k++;
                    }
                }

                // k is the length of the alignment stored densely in xtm and ytm
                tmscore = detailed_search(_xtm, _ytm, k, t, u, simplify_step, score_sum_method, false, params);

                // update the best
                if (tmscore > tmscore_max) {
                    tmscore_max = tmscore;
                    for (i = 0; i < ylen; i++) {
                        invmap[i] = invmap_local[i];
                    }
                }

                // test for convergence to break early
                if (iteration > 0) {
                    if (Math.abs(tmscore_old - tmscore) < 0.000001) {
                        break;
                    }
                }

                tmscore_old = tmscore;

            } // for dp iteration

        } // for gap open

        return tmscore_max;
    }

    // **********************************************************************************
    // searching
    // **********************************************************************************
    
    public double fast_search(double xa[][], double ya[][], int xlen, int ylen, int invmap[], Parameters params) {
        
        double tmscore, tmscore1, tmscore2;
        int i, j, k;

        k = 0;
        for (j = 0; j < ylen; j++) {
            i = invmap[j];
            if (i >= 0) {
                _r1[k][0] = xa[i][0];
                _r1[k][1] = xa[i][1];
                _r1[k][2] = xa[i][2];

                _r2[k][0] = ya[j][0];
                _r2[k][1] = ya[j][1];
                _r2[k][2] = ya[j][2];

                _xtm[k][0] = xa[i][0];
                _xtm[k][1] = xa[i][1];
                _xtm[k][2] = xa[i][2];

                _ytm[k][0] = ya[j][0];
                _ytm[k][1] = ya[j][1];
                _ytm[k][2] = ya[j][2];

                k++;
            } else if (i != -1) {
                throw new RuntimeException("Wrong map!");
            }
        }
        Kabsch.execute(_r1, _r2, k, 1, _t, _u);

        // evaluate score
        double di;
        int len = k;
        double dis[] = new double[len];
        double d00 = params.getD0Bounded();
        double d002 = d00 * d00;
        double d02 = params.getD02();

        int n_ali = k;
        double xrot[] = new double[3];
        tmscore = 0;
        for (k = 0; k < n_ali; k++) {
            Functions.transform(_t, _u, _xtm[k], xrot);
            di = Functions.dist(xrot, _ytm[k]);
            dis[k] = di;
            tmscore += 1 / (1 + di / d02);
        }

        // second iteration
        double d002t = d002;
        while (true) {
            j = 0;
            for (k = 0; k < n_ali; k++) {
                if (dis[k] <= d002t) {
                    _r1[j][0] = _xtm[k][0];
                    _r1[j][1] = _xtm[k][1];
                    _r1[j][2] = _xtm[k][2];

                    _r2[j][0] = _ytm[k][0];
                    _r2[j][1] = _ytm[k][1];
                    _r2[j][2] = _ytm[k][2];

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
            Kabsch.execute(_r1, _r2, j, 1, _t, _u);
            tmscore1 = 0;
            for (k = 0; k < n_ali; k++) {
                Functions.transform(_t, _u, _xtm[k], xrot);
                di = Functions.dist(xrot, _ytm[k]);
                dis[k] = di;
                tmscore1 += 1 / (1 + di / d02);
            }

            // third iteration
            d002t = d002 + 1;

            while (true) {
                j = 0;
                for (k = 0; k < n_ali; k++) {
                    if (dis[k] <= d002t) {
                        _r1[j][0] = _xtm[k][0];
                        _r1[j][1] = _xtm[k][1];
                        _r1[j][2] = _xtm[k][2];

                        _r2[j][0] = _ytm[k][0];
                        _r2[j][1] = _ytm[k][1];
                        _r2[j][2] = _ytm[k][2];

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
            Kabsch.execute(_r1, _r2, j, 1, _t, _u);
            tmscore2 = 0;
            for (k = 0; k < n_ali; k++) {
                Functions.transform(_t, _u, _xtm[k], xrot);
                di = Functions.dist(xrot, _ytm[k]);
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
    
    public double detailed_search_wrapper(
            double xa[][], double ya[][], 
            int xlen, int ylen, 
            int invmap[], 
            double t_out[], double u_out[][], 
            int simplify_step, 
            int score_sum_method,
            boolean align_normalize,
            Parameters params) {

        // pack the alignment into _xtm and _ytm based on the inverse map
        int i, j, k = 0;
        for (i = 0; i < ylen; i++) {
        
            j = invmap[i];
            if (j >= 0) {

                // aligned
                _xtm[k][0] = xa[j][0];
                _xtm[k][1] = xa[j][1];
                _xtm[k][2] = xa[j][2];

                _ytm[k][0] = ya[i][0];
                _ytm[k][1] = ya[i][1];
                _ytm[k][2] = ya[i][2];

                k++;
            }
        }

        // k holds the length of the alignment obtained from the inverse map
        return detailed_search(_xtm, _ytm, k, t_out, u_out, simplify_step, score_sum_method, align_normalize, params);
    }
    
    public double detailed_search(
            double xtm[][], double ytm[][], 
            int align_len, 
            double t_out[], double u_out[][],
            int simplify_step,
            int score_sum_method,
            boolean align_normalize,
            Parameters params) {

        int i, m;
        MutableDouble score = new MutableDouble(0.0);
        int ka, k;
        double t[] = new double[3];
        double u[][] = new double[3][3];
        double dist_th;

        int num_iters = _mode.getScoreIterations(); 
        int max_num_frag_lens = 6; 
        // fragment lengths, align_len, align_len/2, align_len/4 ... 4
        int frag_lens[] = new int[max_num_frag_lens]; 
                                            
        // initialize fragment lengths
        int min_frag_len = 4;
        if (align_len < 4)
            min_frag_len = align_len;
        int num_frag_lens = 0;
        for (i = 0; i < max_num_frag_lens - 1; i++) {
            num_frag_lens++;
            frag_lens[i] = (int) (align_len / Math.pow(2.0, (double) i));
            if (frag_lens[i] <= min_frag_len) {
                frag_lens[i] = min_frag_len;
                break;
            }
        }
        // if we made it all the way to the end
        if (i == max_num_frag_lens - 1) {
            num_frag_lens++;
            frag_lens[i] = min_frag_len;
        }

        // find the maximum score starting from superposition of fragments
        double max_score = -1;
        int sat_indices[] = new int[align_len];
        int num_sat;
        int frag_len; 
        int max_start_pos; 

        for (int j = 0; j < num_frag_lens; j++) {

            frag_len = frag_lens[j];
            max_start_pos = align_len - frag_len;

            int pos = 0;
            while (true) {
                
                // extract the fragment starting from pos and pack
                for (k = 0; k < frag_len; k++) {

                    int offset_k = k + pos;

                    _r1[k][0] = xtm[offset_k][0];
                    _r1[k][1] = xtm[offset_k][1];
                    _r1[k][2] = xtm[offset_k][2];

                    _r2[k][0] = ytm[offset_k][0];
                    _r2[k][1] = ytm[offset_k][1];
                    _r2[k][2] = ytm[offset_k][2];
                }

                // calculate rotation matrix based on the fragment
                Kabsch.execute(_r1, _r2, frag_len, 1, t, u);
                
                // peform rotation and store in xt
                Functions.do_rotation(xtm, _xt, align_len, t, u);

                // calcualte tm-score and get indices satisfying distance threshold
                dist_th = params.getD0Bounded() - 1;
                num_sat = calculate_tm_score(
                        _xt, ytm, align_len, 
                        dist_th, sat_indices, 
                        score, score_sum_method, 
                        align_normalize, params);
                if (score.getValue() > max_score) {
                    max_score = score.getValue();

                    // save the rotation matrix
                    for (k = 0; k < 3; k++) {
                        t_out[k] = t[k];
                        u_out[k][0] = u[k][0];
                        u_out[k][1] = u[k][1];
                        u_out[k][2] = u[k][2];
                    }
                }

                // try to extend the alignment iteratively
                int last_sat_indices[] = new int[align_len];
                dist_th = params.getD0Bounded() + 1;
                for (int it = 0; it < num_iters; it++) {
                    ka = 0;
                    for (k = 0; k < num_sat; k++) {
                        m = sat_indices[k];
                        _r1[k][0] = xtm[m][0];
                        _r1[k][1] = xtm[m][1];
                        _r1[k][2] = xtm[m][2];

                        _r2[k][0] = ytm[m][0];
                        _r2[k][1] = ytm[m][1];
                        _r2[k][2] = ytm[m][2];

                        last_sat_indices[ka] = m;
                        ka++;
                    }

                    // calculate rotation matrix based on the fragment
                    Kabsch.execute(_r1, _r2, num_sat, 1, t, u);
                    
                    // peform rotation and store in xt
                    Functions.do_rotation(xtm, _xt, align_len, t, u);
                    
                    // calcualte the new tm-score and get indices satisfying distance threshold
                    num_sat = calculate_tm_score(
                            _xt, ytm, align_len, 
                            dist_th, sat_indices, 
                            score, score_sum_method, 
                            align_normalize, params);
                    if (score.getValue() > max_score) {
                        max_score = score.getValue();

                        // save the rotation matrix
                        for (k = 0; k < 3; k++) {
                            t_out[k] = t[k];
                            u_out[k][0] = u[k][0];
                            u_out[k][1] = u[k][1];
                            u_out[k][2] = u[k][2];
                        }
                    }

                    // check if it converges
                    if (num_sat == ka) {
                        for (k = 0; k < num_sat; k++) {
                            if (sat_indices[k] != last_sat_indices[k]) {
                                break;
                            }
                        }
                        if (k == num_sat) {
                            break; // stop iteration
                        }
                    }
                } // for iteration

                if (pos < max_start_pos) {
                    pos = Math.min(pos + simplify_step, max_start_pos);
                } else {
                    break;
                }

            } // while(true)
        } // fragment lengths iter
        
        return max_score;
    }
    
    public int calculate_tm_score(
            double x[][], double y[][],
            int align_len, 
            double dist_th, int sat_indices[], 
            MutableDouble score, 
            int score_sum_method,
            boolean align_normalize,
            Parameters params) {

        double score_sum = 0;
        double dist;
        double dist_th2 = dist_th * dist_th;

        int num_sat;
        int relax_factor = 0;
        while (true) {

            num_sat = 0;
            score_sum = 0;
            for (int i = 0; i < align_len; i++) {
                dist = Functions.dist(x[i], y[i]);
                if (dist < dist_th2) {
                    sat_indices[num_sat] = i;
                    num_sat++;
                }
                if (score_sum_method == 8) {
                    if (dist <= params.getScoreD8Squared()) {
                        score_sum += 1 / (1 + dist / params.getD02());
                    }
                } else {
                    score_sum += 1 / (1 + dist / params.getD02());
                }
            }
            // there are not enough close residues, relax the threshold
            if (num_sat < 3 && align_len > 3) {
                relax_factor++;
                dist_th2 = Math.pow(dist_th + relax_factor * 0.5, 2);
            } else {
                break;
            }
        }

        if (align_normalize) {
            score.setValue(score_sum / align_len);
        }
        else {
            score.setValue(score_sum / params.getNormalizeBy());
        }

        return num_sat;
    }
}


