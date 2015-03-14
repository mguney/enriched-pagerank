package com.gun3y.pagerank.entity.html;

import java.io.Serializable;

public class HtmlPage implements Serializable {

    private static final long serialVersionUID = 6915863557498073804L;

    private WebUrl url;
    private boolean redirect;
    private String redirectedToUrl;
    private int statusCode;
    private String contentType;
    private String contentEncoding;
    private String contentCharset;
    private String language;

    private HtmlData htmlData;

    public WebUrl getUrl() {
        return this.url;
    }

    public void setUrl(WebUrl url) {
        this.url = url;
    }

    public boolean isRedirect() {
        return this.redirect;
    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    public String getRedirectedToUrl() {
        return this.redirectedToUrl;
    }

    public void setRedirectedToUrl(String redirectedToUrl) {
        this.redirectedToUrl = redirectedToUrl;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentEncoding() {
        return this.contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String getContentCharset() {
        return this.contentCharset;
    }

    public void setContentCharset(String contentCharset) {
        this.contentCharset = contentCharset;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public HtmlData getHtmlData() {
        return this.htmlData;
    }

    public void setHtmlData(HtmlData htmlData) {
        this.htmlData = htmlData;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName());
        builder.append(" {\n\turl: ");
        builder.append(this.url);
        builder.append("\n\tredirect: ");
        builder.append(this.redirect);
        builder.append("\n\tredirectedToUrl: ");
        builder.append(this.redirectedToUrl);
        builder.append("\n\tstatusCode: ");
        builder.append(this.statusCode);
        builder.append("\n\tcontentType: ");
        builder.append(this.contentType);
        builder.append("\n\tcontentEncoding: ");
        builder.append(this.contentEncoding);
        builder.append("\n\tcontentCharset: ");
        builder.append(this.contentCharset);
        builder.append("\n\tlanguage: ");
        builder.append(this.language);
        builder.append("\n\thtmlData: ");
        builder.append(this.htmlData);
        builder.append("\n}");
        return builder.toString();
    }

}
