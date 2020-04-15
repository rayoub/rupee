package edu.umkc.rupee.auto;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import edu.umkc.rupee.lib.Constants;

public class VastDriver extends DriverBase {

    public static int ONE_SECOND = 1000; 
    public static int ONE_MINUTE = 60 * ONE_SECOND; 

    public String doSearchUpload(String dbId) throws InterruptedException {

        // go to search page
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
        Thread.sleep(5 * ONE_SECOND);

        // get request id
        //String requestId = driver.findElement(By.xpath("/html/body/table[2]/tbody/tr[4]/td/b")).getText();
     
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

        // go to linked page
        driver.get(link);

        // wait for page to load
        Thread.sleep(5000);

        // check if done
        List<WebElement> eles = driver.findElements(By.xpath("/html/body/table[2]/tbody/tr[2]/td"));
        if (eles.size() > 0) {

            System.out.println("... checking status");
            
            WebElement ele2 = eles.get(0);
            if (ele2.getText().equals("VAST Search Done")) {

                System.out.println("... status is done");

                // click link to results
                driver.findElement(By.linkText("entire chain")).click();

                // wait for page to load
                Thread.sleep(5000);

                // get results
                results.ResultsVastScore = getTableData(false);
                results.ResultsRmsd = getTableData(true);
            }
        }

        return results;
    }

    public String getTableData(boolean sortByRmsd) throws InterruptedException {

        StringBuilder builder = new StringBuilder("");

        int sortIndex = 1;
        if (sortByRmsd) {
            sortIndex = 3;
        }

        // form fill to get page of results
        new Select(driver.findElement(By.name("subset"))).selectByIndex(4);
        driver.findElement(By.name("subset")).click();
        new Select(driver.findElement(By.name("table"))).selectByIndex(1);
        driver.findElement(By.name("table")).click();
        new Select(driver.findElement(By.name("sort"))).selectByIndex(sortIndex);
        driver.findElement(By.name("sort")).click();
            
        // change to first page
        new Select(driver.findElement(By.name("doclistpage"))).selectByIndex(0);
        driver.findElement(By.name("doclistpage")).click();

        // display the first page
        driver.findElement(By.name("dispsub")).click();

        // wait for page to load
        Thread.sleep(5000);

        // get results of first page
        try {

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
        }
        catch (Exception e) {

            // probably not enough data
            System.out.println("... error occurred getting first page"); 
            return "";
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
            WebElement baseTable = driver.findElement(By.xpath("/html/body/table[3]"));
            List<WebElement> rows = baseTable.findElements(By.tagName("tr"));
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

    public boolean isExcluded(List<String> excludes, String dbId) {

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
