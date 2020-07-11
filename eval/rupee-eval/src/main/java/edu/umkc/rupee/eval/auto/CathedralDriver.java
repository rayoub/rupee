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
import org.openqa.selenium.Keys;

import edu.umkc.rupee.eval.lib.Benchmarks;
import edu.umkc.rupee.eval.lib.Constants;

public class CathedralDriver extends DriverBase {

    private final int SUBMIT_TIMEOUT = 3600; // 1 hour
    private final String SUBMIT_XPATH = "(.//*[normalize-space(text()) and normalize-space(.)='Search CATH'])[1]/following::input[2]";
    private final String SCAN_XPATH = "//*[@id=\"content\"]/div[1]/div/div/table/tbody/tr/td[5]/div/a[contains(text(),'Submit Structure')]";
    //private final String VIEW_XPATH = "//*[@id=\"content\"]/div[1]/div/table/tbody/tr[last()]/td[5]/a[text()='View']";

    public String doSearch(String dbId) throws Exception {

        driver.manage().deleteAllCookies();
       
        driver.get("http://cathdb.info/search/by_structure");

        // initial form fill
        driver.findElement(By.id("search-pdb-file")).sendKeys(Constants.CASP_PATH + dbId + ".pdb");

        // submit search
        driver.findElement(By.xpath(SUBMIT_XPATH)).click();
        
        Thread.sleep(5000);
       
        // scroll down 
        //Actions actions = new Actions(driver);
        //actions.moveToElement(driver.findElement(By.xpath(SCAN_XPATH))).perform();

        // click submit structure for scan
        driver.findElement(By.xpath(SCAN_XPATH)).sendKeys(Keys.RETURN);

        Thread.sleep(5000);
        
        // and now go to monitor the progress
        driver.findElement(By.linkText("Monitor progress of scan")).click();

        long start = 0, stop = 0;
        start = System.currentTimeMillis();

        // wait for a reload so that the new line item appears
        Thread.sleep(20000);

        // wait for new line item to complete search
        for (int second = 0;; second++) {
            
            if (second >= SUBMIT_TIMEOUT) {
                fail("submit timed out for " + dbId);
            }

            try {
                if (isElementPresent(By.linkText("View")))
                    break;
            } catch (Exception e) { }
            Thread.sleep(1000);
        }

        stop = System.currentTimeMillis();
        System.out.println(dbId + "," + (stop - start));

        // click
        driver.findElement(By.linkText("View")).click();

        Thread.sleep(20000);

        String source = driver.findElement(By.id("results-table")).getText();
        // source = "Time = " + (stop - start) + "\n" + source;

        return source;
    }

    public void doSearchBatch() {

        List<String> dbIds = Benchmarks.get("casp_d250");

        for (int i = 0; i < dbIds.size(); i++) {
            
            String dbId = dbIds.get(i);
            String fileName = Constants.CATHEDRAL_PATH + dbId + ".txt";

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
                    Logger.getLogger(CathedralDriver.class.getName()).log(Level.SEVERE, dbId, e);
                }
            }
        }
    }
}





