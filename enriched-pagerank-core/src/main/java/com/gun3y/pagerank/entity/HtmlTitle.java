package com.gun3y.pagerank.entity;

import java.io.Serializable;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "html_title", schema = "pagerank")
public class HtmlTitle implements Serializable {

    private static final long serialVersionUID = 5620008418277465565L;

    @Id
    @Column(name = "ht_title")
    private String stemmedTitle;

    @Id
    @Column(name = "ht_url")
    private String url;

    public HtmlTitle() {
        super();
    }

    public HtmlTitle(String stemmedTitle, String url) {
        super();
        this.stemmedTitle = stemmedTitle;
        this.url = url;

        if (this.stemmedTitle != null) {
            this.stemmedTitle = this.stemmedTitle.toLowerCase(Locale.ENGLISH);
        }

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

    public boolean validate() {
        return StringUtils.isNotBlank(this.url) && StringUtils.isNotBlank(this.stemmedTitle);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(117, 27).append(this.stemmedTitle).append(this.url).toHashCode();
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
