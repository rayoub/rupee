package edu.umkc.rupee.eval.auto;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.umkc.rupee.eval.lib.Benchmarks;
import edu.umkc.rupee.eval.lib.Constants;

public class VastUploadDriver extends VastDriver {

    public void doSearchBatch(String benchmark) {

        List<String> dbIds = Benchmarks.get(benchmark);

        for (int i = 0; i < dbIds.size(); i++) {
            
            String dbId = dbIds.get(i);
            String fileName = Constants.VAST_PATH_VAST_SCORE + dbId + ".txt";

            try {
                
                // just check one file
                if (Files.notExists(Paths.get(fileName))) {
                    
                    doSearchUpload(dbId);
                }
            } 
            catch (Exception e) { 

                Logger.getLogger(VastUploadDriver.class.getName()).log(Level.SEVERE, dbId, e);
            }
        }
    }
}





