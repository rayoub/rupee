package edu.umkc.rupee.auto;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;

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
   
        // submit 
        driver.findElement(By.name("cmdVSMmdb")).click();

        // wait for page to load
        Thread.sleep(10000);

        String requestId = driver.findElement(By.xpath("/html/body/table[2]/tbody/tr[4]/td/b")).getText();
       
        System.out.println(dbId + "," + requestId); 
    }

    public void doSearchBatch() {

        List<String> dbIds = Benchmarks.get("casp_d250");

        for (int i = 0; i < dbIds.size(); i++) {
            
            String dbId = dbIds.get(i);

            try {

                doSearch(dbId);
            } 
            catch (Exception e) { 

                Logger.getLogger(VastUploadDriver.class.getName()).log(Level.SEVERE, dbId, e);
            }
        }
    }
}





