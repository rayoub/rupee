package edu.umkc.rupee.search.lib;


public class Residue {

    private String pdbId;
    private String chainName;
    private int atomNumber;
    private int residueNumber;
    private String insertCode;
    private String residueCode;
    private String ss8;
    private String ss3;
    private float x;
    private float y;
    private float z;
    private float nX;
    private float nY;
    private float nZ;
    private double phi;
    private double psi;
    private int descriptor;
    private int runFactor;
    private int gram;
    private boolean breakBefore;
    private boolean breakAfter;

    public String getPdbId() {
        return pdbId;
    }

    public void setPdbId(String pdbId) {
        this.pdbId = pdbId;
    }
    
    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public int getAtomNumber() {
        return atomNumber;
    }

    public void setAtomNumber(int atomNumber) {
        this.atomNumber = atomNumber;
    }

    public int getResidueNumber() {
        return residueNumber;
    }

    public void setResidueNumber(int residueNumber) {
        this.residueNumber = residueNumber;
    }

    public String getInsertCode() {
        return insertCode;
    }

    public void setInsertCode(String insertCode) {
        this.insertCode = insertCode;
    }
    
    public String getResidueCode() {
        return residueCode;
    }

    public void setResidueCode(String residueCode) {
        this.residueCode = residueCode;
    }

    public String getSs8() {
        return ss8;
    }

    public void setSs8(String ss8) {
        this.ss8 = ss8;
    }

    public String getSs3() {
        return ss3;
    }

    public void setSs3(String ss3) {
        this.ss3 = ss3;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getNX() {
        return nX;
    }

    public void setNX(float nX) {
        this.nX = nX;
    }

    public float getNY() {
        return nY;
    }

    public void setNY(float nY) {
        this.nY = nY;
    }

    public float getNZ() {
        return nZ;
    }

    public void setNZ(float nZ) {
        this.nZ = nZ;
    }

    public double getPhi() {
        return phi;
    }

    public void setPhi(double phi) {
        this.phi = phi;
    }

    public double getPsi() {
        return psi;
    }

    public void setPsi(double psi) {
        this.psi = psi;
    }

    public int getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(int region) {
        this.descriptor = region;
    }

    public int getRunFactor() {
        return runFactor;
    }

    public void setRunFactor(int runFactor) {
        this.runFactor = runFactor;
    }

    public int getGram() {
        return gram;
    }

    public void setGram(int gram) {
        this.gram = gram;
    }

    public boolean getBreakBefore() {
        return breakBefore;
    }

    public void setBreakBefore(boolean breakBefore) {
        this.breakBefore = breakBefore;
    }

    public boolean getBreakAfter() {
        return breakAfter;
    }

    public void setBreakAfter(boolean breakAfter) {
        this.breakAfter = breakAfter;
    }

    public boolean isHelix() {
        return ss3.equals("Helix");
    }
    
    public boolean isStrand() {
        return ss3.equals("Strand");
    }
}

