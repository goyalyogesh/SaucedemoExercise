package com.saucedemo.automation.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Inventory state shared by every test thread. Keys are "browser|product title"
 * so the Chrome and Firefox threads never overwrite each other's entries.
 */
public final class InventoryState {

    private static final String SEPARATOR = "|";
    private static final ConcurrentHashMap<String, Double> STATE = new ConcurrentHashMap<>();

    private InventoryState() {
    }

    public static void store(String browser, Map<String, Double> products) {
        products.forEach((title, price) -> STATE.put(key(browser, title), price));
    }

    /** The entries stored by one browser, with the browser prefix removed. */
    public static Map<String, Double> forBrowser(String browser) {
        String prefix = browser + SEPARATOR;
        return STATE.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .collect(Collectors.toMap(e -> e.getKey().substring(prefix.length()), Map.Entry::getValue));
    }

    public static int size() {
        return STATE.size();
    }

    public static void clear() {
        STATE.clear();
    }

    private static String key(String browser, String title) {
        return browser + SEPARATOR + title;
    }
}
