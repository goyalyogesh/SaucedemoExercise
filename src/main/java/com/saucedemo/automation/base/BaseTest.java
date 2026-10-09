package com.saucedemo.automation.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Starts one browser per test class instance. With parallel="tests" every
 * &lt;test&gt; in the suite gets its own instance, thread and driver.
 */
public abstract class BaseTest {

    protected final Logger log = LogManager.getLogger(getClass());
    protected String browser;

    @Parameters("browser")
    @BeforeClass(alwaysRun = true)
    public void setUp(@Optional("chrome") String browser) {
        this.browser = browser;
        DriverFactory.initDriver(browser);
        log.info("Started {} on thread {}", browser, Thread.currentThread().getName());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
        log.info("Closed {}", browser);
    }

    protected WebDriver driver() {
        return DriverFactory.getDriver();
    }

    public String getBrowser() {
        return browser;
    }
}
