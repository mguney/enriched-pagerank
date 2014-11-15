package com.gun3y.pagerank.page;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class HtmlData implements Serializable {

    private static final long serialVersionUID = 4197987210914288017L;

    private String html;
    private String text;
    private String title;
    private Map<String, String> metaTags;

    private Set<WebUrl> outgoingUrls;

    public String getHtml() {
        return this.html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, String> getMetaTags() {
        return this.metaTags;
    }

    public void setMetaTags(Map<String, String> metaTags) {
        this.metaTags = metaTags;
    }

    public Set<WebUrl> getOutgoingUrls() {
        return this.outgoingUrls;
    }

    public void setOutgoingUrls(Set<WebUrl> outgoingUrls) {
        this.outgoingUrls = outgoingUrls;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName());
        builder.append(" {\n\ttitle: ");
        builder.append(this.title);
        builder.append("\n\tmetaTags: ");
        builder.append(this.metaTags);
        builder.append("\n\toutgoingUrls: ");
        builder.append(this.outgoingUrls);
        builder.append("\n}");
        return builder.toString();
    }
}
