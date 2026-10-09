package com.saucedemo.automation.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CartPage extends BasePage {

    private static final By CART_ITEM = By.cssSelector(".cart_item");
    private static final By ITEM_NAME = By.cssSelector(".inventory_item_name");
    private static final By CHECKOUT = By.id("checkout");

    public CartPage(WebDriver driver) {
        super(driver);
        wait.until(ExpectedConditions.urlContains("cart.html"));
    }

    public List<String> itemNames() {
        List<WebElement> items = driver.findElements(CART_ITEM);
        return items.stream().map(i -> i.findElement(ITEM_NAME).getText().trim()).toList();
    }

    public CheckoutPage checkout() {
        click(CHECKOUT);
        return new CheckoutPage(driver);
    }
}
