package com.saucedemo.automation.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.saucedemo.automation.model.Product;

public class InventoryPage extends BasePage {

    static final By ITEM = By.cssSelector(".inventory_item");
    private static final By ITEM_NAME = By.cssSelector(".inventory_item_name");
    private static final By ITEM_PRICE = By.cssSelector(".inventory_item_price");
    private static final By ITEM_BUTTON = By.cssSelector("button.btn_inventory");
    private static final By PAGE_TITLE = By.cssSelector(".title");
    private static final By CART_LINK = By.cssSelector(".shopping_cart_link");
    private static final By CART_BADGE = By.cssSelector(".shopping_cart_badge");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    /** Waits until the inventory is on screen, then returns the page heading. */
    public String heading() {
        return textOf(PAGE_TITLE);
    }

    public List<WebElement> itemCards() {
        return wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(ITEM, 0));
    }

    /** Scrapes every product card: title and price. */
    public List<Product> scrapeProducts() {
        return itemCards().stream()
                .map(card -> new Product(
                        card.findElement(ITEM_NAME).getText().trim(),
                        parsePrice(card.findElement(ITEM_PRICE).getText())))
                .toList();
    }

    public void addToCart(String productTitle) {
        WebElement card = itemCards().stream()
                .filter(c -> c.findElement(ITEM_NAME).getText().trim().equals(productTitle))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No product named: " + productTitle));
        wait.until(ExpectedConditions.elementToBeClickable(card.findElement(ITEM_BUTTON))).click();
    }

    /**
     * W3C Actions: scrolls to each card and hovers over it.
     * Returns how many cards were visited.
     */
    public int scrollAndHoverOverCards() {
        Actions actions = new Actions(driver);
        List<WebElement> cards = itemCards();
        for (WebElement card : cards) {
            actions.scrollToElement(card).moveToElement(card).perform();
        }
        return cards.size();
    }

    /** Waits until the element's centre point is on screen (where a hover lands); false on timeout. */
    public boolean waitUntilOnScreen(WebElement element) {
        try {
            return wait.until(d -> isOnScreen(element));
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }

    /** True when the element's centre point is inside the visible window right now. */
    public boolean isOnScreen(WebElement element) {
        Object result = ((JavascriptExecutor) driver).executeScript(
                "var r = arguments[0].getBoundingClientRect();"
                        + "var y = (r.top + r.bottom) / 2; return y >= 0 && y <= window.innerHeight;", element);
        return Boolean.TRUE.equals(result);
    }

    public int cartCount() {
        List<WebElement> badge = driver.findElements(CART_BADGE);
        return badge.isEmpty() ? 0 : Integer.parseInt(badge.get(0).getText().trim());
    }

    public CartPage openCart() {
        click(CART_LINK);
        return new CartPage(driver);
    }

    static double parsePrice(String text) {
        return Double.parseDouble(text.replace("$", "").trim());
    }
}
