package com.saucedemo.automation.utils;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Tab helpers. Everything works on the driver passed in, so each thread
 * keeps its own parent window and never touches another thread's tabs.
 */
public final class WindowUtils {

    private WindowUtils() {
    }

    public static Set<String> handles(WebDriver driver) {
        return driver.getWindowHandles();
    }

    /** Snapshot of the handles now, to compare against after a click opens a tab. */
    public static Set<String> snapshot(WebDriver driver) {
        return new HashSet<>(driver.getWindowHandles());
    }

    /** Waits for a tab that is not in {@code before}, switches to it and returns its handle. */
    public static String switchToNewTab(WebDriver driver, Set<String> before) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("wait.seconds")));
        wait.until(d -> d.getWindowHandles().size() > before.size());
        Set<String> added = new HashSet<>(driver.getWindowHandles());
        added.removeAll(before);
        String newHandle = added.iterator().next();
        driver.switchTo().window(newHandle);
        waitForPageLoad(driver);
        return newHandle;
    }

    /** Selenium 4: creates a blank tab, switches to it and loads the url. */
    public static String openInNewTab(WebDriver driver, String url) {
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get(url);
        waitForPageLoad(driver);
        return driver.getWindowHandle();
    }

    /** Closes the current tab and goes back to the parent window. */
    public static void closeAndReturnTo(WebDriver driver, String parentHandle) {
        driver.close();
        driver.switchTo().window(parentHandle);
    }

    private static void waitForPageLoad(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("wait.seconds")));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe("about:blank")));
        // Third-party pages (x.com, linkedin.com) can stay short of "complete" for a long
        // time, so "interactive" is enough: the document is parsed and has a title.
        wait.until(d -> !"loading".equals(
                ((org.openqa.selenium.JavascriptExecutor) d).executeScript("return document.readyState")));
    }
}
