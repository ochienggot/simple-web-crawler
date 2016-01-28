package com.company;

import com.sun.xml.internal.messaging.saaj.util.JaxmURI;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ngot on 28/12/2015.
 */
public class WebCrawler {
    //public static final String LINK_PREFIX = "a href=\"";
    public static final int WEBPORT = 80;
    private static final String REGEX = "href=\"(.*?)\"";
    private LinkedList<UrlDepthPair> pendingURLs;
    private LinkedList<UrlDepthPair> visited;
    private int maxDepth;

    public WebCrawler(int maxDepth) {
        this.maxDepth = maxDepth;
        pendingURLs = new LinkedList<>();
        visited = new LinkedList<>();
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
                try {
                    if (!visited.contains(url)) {
                        pendingURLs.add(new UrlDepthPair(new URL(url), webpage.getDepth() + 1));
                    }
                } catch (MalformedURLException mue) {
                    System.out.println(mue);
                }
            }

            /*
            // Handles more than one url in a line
            int idx = 0;
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
                    if (!visited.contains(url)) {
                        pendingURLs.add(new UrlDepthPair(url, webpage.getDepth() + 1));
                    }

                } //else {
                   // throw new JaxmURI.MalformedURIException("Bad url");
                //}
            }
            */
        }

        visited.add(webpage);
        sock.close();
    }

    public void processUrls() {

        while (!pendingURLs.isEmpty()) {
            UrlDepthPair nextURLPair = pendingURLs.removeFirst();

            if (nextURLPair.getDepth() < maxDepth) {
                try {
                    processWebPage(nextURLPair);
                }
                catch (IOException e){
                    System.out.println(e);
                }
            }
        }
    }
}
