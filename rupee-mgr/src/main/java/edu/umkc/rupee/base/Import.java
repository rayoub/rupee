package edu.umkc.rupee.base;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

import org.biojava.nbio.structure.AminoAcid;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.biojava.nbio.structure.secstruc.SecStrucCalc;
import org.biojava.nbio.structure.secstruc.SecStrucInfo;
import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.lib.Constants;
import edu.umkc.rupee.lib.Db;
import edu.umkc.rupee.lib.DbTypeCriteria;
import edu.umkc.rupee.lib.Log;
import edu.umkc.rupee.lib.Residue;

public abstract class Import {
    
    // *********************************************************************
    // Abstract Methods 
    // *********************************************************************
    
    public abstract DbTypeCriteria getDbType();

    // *********************************************************************
    // Instance Methods 
    // *********************************************************************

    public void importGrams() {

        IntStream.range(0, Constants.SPLIT_COUNT)
            .boxed()
            .parallel()
            .forEach(splitIndex -> importGramsSplit(splitIndex));
    }

    private void importGramsSplit(int splitIndex) {

        int processed = 0;

        String fileName = "";

        PGSimpleDataSource ds = Db.getDataSource();

        try {

            Connection conn = ds.getConnection();
            conn.setAutoCommit(true);

            // *** get split

            DbTypeCriteria dbType = getDbType();

            System.out.println("Split: " + splitIndex + ", Getting " + dbType.getDescription() + " Ids to Import.");
                
            PreparedStatement stmt = conn.prepareCall("SELECT * FROM get_" + dbType.getDescription().toLowerCase() + "_split(?,?);");
            stmt.setInt(1, splitIndex);
            stmt.setInt(2, Constants.SPLIT_COUNT);
            
            ResultSet rs = stmt.executeQuery();

            System.out.println("Split: " + splitIndex + ", Got " + dbType.getDescription() + " Ids.");

            // *** iterate split
            
            List<Log> logs = new ArrayList<>();
            String message = "Failed to import: %s";

            while (rs.next()) {
               
                String dbId = "";
                String pdbId = "";

                try {
               
                    dbId = rs.getString("db_id");
                    pdbId = rs.getString("pdb_id");
                    fileName = dbType.getImportPath() + dbId + ".pdb.gz";

                    if (Files.notExists(Paths.get(fileName))) {
                        System.out.println("File Not Found: " + fileName);
                        continue;
                    }

                    InputStream inputStream = new FileInputStream(fileName);
                    GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);

                    PDBFileReader reader = new PDBFileReader();
                    reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);
                    reader.getStructure(gzipInputStream);

                    Structure structure = reader.getStructure(fileName);
                    structure.setPDBCode(pdbId);
                
                    Integer[] grams = parseStructure(structure);

                    saveGrams(dbId, grams, conn);

                } catch (Exception e) {
                    logs.add(new Log(Level.SEVERE, e.getClass().getName(), String.format(message, pdbId)));
                    Logger.getLogger(Import.class.getName()).log(Level.SEVERE, pdbId, e);
                }
                
                // output
                processed += 1;
                if (processed % Constants.PROCESSED_INCREMENT == 0) {
                    System.out.println("Split: " + splitIndex + ", Processed: "
                            + (Constants.PROCESSED_INCREMENT * (processed / Constants.PROCESSED_INCREMENT)));
                }
            }

            if (!logs.isEmpty()) {
                Db.saveLogs(logs, conn);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            Logger.getLogger(Import.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void saveGrams(String dbId, Integer[] grams, Connection conn) throws SQLException {
        
        PreparedStatement updt = conn.prepareStatement("SELECT insert_" + getDbType().getDescription().toLowerCase() + "_grams(?, ?);");
        updt.setString(1, dbId);
        updt.setArray(2, conn.createArrayOf("INTEGER", grams));
        updt.execute();
        updt.close();
    }

    // *********************************************************************
    // Static Methods 
    // *********************************************************************

    public static Integer[] parseStructure(Structure structure) {

        List<Integer> grams = new ArrayList<>();

        SecStrucCalc ssCalc = new SecStrucCalc();
            
        // assign secondary structure
        try {
            ssCalc.calculate(structure, true);
        } catch (StructureException e) {
            // do nothing
        }

        // iterate chains
        List<Chain> chains = structure.getChains();
        for(Chain chain : chains) {
        
            // *** gather residues
            
            List<Residue> residues = new ArrayList<>();

            // iterate residues (i.e. groups of atoms)
            List<Group> groups = chain.getAtomGroups();
            for (int i = 1; i < groups.size() - 1; i++) {

                Group g1 = groups.get(i - 1);
                Group g2 = groups.get(i);
                Group g3 = groups.get(i + 1);
               
                String residueCode = g2.getChemComp().getOne_letter_code();

                // we need the carbon alpha
                if (!g2.hasAtom("CA")) {
                    continue;
                }
                
                // get secondary structure assignment
                String ssa = "C";
                Object obj = g2.getProperty(Group.SEC_STRUC);
                if (obj instanceof SecStrucInfo) {
                   SecStrucInfo info = (SecStrucInfo)obj;
                   ssa = String.valueOf(info.getType().type).trim();
                   if (ssa.isEmpty()) {
                       ssa = "C";
                    }
                }

                // map to extension coding
                String sse;
                switch(ssa) {
                    case "G":
                    case "H":
                    case "I":
                        sse = "Helix";
                        break;
                    case "T":
                        sse = "Turn";
                        break;
                    case "E":
                        sse = "Strand";
                        break;
                    case "B":
                        sse = "Bridge";
                        break;
                    case "S":
                    case "C":
                        sse = "Loop";
                        break;
                    default:
                        sse = "Loop";
                }
                
                // calculate torsion angles
                double phi = 360.0;
                double psi = 360.0;
                boolean breakBefore = false;
                boolean breakAfter = false;
                try {
                    if (g1 instanceof AminoAcid && g2 instanceof AminoAcid && g3 instanceof AminoAcid) {
                        AminoAcid a1 = (AminoAcid) g1;
                        AminoAcid a2 = (AminoAcid) g2;
                        AminoAcid a3 = (AminoAcid) g3;
                       
                        // check connectivity
                        breakBefore = !Calc.isConnected(a1,a2);
                        breakAfter = !Calc.isConnected(a2,a3);
                        if (!breakBefore && !breakAfter) {
                            phi = Calc.getPhi(a1,a2);
                            psi = Calc.getPsi(a2,a3);
                        }
                    }
                } catch (StructureException e) {
                    // do nothing
                }

                Residue residue = new Residue();

                residue.setPdbId(structure.getPDBCode());
                residue.setChainId(chain.getChainID());
                residue.setAtomNumber(g2.getAtom("CA").getPDBserial());
                residue.setResidueNumber(g2.getResidueNumber().getSeqNum());
                residue.setResidueCode(String.valueOf(g2.getResidueNumber().getInsCode()));
                residue.setResidueCode(residueCode);
                residue.setSSA(ssa);
                residue.setSSE(sse);
                residue.setPhi(phi);
                residue.setPsi(psi);
                residue.setDescriptor(calculateRegion(phi,psi,sse));
                residue.setBreakBefore(breakBefore);
                residue.setBreakAfter(breakAfter);

                residues.add(residue);
            }
/*
            // *** extend helices and strands

            int helixExtRegion;
            int strandExtRegion;

            // forward
            helixExtRegion = -1;
            strandExtRegion = -1;
            for (int i = 0; i < residues.size(); i++) {

                Residue residue = residues.get(i);

                int helixRegion = calculateHelixRegion(residue.getPhi(), residue.getPsi());
                int strandRegion = calculateStrandRegion(residue.getPhi(), residue.getPsi());

                // reassign
                if (helixRegion == helixExtRegion && helixRegion != -1) {
                    residue.setDescriptor(helixExtRegion);
                    residue.setSSE("Helix");  
                }
                if (strandRegion == strandExtRegion && strandRegion != -1) {
                    residue.setDescriptor(strandExtRegion);
                    residue.setSSE("Strand");  
                }

                // set flags
                helixExtRegion = -1;
                strandExtRegion = -1;
                if (residue.getSSE().equals("Helix")) {
                    helixExtRegion = residue.getDescriptor();
                }
                if (residue.getSSE().equals("Strand")) {
                    strandExtRegion = residue.getDescriptor();
                }
            }
            
            // backward
            helixExtRegion = -1;
            strandExtRegion = -1;
            for (int i = residues.size() - 1; i >= 0; i--) {

                Residue residue = residues.get(i);

                int helixRegion = calculateHelixRegion(residue.getPhi(), residue.getPsi());
                int strandRegion = calculateStrandRegion(residue.getPhi(), residue.getPsi());

                // reassign
                if (helixRegion == helixExtRegion && helixRegion != -1) {
                    residue.setDescriptor(helixExtRegion);
                    residue.setSSE("Helix");  
                }
                if (strandRegion == strandExtRegion && strandRegion != -1) {
                    residue.setDescriptor(strandExtRegion);
                    residue.setSSE("Strand");  
                }

                // set flags
                helixExtRegion = -1;
                strandExtRegion = -1;
                if (residue.getSSE().equals("Helix")) {
                    helixExtRegion = residue.getDescriptor();
                }
                if (residue.getSSE().equals("Strand")) {
                    strandExtRegion = residue.getDescriptor();
                }
            }
*/
            // *** run position encoding by region

            List<Residue> run = new ArrayList<>();
            int lastRegion = Integer.MIN_VALUE; 

            for (Residue residue : residues) {

                // run end
                if (residue.getDescriptor() != lastRegion && lastRegion != Integer.MIN_VALUE) {
                    
                    setRunFactors(run);
                    run.clear();
                    run = new ArrayList<>();
                }

                // accumulate run grams
                run.add(residue);
                lastRegion = residue.getDescriptor();
            }

            // process last run
            if (!run.isEmpty()) {
                setRunFactors(run); 
                run.clear();
            }
            
            // *** calculate descriptor rpe hashes
    
            for (int i = 0; i < residues.size() - 2; i++) {

                Residue residue1 = residues.get(i);
                Residue residue2 = residues.get(i + 1);
                Residue residue3 = residues.get(i + 2);

                // good window
                if (!(
                        residue1.getBreakAfter() || residue2.getBreakAfter() || 
                        residue2.getBreakBefore() || residue3.getBreakBefore()
                    )) {

                    // abcd
                    int gram = 
                        residue1.getDescriptor() * Constants.PRIME_POW_2 + 
                        residue2.getDescriptor() * Constants.PRIME_POW_1 +
                        residue3.getDescriptor();
                   
                    // rr abcd
                    int runFactor = residue1.getRunFactor() % 100;
                    gram = gram + runFactor * Constants.DEC_POW_4; 

                    // set hashes
                    residue1.setGram(gram);
                }
            }

            grams.addAll(residues.stream().filter(r -> r.getGram() > 0).map(r -> r.getGram()).collect(Collectors.toList()));
            
        } // iterating chains

        return grams.stream().toArray(Integer[]::new);
    }

    public static void setRunFactors(List<Residue> residues) {

        // only runs of regular secondary structures receive run factors
        if (residues.get(0).isHelix() || residues.get(0).isStrand() || residues.get(0).isTurn()) {
            int test = Math.floorDiv(residues.size(), 2);
            for (int i = 0; i < residues.size(); i++) {
                if (i >= test) {
                    residues.get(i).setRunFactor(residues.size() - 1 - i);
                } else {
                    residues.get(i).setRunFactor(i);
                }
            }
        }
    }
    
    public static int calculateRegion(double phi, double psi, String sse) {

        if (phi == 360 || psi == 360) 
            return calculateDefaultRegion(sse);
        
        // helix 1, 2, 3, 4
        else if (sse.equals("Helix")) {
            return calculateHelixRegion(phi, psi);
        }

        // strand 5, 6, 7
        else if (sse.equals("Strand")) {
            return calculateStrandRegion(phi, psi) + 4;
        }
      
        // other 8, 9, 10
        else if (sse.equals("Loop")) {
            return calculateLoopRegion(phi, psi) + 7;
        }

        // turns and bridges
        else if (sse.equals("Turn")) {
            return 11;
        }
        else {
            return 12; // Bridge
        }
    }

    public static int calculateDefaultRegion(String sse) {
        
        // helix 1, 2, 3, 4
        if (sse.equals("Helix")) {
            return 3;
        }

        // strand 5, 6, 7
        else if (sse.equals("Strand")) {
            return 5;
        }
      
        // other 8, 9, 10
        else if (sse.equals("Loop")) {
            return 8;
        }

        // turns and bridges
        else if (sse.equals("Turn")) {
            return 11;
        }
        else {
            return 12; // Bridge
        }
    }

    public static int calculateHelixRegion(double phi, double psi) {

        int region = -1;

        if (psi >= -180 && psi < -135) {
            if (phi >= 0 && phi < 180) {
                region = 4;
            }
            else {
                region = 1; 
            }
        }
        else if (psi >= -135 && psi < -75) {
            if (phi >= 0 && phi < 180) {
                region = 4;
            }
            else {
                region = 3;
            }
        }
        else if (psi >= -75 && psi < 90) {
            if (phi >= 0 && phi < 180) {
                region = 2;
            }
            else {
                region = 3;
            }
        }
        else if (psi >= 90 && psi < 120) {
            if (phi >= 0 && phi < 180) {
                region = 2;
            }
            else {
                region = 1;
            }
        }
        else if (psi >= 120 && psi < 180) {
            if (phi >= 0 && phi < 180) {
                region = 4;
            }
            else {
                region = 1;
            }
        }
        
        return region;
    }

    public static int calculateStrandRegion(double phi, double psi) {

        int region = -1;

        if (psi >= -180 && psi < -110) {
            region = 1;
        }
        else if (psi >= -110 && psi < -60) {
            if (phi >= 0 && phi < 180) {
                region = 1;
            }
            else {
                region = 3;
            }
        }
        else if (psi >= -60 && psi < 60) {
            if (phi >= 0 && phi < 180) {
                region = 2;
            }
            else {
                region = 3;
            }
        }
        else if (psi >= 60 && psi < 90) {
            if (phi >= 0 && phi < 180) {
                region = 2;
            }
            else {
                region = 1;
            }
        }
        else if (psi >= 90 && psi < 180) {
            region = 1;
        }

        return region;
    }
    
    public static int calculateLoopRegion(double phi, double psi) {

        int region = -1;

        if (psi >= -180 && psi < -100) {
            region = 1;
        }
        else if (psi >= -100 && psi < -90) {
            if (phi >= 0 && phi < 180) {
                region = 1;
            }
            else {
                region = 3;
            }
        }
        else if (psi >= -90 && psi < 60) {
            if (phi >= 0 && phi < 180) {
                region = 2;
            }
            else {
                region = 3;
            }
        }
        else if (psi >= 60 && psi < 90) {
            if (phi >= 0 && phi < 180) {
                region = 2;
            }
            else {
                region = 1;
            }
        }
        else if (psi >= 90 && psi < 180) {
            region = 1;
        }
            
        return region;
    }
}

