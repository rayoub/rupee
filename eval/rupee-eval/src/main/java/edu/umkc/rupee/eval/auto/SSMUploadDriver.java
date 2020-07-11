package edu.umkc.rupee.eval.auto;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.Select;

import edu.umkc.rupee.eval.lib.Benchmarks;
import edu.umkc.rupee.eval.lib.Constants;

public class SSMUploadDriver extends DriverBase {

    private final int SUBMIT_TIMEOUT = 60;

    public String doSearch(String dbId) throws Exception {
        
        driver.get("http://www.ebi.ac.uk/msd-srv/ssm/");

        // initial form fill
        driver.findElement(By.name("start_server_form")).click();
        driver.findElement(By.name("start_server")).click();
        new Select(driver.findElement(By.name("sel_struct1"))).selectByVisibleText("Coordinate file");
        driver.findElement(By.name("sel_struct1")).click();
        new Select(driver.findElement(By.name("sel_struct2"))).selectByVisibleText("All SCOP 1.73 archive");
        driver.findElement(By.name("sel_struct2")).click();
        driver.findElement(By.name("edt_percent1")).clear();
        driver.findElement(By.name("edt_percent1")).sendKeys("20");
        driver.findElement(By.name("edt_percent1")).clear();
        driver.findElement(By.name("edt_percent1")).sendKeys("20");
        driver.findElement(By.name("edt_percent2")).clear();
        driver.findElement(By.name("edt_percent2")).sendKeys("20");
        driver.findElement(By.name("file1")).clear();
        driver.findElement(By.name("file1")).sendKeys(Constants.CASP_PATH + dbId + ".pdb");
        driver.findElement(By.name("ckb_best_match")).click(); // uncheck the default of checked
        driver.findElement(By.name("ckb_unique_match")).click(); // uncheck the default of checked

        // switch this up based on "rmsd" or "Q-score"
        new Select(driver.findElement(By.name("sel_sorting"))).selectByVisibleText("rmsd");
        driver.findElement(By.name("sel_sorting")).click();

        // click submit
        driver.findElement(By.name("btn_submit_query")).click();
        
        // wait for submit response
        for (int second = 0;; second++) {
            
            if (second >= SUBMIT_TIMEOUT) {
                fail("search timed out for " + dbId);
            } 

            if (isElementPresent(By.name("download_rlist"))) {
                break;
            }
            else if (isElementPresent(By.name("submit"))) {
                throw new WebDriverException("Server low on disk space");
            }
            
            Thread.sleep(5000);
        }

        // download the data
        driver.findElement(By.name("download_rlist")).click();

        // give it some time to load
        Thread.sleep(30000);
  
        return driver.getPageSource(); 
    }

    public void doSearchBatch() {

        List<String> dbIds = Benchmarks.get("casp_d250");

        for (int i = 0; i < dbIds.size(); i++) {
            
            String dbId = dbIds.get(i);
            String fileName = Constants.SSM_PATH + dbId + ".txt";

            if (Files.notExists(Paths.get(fileName))) {
                try {
                    
                    String source = doSearch(dbId);

                    FileOutputStream outputStream = new FileOutputStream(fileName);
                    OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);

                    try (BufferedWriter bufferedWriter = new BufferedWriter(outputWriter);) {
                           bufferedWriter.write(source);
                    }

                    System.out.println((i+1) + ": Processed " + dbId);

                } catch (Exception e) {
                    Logger.getLogger(SSMDriver.class.getName()).log(Level.SEVERE, dbId, e);
                }
            }
        }
    }
}





