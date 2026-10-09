package com.saucedemo.automation.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public final class ScreenshotUtil {

    private static final Path DIR = Paths.get("target", "reports", "screenshots");

    private ScreenshotUtil() {
    }

    /** Screenshot of the given driver as a Base64 string, ready to embed in Extent. */
    public static String asBase64(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    }

    /**
     * Saves the screenshot as a PNG. The name holds the browser, thread id and a
     * nano timestamp, so parallel threads can never overwrite each other's files.
     */
    public static Path save(String base64, String browser, String testName) throws IOException {
        Files.createDirectories(DIR);
        String fileName = "%s_%s_t%d_%d.png".formatted(
                browser, testName, Thread.currentThread().threadId(), System.nanoTime());
        Path file = DIR.resolve(fileName);
        Files.write(file, Base64.getDecoder().decode(base64));
        return file;
    }
}
