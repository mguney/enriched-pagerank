package com.gun3y.pagerank.entity.html;

import java.util.HashSet;
import java.util.Set;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

@Entity
public class EnhancedHtmlPage {

    @Id
    private ObjectId id;

    @Indexed
    private String url;

    @Indexed
    private int pageId;

    private String html;

    private String title;

    private String text;

    private Set<String> stemmedAnchorTitles;

    private Set<String> outgoingUrls;

    private String stemmedTitle;

    private String stemmedText;

    public EnhancedHtmlPage() {
        super();
        this.stemmedAnchorTitles = new HashSet<String>();
    }

    public ObjectId getId() {
        return this.id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPageId() {
        return this.pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
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

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Set<String> getStemmedAnchorTitles() {
        return this.stemmedAnchorTitles;
    }

    public void setStemmedAnchorTitles(Set<String> stemmedAnchorTitles) {
        this.stemmedAnchorTitles = stemmedAnchorTitles;
    }

    public String getStemmedTitle() {
        return this.stemmedTitle;
    }

    public void setStemmedTitle(String stemmedTitle) {
        this.stemmedTitle = stemmedTitle;
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

}
