package edu.umkc.rupee.tm;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;

import sun.java2d.xr.MutableInteger;

public class TMAlign {

    public static void align(Structure xstruct, Structure ystruct) {
    
        //**********************************************************************************/
        //*                                 load data                                      */ 
        //**********************************************************************************/

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
        Variables.xlen = xgroups.size();
        Variables.ylen = ygroups.size();
        Variables.minlen = Math.min(Variables.xlen, Variables.ylen);
       
        // allocate  storage 
        Variables.score = new double[Variables.xlen + 1][Variables.ylen + 1];
        Variables.path = new boolean[Variables.xlen + 1][Variables.ylen + 1];
        Variables.val = new double[Variables.xlen + 1][Variables.ylen + 1];
        Variables.xtm = new double[Variables.minlen][3];
        Variables.ytm = new double[Variables.minlen][3];
        Variables.xt = new double[Variables.xlen][3];
        Variables.secx = new int[Variables.xlen];
        Variables.secy = new int[Variables.ylen];
        Variables.r1 = new double[Variables.minlen][3];
        Variables.r2 = new double[Variables.minlen][3];
        Variables.t = new double[3];
        Variables.u = new double[3][3];

        // get x atom coordinates
        Variables.xa = new double[Variables.xlen][3];
        for (int i = 0; i < xatoms.size(); i++) {
            Atom atom = xatoms.get(i);
            Variables.xa[i][0] = atom.getX();
            Variables.xa[i][1] = atom.getY();
            Variables.xa[i][2] = atom.getZ();
        }

        // get y atom coordinates
        Variables.ya = new double[Variables.ylen][3];
        for (int i = 0; i < yatoms.size(); i++) {
            Atom atom = yatoms.get(i);
            Variables.ya[i][0] = atom.getX();
            Variables.ya[i][1] = atom.getY();
            Variables.ya[i][2] = atom.getZ();
        }

        //**********************************************************************************/
        //*                                 parameter set                                  */ 
        //**********************************************************************************/

        parameter_set4search(Variables.xlen, Variables.ylen); // please set
                                                                // parameters in
                                                                // the function
        int simplify_step = 40; // for similified search engine
        int score_sum_method = 8; // for scoring method, whether only sum over
                                    // pairs with dis<score_d8
        int invmap0[] = new int[Variables.ylen + 1];
        int invmap[] = new int[Variables.ylen + 1];
        double TM = 0;
        double TMmax = -1;
        for (int i = 0; i < Variables.ylen; i++) {
            invmap0[i] = -1;
        }

        double ddcc = 0.4;
        if (Variables.Lnorm <= 40)
            ddcc = 0.1; // Lnorm was setted in parameter_set4search
        double local_d0_search = Variables.d0_search;

        //**********************************************************************************/
        //*          get initial alignment with gapless threading                          */ 
        //**********************************************************************************/

        get_initial(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, invmap0);
        //find the max TMscore for this initial alignment with the simplified search_engin
        TM = detailed_search(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, invmap0, Variables.t,
                Variables.u, simplify_step, score_sum_method, local_d0_search);
        if (TM > TMmax) {
            TMmax = TM;
        }
        // run dynamic programing iteratively to find the best alignment
        TM = DP_iter(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, Variables.t, Variables.u, invmap, 0, 2, 30, local_d0_search);
        if (TM > TMmax) {
            TMmax = TM;
            for (int i = 0; i < Variables.ylen; i++) {
                invmap0[i] = invmap[i];
            }
        }
        
        //**********************************************************************************/
        //*          get initial alignment based on secondary structure                    */
        //**********************************************************************************/
        get_initial_ss(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, invmap);
        TM = detailed_search(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, invmap, Variables.t,
                Variables.u, simplify_step, score_sum_method, local_d0_search);
        if (TM > TMmax) {
            TMmax = TM;
            for (int i = 0; i < Variables.ylen; i++) {
                invmap0[i] = invmap[i];
            }
        }
        if (TM > TMmax * 0.2) {
            TM = DP_iter(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, Variables.t, Variables.u, invmap,
                    0, 2, 30, local_d0_search);
            if (TM > TMmax) {
                TMmax = TM;
                for (int i = 0; i < Variables.ylen; i++) {
                    invmap0[i] = invmap[i];
                }
            }
        }

        //**********************************************************************************/
        //*          get initial alignment based on local superposition                    */
        //**********************************************************************************/

        if (get_initial5(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, invmap)) {
            TM = detailed_search(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, invmap, Variables.t,
                    Variables.u, simplify_step, score_sum_method, local_d0_search);
            if (TM > TMmax) {
                TMmax = TM;
                for (int i = 0; i < Variables.ylen; i++) {
                    invmap0[i] = invmap[i];
                }
            }
            if (TM > TMmax * ddcc) {
                TM = DP_iter(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, Variables.t, Variables.u,
                        invmap, 0, 2, 2, local_d0_search);
                if (TM > TMmax) {
                    TMmax = TM;
                    for (int i = 0; i < Variables.ylen; i++) {
                        invmap0[i] = invmap[i];
                    }
                }
            }
        }
        /*
         * else { cout << endl << endl <<
         * "Warning: initial alignment from local superposition fail!" << endl
         * << endl << endl; }
         */

        //**********************************************************************************/
        //*     get initial alignment based on previous alignment+secondary structure      */
        //**********************************************************************************/
        // =initial3 in original TM-align
        get_initial_ssplus(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, invmap0, invmap);
        TM = detailed_search(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, invmap, Variables.t,
                Variables.u, simplify_step, score_sum_method, local_d0_search);
        if (TM > TMmax) {
            TMmax = TM;
            for (int i = 0; i < Variables.ylen; i++) {
                invmap0[i] = invmap[i];
            }
        }
        if (TM > TMmax * ddcc) {
            TM = DP_iter(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, Variables.t, Variables.u, invmap,
                    0, 2, 30, local_d0_search);
            if (TM > TMmax) {
                TMmax = TM;
                for (int i = 0; i < Variables.ylen; i++) {
                    invmap0[i] = invmap[i];
                }
            }
        }

        //**********************************************************************************/
        //*         get initial alignment based on fragment gapless threading              */
        //**********************************************************************************/
        //=initial4 in original TM-align
        get_initial_fgt(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, invmap);
        TM = detailed_search(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, invmap, Variables.t,
                Variables.u, simplify_step, score_sum_method, local_d0_search);
        if (TM > TMmax) {
            TMmax = TM;
            for (int i = 0; i < Variables.ylen; i++) {
                invmap0[i] = invmap[i];
            }
        }
        if (TM > TMmax * ddcc) {
            TM = DP_iter(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, Variables.t, Variables.u, invmap,
                    1, 2, 2, local_d0_search);
            if (TM > TMmax) {
                TMmax = TM;
                for (int i = 0; i < Variables.ylen; i++) {
                    invmap0[i] = invmap[i];
                }
            }
        }

        //**********************************************************************************//
        //      The alignment will not be changed any more in the following                 //
        //**********************************************************************************//
        //check if the initial alignment is generated approately    
        boolean flag = false;
        for (int i = 0; i < Variables.ylen; i++) {
            if (invmap0[i] >= 0) {
                flag = true;
                break;
            }
        }
        if (!flag) {

            // no alignment bad results exit 1
        }

        //**********************************************************************************//
        //        Detailed TMscore search engine  --> prepare for final TMscore             //
        //**********************************************************************************//       
        //run detailed TMscore search engine for the best alignment, and 
        //extract the best rotation matrix (t, u) for the best alginment
        simplify_step = 1;
        score_sum_method = 8;
        TM = detailed_search_standard(Variables.xa, Variables.ya, Variables.xlen, Variables.ylen, invmap0, Variables.t,
                Variables.u, simplify_step, score_sum_method, local_d0_search, false);

        // select pairs with dis<d8 for final TMscore computation and output
        // alignment
        int n_ali8, k = 0;
        int m1[], m2[];
        double d;
        m1 = new int[Variables.xlen]; // alignd index in x
        m2 = new int[Variables.ylen]; // alignd index in y
        Functions.do_rotation(Variables.xa, Variables.xt, Variables.xlen, Variables.t, Variables.u);
        k = 0;
        for (int j = 0; j < Variables.ylen; j++) {
            int i = invmap0[j];
            if (i >= 0)// aligned
            {
                d = Math.sqrt(Functions.dist(Variables.xt[i], Variables.ya[j]));
                if (d <= Variables.score_d8) {
                    m1[k] = i;
                    m2[k] = j;

                    Variables.xtm[k][0] = Variables.xa[i][0];
                    Variables.xtm[k][1] = Variables.xa[i][1];
                    Variables.xtm[k][2] = Variables.xa[i][2];

                    Variables.ytm[k][0] = Variables.ya[j][0];
                    Variables.ytm[k][1] = Variables.ya[j][1];
                    Variables.ytm[k][2] = Variables.ya[j][2];

                    Variables.r1[k][0] = Variables.xt[i][0];
                    Variables.r1[k][1] = Variables.xt[i][1];
                    Variables.r1[k][2] = Variables.xt[i][2];
                    Variables.r2[k][0] = Variables.ya[j][0];
                    Variables.r2[k][1] = Variables.ya[j][1];
                    Variables.r2[k][2] = Variables.ya[j][2];

                    k++;
                }
            }
        }
        n_ali8 = k;

        MutableDouble rmsd0 = new MutableDouble(0.0);
        Kabsch.execute(Variables.r1, Variables.r2, n_ali8, 0, rmsd0, Variables.t, Variables.u);
        rmsd0.setValue(Math.sqrt(rmsd0.getValue() / n_ali8));

        //*********************************************************************************//
        //                               Final TMscore                                     //
        //                     Please set parameters for output                            //
        //*********************************************************************************//
        MutableDouble rmsd = new MutableDouble(0.0);
        double t0[] = new double[3];
        double u0[][] = new double[3][3];
        double TM1, TM2;
        simplify_step=1;
        score_sum_method=0;

        double Lnorm_0=Variables.ylen;
        
        //normalized by length of structure A
        parameter_set4final(Lnorm_0);
        local_d0_search = Variables.d0_search;
        TM1 = TMscore8_search(Variables.xtm, Variables.ytm, n_ali8, t0, u0, simplify_step, score_sum_method, rmsd, local_d0_search);

        //normalized by length of structure B
        parameter_set4final(Variables.xlen+0.0);
        local_d0_search = Variables.d0_search;
        TM2 = TMscore8_search(Variables.xtm, Variables.ytm, n_ali8, Variables.t, Variables.u, simplify_step, score_sum_method, rmsd, local_d0_search);

        System.out.println(rmsd);        
        System.out.println(TM1 + " " + TM2);

    }

    public static void parameter_set4search(int xlen, int ylen) {
        // parameter initilization for searching: D0_MIN, Lnorm, d0, d0_search,
        // score_d8
        Variables.D0_MIN = 0.5;
        Variables.dcu0 = 4.25; // update 3.85-->4.25

        Variables.Lnorm = Math.min(xlen, ylen); // normaliz TMscore by this in
                                                // searching
        if (Variables.Lnorm <= 19) // update 15-->19
        {
            Variables.d0 = 0.168; // update 0.5-->0.168
        } else {
            Variables.d0 = (1.24 * Math.pow((Variables.Lnorm * 1.0 - 15), 1.0 / 3) - 1.8);
        }
        Variables.D0_MIN = Variables.d0 + 0.8; // this should be moved to above
        Variables.d0 = Variables.D0_MIN; // update: best for search

        Variables.d0_search = Variables.d0;
        if (Variables.d0_search > 8)
            Variables.d0_search = 8;
        if (Variables.d0_search < 4.5)
            Variables.d0_search = 4.5;

        Variables.score_d8 = 1.5 * Math.pow(Variables.Lnorm * 1.0, 0.3) + 3.5; 
    }

    public static void parameter_set4final(double len) {
        Variables.D0_MIN = 0.5;

        Variables.Lnorm = len; // normaliz TMscore by this in searching
        if (Variables.Lnorm <= 21) {
            Variables.d0 = 0.5;
        } else {
            Variables.d0 = (1.24 * Math.pow((Variables.Lnorm * 1.0 - 15), 1.0 / 3) - 1.8);
        }
        if (Variables.d0 < Variables.D0_MIN)
            Variables.d0 = Variables.D0_MIN;

        Variables.d0_search = Variables.d0;
        if (Variables.d0_search > 8)
            Variables.d0_search = 8;
        if (Variables.d0_search < 4.5)
            Variables.d0_search = 4.5;
    }

    // 1, collect those residues with dis<d;
    // 2, calculate TMscore
    public static int score_fun8(double xa[][], double ya[][], int n_ali, double d, int i_ali[], MutableDouble score1,
            int score_sum_method) {
        double score_sum = 0, di;
        double d_tmp = d * d;
        double d02 = Variables.d0 * Variables.d0;
        double score_d8_cut = Variables.score_d8 * Variables.score_d8;

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

        score1.setValue(score_sum / Variables.Lnorm);

        return n_cut;
    }

    public static int score_fun8_standard(double xa[][], double ya[][], int n_ali, double d, int i_ali[], MutableDouble score1,
            int score_sum_method) {
        double score_sum = 0, di;
        double d_tmp = d * d;
        double d02 = Variables.d0 * Variables.d0;
        double score_d8_cut = Variables.score_d8 * Variables.score_d8;

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

    public static double TMscore8_search(double xtm[][], double ytm[][], int Lali, double t0[], double u0[][],
            int simplify_step, int score_sum_method, MutableDouble Rcomm, double local_d0_search) {
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
        int n_it = 20; // maximum number of iterations
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
                    Variables.r1[k][0] = xtm[kk][0];
                    Variables.r1[k][1] = xtm[kk][1];
                    Variables.r1[k][2] = xtm[kk][2];

                    Variables.r2[k][0] = ytm[kk][0];
                    Variables.r2[k][1] = ytm[kk][1];
                    Variables.r2[k][2] = ytm[kk][2];

                    k_ali[ka] = kk;
                    ka++;
                }

                // extract rotation matrix based on the fragment
                Kabsch.execute(Variables.r1, Variables.r2, L_frag, 1, rmsd, t, u);
                if (simplify_step != 1)
                    Rcomm.setValue(0.0);
                Functions.do_rotation(xtm, Variables.xt, Lali, t, u);

                // get subsegment of this fragment
                d = local_d0_search - 1;
                n_cut = score_fun8(Variables.xt, ytm, Lali, d, i_ali, score, score_sum_method);
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
                        Variables.r1[k][0] = xtm[m][0];
                        Variables.r1[k][1] = xtm[m][1];
                        Variables.r1[k][2] = xtm[m][2];

                        Variables.r2[k][0] = ytm[m][0];
                        Variables.r2[k][1] = ytm[m][1];
                        Variables.r2[k][2] = ytm[m][2];

                        k_ali[ka] = m;
                        ka++;
                    }
                    // extract rotation matrix based on the fragment
                    Kabsch.execute(Variables.r1, Variables.r2, n_cut, 1, rmsd, t, u);
                    Functions.do_rotation(xtm, Variables.xt, Lali, t, u);
                    n_cut = score_fun8(Variables.xt, ytm, Lali, d, i_ali, score, score_sum_method);
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

    public static double TMscore8_search_standard(double xtm[][], double ytm[][], int Lali, double t0[], double u0[][],
            int simplify_step, int score_sum_method, MutableDouble Rcomm, double local_d0_search) {
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
        int n_it = 20; // maximum number of iterations
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
                    Variables.r1[k][0] = xtm[kk][0];
                    Variables.r1[k][1] = xtm[kk][1];
                    Variables.r1[k][2] = xtm[kk][2];

                    Variables.r2[k][0] = ytm[kk][0];
                    Variables.r2[k][1] = ytm[kk][1];
                    Variables.r2[k][2] = ytm[kk][2];

                    k_ali[ka] = kk;
                    ka++;
                }
                // extract rotation matrix based on the fragment
                Kabsch.execute(Variables.r1, Variables.r2, L_frag, 1, rmsd, t, u);
                if (simplify_step != 1)
                    Rcomm.setValue(0.0);
                Functions.do_rotation(xtm, Variables.xt, Lali, t, u);

                // get subsegment of this fragment
                d = local_d0_search - 1;
                n_cut = score_fun8_standard(Variables.xt, ytm, Lali, d, i_ali, score, score_sum_method);

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
                        Variables.r1[k][0] = xtm[m][0];
                        Variables.r1[k][1] = xtm[m][1];
                        Variables.r1[k][2] = xtm[m][2];

                        Variables.r2[k][0] = ytm[m][0];
                        Variables.r2[k][1] = ytm[m][1];
                        Variables.r2[k][2] = ytm[m][2];

                        k_ali[ka] = m;
                        ka++;
                    }
                    // extract rotation matrix based on the fragment
                    Kabsch.execute(Variables.r1, Variables.r2, n_cut, 1, rmsd, t, u);
                    Functions.do_rotation(xtm, Variables.xt, Lali, t, u);
                    n_cut = score_fun8_standard(Variables.xt, ytm, Lali, d, i_ali, score, score_sum_method);
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

    // Comprehensive TMscore search engine
    // input: two vector sets: x, y
    // an alignment invmap0[] between x and y
    // simplify_step: 1 or 40 or other integers
    // score_sum_method: 0 for score over all pairs
    // 8 for socre over the pairs with dist<score_d8
    // output: the best rotaion matrix t, u that results in highest TMscore
    public static double detailed_search(double x[][], double y[][], int x_len, int y_len, int invmap0[], double t[],
            double u[][], int simplify_step, int score_sum_method, double local_d0_search) {
        // x is model, y is template, try to superpose onto y
        int i, j, k;
        double tmscore;
        MutableDouble rmsd = new MutableDouble(0.0);

        k = 0;
        for (i = 0; i < y_len; i++) {
            j = invmap0[i];
            if (j >= 0) // aligned
            {
                Variables.xtm[k][0] = x[j][0];
                Variables.xtm[k][1] = x[j][1];
                Variables.xtm[k][2] = x[j][2];

                Variables.ytm[k][0] = y[i][0];
                Variables.ytm[k][1] = y[i][1];
                Variables.ytm[k][2] = y[i][2];
                k++;
            }
        }

        // detailed search 40-->1
        tmscore = TMscore8_search(Variables.xtm, Variables.ytm, k, t, u, simplify_step, score_sum_method, rmsd,
                local_d0_search);
        return tmscore;
    }

    public static double detailed_search_standard(double x[][], double y[][], int x_len, int y_len, int invmap0[],
            double t[], double u[][], int simplify_step, int score_sum_method, double local_d0_search,
            boolean bNormalize) {
        // x is model, y is template, try to superpose onto y
        int i, j, k;
        double tmscore;
        MutableDouble rmsd = new MutableDouble(0.0);

        k = 0;
        for (i = 0; i < y_len; i++) {
            j = invmap0[i];
            if (j >= 0) // aligned
            {
                Variables.xtm[k][0] = x[j][0];
                Variables.xtm[k][1] = x[j][1];
                Variables.xtm[k][2] = x[j][2];

                Variables.ytm[k][0] = y[i][0];
                Variables.ytm[k][1] = y[i][1];
                Variables.ytm[k][2] = y[i][2];
                k++;
            }
        }

        // detailed search 40-->1
        tmscore = TMscore8_search_standard(Variables.xtm, Variables.ytm, k, t, u, simplify_step, score_sum_method, rmsd,
                local_d0_search);
        if (bNormalize)// "-i", to use standard_TMscore, then bNormalize=true,
                        // else bNormalize=false;
            tmscore = tmscore * k / Variables.Lnorm;

        return tmscore;
    }

    // compute the score quickly in three iterations
    public static double get_score_fast(double x[][], double y[][], int x_len, int y_len, int invmap[]) {
        MutableDouble rms = new MutableDouble(0.0);
        double tmscore, tmscore1, tmscore2;
        int i, j, k;

        k = 0;
        for (j = 0; j < y_len; j++) {
            i = invmap[j];
            if (i >= 0) {
                Variables.r1[k][0] = x[i][0];
                Variables.r1[k][1] = x[i][1];
                Variables.r1[k][2] = x[i][2];

                Variables.r2[k][0] = y[j][0];
                Variables.r2[k][1] = y[j][1];
                Variables.r2[k][2] = y[j][2];

                Variables.xtm[k][0] = x[i][0];
                Variables.xtm[k][1] = x[i][1];
                Variables.xtm[k][2] = x[i][2];

                Variables.ytm[k][0] = y[j][0];
                Variables.ytm[k][1] = y[j][1];
                Variables.ytm[k][2] = y[j][2];

                k++;
            } else if (i != -1) {
                Functions.PrintErrorAndQuit("Wrong map!\n");
            }
        }
        Kabsch.execute(Variables.r1, Variables.r2, k, 1, rms, Variables.t, Variables.u);

        // evaluate score
        double di;
        int len = k;
        double dis[] = new double[len];
        double d00 = Variables.d0_search;
        double d002 = d00 * d00;
        double d02 = Variables.d0 * Variables.d0;

        int n_ali = k;
        double xrot[] = new double[3];
        tmscore = 0;
        for (k = 0; k < n_ali; k++) {
            Functions.transform(Variables.t, Variables.u, Variables.xtm[k], xrot);
            di = Functions.dist(xrot, Variables.ytm[k]);
            dis[k] = di;
            tmscore += 1 / (1 + di / d02);
        }

        // second iteration
        double d002t = d002;
        while (true) {
            j = 0;
            for (k = 0; k < n_ali; k++) {
                if (dis[k] <= d002t) {
                    Variables.r1[j][0] = Variables.xtm[k][0];
                    Variables.r1[j][1] = Variables.xtm[k][1];
                    Variables.r1[j][2] = Variables.xtm[k][2];

                    Variables.r2[j][0] = Variables.ytm[k][0];
                    Variables.r2[j][1] = Variables.ytm[k][1];
                    Variables.r2[j][2] = Variables.ytm[k][2];

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
            Kabsch.execute(Variables.r1, Variables.r2, j, 1, rms, Variables.t, Variables.u);
            tmscore1 = 0;
            for (k = 0; k < n_ali; k++) {
                Functions.transform(Variables.t, Variables.u, Variables.xtm[k], xrot);
                di = Functions.dist(xrot, Variables.ytm[k]);
                dis[k] = di;
                tmscore1 += 1 / (1 + di / d02);
            }

            // third iteration
            d002t = d002 + 1;

            while (true) {
                j = 0;
                for (k = 0; k < n_ali; k++) {
                    if (dis[k] <= d002t) {
                        Variables.r1[j][0] = Variables.xtm[k][0];
                        Variables.r1[j][1] = Variables.xtm[k][1];
                        Variables.r1[j][2] = Variables.xtm[k][2];

                        Variables.r2[j][0] = Variables.ytm[k][0];
                        Variables.r2[j][1] = Variables.ytm[k][1];
                        Variables.r2[j][2] = Variables.ytm[k][2];

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
            Kabsch.execute(Variables.r1, Variables.r2, j, 1, rms, Variables.t, Variables.u);
            tmscore2 = 0;
            for (k = 0; k < n_ali; k++) {
                Functions.transform(Variables.t, Variables.u, Variables.xtm[k], xrot);
                di = Functions.dist(xrot, Variables.ytm[k]);
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

    // perform gapless threading to find the best initial alignment
    // input: x, y, x_len, y_len
    // output: y2x0 stores the best alignment: e.g.,
    // y2x0[j]=i means:
    // the jth element in y is aligned to the ith element in x if i>=0
    // the jth element in y is aligned to a gap in x if i==-1
    public static double get_initial(double x[][], double y[][], int x_len, int y_len, int y2x[]) {
        int min_len = Math.min(x_len, y_len);
        if (min_len <= 5)
            Functions.PrintErrorAndQuit("Sequence is too short <=5!\n");

        int min_ali = min_len / 2; // minimum size of considered fragment
        if (min_ali <= 5)
            min_ali = 5;
        int n1, n2;
        n1 = -y_len + min_ali;
        n2 = x_len - min_ali;

        int i, j, k, k_best;
        double tmscore, tmscore_max = -1;

        k_best = n1;
        for (k = n1; k <= n2; k++) {
            // get the map
            for (j = 0; j < y_len; j++) {
                i = j + k;
                if (i >= 0 && i < x_len) {
                    y2x[j] = i;
                } else {
                    y2x[j] = -1;
                }
            }

            // evaluate the map quickly in three iterations
            // this is not real tmscore, it is used to evaluate the goodness of
            // the initial alignment
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

    public static void smooth(int sec[], int len) {
        int i, j;
        // smooth single --x-- => -----
        for (i = 2; i < len - 2; i++) {
            if (sec[i] == 2 || sec[i] == 4) {
                j = sec[i];
                if (sec[i - 2] != j) {
                    if (sec[i - 1] != j) {
                        if (sec[i + 1] != j) {
                            if (sec[i + 2] != j) {
                                sec[i] = 1;
                            }
                        }
                    }
                }
            }
        }

        // smooth double
        // --xx-- => ------

        for (i = 0; i < len - 5; i++) {
            // helix
            if (sec[i] != 2) {
                if (sec[i + 1] != 2) {
                    if (sec[i + 2] == 2) {
                        if (sec[i + 3] == 2) {
                            if (sec[i + 4] != 2) {
                                if (sec[i + 5] != 2) {
                                    sec[i + 2] = 1;
                                    sec[i + 3] = 1;
                                }
                            }
                        }
                    }
                }
            }

            // beta
            if (sec[i] != 4) {
                if (sec[i + 1] != 4) {
                    if (sec[i + 2] == 4) {
                        if (sec[i + 3] == 4) {
                            if (sec[i + 4] != 4) {
                                if (sec[i + 5] != 4) {
                                    sec[i + 2] = 1;
                                    sec[i + 3] = 1;
                                }
                            }
                        }
                    }
                }
            }
        }

        // smooth connect
        for (i = 0; i < len - 2; i++) {
            if (sec[i] == 2) {
                if (sec[i + 1] != 2) {
                    if (sec[i + 2] == 2) {
                        sec[i + 1] = 2;
                    }
                }
            } else if (sec[i] == 4) {
                if (sec[i + 1] != 4) {
                    if (sec[i + 2] == 4) {
                        sec[i + 1] = 4;
                    }
                }
            }
        }

    }

    public static int sec_str(double dis13, double dis14, double dis15, double dis24, double dis25, double dis35) {
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

    // 1->coil, 2->helix, 3->turn, 4->strand
    public static void make_sec(double x[][], int len, int sec[]) {
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

    // get initial alignment from secondary structure alignment
    // input: x, y, x_len, y_len
    // output: y2x stores the best alignment: e.g.,
    // y2x[j]=i means:
    // the jth element in y is aligned to the ith element in x if i>=0
    // the jth element in y is aligned to a gap in x if i==-1
    public static void get_initial_ss(double x[][], double y[][], int x_len, int y_len, int y2x[]) {
        // assign secondary structures
        make_sec(x, x_len, Variables.secx);
        make_sec(y, y_len, Variables.secy);

        double gap_open = -1.0;
        NW.NWDP_TM(Variables.secx, Variables.secy, x_len, y_len, gap_open, y2x);
    }

    // get_initial5 in TMalign
    // get initial alignment of local structure superposition
    // input: x, y, x_len, y_len
    // output: y2x stores the best alignment: e.g.,
    // y2x[j]=i means:
    // the jth element in y is aligned to the ith element in x if i>=0
    // the jth element in y is aligned to a gap in x if i==-1
    public static boolean get_initial5(double x[][], double y[][], int x_len, int y_len, int y2x[]) {
        double GL;
        MutableDouble rmsd = new MutableDouble(0.0);
        double t[] = new double[3];
        double u[][] = new double[3][3];

        double d01 = Variables.d0 + 1.5;
        if (d01 < Variables.D0_MIN)
            d01 = Variables.D0_MIN;
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
                        Variables.r1[k][0] = x[k + i][0];
                        Variables.r1[k][1] = x[k + i][1];
                        Variables.r1[k][2] = x[k + i][2];

                        Variables.r2[k][0] = y[k + j][0];
                        Variables.r2[k][1] = y[k + j][1];
                        Variables.r2[k][2] = y[k + j][2];
                    }

                    // superpose the two structures and rotate it
                    Kabsch.execute(Variables.r1, Variables.r2, n_frag[i_frag], 1, rmsd, t, u);

                    double gap_open = 0.0;
                    NW.NWDP_TM(x, y, x_len, y_len, t, u, d02, gap_open, invmap);
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

    // with invmap(i) calculate score(i,j) using RMSD rotation
    public static void score_matrix_rmsd(double x[][], double y[][], int x_len, int y_len, int y2x[]) {
        double t[] = new double[3];
        double u[][] = new double[3][3];
        MutableDouble rmsd = new MutableDouble(0.0);
        double dij;
        double d01 = Variables.d0 + 1.5;
        if (d01 < Variables.D0_MIN)
            d01 = Variables.D0_MIN;
        double d02 = d01 * d01;

        double xx[] = new double[3];
        int i, k = 0;
        for (int j = 0; j < y_len; j++) {
            i = y2x[j];
            if (i >= 0) {
                Variables.r1[k][0] = x[i][0];
                Variables.r1[k][1] = x[i][1];
                Variables.r1[k][2] = x[i][2];

                Variables.r2[k][0] = y[j][0];
                Variables.r2[k][1] = y[j][1];
                Variables.r2[k][2] = y[j][2];

                k++;
            }
        }
        Kabsch.execute(Variables.r1, Variables.r2, k, 1, rmsd, t, u);
        // do_rotation(x, xt, x_len, t, u);

        for (int ii = 0; ii < x_len; ii++) {
            Functions.transform(t, u, x[ii], xx);
            for (int jj = 0; jj < y_len; jj++) {
                // dij=dist(&xt[ii][0], &y[jj][0]);
                dij = Functions.dist(xx, y[jj]);
                Variables.score[ii + 1][jj + 1] = 1.0 / (1 + dij / d02);
                // cout << ii+1 << " " << jj+1 << " " << score[ii+1][jj+1]<<
                // endl;
            }
        }
    }

    public static void score_matrix_rmsd_sec(double x[][], double y[][], int x_len, int y_len, int y2x[]) {
        double t[] = new double[3];
        double u[][] = new double[3][3];
        MutableDouble rmsd = new MutableDouble(0.0);
        double dij;
        double d01 = Variables.d0 + 1.5;
        if (d01 < Variables.D0_MIN)
            d01 = Variables.D0_MIN;
        double d02 = d01 * d01;

        double xx[] = new double[3];
        int i, k = 0;
        for (int j = 0; j < y_len; j++) {
            i = y2x[j];
            if (i >= 0) {
                Variables.r1[k][0] = x[i][0];
                Variables.r1[k][1] = x[i][1];
                Variables.r1[k][2] = x[i][2];

                Variables.r2[k][0] = y[j][0];
                Variables.r2[k][1] = y[j][1];
                Variables.r2[k][2] = y[j][2];

                k++;
            }
        }
        Kabsch.execute(Variables.r1, Variables.r2, k, 1, rmsd, t, u);

        for (int ii = 0; ii < x_len; ii++) {
            Functions.transform(t, u, x[ii], xx);
            for (int jj = 0; jj < y_len; jj++) {
                dij = Functions.dist(xx, y[jj]);
                if (Variables.secx[ii] == Variables.secy[jj]) {
                    Variables.score[ii + 1][jj + 1] = 1.0 / (1 + dij / d02) + 0.5;
                } else {
                    Variables.score[ii + 1][jj + 1] = 1.0 / (1 + dij / d02);
                }
            }
        }
    }

    // get initial alignment from secondary structure and previous alignments
    // input: x, y, x_len, y_len
    // output: y2x stores the best alignment: e.g.,
    // y2x[j]=i means:
    // the jth element in y is aligned to the ith element in x if i>=0
    // the jth element in y is aligned to a gap in x if i==-1
    public static void get_initial_ssplus(double x[][], double y[][], int x_len, int y_len, int y2x0[], int y2x[]) {

        // create score matrix for DP
        score_matrix_rmsd_sec(x, y, x_len, y_len, y2x0);

        double gap_open = -1.0;
        NW.NWDP_TM(x_len, y_len, gap_open, y2x);
    }

    public static void find_max_frag(double x[][], int len, MutableInteger start_max, MutableInteger end_max) {
        int r_min, fra_min = 4; // minimum fragment for search
        double d;
        int start;
        int Lfr_max = 0, flag;

        r_min = (int) (len * 1.0 / 3.0); // minimum fragment, in case too small
                                            // protein
        if (r_min > fra_min)
            r_min = fra_min;

        int inc = 0;
        double dcu0_cut = Variables.dcu0 * Variables.dcu0;
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
                } else //if (resno[i] == (resno[i - 1] + 1)) // necessary??
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
                double dinc = Math.pow(1.1, (double) inc) * Variables.dcu0;
                dcu_cut = dinc * dinc;
            }
        } // while <;
    }

    // perform fragment gapless threading to find the best initial alignment
    // input: x, y, x_len, y_len
    // output: y2x0 stores the best alignment: e.g.,
    // y2x0[j]=i means:
    // the jth element in y is aligned to the ith element in x if i>=0
    // the jth element in y is aligned to a gap in x if i==-1
    public static double get_initial_fgt(double x[][], double y[][], int x_len, int y_len, int y2x[]) {
        int fra_min = 4; // minimum fragment for search
        int fra_min1 = fra_min - 1; // cutoff for shift, save time

        MutableInteger xstart = new MutableInteger(0);
        MutableInteger ystart = new MutableInteger(0);
        MutableInteger xend = new MutableInteger(0);
        MutableInteger yend = new MutableInteger(0);

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

    // heuristic run of dynamic programing iteratively to find the best
    // alignment
    // input: initial rotation matrix t, u
    // vectors x and y, d0
    // output: best alignment that maximizes the TMscore, will be stored in
    // invmap
    public static double DP_iter(double x[][], double y[][], int x_len, int y_len, double t[], double u[][],
            int invmap0[], int g1, int g2, int iteration_max, double local_d0_search) {
        double gap_open[] = { -0.6, 0 };
        MutableDouble rmsd = new MutableDouble(0.0);
        int invmap[] = new int[y_len + 1];

        int iteration, i, j, k;
        double tmscore, tmscore_max, tmscore_old = 0;
        int score_sum_method = 8, simplify_step = 40;
        tmscore_max = -1;

        // double d01=d0+1.5;
        double d02 = Variables.d0 * Variables.d0;
        for (int g = g1; g < g2; g++) {
            for (iteration = 0; iteration < iteration_max; iteration++) {
                NW.NWDP_TM(x, y, x_len, y_len, t, u, d02, gap_open[g], invmap);

                k = 0;
                for (j = 0; j < y_len; j++) {
                    i = invmap[j];

                    if (i >= 0) // aligned
                    {
                        Variables.xtm[k][0] = x[i][0];
                        Variables.xtm[k][1] = x[i][1];
                        Variables.xtm[k][2] = x[i][2];

                        Variables.ytm[k][0] = y[j][0];
                        Variables.ytm[k][1] = y[j][1];
                        Variables.ytm[k][2] = y[j][2];
                        k++;
                    }
                }

                // tmscore=TMscore8_search(xtm, ytm, k, t, u, simplify_step,
                // score_sum_method, &rmsd);
                tmscore = TMscore8_search(Variables.xtm, Variables.ytm, k, t, u, simplify_step, score_sum_method, rmsd,
                        local_d0_search);

                if (tmscore > tmscore_max) {
                    tmscore_max = tmscore;
                    for (i = 0; i < y_len; i++) {
                        invmap0[i] = invmap[i];
                    }
                }

                if (iteration > 0) {
                    if (Math.abs(tmscore_old - tmscore) < 0.000001) {
                        break;
                    }
                }
                tmscore_old = tmscore;
            } // for iteration

        } // for gapopen

        return tmscore_max;
    }

    public static double standard_TMscore(double x[][], double y[][], int x_len, int y_len, int invmap[], MutableInteger L_ali,
            MutableDouble RMSD) {
        Variables.D0_MIN = 0.5;
        Variables.Lnorm = y_len;
        if (Variables.Lnorm > 21)
            Variables.d0 = (1.24 * Math.pow((Variables.Lnorm * 1.0 - 15), 1.0 / 3) - 1.8);
        else
            Variables.d0 = Variables.D0_MIN;
        if (Variables.d0 < Variables.D0_MIN)
            Variables.d0 = Variables.D0_MIN;
        double d0_input = Variables.d0;// Scaled by seq_min

        double tmscore;// collected alined residues from invmap
        int n_al = 0;
        int i;
        for (int j = 0; j < y_len; j++) {
            i = invmap[j];
            if (i >= 0) {
                Variables.xtm[n_al][0] = x[i][0];
                Variables.xtm[n_al][1] = x[i][1];
                Variables.xtm[n_al][2] = x[i][2];

                Variables.ytm[n_al][0] = y[j][0];
                Variables.ytm[n_al][1] = y[j][1];
                Variables.ytm[n_al][2] = y[j][2];

                Variables.r1[n_al][0] = x[i][0];
                Variables.r1[n_al][1] = x[i][1];
                Variables.r1[n_al][2] = x[i][2];

                Variables.r2[n_al][0] = y[j][0];
                Variables.r2[n_al][1] = y[j][1];
                Variables.r2[n_al][2] = y[j][2];

                n_al++;
            } else if (i != -1) {
                Functions.PrintErrorAndQuit("Wrong map!\n");
            }
        }
        L_ali.setValue(n_al);

        Kabsch.execute(Variables.r1, Variables.r2, n_al, 0, RMSD, Variables.t, Variables.u);
        RMSD.setValue(Math.sqrt(RMSD.getValue() / (1.0 * n_al)));

        int temp_simplify_step = 1;
        int temp_score_sum_method = 0;
        Variables.d0_search = d0_input;
        MutableDouble rms = new MutableDouble(0.0);
        tmscore = TMscore8_search_standard(Variables.xtm, Variables.ytm, n_al, Variables.t, Variables.u,
                temp_simplify_step, temp_score_sum_method, rms, d0_input);
        tmscore = tmscore * n_al / (1.0 * Variables.Lnorm);

        return tmscore;
    }
}
