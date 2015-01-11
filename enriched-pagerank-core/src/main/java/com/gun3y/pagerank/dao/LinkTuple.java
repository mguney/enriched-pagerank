package com.gun3y.pagerank.dao;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.gun3y.pagerank.entity.LinkType;

public class LinkTuple implements Serializable {

    private static final long serialVersionUID = -3980877700782161276L;

    String from;

    String to;

    String rel;

    LinkType linkType;

    public LinkTuple(String from, LinkType linkType, String to) {
        this.from = from;
        this.linkType = linkType;
        this.to = to;
    }

    public LinkTuple(String from, LinkType linkType, String to, String rel) {
        this.from = from;
        this.linkType = linkType;
        this.to = to;
        this.rel = rel;
    }

    public boolean validate() {
        return StringUtils.isNotBlank(this.from) && StringUtils.isNotBlank(this.to) && this.linkType != null;
    }

    @Override
    public String toString() {
        return String.format("%s\t%s\t%s\t%s", this.from, this.to, this.linkType, this.rel);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.from).append(this.to).append(this.rel).append(this.linkType).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof LinkTuple) {
            final LinkTuple other = (LinkTuple) obj;
            return new EqualsBuilder().append(this.from, other.from).append(this.to, other.to).append(this.rel, other.rel)
                    .append(this.linkType, other.linkType).isEquals();
        }
        else {
            return false;
        }
    }

}
