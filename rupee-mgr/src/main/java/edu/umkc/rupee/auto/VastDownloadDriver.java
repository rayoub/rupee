package edu.umkc.rupee.auto;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.javatuples.Pair;

import edu.umkc.rupee.lib.Constants;
import edu.umkc.rupee.lib.Db;

public class VastDownloadDriver extends VastDriver {

    public void doSearchBatch() {

        List<Pair<String,String>> pairs = Db.getVastRequestIds(); 

        for (int i = 0; i < pairs.size(); i++) {

            Pair<String,String> pair = pairs.get(i);
            String dbId = pair.getValue0();
            String link = pair.getValue1();
            String fileNameRmsd = Constants.VAST_PATH_RMSD + dbId + ".txt";
            String fileNameVastScore = Constants.VAST_PATH_VAST_SCORE + dbId + ".txt";

            if (Files.notExists(Paths.get(fileNameRmsd)) || Files.notExists(Paths.get(fileNameVastScore))) {
            
                System.out.println("Processing request for: " + dbId);

                try {

                    VastResults results = doSearchDownload(dbId, link);

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
                    
                    System.out.println("Processed request for: " + dbId);
                } 
                catch (Exception e) { 

                    System.out.println("... got the too much traffic page");
                }
            }
        }
    }
}





