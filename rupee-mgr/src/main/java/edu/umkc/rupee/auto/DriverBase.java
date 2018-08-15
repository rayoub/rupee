package edu.umkc.rupee.auto;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;

public class DriverBase {

    private final int IMPLICIT_TIMEOUT = 30;

    protected WebDriver driver;
    private StringBuffer verificationErrors = new StringBuffer();

    public void setUp() throws Exception {

        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(IMPLICIT_TIMEOUT, TimeUnit.SECONDS);
    }

    public void tearDown() throws Exception {

        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

    protected boolean isElementPresent(By by) {

        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected void fail(String message) {

        throw new WebDriverException(message);
    }
}


