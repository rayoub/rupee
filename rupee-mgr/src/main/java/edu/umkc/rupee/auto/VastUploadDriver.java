package edu.umkc.rupee.auto;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import edu.umkc.rupee.lib.Benchmarks;
import edu.umkc.rupee.lib.Constants;

public class VastUploadDriver extends DriverBase {

    public void doSearch(String dbId) throws Exception {
        
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

        // get request id
        //String requestId = driver.findElement(By.xpath("/html/body/table[2]/tbody/tr[4]/td/b")).getText();
     
        // submit to search
        driver.findElement(By.xpath("/html/body/table[2]/tbody/tr[17]/td/table/tbody/tr/td[1]/input[2]")).click();
        
        // wait for page to load
        Thread.sleep(5000);

        // get the link of return page
        WebElement ele = driver.findElement(By.xpath("/html/body/table[2]/tbody/tr[7]/td/a"));
        String request = ele.getAttribute("href");

        // output to a file  
        System.out.println(dbId + "," + request); 
    }

    public void doSearchBatch() {

        List<String> dbIds = Benchmarks.get("casp_d250");

        for (int i = 0; i < dbIds.size(); i++) {
            
            String dbId = dbIds.get(i);
            String fileName = Constants.VAST_PATH + "casp_d250_casp_chain_v01_01_2020/" + dbId + ".txt";

            try {

                if (Files.notExists(Paths.get(fileName))) {
                    doSearch(dbId);
                }
            } 
            catch (Exception e) { 

                Logger.getLogger(VastUploadDriver.class.getName()).log(Level.SEVERE, dbId, e);
            }
        }
    }
}





