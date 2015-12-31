package com.company;

import java.util.LinkedList;

/**
 * Created by ngot on 31/12/2015.
 */
// URL pool object should be thread safe
public class URLPool {
    private LinkedList<UrlDepthPair> pending;
    private LinkedList<UrlDepthPair> visited;
    private int maxDepth;
    private int waiting;

    public URLPool() {
        pending = new LinkedList<>();
        visited = new LinkedList<>();
    }

    /**
     * fetches a URL-depth pair from the pool and remove it from the list simultaneously
     */
    public synchronized  UrlDepthPair get() {
        try {
            while (pending.size() == 0) {
                waiting++;
                wait();
                waiting--;
            }
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        return pending.removeFirst();
    }

    public synchronized void put(UrlDepthPair url) {
        pending.addLast(url);
        //pending.notify();
    }

    public synchronized void putVisited(UrlDepthPair url) {
        visited.addLast(url);
    }

    public synchronized LinkedList<UrlDepthPair> pending() {
        return pending;
    }

    public synchronized LinkedList<UrlDepthPair> visited() {
        return visited;
    }

    public synchronized int getMaxDepth() {
        return maxDepth;
    }

    public synchronized int getWaitingThreads() {
        return waiting;
    }
}
