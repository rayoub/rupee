package edu.umkc.rupee.lib;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import org.biojava.nbio.structure.AminoAcid;
import org.biojava.nbio.structure.Atom;
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

import edu.umkc.rupee.defs.DbType;
import edu.umkc.rupee.tm.Kabsch;
import edu.umkc.rupee.tm.TmAlign;
import edu.umkc.rupee.tm.TmMode;
import edu.umkc.rupee.tm.TmResults;

public class Dump {

    public static void dumpCossackCoords() {

        String dbId = "d1euda1";
        String pdbId = "1eud";
        String fileName = DbType.SCOP.getImportPath() + dbId + ".pdb.gz";

        if (Files.notExists(Paths.get(fileName))) {
            System.out.println("File Not Found: " + fileName);
            return;
        }

        try {

            InputStream inputStream = new FileInputStream(fileName);
            GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);

            PDBFileReader reader = new PDBFileReader();
            reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);
            reader.getStructure(gzipInputStream);

            Structure structure = reader.getStructure(fileName);
            structure.setPDBCode(pdbId);

            printHeader();

            dumpCossackCoords(structure);
        
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void dumpCossackCoords(Structure structure) {

        // assign secondary structure
        SecStrucCalc ssCalc = new SecStrucCalc();
        try {
            ssCalc.calculate(structure, true);
        } catch (StructureException e) {
            // do nothing
        }

        // get first chain 
        Chain chain = structure.getChainByIndex(0);

        // gather residues 
        List<Residue> residues = new ArrayList<>();
        List<Group> groups = chain.getAtomGroups();
        for (int i = 1; i < groups.size() - 1; i++) {

            Group g1 = groups.get(i - 1);
            Group g2 = groups.get(i);
            Group g3 = groups.get(i + 1);
           
            String residueCode = g2.getChemComp().getOne_letter_code();

            // we need the carbon alpha
            if (!g2.hasAtom("CA") || !g2.hasAtom("N")) {
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
                case "T":
                    sse = "Helix";
                    break;
                case "E":
                case "B":
                    sse = "Strand";
                    break;
                case "S":
                case "C":
                    sse = "Loop";
                    break;
                default:
                    sse = "Loop";
            }

            // get coordinates
            Atom ca = g2.getAtom("CA");
            float x = (float) ca.getX();
            float y = (float) ca.getY();
            float z = (float) ca.getZ();
            
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
           
            // get N coordinates
            Atom n = g2.getAtom("N");
            float nx = (float) n.getX();
            float ny = (float) n.getY();
            float nz = (float) n.getZ();

            Residue residue = new Residue();

            residue.setPdbId(structure.getPDBCode());
            residue.setChainName(chain.getId());
            residue.setAtomNumber(g2.getAtom("CA").getPDBserial());
            residue.setResidueNumber(g2.getResidueNumber().getSeqNum());
            residue.setInsertCode(String.valueOf(g2.getResidueNumber().getInsCode()));
            residue.setResidueCode(residueCode);
            residue.setSSA(ssa);
            residue.setSSE(sse);
            residue.setPhi(phi);
            residue.setPsi(psi);
            residue.setX(x);
            residue.setY(y);
            residue.setZ(z);
            residue.setNX(nx);
            residue.setNY(ny);
            residue.setNZ(nz);
            residue.setBreakBefore(breakBefore);
            residue.setBreakAfter(breakAfter);

            residues.add(residue);
        }

        // calculate coords
        for (int i = 1; i < residues.size() - 1; i++) {

            Residue residue1 = residues.get(i - 1);
            Residue residue2 = residues.get(i);
            Residue residue3 = residues.get(i + 1);

            // good window
            if (!(
                    residue1.getBreakAfter() || residue2.getBreakAfter() || 
                    residue2.getBreakBefore() || residue3.getBreakBefore()
                )) {
            
                double[][] coords = new double[3][4];

                // translate ca2 to origin

                // ca1
                coords[0][0] = residue1.getX() - residue2.getX();
                coords[1][0] = residue1.getY() - residue2.getY();
                coords[2][0] = residue1.getZ() - residue2.getZ();
               
                // n2 
                coords[0][1] = residue2.getNX() - residue2.getX();
                coords[1][1] = residue2.getNY() - residue2.getY();
                coords[2][1] = residue2.getNZ() - residue2.getZ();

                // ca2
                coords[0][2] = 0;
                coords[1][2] = 0;
                coords[2][2] = 0;

                // ca3
                coords[0][3] = residue3.getX() - residue2.getX();
                coords[1][3] = residue3.getY() - residue2.getY();
                coords[2][3] = residue3.getZ() - residue2.getZ();

                // 1. rotate ca1 about the x-axis to fall in the xy-plane
                // 2. rotate ca1 about the z-axis to fall in the xz-plane along negative x-axis
                // 1&2 will put ca1 on the x-axis an approximately fixed distance from ca2 at the origin
                // 3. rotate n2 about the x-axis to fall in the xy-plane with y > 0 

                // 1.
                double x = coords[0][0]; 
                double y = coords[1][0]; 
                double z = coords[2][0]; 

                if (z != 0) {

                    double cn = Math.sqrt(Math.pow(y,2) + Math.pow(z,2));
                    double pn = Math.sqrt(Math.pow(y,2));
                    double dot = Math.pow(y,2);
                    double cos = dot / (pn * cn);
                    double angle = Math.acos(cos);

                    if (((z > 0) && (y > 0)) || ((z < 0) && (y < 0))) {
                        angle = Math.PI - angle;
                    }

                    double[][] r = getRotationX(angle);
                    coords = matmul(r, coords);
                } 

                // 2.
                x = coords[0][0]; 
                y = coords[1][0]; 
                z = coords[2][0]; 

                if (!(x < 0 && y == 0)) {

                    double cn = Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
                    double pn = Math.sqrt(Math.pow(x,2));
                    double dot = Math.pow(x,2);
                    double cos = dot / (pn * cn);
                    double angle = Math.acos(cos);
                    
                    if (y < 0) {
                        if (x < 0) {
                            angle = 2 * Math.PI - angle;
                        } 
                        else {
                            angle = Math.PI + angle;
                        }
                    }
                    else if (x > 0) {
                        angle = Math.PI - angle; 
                    }

                    double[][] r = getRotationZ(angle);
                    coords = matmul(r, coords);
                }

                // 3. 
                x = coords[0][1]; 
                y = coords[1][1]; 
                z = coords[2][1]; 

                if (z != 0) {

                    double cn = Math.sqrt(Math.pow(y,2) + Math.pow(z,2));
                    double pn = Math.sqrt(Math.pow(y,2));
                    double dot = Math.pow(y,2);
                    double cos = dot / (pn * cn);
                    double angle = Math.acos(cos);

                    if (y < 0) {
                        if (z < 0) {
                            angle = Math.PI - angle;
                        } 
                        else {
                            angle = Math.PI + angle;
                        }
                    }
                    else if (z > 0) {
                        angle = 2 * Math.PI - angle; 
                    }

                    double[][] r = getRotationX(angle);
                    coords = matmul(r, coords);
                }

                printRow(residue2, coords);
            }
        } 
    }

    public static double[][] getRotationX(double angle) {

        double[][] r = { 
            { 1.0, 0.0, 0.0 },
            { 0.0, Math.cos(angle), -Math.sin(angle) },
            { 0.0, Math.sin(angle), Math.cos(angle) }
        };
        
        return r;
    }

    public static double[][] getRotationY(double angle) {

        double[][] r = { 
            { Math.cos(angle), 0.0, Math.sin(angle) },
            { 0.0, 1.0, 0.0 },
            { -Math.sin(angle), 0.0, Math.cos(angle) }
        };
        
        return r;
    }
    
    public static double[][] getRotationZ(double angle) {

        double[][] r = { 
            { Math.cos(angle), -Math.sin(angle), 0.0 },
            { Math.sin(angle), Math.cos(angle), 0.0 },
            { 0.0, 0.0, 1.0 }
        };
        
        return r;
    }

    public static double[][] matmul(double[][] a, double[][] b) {

        int aRows = a.length;
        int aColumns = a[0].length;
        int bColumns = b[0].length;

        // initialize matrix
        double[][] c = new double[aRows][bColumns];
        for (int i = 0; i < aRows; i++) {
            for (int j = 0; j < bColumns; j++) {
                c[i][j] = 0.0;
            }
        }

        // multiply matrices
        for (int i = 0; i < aRows; i++) { 
            for (int j = 0; j < bColumns; j++) { 
                for (int k = 0; k < aColumns; k++) { 
                    c[i][j] += a[i][k] * b[k][j];
                }
            }
        }

        return c;
    }

    public static void printHeader() {

        System.out.println(
            "residue_number," + 
            "residue_code," + 
            "ss," + 
            "phi," + 
            "psi," + 
            "x," + 
            "y," + 
            "z," + 
            "break_before," + 
            "break_after"
        );
    }

    public static void printRow(Residue residue, double[][] coords) {

        System.out.println(
            residue.getResidueNumber() + "," + 
            residue.getResidueCode() + "," + 
            residue.getSSE() + "," + 
            residue.getPhi() + "," + 
            residue.getPsi() + "," + 
            coords[0][3] + "," + 
            coords[1][3] + "," + 
            coords[2][3] + "," + 
            (residue.getBreakBefore()?1:0) + "," + 
            (residue.getBreakAfter()?1:0) 
        );
    }

    public static void displayData(double[][] coords) {

        System.out.println(
            (Math.abs(coords[0][3]) < 0.01? 0.0 : coords[0][3])
            + " " + 
            (Math.abs(coords[1][3]) < 0.01? 0.0 : coords[1][3])
            + " " + 
            (Math.abs(coords[2][3]) < 0.01? 0.0 : coords[2][3])
        );           
    }

    public static void checkResults() {

        // iterate through the mtm results and find mismatches on scores
        
        PGSimpleDataSource ds = Db.getDataSource();

        try {

            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);

            String command = "SELECT db_id_1, db_id_2, mtm_tm_score FROM mtm_result WHERE n <= 10";
                
            PreparedStatement stmt = conn.prepareCall(command);
            
            PDBFileReader reader = new PDBFileReader();
            reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                String dbId1 = rs.getString("db_id_1");
                String dbId2 = rs.getString("db_id_2");
                double mtmScore = rs.getDouble("mtm_tm_score");
                
                String path = Constants.CASP_PATH + dbId1 + ".pdb";
                FileInputStream queryFile = new FileInputStream(path);
                Structure queryStructure = reader.getStructure(queryFile);
                   
                String targetFileName = DbType.CHAIN.getImportPath() + dbId2 + ".pdb.gz";
                if (!Files.exists(Paths.get(targetFileName))) {
                    continue;
                }

                FileInputStream targetFile = new FileInputStream(targetFileName);
                GZIPInputStream targetFileGz = new GZIPInputStream(targetFile);
                Structure targetStructure = reader.getStructure(targetFileGz);

                Kabsch kabsch = new Kabsch();
                TmAlign tm = new TmAlign(queryStructure, targetStructure, TmMode.REGULAR, kabsch);
                TmResults results = tm.align();
            
                double diff = Math.abs(results.getTmScoreQ() - mtmScore);
                if (diff > 0.001) {
                    System.out.println(dbId1 + ", " + diff);
                }
            }
        } 
        catch (Exception e) {
            System.out.println("an error occurred: " + e.getMessage());
        }
    }

    public static void dumpColumnOfNumbers(int lines) {

        Random random = new Random();
        for (int i = 0; i < lines; i++) {
            //System.out.println(random.nextInt(900) + 100);
            System.out.println(random.nextInt(9000) + 1000);
        } 
    }



}
