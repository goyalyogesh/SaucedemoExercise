package com.saucedemo.automation.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.saucedemo.automation.model.Product;
import com.saucedemo.automation.utils.CheckoutQueue;
import com.saucedemo.automation.utils.InventoryState;
import com.saucedemo.automation.utils.ProductUtils;

/** Browser-free unit tests for the stream, map and queue helpers (run with testng-unit.xml). */
public class DataUtilsTest {

    private static final List<Product> SITE = List.of(
            new Product("Backpack", 29.99),
            new Product("Bike Light", 9.99),
            new Product("Bolt T-Shirt", 15.99),
            new Product("Fleece Jacket", 49.99),
            new Product("Onesie", 7.99),
            new Product("Red T-Shirt", 15.99));

    @BeforeMethod
    public void reset() {
        InventoryState.clear();
    }

    @Test
    public void mapExtractsTitlesAndPrices() {
        Assert.assertEquals(ProductUtils.titles(SITE).size(), 6);
        Assert.assertEquals(ProductUtils.prices(SITE).get(1), 9.99);
    }

    @Test
    public void filterDropsCheapProductsAndSortsDescending() {
        List<Product> result = ProductUtils.filterAndSort(SITE, 15.00);
        Assert.assertEquals(ProductUtils.titles(result),
                List.of("Fleece Jacket", "Backpack", "Bolt T-Shirt", "Red T-Shirt"));
    }

    @Test
    public void thresholdItselfIsKept() {
        List<Product> result = ProductUtils.filterAndSort(List.of(new Product("Exactly", 15.00)), 15.00);
        Assert.assertEquals(result.size(), 1);
    }

    @Test
    public void queueIsProcessedInOrderWithPollFirst() {
        CheckoutQueue queue = new CheckoutQueue(List.of("a", "b", "c"));
        List<String> processed = new ArrayList<>();
        String name;
        while ((name = queue.next()) != null) {
            processed.add(name);
        }
        Assert.assertEquals(processed, List.of("a", "b", "c"));
        Assert.assertTrue(queue.isEmpty());
        Assert.assertNull(queue.next());
    }

    @Test
    public void browsersDoNotOverwriteEachOther() throws InterruptedException {
        Map<String, Double> products = ProductUtils.toTitlePriceMap(ProductUtils.filterAndSort(SITE, 15.00));
        Thread chrome = new Thread(() -> InventoryState.store("chrome", products));
        Thread firefox = new Thread(() -> InventoryState.store("firefox", products));
        chrome.start();
        firefox.start();
        chrome.join();
        firefox.join();

        Assert.assertEquals(InventoryState.size(), 8);
        Assert.assertEquals(InventoryState.forBrowser("chrome"), products);
        Assert.assertEquals(InventoryState.forBrowser("firefox"), products);
    }
}
