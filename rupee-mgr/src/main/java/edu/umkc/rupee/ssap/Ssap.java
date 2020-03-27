package edu.umkc.rupee.ssap;

import java.util.List;
import java.util.stream.Collectors;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;

// TODO:
// 1. need to figure out representation of alignment

public class Ssap {

    // constants
    private static double FINAL_SCORE_SCALING = 1000.0;
    private static double RESIDUE_A_VALUE = 500.0;
    private static double RESIDUE_B_VALUE = 10.0;
    private static double GAP_PENALTY = RESIDUE_A_VALUE / RESIDUE_B_VALUE;
    private static int NUM_EXCLUDED = 5;
    private static double INTEGER_SCALING = 10.0;
    private static double DIST_CUTOFF = 10.0;

    // natural log
    private static double MAX_LOG = Math.log((RESIDUE_A_VALUE / RESIDUE_B_VALUE) * FINAL_SCORE_SCALING);

    // structure privates
    private int _alen, _blen;
    private Chain _achain;
    private Chain _bchain;
    private List<Group> _agroups;
    private List<Group> _bgroups;
    private List<Atom> _aatoms;
    private List<Atom> _batoms;

    public Ssap(Structure astruct, Structure bstruct) {

        // get first chain in each structure
        _achain = astruct.getChains().get(0);
        _bchain = bstruct.getChains().get(0);

        // get groups of atoms per residue
        _agroups = _achain.getAtomGroups().stream().filter(g -> !g.isHetAtomInFile() && g.hasAtom("CA")).collect(Collectors.toList());
        _bgroups = _bchain.getAtomGroups().stream().filter(g -> !g.isHetAtomInFile() && g.hasAtom("CA")).collect(Collectors.toList());

        // get carbon alpha atoms per residue
        _aatoms = _agroups.stream().map(g -> g.getAtom("CA")).collect(Collectors.toList());
        _batoms = _bgroups.stream().map(g -> g.getAtom("CA")).collect(Collectors.toList());

        // get number of residues
        _alen = _aatoms.size();
        _blen = _batoms.size();
    }

    public double getScore(Alignment alignment) {

        /* NOTES:
            since 0 is used as an empty position the positions are offset by 1 with respect to the actual residues. 
            however, the indices for the alignment are not.
            Offset this and offset that was copied over and I chose not to change to avoid indexing errors.
        */

        // score matrix
        int maxAlignLen = _alen + _blen + 10;
        double[][] scoreMatrix = new double[maxAlignLen][maxAlignLen];

	    // distance calculations
        for (int i = 0; i < alignment.getLength(); i++) {

            if (alignment.hasBothPositions(i)) {

                int iaPos = alignment.getAPositionOffset1(i);
                int ibPos = alignment.getBPositionOffset1(i);

                for (int j = 0; j < alignment.getLength(); j++) {

                    if (alignment.hasBothPositions(j)) {

                        int jaPos = alignment.getAPositionOffset1(j);
                        int jbPos = alignment.getBPositionOffset1(j);

                        boolean comparable = isPairComparableOffset1(iaPos, ibPos, jaPos, jbPos);

                        if (comparable) {
                           
                            double[] fromAtomA = _aatoms.get(iaPos - 1).getCoords();
                            double[] fromAtomB = _batoms.get(ibPos - 1).getCoords();
                            double[] toAtomA = _aatoms.get(jaPos - 1).getCoords();
                            double[] toAtomB = _batoms.get(jbPos - 1).getCoords();

                            double ax = (toAtomA[0] - fromAtomA[0]) * INTEGER_SCALING;
                            double ay = (toAtomA[1] - fromAtomA[1]) * INTEGER_SCALING;
                            double az = (toAtomA[2] - fromAtomA[2]) * INTEGER_SCALING;
                            
                            double bx = (toAtomB[0] - fromAtomB[0]) * INTEGER_SCALING;
                            double by = (toAtomB[1] - fromAtomB[1]) * INTEGER_SCALING;
                            double bz = (toAtomB[2] - fromAtomB[2]) * INTEGER_SCALING;

                            double sumOfSquares =  Math.pow(ax - bx, 2) + Math.pow(ay - by, 2) + Math.pow(az - bz, 2);

                            double scaledA = RESIDUE_A_VALUE * INTEGER_SCALING * INTEGER_SCALING;
                            double scaledB = RESIDUE_B_VALUE * INTEGER_SCALING * INTEGER_SCALING;

                            double score = 0.0;
                            if (sumOfSquares <= DIST_CUTOFF * INTEGER_SCALING * INTEGER_SCALING) {
                                score = scaledA / (sumOfSquares + scaledB);
                            }

                            scoreMatrix[jbPos][jaPos] = score;
                        }
                    }
                }
            }
        }
	    
        boolean isFirstWithBothPos = true;
        boolean prevHadBothPos = false;
        int aPosPrev = 0;
        int bPosPrev = 0;
        double maxScore = 0;

        // accumulate score 
        for (int i = 0; i < alignment.getLength(); i++) {

            boolean hasBothPos = alignment.hasBothPositions(i);
            if (hasBothPos) {

                int aPos = alignment.getAPositionOffset1(i);
                int bPos = alignment.getBPositionOffset1(i);

                double gap = 0;
                if (!isFirstWithBothPos) {
                    if (!prevHadBothPos || (aPos != aPosPrev + 1) || (bPos != bPosPrev + 1)) {
                        gap = GAP_PENALTY;
                    }
                }
                maxScore = scoreMatrix[bPos][aPos] - gap;
                isFirstWithBothPos = false;
            }

            prevHadBothPos = hasBothPos;
            if (prevHadBothPos) {
                aPosPrev = alignment.getAPositionOffset1(i);
                bPosPrev = alignment.getBPositionOffset1(i);
            }
        }

        // nonnegative
        maxScore = Math.max(maxScore, 0.0);

        // normalize
        int maxLen = Math.max(_alen, _blen);
        int numComparable = (maxLen - NUM_EXCLUDED) * (maxLen - NUM_EXCLUDED - 1);
        if (maxScore != 0.0 && numComparable > 0) {

            double scaledScore = (maxScore * FINAL_SCORE_SCALING) / numComparable;
            double finalScore = (100.0 * Math.log(scaledScore)) / MAX_LOG;
            maxScore = finalScore;
        }

        return maxScore;
    }

    /* STATIC HELPER FUNCTIONS COPIED OVER AND NOT DECOUPLED TO THE NTH DEGREE */

    private static boolean isPairComparableOffset1(int fromIndexA, int fromIndexB, int toIndexA, int toIndexB) {

		if (!isPairNotExcluded(fromIndexA, toIndexA) || !isPairNotExcluded(fromIndexB, toIndexB)) {
			return false;
		}
        else {
            return true;
        }
    }

    private static boolean isPairNotExcluded(int index1, int index2) {

        int max = Math.max(index1, index2);
        int min = Math.min(index1, index2);
        int diff = max - min; 
        
        return diff > 5;
    }
}




