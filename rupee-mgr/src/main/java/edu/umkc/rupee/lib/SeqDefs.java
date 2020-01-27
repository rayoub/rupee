package edu.umkc.rupee.lib;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.biojava.nbio.structure.AminoAcid;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.io.PDBFileReader;

public class SeqDefs {

    public static void printSeqs(String path) throws Exception {
       
        Files.newDirectoryStream(Paths.get(path), "*.pdb")
            .forEach(fileName -> {
 
                PDBFileReader reader = new PDBFileReader();
                reader.setFetchBehavior(FetchBehavior.LOCAL_ONLY);
               
                String seqName = fileName.getFileName().toString(); 
                seqName = seqName.substring(0, seqName.length() - 4);
    
                try {
                    
                    FileInputStream queryFile = new FileInputStream(fileName.toString());
                    Structure structure = reader.getStructure(queryFile);

                    printSeq(seqName, structure);
                
                } catch (Exception e) {
                    Logger.getLogger(ChainDefs.class.getName()).log(Level.INFO, null, e);
                }
        });
    }

    public static void printSeq(String seqName, Structure structure) throws Exception {

        StringBuilder builder = new StringBuilder();
        builder.append(">");
        builder.append(seqName);
        builder.append("\n");

        // iterate first chain in first model
        List<Group> groups = structure.getModel(0).get(0).getAtomGroups();
        for (Group group : groups) {
            if (!(group.isAminoAcid() && group.hasAminoAtoms())) {
                throw new Exception("Invalid Atom Group");
            }
            else {
                AminoAcid aa = (AminoAcid)group;
                builder.append(aa.getAminoType());
            }
        }
        
        builder.append("\n");

        System.out.println(builder.toString());
    }
}
