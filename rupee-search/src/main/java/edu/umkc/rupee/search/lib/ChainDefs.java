package edu.umkc.rupee.search.lib;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    public static void writePdbChains(String version) throws IOException {

        List<String> pdbLines = new ArrayList<>();
        //pdbLines.addAll(getChains(Constants.PDB_PDB_PATH, false));
        pdbLines.addAll(getChains(Constants.PDB_BUNDLE_PATH, true));
        StringJoiner pdbJoiner = new StringJoiner(System.lineSeparator());
        for (String line : pdbLines) {

            pdbJoiner.add(line);
        }
        FileWriter pdbWriter = new FileWriter(Constants.CHAIN_PATH + "pdb_" + version + ".txt");
        pdbWriter.write(pdbJoiner.toString());
        pdbWriter.close();
    }
    
    public static void writeObsoleteChains(String version) throws IOException {

        List<String> obsoleteLines = new ArrayList<>();
        obsoleteLines.addAll(getChains(Constants.PDB_OBSOLETE_PATH, false));
        StringJoiner obsoleteJoiner = new StringJoiner(System.lineSeparator());
        for (String line : obsoleteLines) {

            obsoleteJoiner.add(line);
        }
        FileWriter obsoleteWriter = new FileWriter(Constants.CHAIN_PATH + "obsolete_" + version + ".txt");
        obsoleteWriter.write(obsoleteJoiner.toString());
        obsoleteWriter.close();
    }

    private static List<String> getChains(String path, boolean fileNameChain) throws IOException {
      
        List<String> lines = new ArrayList<>();

        Files.newDirectoryStream(Paths.get(path), "*.ent.gz")
            .forEach(pathName -> {
 
                PDBFileReader reader = new PDBFileReader();
                reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);
               
                String fileName = pathName.getFileName().toString().split("\\.")[0];
                String pdbId = fileName.substring(3,7).toLowerCase();
    
                try {
                    
                    FileInputStream queryFile = new FileInputStream(fileName.toString());
                    GZIPInputStream queryFileGz = new GZIPInputStream(queryFile);

                    Structure structure = reader.getStructure(queryFileGz);
                    structure.setPDBCode(pdbId);

                    lines.addAll(getChains(structure, fileName, fileNameChain));
                
                } catch (Exception e) {
                    Logger.getLogger(ChainDefs.class.getName()).log(Level.INFO, null, e);
                }
        });

        return lines;
    }

    private static List<String> getChains(Structure structure, String fileName, boolean fileNameChain) {

        // get chains for first model
        List<Chain> chains = structure.getModel(0);

        List<String> lines = null; 
        if (fileNameChain) {

            String chainName = fileName.substring(7);

            // get a line of data for each chain
            lines = chains.parallelStream()
                .map(ChainDef::new)
                .filter(chainDef -> !chainName.isEmpty() && chainDef.getResidueCount() >= 1)
                .distinct()
                .map(chainDef -> 
                    structure.getPDBCode() + chainName + " " + 
                    structure.getPDBCode() + " " + chainName + " " + 
                    chainDef.getResidueCount()
                )
                .collect(Collectors.toList());
        }
        else {

            // get a line of data for each chain
            lines = chains.parallelStream()
                .map(ChainDef::new)
                .filter(chainDef -> !chainDef.getChainName().isEmpty() && chainDef.getResidueCount() >= 1)
                .distinct()
                .map(chainDef -> 
                    structure.getPDBCode() + chainDef.getChainName() + " " + 
                    structure.getPDBCode() + " " + chainDef.getChainName() + " " + 
                    chainDef.getResidueCount()
                )
                .collect(Collectors.toList());
        }

        return lines;
    }
}
