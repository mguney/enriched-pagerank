package com.gun3y.pagerank.entity;

import javax.persistence.*;

@Entity
@Table(name = "LINK_TUPLE_COUNT", schema = "pagerank")
public class LinkTupleCount {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "LTC_ID")
    private Integer id;

    @Column(name = "LTC_FROM")
    private String from;

    @Column(name = "LTC_TO")
    private String to;

    @Column(name = "LTC_REL")
    private String rel;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "LTC_LINK_TYPE")
    private LinkType linkType;

    @Column(name = "LTC_COUNT")
    private Integer count;

    public LinkTupleCount() {
        super();
    }

    public LinkTupleCount(String from, String to, String rel, LinkType linkType) {
        this.from = from;
        this.to = to;
        this.rel = rel;
        this.linkType = linkType;
    }

    public LinkTupleCount(LinkTuple linkTuple) {
        this.from = linkTuple.getFrom();
        this.to = linkTuple.getTo();
        this.rel = linkTuple.getRel();
        this.linkType = linkTuple.getLinkType();
        this.count = 1;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getRel() {
        return this.rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public LinkType getLinkType() {
        return this.linkType;
    }

    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
