package com.gun3y.pagerank.entity;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class HtmlTitle implements Serializable {

    private static final long serialVersionUID = 5620008418277465565L;

    private String stemmedTitle;

    private String url;

    public HtmlTitle() {
        super();
    }

    public HtmlTitle(String stemmedTitle, String url) {
        super();
        this.stemmedTitle = stemmedTitle;
        this.url = url;
    }

    public String getStemmedTitle() {
        return this.stemmedTitle;
    }

    public void setStemmedTitle(String stemmedTitle) {
        this.stemmedTitle = stemmedTitle;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUniqueId() {
        return this.stemmedTitle + "$_$" + this.url;
    }

    public boolean validate() {
        return StringUtils.isNotBlank(this.url) && StringUtils.isNotBlank(this.stemmedTitle);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(103, 27).append(this.stemmedTitle).append(this.url).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof HtmlTitle) {
            final HtmlTitle other = (HtmlTitle) obj;
            return new EqualsBuilder().append(this.stemmedTitle, other.stemmedTitle).append(this.url, other.url).isEquals();
        }
        else {
            return false;
        }
    }

}
