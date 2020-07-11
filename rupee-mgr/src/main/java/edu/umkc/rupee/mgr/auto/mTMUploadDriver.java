package edu.umkc.rupee.mgr.auto;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import edu.umkc.rupee.mgr.eval.Benchmarks;
import edu.umkc.rupee.mgr.lib.Constants;

public class mTMUploadDriver extends DriverBase {

    private final int SUBMIT_TIMEOUT = 60;
    private final int SEARCH_TIMEOUT = 840;

    public void doSearch(String dbId) throws Exception {
        
        driver.get("http://yanglab.nankai.edu.cn/mTM-align/");

        // file upload
        driver.findElement(By.name("pdb_file")).clear();
        driver.findElement(By.name("pdb_file")).sendKeys(Constants.CASP_PATH + dbId + ".pdb");

        // commented: search pdbc
        // uncommented: search dom
        //driver.findElement(By.cssSelector("#optionForm3 > input:nth-child(5)")).click(); 
        
        driver.findElement(By.id("submit")).click();

        // wait for submit response
        for (int second = 0;; second++) {
            
            if (second >= SUBMIT_TIMEOUT) {
                fail("submit timed out for " + dbId);
            }

            try {
                if (isElementPresent(By.cssSelector("a[href*='output']"))) break;
            } catch (Exception e) { }
            Thread.sleep(1000);
        }
       
        // get the job id
        WebElement element = driver.findElement(By.cssSelector("a[href*='output']"));
        String[] parts = element.getAttribute("href").split("/");
        String jobId = parts[parts.length - 1];

        // debug output
        System.out.println("Job Id: " + jobId);

        // now follow it to the wait page
        element.click();
        
        // now wait for the search results 
        for (int second = 0;; second++) {
            
            if (second >= SEARCH_TIMEOUT) fail("search timed out for " + dbId);

                try {
                    if (isElementPresent(By.cssSelector("a[href='query.csv']")))
                        break;
                } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        // give it sometime to download
        Thread.sleep(5000);

        // delete if already present
        Path from = Paths.get(Constants.DOWNLOAD_PATH + "query.csv");
        Files.deleteIfExists(from);

        // click it
        driver.findElement(By.cssSelector("a[href='query.csv']")).click();

        // give it sometime to download
        Thread.sleep(5000);

        // give it a good name
        Path to = Paths.get(Constants.MTM_PATH + dbId + ".txt");
        Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
    }
    
    public void doSearchBatch() {

        List<String> dbIds = Benchmarks.get("casp_d250");

        for (int i = 0; i < dbIds.size(); i++) {
            
            String dbId = dbIds.get(i);
            String fileName = Constants.MTM_PATH + dbId + ".txt";

            if (Files.notExists(Paths.get(fileName))) {
                try {
                    
                    doSearch(dbId);
                    System.out.println((i+1) + ": Processed " + dbId);

                } catch (Exception e) {
                    Logger.getLogger(mTMUploadDriver.class.getName()).log(Level.SEVERE, dbId, e);
                }
            }
        }
    }
}





