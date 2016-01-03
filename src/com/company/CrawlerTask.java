package com.company;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ngot on 31/12/2015.
 */
public class CrawlerTask implements Runnable {
    private static URLPool urlPool;
    private static final int WEBPORT = 80;
    private static final String REGEX = "href=\"(.*?)\"";
    private volatile boolean running = true;
    private int maxPatience;

    public CrawlerTask(URLPool urlPool, int maxPatience) {
        this.urlPool = urlPool;
        this.maxPatience = maxPatience;
    }

    public boolean isUrlValid(String url) {
        return url.startsWith(UrlDepthPair.URL_PREFIX);
    }

    public void run() {
        // Thread runs in endless loop - passively (not busy-waiting) waiting for
        // pending urls
        while (running) {
            try {
                UrlDepthPair currentPair = urlPool.get(); // wait done w/i URLpool object
                processWebPage(currentPair);
            } catch (IOException e) {
                System.out.println(e);
            } catch (InterruptedException ie) {
                running = false;
            }
        }
    }

    private void processWebPage(UrlDepthPair webpage) throws IOException {

        Socket sock = new Socket(webpage.getWebHost(), WEBPORT);
        sock.setSoTimeout(maxPatience);

        // Allows sending of data over the socket to the 'other side'
        OutputStream os = sock.getOutputStream();
        PrintWriter writer = new PrintWriter(os, true);

        // Send HTTP request
        String docPath = webpage.getDepth() == 0 ? "/" : webpage.getDocPath();

        writer.println("GET " + docPath + " HTTP/1.1");
        writer.println("Host: " + webpage.getWebHost());
        writer.println("Connection: close");
        writer.println();

        // Read the HTTP response
        InputStream is = sock.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }

            Pattern p = Pattern.compile(REGEX);
            Matcher m = p.matcher(line);
            while (m.find()) {
                String url = m.group(1);
                if (isUrlValid(url)) {
                    UrlDepthPair current = new UrlDepthPair(new URL(url), webpage.getDepth() + 1);

                    if (!urlPool.seen(current)) {
                        int depth = webpage.getDepth();
                        if (depth < urlPool.getMaxDepth()) {
                            urlPool.put(current);
                        }
                    }
                }
            }
        }
        sock.close();
    }
}
