package com.saucedemo.automation.pages;

import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.saucedemo.automation.utils.WindowUtils;

/** The social links in the page footer. */
public class FooterPage extends BasePage {

    public enum Social {
        /** Twitter was renamed X; the site's footer link is now data-test="social-x". */
        TWITTER("[data-test='social-x']"),
        FACEBOOK("[data-test='social-facebook']"),
        LINKEDIN("[data-test='social-linkedin']");

        private final By locator;

        Social(String css) {
            this.locator = By.cssSelector(css);
        }
    }

    /** What was seen in the tab that a footer link opened. */
    public record TabResult(Social social, Set<String> handlesWhileOpen, String parentHandle,
            String newHandle, String title, String url) {
    }

    public FooterPage(WebDriver driver) {
        super(driver);
    }

    public String hrefOf(Social social) {
        return visible(social.locator).getDomAttribute("href");
    }

    /**
     * Clicks the footer link (it opens in a new tab), switches to that tab, reads
     * its title and url, closes it and returns to the parent window.
     */
    public TabResult openInNewTabAndClose(Social social) {
        String parent = driver.getWindowHandle();
        // The footer is fixed to the bottom of the window, so no scrolling is needed
        WebElement link = visible(social.locator);

        Set<String> before = WindowUtils.snapshot(driver);
        link.click();

        String newHandle = WindowUtils.switchToNewTab(driver, before);
        Set<String> whileOpen = WindowUtils.snapshot(driver);
        String title = driver.getTitle();
        String url = driver.getCurrentUrl();

        WindowUtils.closeAndReturnTo(driver, parent);
        return new TabResult(social, whileOpen, parent, newHandle, title, url);
    }
}
