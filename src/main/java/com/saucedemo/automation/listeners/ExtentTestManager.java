package com.saucedemo.automation.listeners;

import com.aventstack.extentreports.ExtentTest;

/**
 * Keeps the current ExtentTest per thread, so steps logged by the Chrome and
 * Firefox threads always land in their own test entry.
 */
public final class ExtentTestManager {

    private static final ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();

    private ExtentTestManager() {
    }

    static void set(ExtentTest test) {
        CURRENT.set(test);
    }

    static void remove() {
        CURRENT.remove();
    }

    public static ExtentTest get() {
        return CURRENT.get();
    }

    /** Records a passed step in the report (does nothing if no report test is active). */
    public static void pass(String message) {
        ExtentTest test = CURRENT.get();
        if (test != null) {
            test.pass(message);
        }
    }

    /** Records an informational step in the report. */
    public static void info(String message) {
        ExtentTest test = CURRENT.get();
        if (test != null) {
            test.info(message);
        }
    }
}
