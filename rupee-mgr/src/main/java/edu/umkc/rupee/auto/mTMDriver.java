package edu.umkc.rupee.auto;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import edu.umkc.rupee.eval.Benchmarks;
import edu.umkc.rupee.lib.Constants;

public class mTMDriver extends DriverBase {

    private final boolean TIMING = false;
    private final int SUBMIT_TIMEOUT = 60;
    private final int SEARCH_TIMEOUT = 840;

    public void doSearch(String scopId) throws Exception {
        
        driver.get("http://yanglab.nankai.edu.cn/mTM-align/");

        // initial form fill
        driver.findElement(By.id("pdbid")).click();
        driver.findElement(By.id("pdbid")).clear();
        driver.findElement(By.id("pdbid")).sendKeys(scopId);

        // commented: search pdbc
        // uncommented: search dom
        driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Select database (version 2018-10-12)'])[1]/following::input[2]")).click();

        long start = 0, stop = 0;
        if (TIMING) {
            start = System.currentTimeMillis();
        }

        driver.findElement(By.id("submit")).click();

        // wait for submit response
        for (int second = 0;; second++) {
            
            if (second >= SUBMIT_TIMEOUT) {
                fail("submit timed out for " + scopId);
            }

            try {
                if (isElementPresent(By.cssSelector("a[href*='output']")))
                    break;
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
            
            if (second >= SEARCH_TIMEOUT) fail("search timed out for " + scopId);

                try {
                    if (isElementPresent(By.cssSelector("a[href='query.csv']")))
                        break;
                } catch (Exception e) {
            }
            Thread.sleep(1000);
        }

        if (TIMING) {

            stop = System.currentTimeMillis();
            System.out.println(scopId + "," + (stop -start));
        }
        else {
            
            // click it
            driver.findElement(By.cssSelector("a[href='query.csv']")).click();

            // give it sometime to download
            Thread.sleep(5000);

            // give it a good name
            Path from = Paths.get(Constants.DOWNLOAD_PATH + "query.csv");
            Path to = Paths.get(Constants.MTM_PATH + scopId + ".txt");
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    public void doSearchBatch() {

        List<String> dbIds = Benchmarks.get("scop_d62");

        for (int i = 0; i < dbIds.size(); i++) {
            
            String scopId = dbIds.get(i);
            String fileName = Constants.MTM_PATH + scopId + ".txt";

            if (Files.notExists(Paths.get(fileName))) {
                try {
                    
                    doSearch(scopId);
                    System.out.println((i+1) + ": Processed " + scopId);

                } catch (Exception e) {
                    Logger.getLogger(mTMDriver.class.getName()).log(Level.SEVERE, scopId, e);
                }
            }
        }
    }
}





