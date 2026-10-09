package com.saucedemo.automation.utils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.saucedemo.automation.model.Product;

/** Stream / lambda processing of scraped products. */
public final class ProductUtils {

    private ProductUtils() {
    }

    /** .map(): the titles of the products. */
    public static List<String> titles(List<Product> products) {
        return products.stream().map(Product::title).toList();
    }

    /** .map(): the prices of the products. */
    public static List<Double> prices(List<Product> products) {
        return products.stream().map(Product::price).toList();
    }

    /**
     * Drops products priced below the threshold (.filter()) and orders what is
     * left by price, highest first (.sorted(Comparator)).
     */
    public static List<Product> filterAndSort(List<Product> products, double threshold) {
        return products.stream()
                .filter(p -> p.price() >= threshold)
                .sorted(Comparator.comparingDouble(Product::price).reversed()
                        .thenComparing(Product::title))
                .toList();
    }

    /** Title to price pairs, keeping the (already sorted) order. */
    public static Map<String, Double> toTitlePriceMap(List<Product> products) {
        Map<String, Double> map = new LinkedHashMap<>();
        products.forEach(p -> map.put(p.title(), p.price()));
        return map;
    }
}
