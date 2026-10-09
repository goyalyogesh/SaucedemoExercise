package com.saucedemo.automation.utils;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

/**
 * Product names waiting to be checked out, in order. ArrayDeque is not
 * thread-safe, so every test thread creates its own CheckoutQueue.
 */
public class CheckoutQueue {

    private final Deque<String> queue = new ArrayDeque<>();

    public CheckoutQueue(Collection<String> productNames) {
        productNames.forEach(queue::addLast);
    }

    /** Next product to process, or null once the queue is empty. */
    public String next() {
        return queue.pollFirst();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}
