package edu.umkc.rupee.auto;

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

import edu.umkc.rupee.lib.Benchmarks;
import edu.umkc.rupee.lib.Constants;

public class SSMSearchDriver extends DriverBase {

    private final int SUBMIT_TIMEOUT = 60;

    public String doSearch(String scopId) throws Exception {
        
        driver.get("http://www.ebi.ac.uk/msd-srv/ssm/");

        // initial form fill
        driver.findElement(By.name("start_server_form")).click();
        driver.findElement(By.name("start_server")).click();
        new Select(driver.findElement(By.name("sel_struct1"))).selectByVisibleText("SCOP entry");
        driver.findElement(By.name("sel_struct1")).click();
        new Select(driver.findElement(By.name("sel_struct2"))).selectByVisibleText("All SCOP 1.73 archive");
        driver.findElement(By.name("sel_struct2")).click();
        driver.findElement(By.name("edt_scopcode1")).clear();
        driver.findElement(By.name("edt_scopcode1")).sendKeys(scopId);
        driver.findElement(By.name("btn_submit_query")).click();

        // wait for submit response
        for (int second = 0;; second++) {
            
            if (second >= SUBMIT_TIMEOUT) fail("search timed out for " + scopId);

            if (isElementPresent(By.name("download_rlist"))) {
                break;
            }
            else if (isElementPresent(By.name("submit"))) {
                throw new WebDriverException("Server low on disk space");
            }
            
            Thread.sleep(1000);
        }

        // download the data
        driver.findElement(By.name("download_rlist")).click();

        // give it some time to load
        Thread.sleep(2000);
  
        return driver.getPageSource(); 
    }

    public void doSearchBatch() {

        List<String> d193 = Benchmarks.get("scop_d193");

        for (int i = 0; i < d193.size(); i++) {
            
            String scopId = d193.get(i);
            String fileName = Constants.SSM_PATH + scopId + ".txt";

            if (Files.notExists(Paths.get(fileName))) {
                try {
                    
                    String source = doSearch(scopId);

                    FileOutputStream outputStream = new FileOutputStream(fileName);
                    OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);

                    try (BufferedWriter bufferedWriter = new BufferedWriter(outputWriter);) {
                           bufferedWriter.write(source);
                    }

                    System.out.println((i+1) + ": Processed " + scopId);

                } catch (Exception e) {
                    Logger.getLogger(SSMSearchDriver.class.getName()).log(Level.SEVERE, scopId, e);
                }
            }
        }
    }
}





