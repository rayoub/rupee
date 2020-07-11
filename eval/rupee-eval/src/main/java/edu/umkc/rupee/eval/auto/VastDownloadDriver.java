package edu.umkc.rupee.eval.auto;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.javatuples.Pair;
import org.postgresql.ds.PGSimpleDataSource;

import edu.umkc.rupee.eval.lib.Constants;
import edu.umkc.rupee.eval.lib.Db;

public class VastDownloadDriver extends VastDriver {

    public void doSearchBatch() {

        List<Pair<String,String>> pairs = getVastRequestIds(); 

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
    
    public static List<Pair<String,String>> getVastRequestIds() {

        List<Pair<String, String>> pairs = new ArrayList<>();

        try {

            PGSimpleDataSource ds = Db.getDataSource();
            Connection conn = ds.getConnection();

            PreparedStatement stmt = conn.prepareCall("SELECT db_id, request_id FROM vast_request ORDER BY db_id");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                Pair<String, String> pair = Pair.with(rs.getString("db_id") , rs.getString("request_id"));
                pairs.add(pair);
            }

            rs.close();
            stmt.close();
            conn.close();
        
        } catch (SQLException e) {
            Logger.getLogger(VastDownloadDriver.class.getName()).log(Level.WARNING, null, e);
        }

        return pairs;
    }
}





