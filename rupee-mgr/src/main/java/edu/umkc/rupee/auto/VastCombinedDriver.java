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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import edu.umkc.rupee.lib.Benchmarks;
import edu.umkc.rupee.lib.Constants;

public class VastCombinedDriver extends DriverBase {

    private static int ONE_MINUTE = 60 * 1000; // in seconds

    public VastResults doSearch(String dbId) {

        String link = "";
        VastResults results = new VastResults();

        try {
            link = doSearchUpload(dbId);
        }
        catch (InterruptedException e) {
            link = "";
            System.out.println("Interrupted while uploading structure for db_id: " + dbId);
        }

        if (!link.isEmpty()) {

            System.out.println("results link: " + link);

            // wait 2 hour
            for (int i = 0; i < 24; i++) {

                try {
                    results = doSearchDownload(dbId, link);
                }
                catch (InterruptedException e) {

                    System.out.println("Interrupted while getting results for db_id: " + dbId);
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
                        System.out.println("Interrupted while waiting for results for db_id: " + dbId);
                        break;
                    }
                }
            }

            if (results.ResultsRmsd.isEmpty()) {
                System.out.println("No results for db_id: " + dbId);
            }
        }
        else {
            System.out.println("No upload link for db_id: " + dbId); 
        }

        return results;
    }

    public String doSearchUpload(String dbId) throws InterruptedException {
        
        driver.get("https://www.ncbi.nlm.nih.gov/Structure/VAST/");
        
        // file upload
        String filePath = Constants.CASP_PATH + dbId + ".pdb"; 
        driver.findElement(By.name("pdbfile")).clear();
        driver.findElement(By.name("pdbfile")).sendKeys(filePath);

        // all pdb
        driver.findElement(By.xpath("//input[@value='All']")).click();
   
        // submit to upload
        driver.findElement(By.name("cmdVSMmdb")).click();

        // wait for page to load
        Thread.sleep(5000);
     
        // submit to search
        driver.findElement(By.xpath("/html/body/table[2]/tbody/tr[17]/td/table/tbody/tr/td[1]/input[2]")).click();
        
        // wait for page to load
        Thread.sleep(5000);

        // get the link of return page
        WebElement ele = driver.findElement(By.xpath("/html/body/table[2]/tbody/tr[7]/td/a"));
        String link = ele.getAttribute("href");

        return link;
    }

    public VastResults doSearchDownload(String dbId, String link) throws InterruptedException {
       
        VastResults results = new VastResults();

        driver.get(link);

        // wait for page to load
        Thread.sleep(5000);

        // check if done
        List<WebElement> eles = driver.findElements(By.xpath("/html/body/table[2]/tbody/tr[2]/td"));
        if (eles.size() > 0) {

            System.out.println("checking status");
            
            WebElement ele2 = eles.get(0);
            if (ele2.getText().equals("VAST Search Done")) {

                System.out.println("status is done");

                // click link to results
                driver.findElement(By.linkText("entire chain")).click();

                // wait for page to load
                Thread.sleep(5000);

                // get vast score first since we don't want to click rmsd sort
                results.ResultsVastScore = getTableData(false);
                results.ResultsRmsd = getTableData(true);
            }
        }

        return results;
    }

    public String getTableData(boolean sortByRmsd) throws InterruptedException {

        StringBuilder builder = new StringBuilder("");

        // form fill to get page of results
        new Select(driver.findElement(By.name("subset"))).selectByIndex(4);
        driver.findElement(By.name("subset")).click();
        new Select(driver.findElement(By.name("table"))).selectByIndex(1);
        driver.findElement(By.name("table")).click();
        if (sortByRmsd) {
            new Select(driver.findElement(By.name("sort"))).selectByIndex(3);
            driver.findElement(By.name("sort")).click();
        }
            
        // change to first page
        new Select(driver.findElement(By.name("doclistpage"))).selectByIndex(0);
        driver.findElement(By.name("doclistpage")).click();

        // display the first page
        driver.findElement(By.name("dispsub")).click();

        // wait for page to load
        Thread.sleep(5000);

        // get results of first page
        WebElement baseTable = driver.findElement(By.xpath("/html/body/table[3]"));
        List<WebElement> rows = baseTable.findElements(By.tagName("tr"));
        for (int i = 0; i < rows.size(); i++) {
           
            WebElement row = rows.get(i); 
            List<WebElement> items = row.findElements(By.tagName("td"));
            for (int j = 0; j < items.size(); j++) {

                WebElement item = items.get(j);
                String output = item.getText();
                if (j > 0 && j < items.size() - 1) {
                    output += ",";
                }
                builder.append(output);
            }
            builder.append(System.lineSeparator());
        }

        try {

            // change to second page
            new Select(driver.findElement(By.name("doclistpage"))).selectByIndex(1);
            driver.findElement(By.name("doclistpage")).click();

            // display the second page
            driver.findElement(By.name("dispsub")).click();

            // wait for page to load
            Thread.sleep(5000);

            // get results of second page
            baseTable = driver.findElement(By.xpath("/html/body/table[3]"));
            rows = baseTable.findElements(By.tagName("tr"));
            for (WebElement row : rows) {
                
                List<WebElement> items = row.findElements(By.tagName("td"));
                for (int j = 0; j < items.size(); j++) {

                    WebElement item = items.get(j);
                    String output = item.getText();
                    if (j > 0 && j < items.size() - 1) {
                        output += ",";
                    }
                    builder.append(output);
                }
                builder.append(System.lineSeparator());
            }

            // change to third page
            new Select(driver.findElement(By.name("doclistpage"))).selectByIndex(2);
            driver.findElement(By.name("doclistpage")).click();

            // display the third page
            driver.findElement(By.name("dispsub")).click();

            // wait for page to load
            Thread.sleep(5000);

            // get results of third page
            baseTable = driver.findElement(By.xpath("/html/body/table[3]"));
            rows = baseTable.findElements(By.tagName("tr"));
            for (WebElement row : rows) {
                
                List<WebElement> items = row.findElements(By.tagName("td"));
                for (int j = 0; j < items.size(); j++) {

                    WebElement item = items.get(j);
                    String output = item.getText();
                    if (j > 0 && j < items.size() - 1) {
                        output += ",";
                    }
                    builder.append(output);
                }
                builder.append(System.lineSeparator());
            }
        }
        catch (Exception exception) {

            // there may not be other pages
        }

        return builder.toString();
    }

    public void doSearchBatch() {

        List<String> excludes = new ArrayList<>();
        excludes.add("T0957s2TS145-D1");

        int EARLY_EXIT = 100;

        List<String> dbIds = Benchmarks.get("casp_d190");

        int count = 0;
        for (int i = 0; i < dbIds.size(); i++) {
            
            String dbId = dbIds.get(i);
            String fileNameRmsd = Constants.VAST_PATH_RMSD + dbId + ".txt";
            String fileNameVastScore = Constants.VAST_PATH_VAST_SCORE + dbId + ".txt";

            try {

                // for now lets not do the rmsd work again
                if (!isExcluded(excludes, dbId) && Files.notExists(Paths.get(fileNameRmsd))) {
            
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

    private boolean isExcluded(List<String> excludes, String dbId) {

        boolean excluded = false;
        for (String exclude : excludes) {
            if (dbId.startsWith(exclude)) {
                excluded = true;
                break;
            }
        }
        return excluded;
    }
}





