package com.saucedemo.automation.pages;

import static org.openqa.selenium.support.locators.RelativeLocator.with;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CheckoutPage extends BasePage {

    private static final By FIRST_NAME = By.id("first-name");
    private static final By POSTAL_CODE = By.id("postal-code");
    private static final By CONTINUE = By.id("continue");
    private static final By FINISH = By.id("finish");
    private static final By COMPLETE_HEADER = By.cssSelector(".complete-header");

    public CheckoutPage(WebDriver driver) {
        super(driver);
        wait.until(ExpectedConditions.urlContains("checkout-step-one.html"));
    }

    public CheckoutPage enterFirstName(String firstName) {
        type(FIRST_NAME, firstName);
        return this;
    }

    /** Selenium 4 relative locator: the input sitting below the First Name field. */
    public WebElement lastNameField() {
        visible(FIRST_NAME);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                with(By.tagName("input")).below(FIRST_NAME)));
    }

    public CheckoutPage enterLastName(String lastName) {
        WebElement field = lastNameField();
        field.clear();
        field.sendKeys(lastName);
        return this;
    }

    public CheckoutPage enterPostalCode(String postalCode) {
        type(POSTAL_CODE, postalCode);
        return this;
    }

    public CheckoutPage continueToOverview() {
        click(CONTINUE);
        wait.until(ExpectedConditions.urlContains("checkout-step-two.html"));
        return this;
    }

    public CheckoutPage finish() {
        click(FINISH);
        return this;
    }

    public String confirmationMessage() {
        return textOf(COMPLETE_HEADER);
    }
}
