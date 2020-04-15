package edu.umkc.rupee.auto;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.umkc.rupee.lib.Benchmarks;
import edu.umkc.rupee.lib.Constants;

public class VastUploadDriver extends VastDriver {

    public void doSearchBatch() {

        List<String> excludes = new ArrayList<>();
        excludes.add("T0957s2TS145-D1");

        int EARLY_EXIT = 5;

        List<String> dbIds = Benchmarks.get("casp_d250");

        int count = 0;
        for (int i = 0; i < dbIds.size(); i++) {
            
            String dbId = dbIds.get(i);
            String fileName = Constants.VAST_PATH_RMSD + dbId + ".txt";

            try {

                if (!isExcluded(excludes, dbId) && Files.notExists(Paths.get(fileName))) {
                    
                    count++;
                    doSearchUpload(dbId);
                }
            } 
            catch (Exception e) { 

                Logger.getLogger(VastUploadDriver.class.getName()).log(Level.SEVERE, dbId, e);
            }
            
            // early exit
            if (count >= EARLY_EXIT) {
                break;
            }
        }
    }
}





