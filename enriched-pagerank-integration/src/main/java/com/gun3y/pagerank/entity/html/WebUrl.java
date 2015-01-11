package com.gun3y.pagerank.entity.html;

import java.io.Serializable;

public class WebUrl implements Serializable {

    private static final long serialVersionUID = -1115210962314547587L;

    private String url;
    private int docid;
    private int parentDocid;
    private String parentUrl;
    private short depth;
    private String domain;
    private String subDomain;
    private String path;
    private String anchor;
    private byte priority;
    private String tag;

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDocid() {
        return this.docid;
    }

    public void setDocid(int docid) {
        this.docid = docid;
    }

    public int getParentDocid() {
        return this.parentDocid;
    }

    public void setParentDocid(int parentDocid) {
        this.parentDocid = parentDocid;
    }

    public String getParentUrl() {
        return this.parentUrl;
    }

    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }

    public short getDepth() {
        return this.depth;
    }

    public void setDepth(short depth) {
        this.depth = depth;
    }

    public String getDomain() {
        return this.domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSubDomain() {
        return this.subDomain;
    }

    public void setSubDomain(String subDomain) {
        this.subDomain = subDomain;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAnchor() {
        return this.anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public byte getPriority() {
        return this.priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName());
        builder.append(" {\n\turl: ");
        builder.append(this.url);
        builder.append("\n\tdocid: ");
        builder.append(this.docid);
        builder.append("\n\tparentDocid: ");
        builder.append(this.parentDocid);
        builder.append("\n\tparentUrl: ");
        builder.append(this.parentUrl);
        builder.append("\n\tdepth: ");
        builder.append(this.depth);
        builder.append("\n\tdomain: ");
        builder.append(this.domain);
        builder.append("\n\tsubDomain: ");
        builder.append(this.subDomain);
        builder.append("\n\tpath: ");
        builder.append(this.path);
        builder.append("\n\tanchor: ");
        builder.append(this.anchor);
        builder.append("\n\tpriority: ");
        builder.append(this.priority);
        builder.append("\n\ttag: ");
        builder.append(this.tag);
        builder.append("\n}");
        return builder.toString();
    }

}
