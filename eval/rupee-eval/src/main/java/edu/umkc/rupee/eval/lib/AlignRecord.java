package edu.umkc.rupee.eval.lib;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.align.model.AFPChain;

public class AlignRecord {

    public String algorithmName;
    public AFPChain afps;
    public Atom[] atoms1;
    public Atom[] atoms2;
}
