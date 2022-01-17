package edu.umkc.rupee.tm;

import java.util.List;

import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;

public class TmResult {

    // structures
    private String _xname;
    private String _yname;
    private Structure _xstruct;
    private Structure _ystruct;
    private List<Group> _xgroups;
    private List<Group> _ygroups;

    // alignment
    private int _xlen, _ylen;                       // length of proteins
    private double _xa[][], _ya[][];                // for input coordinates
    private double _xt[][];                         // for saving the superposition coords of xa or xtm
    private char _seqx[], _seqy[];                  // for amino acid sequence
    private int _m1[], _m2[];
    private double _t[];                            // Kabsch translation vector and rotation matrix
    private double _u[][];

    // scoring
    private int _alignlen;
    private double _tmq;
    private double _tmt;
    private double _tmavg;
    private double _rmsd;

    public String get_xname() {
        return _xname;
    }

    public void set_xname(String xname) {
        this._xname = xname;
    }

    public String get_yname() {
        return _yname;
    }

    public void set_yname(String yname) {
        this._yname = yname;
    }

    public Structure get_xstruct() {
        return _xstruct;
    }

    public void set_xstruct(Structure xstruct) {
        this._xstruct = xstruct;
    }

    public Structure get_ystruct() {
        return _ystruct;
    }

    public void set_ystruct(Structure ystruct) {
        this._ystruct = ystruct;
    }

    public List<Group> get_xgroups() {
        return _xgroups;
    }

    public void set_xgroups(List<Group> _xgroups) {
        this._xgroups = _xgroups;
    }

    public List<Group> get_ygroups() {
        return _ygroups;
    }

    public void set_ygroups(List<Group> _ygroups) {
        this._ygroups = _ygroups;
    }

    public int get_xlen() {
        return _xlen;
    }

    public void set_xlen(int xlen) {
        this._xlen = xlen;
    }

    public int get_ylen() {
        return _ylen;
    }

    public void set_ylen(int ylen) {
        this._ylen = ylen;
    }

    public double[][] get_xa() {
        return _xa;
    }

    public void set_xa(double[][] xa) {
        this._xa = xa;
    }

    public double[][] get_ya() {
        return _ya;
    }

    public void set_ya(double[][] ya) {
        this._ya = ya;
    }

    public double[][] get_xt() {
        return _xt;
    }

    public void set_xt(double[][] xt) {
        this._xt = xt;
    }

    public char[] get_seqx() {
        return _seqx;
    }

    public void set_seqx(char[] seqx) {
        this._seqx = seqx;
    }

    public char[] get_seqy() {
        return _seqy;
    }

    public void set_seqy(char[] seqy) {
        this._seqy = seqy;
    }

    public int[] get_m1() {
        return _m1;
    }

    public void set_m1(int[] m1) {
        this._m1 = m1;
    }

    public int[] get_m2() {
        return _m2;
    }

    public void set_m2(int[] m2) {
        this._m2 = m2;
    }

    public double[] get_t() {
        return _t;
    }

    public void set_t(double[] t) {
        this._t = t;
    }

    public double[][] get_u() {
        return _u;
    }

    public void set_u(double[][] u) {
        this._u = u;
    }

    public int get_alignlen() {
        return _alignlen;
    }

    public void set_alignlen(int alignlen) {
        this._alignlen = alignlen;
    }

    public double get_tmq() {
        return _tmq;
    }

    public void set_tmq(double tmq) {
        this._tmq = tmq;
    }

    public double get_tmt() {
        return _tmt;
    }

    public void set_tmt(double tmt) {
        this._tmt = tmt;
    }

    public double get_tmavg() {
        return _tmavg;
    }

    public void set_tmavg(double tmavg) {
        this._tmavg = tmavg;
    }

    public double get_rmsd() {
        return _rmsd;
    }

    public void set_rmsd(double rmsd) {
        this._rmsd = rmsd;
    }
}
