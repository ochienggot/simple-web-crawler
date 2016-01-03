package com.company;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static List<Thread> threadList = new ArrayList<>();

    public static void main(String[] args) {
        //System.out.println("In thread: " + Thread.currentThread().getName());

        if (args.length < 3) {
            System.out.println("Usage: java Crawler <URL> <depth> <threads>");
            System.exit(1);
        }

        int maxDepth = 0;
        int numberOfThreads = 0;
        int maxPatience = 0;
        try {
            maxDepth = Integer.parseInt(args[1]);
            numberOfThreads = Integer.parseInt(args[2]);
            maxPatience = Integer.parseInt(args[3]);
        }
        catch (NumberFormatException nfe) {
            System.out.println("The second and third arguments must be integers.");
            System.exit(1);
        }

        URLPool urlPool = new URLPool(maxDepth);
        try {
            urlPool.put(new UrlDepthPair(new URL(args[0]), 0));
        } catch (MalformedURLException mue) {
            System.out.println(mue);
        }

        // Create and start requested number of threads
        for (int i = 0; i < numberOfThreads; i++) {
            CrawlerTask c = new CrawlerTask(urlPool, maxPatience);
            Thread t = new Thread(c);
            threadList.add(t);
            t.start();
        }

        while (urlPool.getWaitingThreads() != numberOfThreads) {
            try {
                //System.out.println("Waiting threads: " + urlPool.getWaitingThreads());
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.out.println("caught Interrupted Exception, ignoring...");
            }
        }


        urlPool.seen().stream()
                .forEach(System.out::println);

        //System.exit(0);
        // Interrupt threads waiting on shared resource, cleaner exit
        threadList.stream()
                .forEach(Thread::interrupt);
    }
}
