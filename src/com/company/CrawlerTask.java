package com.company;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ngot on 31/12/2015.
 */
public class CrawlerTask implements Runnable {
    private static URLPool urlPool;
    public static final int WEBPORT = 80;
    private static final String REGEX = "href=\"(.*?)\"";

    public CrawlerTask(URLPool urlPool) {
        this.urlPool = urlPool;
    }
    public void run() {
        while (urlPool.pending().size() > 0) {
            UrlDepthPair currentPair = urlPool.get(); // wait done w/i URLpool object
            try {
                processWebPage(currentPair);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    private void processWebPage(UrlDepthPair webpage) throws IOException {

        Socket sock = new Socket(webpage.getWebHost(), WEBPORT);
        sock.setSoTimeout(3000);

        // Allows sending of data over the socket to the 'other side'
        OutputStream os = sock.getOutputStream();
        PrintWriter writer = new PrintWriter(os, true);

        // Send HTTP request
        String docPath = webpage.getDepth() == 0 ? "/stories" : webpage.getDocPath();

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
                if (!urlPool.visited().contains(url) && webpage.isUrlValid()) {
                    int depth = webpage.getDepth();
                    if (depth < urlPool.getMaxDepth()) {
                        urlPool.put(new UrlDepthPair(url, webpage.getDepth() + 1));
                    }
                }
                urlPool.putVisited(webpage);
            }

        }
        sock.close();
    }
}
