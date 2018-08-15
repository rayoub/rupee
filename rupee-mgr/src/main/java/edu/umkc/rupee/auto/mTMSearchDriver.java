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

import edu.umkc.rupee.lib.Constants;

public class mTMSearchDriver extends DriverBase {

    private final int SUBMIT_TIMEOUT = 60;
    private final int SEARCH_TIMEOUT = 720;

    public void doSearch(String scopId) throws Exception {
        
        driver.get("http://yanglab.nankai.edu.cn/mTM-align/");

        // initial form fill
        driver.findElement(By.id("pdbid")).click();
        driver.findElement(By.id("pdbid")).clear();
        driver.findElement(By.id("pdbid")).sendKeys(scopId);

        // commented: search pdbc
        // uncommented: search dom
        //driver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Select database (version 2018-08-03)'])[1]/following::input[2]")).click();
        
        driver.findElement(By.id("submit")).click();

        // wait for submit response
        for (int second = 0;; second++) {
            
            if (second >= SUBMIT_TIMEOUT) fail("submit timed out for " + scopId);

                try {
                    if (isElementPresent(By.cssSelector("a[href*='output']")))
                        break;
                } catch (Exception e) {
            }
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

        // click it
        driver.findElement(By.cssSelector("a[href='query.csv']")).click();

        // give it sometime to download
        Thread.sleep(5000);

        // give it a good name
        Path from = Paths.get(Constants.DOWNLOAD_PATH + "query.csv");
        Path to = Paths.get(Constants.MTM_PATH + scopId + ".txt");
        Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
    }
    
    public void doSearchBatch() {

        List<String> d500 = Benchmarks.getD500();

        for (int i = 0; i < d500.size(); i++) {
            
            String scopId = d500.get(i);
            String fileName = Constants.MTM_PATH + scopId + ".txt";

            if (Files.notExists(Paths.get(fileName))) {
                try {
                    
                    doSearch(scopId);
                    System.out.println((i+1) + ": Processed " + scopId);

                } catch (Exception e) {
                    Logger.getLogger(SSMSearchDriver.class.getName()).log(Level.SEVERE, scopId, e);
                }
            }
        }
    }
}





