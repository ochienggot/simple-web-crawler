package com.company;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ngot on 27/12/2015.
 */
public class UrlDepthPair {
    public static final String URL_PREFIX = "http://";
    private String url;
    private int depth;

    public UrlDepthPair(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "url: " + url + " search depth: " + depth;
    }

    public String getWebHost() {

        int idx = URL_PREFIX.length();
        int idx2 = url.indexOf("/", idx);

        try {
            if (isUrlValid()) {
                return url.substring(idx, idx2);
            }
        } catch (IndexOutOfBoundsException iob) {
            System.out.println("wrapped url");
        }
        return null;
    }

    public String getDocPath() {
        Pattern p = Pattern.compile("http://.*?/(.*)");
        Matcher m = p.matcher(url);
        if (m.find()) {
            return m.group(1);
        } else {
            return "/";
        }
    }

    public boolean isUrlValid() {
        return url.startsWith(URL_PREFIX);
    }

}
