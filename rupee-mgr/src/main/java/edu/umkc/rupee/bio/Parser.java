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
package edu.umkc.rupee.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.biojava.nbio.structure.AminoAcidImpl;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.AtomImpl;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.ChainImpl;
import org.biojava.nbio.structure.Element;
import org.biojava.nbio.structure.EntityInfo;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.HetatomImpl;
import org.biojava.nbio.structure.NucleotideImpl;
import org.biojava.nbio.structure.ResidueNumber;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureImpl;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.io.EntityFinder;
import org.biojava.nbio.structure.io.mmcif.ChemCompGroupFactory;

public class Parser {

    // constants
    private static final String NEWLINE = System.getProperty("line.separator");
    private static final int MAX_ATOMS = Integer.MAX_VALUE;

    private Structure structure;
    private List<List<Chain>> allModels; 
    private List<Chain> currentModel; 
    private Chain currentChain;
    private Group currentGroup;
    private int atomCount;
    private boolean startOfMolecule;
    private boolean startOfModel;
    private List<EntityInfo> entities;

    public Parser() {

        structure = null;
        allModels = null;
        currentModel = null;
        currentChain = null;
        currentGroup = null;
        atomCount = 0;
        startOfMolecule = true;
        startOfModel = true;
        entities = null;
    }

    /**
     * Handler for ATOM. Record Format:
     *
     * <pre>
     * ATOM      1  N   ASP A  15     110.964  24.941  59.191  1.00 83.44           N
     *
     * COLUMNS        DATA TYPE       FIELD         DEFINITION
     * ---------------------------------------------------------------------------------
     * 1 -  6        Record name     "ATOM  "
     * 7 - 11        Integer         serial        Atom serial number.
     * 13 - 16        Atom            name          Atom name.
     * 17             Character       altLoc        Alternate location indicator.
     * 18 - 20        Residue name    resName       Residue name.
     * 22             Character       chainID       Chain identifier.
     * 23 - 26        Integer         resSeq        Residue sequence number.
     * 27             AChar           iCode         Code for insertion of residues.
     * 31 - 38        Real(8.3)       x             Orthogonal coordinates for X in Angstroms.
     * 39 - 46        Real(8.3)       y             Orthogonal coordinates for Y in Angstroms.
     * 47 - 54        Real(8.3)       z             Orthogonal coordinates for Z in Angstroms.
     * 55 - 60        Real(6.2)       occupancy     Occupancy.
     * 61 - 66        Real(6.2)       tempFactor    Temperature factor.
     * 73 - 76        LString(4)      segID         Segment identifier, left-justified.
     * 77 - 78        LString(2)      element       Element symbol, right-justified.
     * 79 - 80        LString(2)      charge        Charge on the atom.
     * </pre>
     */
    private void pdb_ATOM_Handler(String line) {

        // let's first get the chain name which will serve to identify if we are
        // starting a new molecule
        String chainName = line.substring(21, 22);

        if (currentChain != null && !currentChain.getName().equals(chainName)) {
            // new chain name: another molecule coming
            startOfMolecule = true;
        }

        if (startOfMolecule) {
            // we add last chain if there was one
            if (currentChain != null) {
                currentModel.add(currentChain);
                // let's not forget adding the last group to the finishing chain
                if (currentGroup != null) {
                    currentChain.addGroup(currentGroup);
                }
            }
            // we initialise the new molecule to come
            currentChain = new ChainImpl();
            // note that the chainId (asym id) is set properly later in
            // assignAsymIds
            currentChain.setId(chainName);
            currentChain.setName(chainName);

        }

        if (startOfModel) {
            // we add last model if there was one
            if (currentModel != null) {
                allModels.add(currentModel);
            }
            // we initialise the model to come
            currentModel = new ArrayList<>();
        }

        // let's get the residue number and see if we need to start a new group

        String groupCode3 = line.substring(17, 20).trim();
        String resNum = line.substring(22, 26).trim();
        Character iCode = line.substring(26, 27).charAt(0);
        if (iCode == ' ')
            iCode = null;
        ResidueNumber residueNumber = new ResidueNumber(chainName, Integer.valueOf(resNum), iCode);

        // recordName groupCode3
        // | | resNum
        // | | | iCode
        // | | | | | ||
        // ATOM 1 N ASP A 15 110.964 24.941 59.191 1.00 83.44 N
        // ATOM 1964 N ARG H 221A 5.963 -16.715 27.669 1.00 28.59 N

        Character aminoCode1 = StructureTools.get1LetterCode(groupCode3);

        String recordName = line.substring(0, 6).trim();

        boolean isHetAtomInFile = false;

        if (recordName.equals("HETATM")) {
            // HETATOM RECORDS are treated slightly differently
            // some modified amino acids that we want to treat as amino acids
            // can be found as HETATOM records
            if (aminoCode1 != null && aminoCode1.equals(StructureTools.UNKNOWN_GROUP_LABEL))
                aminoCode1 = null;

            isHetAtomInFile = true;
        }

        if (startOfMolecule) {

            currentGroup = getNewGroup(recordName, aminoCode1, groupCode3);

            currentGroup.setPDBName(groupCode3);
            currentGroup.setResidueNumber(residueNumber);
            currentGroup.setHetAtomInFile(isHetAtomInFile);

        }

        // resetting states
        startOfModel = false;
        startOfMolecule = false;

        Character altLoc = new Character(line.substring(16, 17).charAt(0));
        Group altGroup = null;

        // check if residue number is the same ...
        if (!residueNumber.equals(currentGroup.getResidueNumber())) {

            currentChain.addGroup(currentGroup);
            currentGroup.trimToSize();

            currentGroup = getNewGroup(recordName, aminoCode1, groupCode3);

            currentGroup.setPDBName(groupCode3);
            currentGroup.setResidueNumber(residueNumber);
            currentGroup.setHetAtomInFile(isHetAtomInFile);

        } else {
            // same residueNumber, but altLocs...

            // test altLoc
            if (!altLoc.equals(' ')) {
                altGroup = getCorrectAltLocGroup(altLoc, recordName, aminoCode1, groupCode3);
                if (altGroup.getChain() == null) {
                    // need to set current chain
                    altGroup.setChain(currentChain);
                }

            }
        }

        atomCount++;

        if (atomCount >= MAX_ATOMS) {
            return;
        }

        // 1 2 3 4 5 6
        // 012345678901234567890123456789012345678901234567890123456789
        // ATOM 1 N MET 1 20.154 29.699 5.276 1.0
        // ATOM 112 CA ASP 112 41.017 33.527 28.371 1.00 0.00
        // ATOM 53 CA MET 7 23.772 33.989 -21.600 1.00 0.00 C
        // ATOM 112 CA ASP 112 37.613 26.621 33.571 0 0

        String fullname = line.substring(12, 16);
        if (!fullname.equals(" CA ")) {
            atomCount--;
            return;
        }

        // create new atom
        AtomImpl atom = new AtomImpl();

        int pdbnumber = Integer.parseInt(line.substring(6, 11).trim());
        atom.setPDBserial(pdbnumber);

        atom.setAltLoc(altLoc);
        atom.setName(fullname.trim());

        double x = Double.parseDouble(line.substring(30, 38).trim());
        double y = Double.parseDouble(line.substring(38, 46).trim());
        double z = Double.parseDouble(line.substring(46, 54).trim());

        double[] coords = new double[3];
        coords[0] = x;
        coords[1] = y;
        coords[2] = z;
        atom.setCoords(coords);

        atom.setOccupancy(1.0f);
        atom.setTempFactor(0.0f);
       
        // since we are only considering carbons 
        Element element = Element.C;
        atom.setElement(element);
        
        // see if chain_id is one of the previous chains ...
        if (altGroup != null) {
            altGroup.addAtom(atom);
            altGroup = null;
        } else {
            currentGroup.addAtom(atom);
        }

        // make sure that main group has all atoms
        // GitHub issue: #76
        if (!currentGroup.hasAtom(atom.getName())) {
            currentGroup.addAtom(atom);
        }
    }

    private Group getCorrectAltLocGroup(Character altLoc, String recordName, Character aminoCode1, String groupCode3) {

        // see if we know this altLoc already;
        List<Atom> atoms = currentGroup.getAtoms();
        if (atoms.size() > 0) {
            Atom a1 = atoms.get(0);
            // we are just adding atoms to the current group
            // probably there is a second group following later...
            if (a1.getAltLoc().equals(altLoc)) {

                return currentGroup;
            }
        }

        List<Group> altLocs = currentGroup.getAltLocs();
        for (Group altLocG : altLocs) {
            atoms = altLocG.getAtoms();
            if (atoms.size() > 0) {
                for (Atom a1 : atoms) {
                    if (a1.getAltLoc().equals(altLoc)) {

                        return altLocG;
                    }
                }
            }
        }

        // no matching altLoc group found.
        // build it up.

        if (groupCode3.equals(currentGroup.getPDBName())) {
            if (currentGroup.getAtoms().size() == 0) {
                // System.out.println("current group is empty " + current_group
                // + " " + altLoc);
                return currentGroup;
            }
            // System.out.println("cloning current group " + current_group + " "
            // + current_group.getAtoms().get(0).getAltLoc() + " altLoc " +
            // altLoc);
            Group altLocG = (Group) currentGroup.clone();
            // drop atoms from cloned group...
            // https://redmine.open-bio.org/issues/3307
            altLocG.setAtoms(new ArrayList<Atom>());
            altLocG.getAltLocs().clear();
            currentGroup.addAltLoc(altLocG);
            return altLocG;
        }

        // System.out.println("new group " + recordName + " " + aminoCode1 + " "
        // +groupCode3);
        Group altLocG = getNewGroup(recordName, aminoCode1, groupCode3);

        altLocG.setPDBName(groupCode3);

        altLocG.setResidueNumber(currentGroup.getResidueNumber());
        currentGroup.addAltLoc(altLocG);
        return altLocG;
    }

    private Group getNewGroup(String recordName, Character aminoCode1, String aminoCode3) {

        Group g = ChemCompGroupFactory.getGroupFromChemCompDictionary(aminoCode3);
        if (g != null && !g.getChemComp().isEmpty())
            return g;

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

    private void pdb_TER_Handler() {
        startOfMolecule = true;
    }

    public Structure parsePDBFile(InputStream inStream) throws IOException {

        BufferedReader buf = getBufferedReader(inStream);
        return parsePDBFile(buf);
    }

    private BufferedReader getBufferedReader(InputStream inStream) throws IOException {

        BufferedReader buf;
        if (inStream == null) {
            throw new IOException("input stream is null!");
        }
        buf = new BufferedReader(new InputStreamReader(inStream));
        return buf;
    }

    public Structure parsePDBFile(BufferedReader buf) throws IOException {

        // reset
        structure = new StructureImpl();
        allModels = new ArrayList<>();
        currentModel = null;
        currentChain = null;
        currentGroup = null;
        atomCount = 0;
        startOfMolecule = true;
        startOfModel = true;
        entities = new ArrayList<EntityInfo>();

        String line = null;
        while ((line = buf.readLine()) != null) {

            // ignore empty lines
            if (line.equals("") || (line.equals(NEWLINE))) {
                continue;
            }

            // ignore short TER and END lines
            if (line.startsWith("END")) {
                continue;
            }

            if (line.length() < 6 && !line.startsWith("TER")) {
                continue;
            }

            String recordName = null;
            if (line.length() < 6)
                recordName = line.trim();
            else
                recordName = line.substring(0, 6).trim();

            try {
                if (recordName.equals("ATOM"))
                    pdb_ATOM_Handler(line);
                else if (recordName.equals("HETATM"))
                    pdb_ATOM_Handler(line);
                else if (recordName.equals("TER"))
                    pdb_TER_Handler();
            } catch (StringIndexOutOfBoundsException | NullPointerException ex) {
            }
        }

        triggerEndFileChecks();

        // now correct the alternate location group
        StructureTools.cleanUpAltLocs(structure);

        return structure;
    }

    private void triggerEndFileChecks() {

        // we need to add the last chain and model, checking for nulls (e.g. the
        // file could be completely empty of ATOM lines)
        if (currentChain != null && currentGroup != null) {
            currentChain.addGroup(currentGroup);
        }
        if (currentModel != null && currentChain != null) {
            currentModel.add(currentChain);
        }
        if (currentModel != null) {
            allModels.add(currentModel);
        }

        // reordering chains following the mmcif model and assigning entities
        assignChainsAndEntities();
        structure.setEntityInfos(entities);
    }

    /**
     * Split the given chain (containing non-polymer groups and water groups
     * only) into individual chains per non-polymer group and individual chains
     * per contiguous sets of water groups.
     *
     * @param chain
     * @return a list of lists of size 2: first list is the split non-poly
     *         chains, second list is the split water chains
     */
    private static List<List<Chain>> splitNonPolyChain(Chain chain) {
        List<Chain> splitNonPolys = new ArrayList<>();
        List<Chain> waterChains = new ArrayList<>();

        Chain split = null;
        boolean previousGroupIsWater = false;

        for (Group g : chain.getAtomGroups()) {

            if (!previousGroupIsWater) {
                // add last one if there's one
                if (split != null) {
                    splitNonPolys.add(split);
                }
                split = new ChainImpl();
                split.setName(chain.getName());
            } else if (!g.isWater()) {
                // previous group is water and this group is not water: we
                // change from a water chain to a non-poly
                // we'll need to add now the water chain to the list of water
                // chains
                waterChains.add(split);
                split = new ChainImpl();
                split.setName(chain.getName());
            }

            if (g.isWater()) {
                previousGroupIsWater = true;
            } else {
                previousGroupIsWater = false;

            }

            // this should include alt locs (referenced from the main group)
            split.addGroup(g);

        }

        // adding the last split chain: either to water or non-poly depending on
        // what was the last seen group
        if (split != null) {
            if (previousGroupIsWater)
                waterChains.add(split);
            else
                splitNonPolys.add(split);
        }

        List<List<Chain>> all = new ArrayList<>(2);
        all.add(splitNonPolys);
        all.add(waterChains);

        return all;
    }

    /**
     * Assign asym ids following the rules used by the PDB to assign asym ids in
     * mmCIF files
     *
     * @param polys
     * @param nonPolys
     * @param waters
     */
    private void assignAsymIds(List<List<Chain>> polys, List<List<Chain>> nonPolys, List<List<Chain>> waters) {

        for (int i = 0; i < polys.size(); i++) {
            String asymId = "A";

            for (Chain poly : polys.get(i)) {
                poly.setId(asymId);
                asymId = getNextAsymId(asymId);
            }
            for (Chain nonPoly : nonPolys.get(i)) {
                nonPoly.setId(asymId);
                asymId = getNextAsymId(asymId);
            }
            for (Chain water : waters.get(i)) {
                water.setId(asymId);
                asymId = getNextAsymId(asymId);
            }
        }
    }

    /**
     * Gets the next asym id given an asymId, according to the convention
     * followed by mmCIF files produced by the PDB i.e.:
     * A,B,...,Z,AA,BA,CA,...,ZA,AB,BB,CB,...,ZB,.......,ZZ,AAA,BAA,CAA,...
     *
     * @param asymId
     * @return
     */
    private String getNextAsymId(String asymId) {
        if (asymId.length() == 1) {
            if (!asymId.equals("Z")) {
                return Character.toString(getNextChar(asymId.charAt(0)));
            } else {
                return "AA";
            }
        } else if (asymId.length() == 2) {
            if (asymId.equals("ZZ")) {
                return "AAA";
            }
            char[] c = new char[2];
            asymId.getChars(0, 2, c, 0);
            c[0] = getNextChar(c[0]);
            if (c[0] == 'A') {
                c[1] = getNextChar(c[1]);
            }
            return new String(c);
        } else if (asymId.length() == 3) {
            char[] c = new char[3];
            asymId.getChars(0, 3, c, 0);
            c[0] = getNextChar(c[0]);
            if (c[0] == 'A') {
                c[1] = getNextChar(c[1]);
                if (c[1] == 'A') {
                    c[2] = getNextChar(c[2]);
                }
            }
            return new String(c);
        }
        return null;
    }

    private char getNextChar(char c) {
        if (c != 'Z') {
            return ((char) (c + 1));
        } else {
            return 'A';
        }
    }

    /**
     * Here we assign chains following the mmCIF data model: one chain per
     * polymer, one chain per non-polymer group and several water chains.
     * <p>
     * Subsequently we assign entities for them: either from those read from
     * COMPOUND records or from those found heuristically through
     * {@link EntityFinder}
     *
     */
    private void assignChainsAndEntities() {

        List<List<Chain>> polyModels = new ArrayList<>();
        List<List<Chain>> nonPolyModels = new ArrayList<>();
        List<List<Chain>> waterModels = new ArrayList<>();

        for (List<Chain> model : allModels) {

            List<Chain> polyChains = new ArrayList<>();
            List<Chain> nonPolyChains = new ArrayList<>();
            List<Chain> waterChains = new ArrayList<>();

            polyModels.add(polyChains);
            nonPolyModels.add(nonPolyChains);
            waterModels.add(waterChains);

            for (Chain c : model) {

                // we only have entities for polymeric chains, all others are
                // ignored for assigning entities
                if (c.isWaterOnly()) {
                    waterChains.add(c);

                } else if (c.isPureNonPolymer()) {
                    nonPolyChains.add(c);

                } else {
                    polyChains.add(c);
                }
            }
        }

        List<List<Chain>> splitNonPolyModels = new ArrayList<>();
        for (int i = 0; i < nonPolyModels.size(); i++) {
            List<Chain> nonPolyModel = nonPolyModels.get(i);
            List<Chain> waterModel = waterModels.get(i);

            List<Chain> splitNonPolys = new ArrayList<>();
            splitNonPolyModels.add(splitNonPolys);

            for (Chain nonPoly : nonPolyModel) {
                List<List<Chain>> splits = splitNonPolyChain(nonPoly);
                splitNonPolys.addAll(splits.get(0));
                waterModel.addAll(splits.get(1));
            }
        }

        // now we have all chains as in mmcif, let's assign ids following the
        // mmcif rules
        assignAsymIds(polyModels, splitNonPolyModels, waterModels);

        // find entities heuristically with EntityFinder
        entities = EntityFinder.findPolyEntities(polyModels);

        // now we assign entities to the nonpoly and water chains
        EntityFinder.createPurelyNonPolyEntities(splitNonPolyModels, waterModels, entities);

        // in some rare cases purely non-polymer or purely water chain are
        // present in pdb files
        // see https://github.com/biojava/biojava/pull/394
        // these case should be covered by the above

        // now that we have entities in chains we add the chains to the
        // structure

        for (int i = 0; i < allModels.size(); i++) {
            List<Chain> model = new ArrayList<>();
            model.addAll(polyModels.get(i));
            model.addAll(splitNonPolyModels.get(i));
            model.addAll(waterModels.get(i));
            structure.addModel(model);
        }
    }
}
