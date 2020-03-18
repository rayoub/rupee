/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on Apr 7, 2010
 * Author: Andreas Prlic
 *
 */
package edu.umkc.rupee.bio;

import javax.vecmath.Matrix4d;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.geometry.SuperPositions;

public class AFPChainScorer {

    public static double getTMScore(AFPChain align, Atom[] ca1, Atom[] ca2) throws StructureException {
        return getTMScore(align, ca1, ca2, true);
    }

    public static double getTMScore(AFPChain align, Atom[] ca1, Atom[] ca2, boolean normalizeMin)
            throws StructureException {
        if (align.getNrEQR() == 0)
            return -1;

        // Create new arrays for the subset of atoms in the alignment.
        Atom[] ca1aligned = new Atom[align.getOptLength()];
        Atom[] ca2aligned = new Atom[align.getOptLength()];
        int pos = 0;
        int[] blockLens = align.getOptLen();
        int[][][] optAln = align.getOptAln();
        assert (align.getBlockNum() <= optAln.length);

        for (int block = 0; block < align.getBlockNum(); block++) {
            for (int i = 0; i < blockLens[block]; i++) {
                int pos1 = optAln[block][0][i];
                int pos2 = optAln[block][1][i];
                Atom a1 = ca1[pos1];
                Atom a2 = (Atom) ca2[pos2].clone();

                ca1aligned[pos] = a1;
                ca2aligned[pos] = a2;
                pos++;
            }
        }

        // Superimpose
        Matrix4d trans = SuperPositions.superpose(Calc.atomsToPoints(ca1aligned), Calc.atomsToPoints(ca2aligned));

        Calc.transform(ca2aligned, trans);

        return Calc.getTMScore(ca1aligned, ca2aligned, ca1.length, ca2.length, normalizeMin);
    }
}
