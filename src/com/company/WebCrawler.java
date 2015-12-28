package com.company;

import com.sun.xml.internal.messaging.saaj.util.JaxmURI;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ngot on 28/12/2015.
 */
public class WebCrawler {
    public static final String LINK_PREFIX = "a href=\"";
    public static final int WEBPORT = 80;
    private LinkedList<UrlDepthPair> pendingURLs;
    private List<UrlDepthPair> visited;
    private int maxDepth;

    public WebCrawler(int maxDepth) {
        this.maxDepth = maxDepth;
        pendingURLs = new LinkedList<>();
        visited = new ArrayList<>();
    }

    public List<UrlDepthPair> getSites() {
        return visited;
    }

    /**
     * Attempts to process a particular page
     * @param webpage pair of url hostname and depth
     * @throws IOException
     */
    public void processWebPage(UrlDepthPair webpage) throws IOException {

        Socket sock = new Socket(webpage.getWebHost(), WEBPORT);
        sock.setSoTimeout(3000);

        // Allows sending of data over the socket to the 'other side'
        OutputStream os = sock.getOutputStream();
        PrintWriter writer = new PrintWriter(os, true);

        // Send HTTP request
        String docPath = webpage.getDepth() > 0 ? "/stories" : "/";

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
                break; // done reading document
            }
            int idx = 0;

            // Handles more than one url in a line
            while (true) {
                idx = line.indexOf(LINK_PREFIX, idx);
                if (idx == -1) {
                    break;
                }

                idx += LINK_PREFIX.length();
                int idx2 = line.indexOf("\"", idx);
                String url = "";
                try {
                    url = line.substring(idx, idx2);
                } catch (IndexOutOfBoundsException iob) {
                    System.out.println("wrapped url");
                }

                if (url.startsWith(UrlDepthPair.URL_PREFIX)) {
                    pendingURLs.add(new UrlDepthPair(url, webpage.getDepth() + 1));

                } //else {
                   // throw new JaxmURI.MalformedURIException("Bad url");
                //}
            }
        }

        visited.add(webpage);
        sock.close();
    }

    public void processUrls() {

        while (!pendingURLs.isEmpty()) {
            UrlDepthPair nextURLPair = pendingURLs.removeFirst();

            if (nextURLPair.getDepth() < maxDepth) {
                try {
                    System.out.println("Processing url");
                    processWebPage(nextURLPair);
                }
                catch (IOException e){
                    System.out.println(e);
                }
            }
        }
    }
}
