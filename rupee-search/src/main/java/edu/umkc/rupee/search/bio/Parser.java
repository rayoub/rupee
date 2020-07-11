/*
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
 * Created on 16.03.2004
 *
 */
package edu.umkc.rupee.search.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.biojava.nbio.structure.AminoAcidImpl;
import org.biojava.nbio.structure.AtomImpl;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.ChainImpl;
import org.biojava.nbio.structure.Element;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.HetatomImpl;
import org.biojava.nbio.structure.NucleotideImpl;
import org.biojava.nbio.structure.ResidueNumber;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureImpl;
import org.biojava.nbio.structure.StructureTools;

public class Parser {

    private static String CHAIN_NAME = "X";

    private Structure _structure;
    private List<Chain> _model;
    private Chain _chain;
    private Group _lastGroup;

    public Parser() {

        _structure = new StructureImpl();
        _model = new ArrayList<Chain>();
        _chain = new ChainImpl();
        _chain.setId(CHAIN_NAME);
        _chain.setName(CHAIN_NAME);
        _lastGroup = null;
    }

    public Structure parsePdbFile(InputStream stream) throws IOException {
            
        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader buffer = new BufferedReader(reader);

        String line = null;
        int lineNumber = 0;
        while ((line = buffer.readLine()) != null) {

            parseLine(line, lineNumber);
        }

        _model.add(_chain);
        _structure.addModel(_model);

        return _structure;
    }

    /*
     * Record Format:
     *
     * COLUMNS        DATA TYPE       FIELD         DEFINITION
     * ---------------------------------------------------------------------------------
     * 0  -  5        Record name     "ATOM  "
     * 6  - 10        Integer         serial        Atom serial number.
     * 12 - 14        Atom            name          Atom name.
     * 16             Character       altLoc        Alternate location indicator.
     * 17 - 19        Residue name    resName       Residue name.
     * 21             Character       chainID       Chain identifier.
     * 22 - 25        Integer         resSeq        Residue sequence number.
     * 26             AChar           iCode         Code for insertion of residues.
     * 30 - 37        Real(8.3)       x             Orthogonal coordinates for X in Angstroms.
     * 38 - 45        Real(8.3)       y             Orthogonal coordinates for Y in Angstroms.
     * 46 - 53        Real(8.3)       z             Orthogonal coordinates for Z in Angstroms.
     * 54 - 59        Real(6.2)       occupancy     Occupancy.
     * 60 - 65        Real(6.2)       tempFactor    Temperature factor.
     * 72 - 75        LString(4)      segID         Segment identifier, left-justified.
     * 76 - 77        LString(2)      element       Element symbol, right-justified.
     * 78 - 79        LString(2)      charge        Charge on the atom.
     */
    private void parseLine(String line, int lineNumber) {

        // only processing carbon alphas
        String fullname = line.substring(12, 16);
        if (!fullname.equals(" CA ")) {
            return;
        }

        // get residue number and amino code
        String resName = line.substring(17, 20).trim();
        String resNum = line.substring(22, 26).trim();
        Character iCode = line.substring(26, 27).charAt(0);
        if (iCode == ' ')
            iCode = null;
        ResidueNumber residueNumber = new ResidueNumber(CHAIN_NAME, Integer.valueOf(resNum), iCode);
        Character aminoCode1 = StructureTools.get1LetterCode(resName);

        // check if HETATM record
        boolean isHetAtomInFile = false;
        String recordName = line.substring(0, 6).trim();
        if (recordName.equals("HETATM")) {
            if (aminoCode1 != null && aminoCode1.equals(StructureTools.UNKNOWN_GROUP_LABEL))
                aminoCode1 = null;

            isHetAtomInFile = true;
        }

        // create group 
        Group group = getNewGroup(recordName, aminoCode1, resName);
        group.setPDBName(resName);
        group.setResidueNumber(residueNumber);
        group.setHetAtomInFile(isHetAtomInFile);

        if (_lastGroup != null && residueNumber.equals(_lastGroup.getResidueNumber())) {
       
            // ignore altnerate locations
            return;
        }

        // create atom
        AtomImpl atom = new AtomImpl();

        int serial = Integer.parseInt(line.substring(6, 11).trim());
        atom.setPDBserial(serial);
        atom.setName(fullname.trim());

        double x = Double.parseDouble(line.substring(30, 38).trim());
        double y = Double.parseDouble(line.substring(38, 46).trim());
        double z = Double.parseDouble(line.substring(46, 54).trim());

        double[] coords = new double[3];
        coords[0] = x;
        coords[1] = y;
        coords[2] = z;
        atom.setCoords(coords);

     //   atom.setOccupancy(1.0f);
      //  atom.setTempFactor(0.0f);
       
        // since we are only considering carbons 
        Element element = Element.C;
        atom.setElement(element);
       
        // add atom to group 
        group.addAtom(atom);

        // add group to chain
        _chain.addGroup(group);

        // set last group
        _lastGroup = group;
    }

    private Group getNewGroup(String recordName, Character aminoCode1, String aminoCode3) {

        Group group;
        if (aminoCode1 == null || StructureTools.UNKNOWN_GROUP_LABEL == aminoCode1) {
            group = new HetatomImpl();

        } else if (StructureTools.isNucleotide(aminoCode3)) {
            // it is a nucleotide
            NucleotideImpl nu = new NucleotideImpl();
            group = nu;

        } else {
            AminoAcidImpl aa = new AminoAcidImpl();
            aa.setAminoType(aminoCode1);
            group = aa;
        }

        return group;
    }
}
