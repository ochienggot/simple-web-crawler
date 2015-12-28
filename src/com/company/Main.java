package com.company;

import java.io.*;
import java.net.MalformedURLException;

public class Main {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage: java Crawler <URL> <depth>");
            System.exit(1);
        }

        int maxDepth = 0;
        try {
            maxDepth = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException nfe) {
            System.out.println("The second argument must be an integer.");
            System.exit(1);
        }

        UrlDepthPair webpage = new UrlDepthPair(args[0], 0);
        WebCrawler p = new WebCrawler(maxDepth);

        try {
            p.processWebPage(webpage);
            p.processUrls();

        } catch (MalformedURLException e) {
            System.out.println(e);

        } catch (IOException io) {
            System.out.println(io);
        }

        p.getSites().stream()
                .forEach(System.out::println);
    }


}
