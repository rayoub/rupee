package edu.umkc.rupee.mgr.lib;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
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
            this.residueCount = (int)chain.getAtomGroups().stream().filter(group -> !group.isHetAtomInFile() && group.isAminoAcid() && group.hasAtom("CA")).count();
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

            ChainDef d = (ChainDef)o;
            return Objects.equals(this.chainName, d.getChainName()); 
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(this.chainName);
        }
    }

    public static void printChains(String path) throws IOException {
       
        Files.newDirectoryStream(Paths.get(path), "*.ent.gz")
            .forEach(fileName -> {
 
                PDBFileReader reader = new PDBFileReader();
                reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);
                
                String pdbId = fileName.getFileName().toString().substring(3,7).toLowerCase();
    
                try {
                    
                    FileInputStream queryFile = new FileInputStream(fileName.toString());
                    GZIPInputStream queryFileGz = new GZIPInputStream(queryFile);

                    Structure structure = reader.getStructure(queryFileGz);
                    structure.setPDBCode(pdbId);

                    printChains(structure);
                
                } catch (Exception e) {
                    Logger.getLogger(ChainDefs.class.getName()).log(Level.INFO, null, e);
                }
        });
    }

    private static void printChains(Structure structure) {

        // iterate chains
        List<Chain> chains = structure.getModel(0);
        chains.stream()
            .map(ChainDef::new)
            .filter(chainDef -> !chainDef.getChainName().isEmpty() && chainDef.getResidueCount() >= 1)
            .distinct()
            .forEach(chainDef -> {
                System.out.println(
                        structure.getPDBCode() + chainDef.getChainName() + " " + 
                        structure.getPDBCode() + " " + chainDef.getChainName() + " " + 
                        chainDef.getResidueCount());
            });
    }
}
