package com.gun3y.pagerank.store;

import org.apache.commons.lang3.StringUtils;

import com.gun3y.pagerank.entity.graph.LinkType;

public class LinkTuple {

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

}
