package com.saucedemo.automation.listeners;

import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.saucedemo.automation.base.BaseTest;
import com.saucedemo.automation.base.DriverFactory;
import com.saucedemo.automation.utils.ScreenshotUtil;

/**
 * Feeds TestNG results into the Extent Spark report. TestNG calls these methods
 * on the thread that ran the test, so the ThreadLocal driver and ExtentTest
 * are always the right ones for that browser.
 */
public class TestListener implements ITestListener {

    private static final Logger LOG = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        String browser = browserOf(result);
        ExtentTest test = ExtentManager.getInstance()
                .createTest("[" + browser + "] " + result.getMethod().getMethodName(),
                        result.getMethod().getDescription())
                .assignCategory(browser);
        ExtentTestManager.set(test);
        LOG.info("START [{}] {}", browser, result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTestManager.get().pass("Test passed");
        ExtentTestManager.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentTestManager.get();
        String browser = browserOf(result);
        if (test == null) {
            return;
        }
        if (DriverFactory.hasDriver()) {
            try {
                String base64 = ScreenshotUtil.asBase64(DriverFactory.getDriver());
                Path file = ScreenshotUtil.save(base64, browser, result.getMethod().getMethodName());
                test.fail(result.getThrowable(), MediaEntityBuilder
                        .createScreenCaptureFromBase64String(base64, "Failure - " + browser).build());
                LOG.error("FAIL [{}] {} - screenshot {}", browser, result.getMethod().getMethodName(), file);
            } catch (Exception e) {
                LOG.warn("Could not capture screenshot: {}", e.toString());
                test.fail(result.getThrowable());
            }
        } else {
            test.fail(result.getThrowable());
        }
        ExtentTestManager.remove();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentTestManager.get();
        if (test == null) {
            test = ExtentManager.getInstance()
                    .createTest("[" + browserOf(result) + "] " + result.getMethod().getMethodName())
                    .assignCategory(browserOf(result));
        }
        test.skip(result.getThrowable() == null ? "Skipped" : result.getThrowable().toString());
        ExtentTestManager.remove();
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.flush();
    }

    private static String browserOf(ITestResult result) {
        Object instance = result.getInstance();
        if (instance instanceof BaseTest base && base.getBrowser() != null) {
            return base.getBrowser();
        }
        String param = result.getTestContext().getCurrentXmlTest().getParameter("browser");
        return param == null ? "n/a" : param;
    }
}
