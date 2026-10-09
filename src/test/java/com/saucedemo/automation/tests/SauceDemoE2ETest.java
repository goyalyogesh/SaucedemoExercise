package com.saucedemo.automation.tests;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.saucedemo.automation.base.BaseTest;
import com.saucedemo.automation.listeners.ExtentTestManager;
import com.saucedemo.automation.model.Product;
import com.saucedemo.automation.pages.CartPage;
import com.saucedemo.automation.pages.CheckoutPage;
import com.saucedemo.automation.pages.FooterPage;
import com.saucedemo.automation.pages.FooterPage.Social;
import com.saucedemo.automation.pages.FooterPage.TabResult;
import com.saucedemo.automation.pages.InventoryPage;
import com.saucedemo.automation.pages.LoginPage;
import com.saucedemo.automation.utils.CheckoutQueue;
import com.saucedemo.automation.utils.ConfigReader;
import com.saucedemo.automation.utils.InventoryState;
import com.saucedemo.automation.utils.ProductUtils;
import com.saucedemo.automation.utils.WindowUtils;

/**
 * Login -> extract products -> stream filter -> queue -> checkout, run once per
 * browser. The steps are separate test methods chained with dependsOnMethods, so
 * the report shows each step on its own and a failure stops the rest of that browser.
 * Fields are safe to use: parallel="tests" gives each browser its own instance.
 */
public class SauceDemoE2ETest extends BaseTest {

    private static final List<String> EXPECTED_ORDER = List.of(
            "Sauce Labs Fleece Jacket",
            "Sauce Labs Backpack",
            "Sauce Labs Bolt T-Shirt",
            "Test.allTheThings() T-Shirt (Red)");

    private InventoryPage inventory;
    private List<Product> allProducts;
    private List<Product> filtered;
    private CheckoutQueue queue;
    private CartPage cart;
    private CheckoutPage checkout;

    @Test(description = "Log in as standard_user")
    public void step01_login() {
        inventory = new LoginPage(driver())
                .open(ConfigReader.get("base.url"))
                .loginAs(ConfigReader.get("username"), ConfigReader.get("password"));
        Assert.assertEquals(inventory.heading(), "Products", "inventory page heading");
        ExtentTestManager.pass("Logged in as " + ConfigReader.get("username"));
    }

    @Test(dependsOnMethods = "step01_login", description = "Scrape every .inventory_item card")
    public void step02_extractAllProducts() {
        allProducts = inventory.scrapeProducts();
        Assert.assertEquals(allProducts.size(), 6, "number of products on the page");
        ExtentTestManager.pass("Titles: " + ProductUtils.titles(allProducts));
        ExtentTestManager.pass("Prices: " + ProductUtils.prices(allProducts));
    }

    @Test(dependsOnMethods = "step02_extractAllProducts", description = "Stream filter, then sort by price descending")
    public void step03_filterAndSortWithStreams() {
        double threshold = ConfigReader.getDouble("price.threshold");
        filtered = ProductUtils.filterAndSort(allProducts, threshold);

        Assert.assertTrue(filtered.stream().allMatch(p -> p.price() >= threshold),
                "nothing priced below " + threshold + " may remain");
        for (int i = 1; i < filtered.size(); i++) {
            Assert.assertTrue(filtered.get(i - 1).price() >= filtered.get(i).price(),
                    "prices must descend at position " + i);
        }
        Assert.assertEquals(ProductUtils.titles(filtered), EXPECTED_ORDER);
        ExtentTestManager.pass("Kept " + filtered.size() + " of " + allProducts.size()
                + " products priced at or above $" + threshold + ": " + filtered);
    }

    @Test(dependsOnMethods = "step03_filterAndSortWithStreams", description = "Keep filtered products in a ConcurrentHashMap")
    public void step04_storeInConcurrentHashMap() {
        Map<String, Double> products = ProductUtils.toTitlePriceMap(filtered);
        InventoryState.store(browser, products);

        Assert.assertEquals(InventoryState.forBrowser(browser), products,
                "this browser's slice of the shared map");
        ExtentTestManager.pass("Stored " + products.size() + " entries under key prefix '" + browser
                + "|' (shared map now holds " + InventoryState.size() + " entries)");
    }

    @Test(dependsOnMethods = "step04_storeInConcurrentHashMap", description = "Push product names into an ArrayDeque")
    public void step05_queueProductNames() {
        queue = new CheckoutQueue(ProductUtils.titles(filtered));
        Assert.assertEquals(queue.size(), filtered.size());
        ExtentTestManager.pass("Queued " + queue.size() + " product names");
    }

    @Test(dependsOnMethods = "step05_queueProductNames", description = "Scroll to and hover over every inventory card")
    public void step06_scrollAndHoverOverCards() {
        Assert.assertEquals(inventory.scrollAndHoverOverCards(), allProducts.size());
        List<WebElement> cards = inventory.itemCards();
        Assert.assertTrue(inventory.waitUntilOnScreen(cards.get(cards.size() - 1)),
                "last card should be on screen after scrolling and hovering");
        ExtentTestManager.pass("Scrolled to and hovered over " + cards.size() + " cards");
    }

    @Test(dependsOnMethods = "step06_scrollAndHoverOverCards", description = "Footer links open in new tabs; verify distinct handles")
    public void step07_footerLinksOpenInDistinctTabs() {
        FooterPage footer = new FooterPage(driver());
        String parent = driver().getWindowHandle();
        Set<String> allHandlesSeen = new HashSet<>();
        allHandlesSeen.add(parent);

        for (Social social : Social.values()) {
            TabResult result = footer.openInNewTabAndClose(social);

            Assert.assertEquals(result.handlesWhileOpen().size(), 2, social + ": parent + one new tab");
            Assert.assertTrue(result.handlesWhileOpen().contains(parent), social + ": parent still open");
            Assert.assertTrue(allHandlesSeen.add(result.newHandle()), social + ": handle must be distinct");
            Assert.assertTrue(hostOf(result.url()).contains(expectedDomain(social)),
                    social + " opened " + result.url());
            Assert.assertEquals(WindowUtils.handles(driver()), Set.of(parent), social + ": tab closed");
            ExtentTestManager.pass(social + " tab title='" + result.title() + "' url=" + result.url());
        }
        Assert.assertEquals(driver().getWindowHandle(), parent, "back on the parent window");
        ExtentTestManager.pass("3 distinct tab handles seen, parent window kept");
    }

    @Test(dependsOnMethods = "step07_footerLinksOpenInDistinctTabs", description = "driver.switchTo().newWindow(WindowType.TAB)")
    public void step08_newWindowTabApi() {
        FooterPage footer = new FooterPage(driver());
        String parent = driver().getWindowHandle();
        // read the links while the parent tab is still the active one
        List<String> hrefs = Arrays.stream(Social.values()).map(footer::hrefOf).toList();

        Set<String> seen = new HashSet<>(WindowUtils.handles(driver()));
        for (String href : hrefs) {
            String handle = WindowUtils.openInNewTab(driver(), href);
            Assert.assertTrue(seen.add(handle), "handle must be new and distinct: " + handle);
            ExtentTestManager.info("Opened " + href + " title='" + driver().getTitle() + "'");
        }
        Assert.assertEquals(WindowUtils.handles(driver()).size(), 1 + hrefs.size(), "parent + new tabs");
        Assert.assertEquals(WindowUtils.handles(driver()), seen);

        for (String handle : seen) {
            if (!handle.equals(parent)) {
                driver().switchTo().window(handle);
                driver().close();
            }
        }
        driver().switchTo().window(parent);
        Assert.assertEquals(WindowUtils.handles(driver()), Set.of(parent));
        ExtentTestManager.pass("Opened " + hrefs.size() + " tabs with newWindow(TAB), closed them, parent intact");
    }

    @Test(dependsOnMethods = "step08_newWindowTabApi", description = "pollFirst() each queued product into the cart")
    public void step09_processQueueIntoCart() {
        int expected = queue.size();
        String name;
        int added = 0;
        while ((name = queue.next()) != null) {
            inventory.addToCart(name);
            added++;
            Assert.assertEquals(inventory.cartCount(), added, "cart badge after adding " + name);
            ExtentTestManager.info("Polled and added: " + name);
        }
        Assert.assertTrue(queue.isEmpty(), "queue should be empty");
        Assert.assertEquals(added, expected);
        ExtentTestManager.pass("Processed " + added + " queued products in order; cart badge = " + inventory.cartCount());
    }

    @Test(dependsOnMethods = "step09_processQueueIntoCart", description = "Cart contents and checkout information form")
    public void step10_cartAndCheckoutInformation() {
        cart = inventory.openCart();
        Assert.assertEquals(cart.itemNames(), ProductUtils.titles(filtered), "cart holds the filtered products");
        ExtentTestManager.pass("Cart holds " + cart.itemNames().size() + " products");

        checkout = cart.checkout().enterFirstName(ConfigReader.get("checkout.firstName"));
        WebElement lastName = checkout.lastNameField();
        Assert.assertEquals(lastName.getDomAttribute("id"), "last-name",
                "relative locator below #first-name must find Last Name");
        ExtentTestManager.pass("Relative locator with(input).below(#first-name) found #last-name");

        checkout.enterLastName(ConfigReader.get("checkout.lastName"))
                .enterPostalCode(ConfigReader.get("checkout.postalCode"));
        Assert.assertEquals(checkout.lastNameField().getDomProperty("value"), ConfigReader.get("checkout.lastName"));
    }

    @Test(dependsOnMethods = "step10_cartAndCheckoutInformation", description = "Finish the order")
    public void step11_completeCheckout() {
        checkout.continueToOverview().finish();
        Assert.assertEquals(checkout.confirmationMessage(), "Thank you for your order!");
        ExtentTestManager.pass("Order completed: Thank you for your order!");
    }

    private static String hostOf(String url) {
        return URI.create(url).getHost().toLowerCase();
    }

    private static String expectedDomain(Social social) {
        return switch (social) {
            case TWITTER -> "x.com"; // twitter.com redirects to x.com
            case FACEBOOK -> "facebook.com";
            case LINKEDIN -> "linkedin.com";
        };
    }
}
