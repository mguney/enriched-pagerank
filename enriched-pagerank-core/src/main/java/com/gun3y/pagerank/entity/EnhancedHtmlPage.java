package com.gun3y.pagerank.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gun3y.pagerank.common.LineItem;

public class EnhancedHtmlPage implements Serializable {

    private static final long serialVersionUID = -8270823792664046613L;

    private String url;

    private String html;

    private String title;

    private Set<String> anchors;

    private Set<String> outgoingUrls;

    private String stemmedText;

    private List<LineItem> lines;

    public EnhancedHtmlPage() {
        super();
        this.setAnchors(new HashSet<String>());
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHtml() {
        return this.html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStemmedText() {
        return this.stemmedText;
    }

    public void setStemmedText(String stemmedText) {
        this.stemmedText = stemmedText;
    }

    public Set<String> getOutgoingUrls() {
        return this.outgoingUrls;
    }

    public void setOutgoingUrls(Set<String> outgoingUrls) {
        this.outgoingUrls = outgoingUrls;
    }

    public List<LineItem> getLines() {
        return this.lines;
    }

    public void setLines(List<LineItem> lines) {
        this.lines = lines;
    }

    public Set<String> getAnchors() {
        return this.anchors;
    }

    public void setAnchors(Set<String> anchors) {
        this.anchors = anchors;
    }

}
