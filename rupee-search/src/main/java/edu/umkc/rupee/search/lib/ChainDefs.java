package edu.umkc.rupee.search.lib;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.io.PDBFileReader;

public class ChainDefs {

    public static class ChainDef {

        private String chainName;
        private int residueCount;

        public ChainDef(Chain chain) {
        
            this.chainName = chain.getName().trim();
            this.residueCount = (int) chain.getAtomGroups().stream().filter(group -> !group.isHetAtomInFile() && group.isAminoAcid() && group.hasAtom("CA")).count();
        }

        public String getChainName() {
            return chainName;
        }

        public void setChainName(String chainName) {
            this.chainName = chainName;
        }

        public int getResidueCount() {
            return residueCount;
        }
        
        public void setResidueCount(int residueCount) {
            this.residueCount = residueCount;
        }

        @Override
        public boolean equals(Object o) {

            ChainDef d = (ChainDef) o;
            return Objects.equals(this.chainName, d.getChainName()); 
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(this.chainName);
        }
    }

    public enum ChainDefType {

        PDB("pdb"),
        OBSOLETE("obsolete");

        private String name;

        ChainDefType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static void writePdbChains(String version) throws IOException {

        String outFile = Constants.CHAIN_PATH + ChainDefType.PDB.getName() + "_" + version + ".txt";
        
        Files.deleteIfExists(Paths.get(outFile));

        writeChains(Constants.PDB_PDB_PATH, false, version, outFile);
        writeChains(Constants.PDB_BUNDLE_PATH, true, version, outFile);
    }
    
    public static void writeObsoleteChains(String version) throws IOException {

        String outFile = Constants.CHAIN_PATH + ChainDefType.OBSOLETE.getName() + "_" + version + ".txt";

        Files.deleteIfExists(Paths.get(outFile));

        writeChains(Constants.PDB_OBSOLETE_PATH, false, version, outFile);
    }

    private static void writeChains(String path, boolean fileNameChain, String version, String outFile) throws IOException {
      
        Files.newDirectoryStream(Paths.get(path), "*.ent.gz")
            .forEach(pathName -> {
 
                PDBFileReader reader = new PDBFileReader();
                reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);

                String fileName = pathName.getFileName().toString();
                String pdbId = fileName.substring(3,7).toLowerCase();
    
                try {
                    
                    FileInputStream queryFile = new FileInputStream(pathName.toString());
                    GZIPInputStream queryFileGz = new GZIPInputStream(queryFile);

                    Structure structure = reader.getStructure(queryFileGz);
                    structure.setPDBCode(pdbId);

                    // *** write out file

                    List<String> lines = getChains(structure, fileName, fileNameChain);
                    if (lines.size() > 0) {

                        StringJoiner joiner = new StringJoiner(System.lineSeparator());
                        for (String line : lines) {
                            joiner.add(line);
                        }

                        FileWriter writer = new FileWriter(outFile, true);
                        writer.write(joiner.toString() + System.lineSeparator());
                        writer.close();
                    }
                
                } catch (Exception e) {
                    Logger.getLogger(ChainDefs.class.getName()).log(Level.INFO, null, e);
                }
        });
    }

    private static List<String> getChains(Structure structure, String fileName, boolean fileNameChain) {

        // get chains for first model
        List<Chain> chains = structure.getModel(0);

        List<String> lines = null; 
        if (fileNameChain) {
            
            String chainName = fileName.split("\\.")[0].substring(7);

            // get a line of data for each chain
            lines = chains.stream()
                .map(ChainDef::new)
                .filter(chainDef -> !chainName.isEmpty() && chainDef.getResidueCount() >= 1)
                .distinct()
                .map(chainDef -> 
                    structure.getPDBCode() + chainName + " " + 
                    structure.getPDBCode() + " " + chainName + " " + 
                    chainDef.getResidueCount()
                )
                .map(line -> line.trim())
                .collect(Collectors.toList());
        }
        else {

            // get a line of data for each chain
            lines = chains.stream()
                .map(ChainDef::new)
                .filter(chainDef -> !chainDef.getChainName().isEmpty() && chainDef.getResidueCount() >= 1)
                .distinct()
                .map(chainDef -> 
                    structure.getPDBCode() + chainDef.getChainName() + " " + 
                    structure.getPDBCode() + " " + chainDef.getChainName() + " " + 
                    chainDef.getResidueCount()
                )
                .map(line -> line.trim())
                .collect(Collectors.toList());
        }

        return lines;
    }
}
