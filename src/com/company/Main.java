package com.company;

import java.io.*;
import java.net.Inet4Address;
import java.net.MalformedURLException;

public class Main {

    public static void main(String[] args) {

        if (args.length != 3) {
            System.out.println("Usage: java Crawler <URL> <depth>");
            System.exit(1);
        }

        int maxDepth = 0;
        int numberOfThreads = 0;
        try {
            maxDepth = Integer.parseInt(args[1]);
            numberOfThreads = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException nfe) {
            System.out.println("The second and third argument must be an integer.");
            System.exit(1);
        }
        // Create URL pool
        URLPool urlPool = new URLPool();
        // Add initial url to pool
        UrlDepthPair webpage = new UrlDepthPair(args[0], 0);
        urlPool.put(webpage);

        // Create and start requested number of threads
        for (int i = 0; i < numberOfThreads; i++) {
            CrawlerTask c = new CrawlerTask(urlPool);
            Thread t = new Thread(c);
            t.start();
        }

        while (urlPool.getWaitingThreads() != numberOfThreads) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.out.println("caught Interrupted Exception, ignoring...");
            }
        }

        urlPool.visited().stream()
                .forEach(System.out::println);

        System.exit(0); // is this necessary??
    }
}
