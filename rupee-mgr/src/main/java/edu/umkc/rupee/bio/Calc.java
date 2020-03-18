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

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.StructureException;

public class Calc {

    /**
     * Convert an array of atoms into an array of vecmath points
     *
     * @param atoms list of atoms
     * @return list of Point3ds storing the x,y,z coordinates of each atom
     */
    public static Point3d[] atomsToPoints(Atom[] atoms) {
        Point3d[] points = new Point3d[atoms.length];
        for (int i = 0; i < atoms.length; i++) {
            points[i] = atoms[i].getCoordsAsPoint3d();
        }
        return points;
    }

    /**
         * Transform an array of atoms at once. The transformation Matrix must be a
         * post-multiplication Matrix.
         *
         * @param ca
         *            array of Atoms to shift
         * @param t
         *            transformation Matrix4d
         */
        public static void transform(Atom[] ca, Matrix4d t) {
                for (Atom atom : ca)
                        Calc.transform(atom, t);
        }

        /**
         * Transforms an atom object, given a Matrix4d (i.e. the vecmath library
         * double-precision 4x4 rotation+translation matrix). The transformation
         * Matrix must be a post-multiplication Matrix.
         *
         * @param atom
         * @param m
         */
        public static final void transform(Atom atom, Matrix4d m) {

                Point3d p = new Point3d(atom.getX(), atom.getY(), atom.getZ());
                m.transform(p);

                atom.setX(p.x);
                atom.setY(p.y);
                atom.setZ(p.z);
        }

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
     * @param normalizeAvg Whether to normalize by the
     *                     <strong>average</strong>-length of structures
     *
     * @return The TM-Score
     * @throws StructureException
     */
    public static double getTMScore(Atom[] atomSet1, Atom[] atomSet2, int len1, int len2, boolean normalizeAvg)
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

        double Lnorm;
        if (normalizeAvg) {
            Lnorm = ((double)len1 + len2) / 2.0;
        } else {
            Lnorm = len1; // default to query protein 
        }

        int Laln = atomSet1.length;

        double d0 = 1.24 * Math.cbrt(Lnorm - 15.0) - 1.8;
        double d0sq = d0 * d0;

        double sum = 0;
        for (int i = 0; i < Laln; i++) {
            double d = Calc.getDistance(atomSet1[i], atomSet2[i]);
            sum += 1. / (1 + d * d / d0sq);
        }

        return sum / Lnorm;
    }
}
