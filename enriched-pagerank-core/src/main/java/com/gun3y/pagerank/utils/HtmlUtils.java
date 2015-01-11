package com.gun3y.pagerank.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

public class HtmlUtils {

    public static final String REGEX_HTML_PAGES = ".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$";

    public static boolean checkUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        try {
            new URL(url);
        }
        catch (MalformedURLException e) {
            return false;
        }

        return true;
    }

    public static void main(String[] args) {

        System.out.println(checkUrl("#"));
    }
}
