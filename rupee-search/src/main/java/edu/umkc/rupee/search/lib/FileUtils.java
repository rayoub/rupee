package edu.umkc.rupee.search.lib;

import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {

    public static String appendExt(String fileName) {

        String fileNameWithExt = fileName + ".pdb.gz";
        if (Files.notExists(Paths.get(fileNameWithExt))) {
            fileNameWithExt = fileName + ".ent.gz";
            if (Files.notExists(Paths.get(fileNameWithExt))) {
                fileNameWithExt = fileName + ".pdb";
                if (Files.notExists(Paths.get(fileNameWithExt))) {
                    fileNameWithExt = fileName + ".ent";
                    if (Files.notExists(Paths.get(fileNameWithExt))) {
                        fileNameWithExt = "";
                    }
                }
            }
        }
        return fileNameWithExt;
    }
}


