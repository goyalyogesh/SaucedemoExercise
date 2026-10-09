package com.saucedemo.automation.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

/** One ExtentReports instance for the whole run, written to target/reports/spark.html. */
public final class ExtentManager {

    public static final String REPORT_DIR = "target/reports";
    public static final String REPORT_FILE = REPORT_DIR + "/spark.html";

    private static ExtentReports extent;

    private ExtentManager() {
    }

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_FILE);
            spark.config().setDocumentTitle("Sauce Demo E2E");
            spark.config().setReportName("Sauce Demo Parallel E2E Data Pipeline");
            spark.config().setTheme(Theme.STANDARD);
            spark.config().setOfflineMode(true);

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java", System.getProperty("java.version"));
            extent.setSystemInfo("Site", "https://www.saucedemo.com/");
        }
        return extent;
    }

    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
        }
    }
}
