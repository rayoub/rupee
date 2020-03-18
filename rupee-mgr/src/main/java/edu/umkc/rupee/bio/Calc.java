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
 * Created on 08.05.2004
 *
 */
package edu.umkc.rupee.bio;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.StructureException;

public class Calc {

    /**
     * calculate distance between two atoms.
     *
     * @param a an Atom object
     * @param b an Atom object
     * @return a double
     */
    public static final double getDistance(Atom a, Atom b) {
        double x = a.getX() - b.getX();
        double y = a.getY() - b.getY();
        double z = a.getZ() - b.getZ();

        double s = x * x + y * y + z * z;

        return Math.sqrt(s);
    }

    /**
     * Calculate the TM-Score for the superposition.
     *
     * Atom sets must be pre-rotated.
     *
     * <p>
     * Citation:<br/>
     * <i>Zhang Y and Skolnick J (2004). "Scoring function for automated assessment
     * of protein structure template quality". Proteins 57: 702 - 710.</i>
     *
     * @param atomSet1     atom array 1
     * @param atomSet2     atom array 2
     * @param len1         The full length of the protein supplying atomSet1
     * @param len2         The full length of the protein supplying atomSet2
     * @param normalizeMin Whether to normalize by the
     *                     <strong>minimum</strong>-length structure, that is,
     *                     {@code min\{len1,len2\}}. If false, normalized by the
     *                     {@code max\{len1,len2\}}).
     *
     * @return The TM-Score
     * @throws StructureException
     */
    public static double getTMScore(Atom[] atomSet1, Atom[] atomSet2, int len1, int len2, boolean normalizeMin)
            throws StructureException {
        if (atomSet1.length != atomSet2.length) {
            throw new StructureException("The two atom sets are not of same length!");
        }
        if (atomSet1.length > len1) {
            throw new StructureException("len1 must be greater or equal to the alignment length!");
        }
        if (atomSet2.length > len2) {
            throw new StructureException("len2 must be greater or equal to the alignment length!");
        }

        int Lnorm;
        if (normalizeMin) {
            Lnorm = Math.min(len1, len2);
        } else {
            Lnorm = Math.max(len1, len2);
        }

        int Laln = atomSet1.length;

        double d0 = 1.24 * Math.cbrt(Lnorm - 15.) - 1.8;
        double d0sq = d0 * d0;

        double sum = 0;
        for (int i = 0; i < Laln; i++) {
            double d = Calc.getDistance(atomSet1[i], atomSet2[i]);
            sum += 1. / (1 + d * d / d0sq);
        }

        return sum / Lnorm;
    }
}
