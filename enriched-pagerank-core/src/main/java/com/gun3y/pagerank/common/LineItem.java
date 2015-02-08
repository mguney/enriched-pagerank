package com.gun3y.pagerank.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

public class LineItem implements Serializable {

    private static final long serialVersionUID = 4505341851671159100L;

    Map<String, Pair<String, String>> urls = new HashMap<String, Pair<String, String>>();

    String line;

    public Map<String, Pair<String, String>> getUrls() {
        return this.urls;
    }

    public void setUrls(Map<String, Pair<String, String>> urls) {
        this.urls = urls;
    }

    public String getLine() {
        return this.line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public LineItem(String text, Map<String, Pair<String, String>> map) {
        this.line = text;
        this.urls = map;
    }
}
