package edu.umkc.rupee.auto;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.umkc.rupee.lib.Benchmarks;
import edu.umkc.rupee.lib.Constants;

public class VastCombinedDriver extends VastDriver {

    public VastResults doSearch(String dbId) {

        String link = "";
        VastResults results = new VastResults();

        try {
            link = doSearchUpload(dbId);
        }
        catch (InterruptedException e) {
            link = "";
            System.out.println("interrupted while uploading structure for db_id: " + dbId);
        }

        if (!link.isEmpty()) {

            System.out.println("... got the results link");
            System.out.println(dbId + "," + link);

            // wait 2 hour
            for (int i = 0; i < 24; i++) {

                try {
                    results = doSearchDownload(dbId, link);
                }
                catch (InterruptedException e) {

                    System.out.println("interrupted while getting results for db_id: " + dbId);
                    break;
                }

                if (!results.ResultsRmsd.isEmpty()) {

                    // jump to return results
                    break;
                }
                else {

                    // continue waiting
                    try {
                        Thread.sleep(5 * ONE_MINUTE);
                    }
                    catch( InterruptedException e) {
                        System.out.println("interrupted while waiting for results for db_id: " + dbId);
                        break;
                    }
                }
            }

            if (results.ResultsRmsd.isEmpty()) {
                System.out.println("no results for db_id: " + dbId);
            }
        }
        else {
            System.out.println("no results link for db_id: " + dbId); 
        }

        return results;
    }

    public void doSearchBatch() {

        int EARLY_EXIT = 100;

        List<String> dbIds = Benchmarks.get("casp_d190");

        int count = 0;
        for (int i = 0; i < dbIds.size(); i++) {
            
            String dbId = dbIds.get(i);
            String fileNameRmsd = Constants.VAST_PATH_RMSD + dbId + ".txt";
            String fileNameVastScore = Constants.VAST_PATH_VAST_SCORE + dbId + ".txt";

            try {

                // for now lets not do the rmsd work again
                if (!isExcluded(dbId) && Files.notExists(Paths.get(fileNameRmsd))) {
            
                    count++;
                    System.out.println(count + ":Processing request for " + dbId);

                    VastResults results = doSearch(dbId);

                    if (!results.ResultsRmsd.isEmpty()) {
                        
                        FileOutputStream outputStream = new FileOutputStream(fileNameRmsd);
                        OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);

                        try (BufferedWriter bufferedWriter = new BufferedWriter(outputWriter);) {
                               bufferedWriter.write(results.ResultsRmsd);
                        }
                    }

                    if (!results.ResultsVastScore.isEmpty()) {
                        
                        FileOutputStream outputStream = new FileOutputStream(fileNameVastScore);
                        OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);

                        try (BufferedWriter bufferedWriter = new BufferedWriter(outputWriter);) {
                               bufferedWriter.write(results.ResultsVastScore);
                        }
                    }

                    System.out.println(count + ": Processed request for: " + dbId);
                }
            } 
            catch (Exception e) { 

                Logger.getLogger(VastCombinedDriver.class.getName()).log(Level.SEVERE, dbId, e);
            }

            // early exit
            if (count >= EARLY_EXIT) {
                break;
            }
        }
    }
}





