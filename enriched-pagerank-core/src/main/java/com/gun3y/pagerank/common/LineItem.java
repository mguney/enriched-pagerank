package com.gun3y.pagerank.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import edu.emory.clir.clearnlp.dependency.DEPTree;

public class LineItem implements Serializable {

    private static final long serialVersionUID = 4505341851671159100L;

    Map<String, String> urls = new HashMap<String, String>();

    String line;

    DEPTree tree;

    public DEPTree getTree() {
        return this.tree;
    }

    public void setTree(DEPTree tree) {
        this.tree = tree;
    }

    public Map<String, String> getUrls() {
        return this.urls;
    }

    public void setUrls(Map<String, String> urls) {
        this.urls = urls;
    }

    public String getLine() {
        return this.line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public LineItem(String text, Map<String, String> map) {
        this.line = text;
        this.urls = map;
    }

    public LineItem(String text, Map<String, String> map, DEPTree tree) {
        this.line = text;
        this.urls = map;
        this.tree = tree;
    }
}
