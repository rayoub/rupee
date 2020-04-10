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

import org.javatuples.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import edu.umkc.rupee.lib.Constants;
import edu.umkc.rupee.lib.Db;

public class VastDriver extends DriverBase {

    public String doSearch(String dbId, String request) throws Exception {
        
        StringBuilder builder = new StringBuilder("");

        driver.get(request);

        // wait for page to load
        Thread.sleep(5000);

        // check if done
        List<WebElement> eles = driver.findElements(By.xpath("/html/body/table[2]/tbody/tr[2]/td"));
        if (eles.size() > 0) {

            System.out.println("found status text");
            
            WebElement ele2 = eles.get(0);
            if (ele2.getText().equals("VAST Search Done")) {

                System.out.println("status is done");

                // click link to results
                driver.findElement(By.linkText("entire chain")).click();

                // wait for page to load
                Thread.sleep(5000);

                // form fill
                new Select(driver.findElement(By.name("subset"))).selectByIndex(4);
                driver.findElement(By.name("subset")).click();
                new Select(driver.findElement(By.name("sort"))).selectByIndex(3);
                driver.findElement(By.name("sort")).click();
                new Select(driver.findElement(By.name("table"))).selectByIndex(1);
                driver.findElement(By.name("table")).click();

                // display the first page
                driver.findElement(By.name("dispsub")).click();

                // wait for page to load
                Thread.sleep(5000);

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

                // changing to second page
                new Select(driver.findElement(By.name("doclistpage"))).selectByIndex(1);
                driver.findElement(By.name("doclistpage")).click();

                // display the second page
                driver.findElement(By.name("dispsub")).click();

                // wait for page to load
                Thread.sleep(5000);

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
        }

        return builder.toString();
    }

    public void doSearchBatch() {

        List<String> excludes = new ArrayList<>();
//        excludes.add("T1022");
//        excludes.add("T0989");
//        excludes.add("T0968");
//        excludes.add("T0953");
//        excludes.add("T0957");
//        excludes.add("T0969");

        List<Pair<String,String>> pairs = Db.getVastRequestIds(); 

        int processed = 0;
        for (int i = 0; i < pairs.size(); i++) {

            Pair<String,String> pair = pairs.get(i);
            String dbId = pair.getValue0();
            String requestId = pair.getValue1();
            String fileName = Constants.VAST_PATH + "temp/" + dbId + ".txt";

            if (!isExcluded(excludes, dbId) && Files.notExists(Paths.get(fileName))) {
            
                System.out.println("Processing request for: " + dbId);

                try {

                    String source = doSearch(dbId, requestId);

                    if (!source.isEmpty()) {
                        FileOutputStream outputStream = new FileOutputStream(fileName);
                        OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);

                        try (BufferedWriter bufferedWriter = new BufferedWriter(outputWriter);) {
                               bufferedWriter.write(source);
                        }
                   
                        processed++;
                        System.out.println(processed + ": Processed request for: " + dbId);
                    }
                } 
                catch (Exception e) { 

                    Logger.getLogger(VastDriver.class.getName()).log(Level.SEVERE, dbId, e);
                }
            }
        }
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





