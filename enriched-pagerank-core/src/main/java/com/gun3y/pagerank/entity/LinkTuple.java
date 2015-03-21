package com.gun3y.pagerank.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "link_tuple", schema = "pagerank")
public class LinkTuple implements Serializable, Comparable<LinkTuple> {

    private static final long serialVersionUID = -3980877700782161276L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "lt_id")
    private Integer id;

    @Column(name = "lt_from")
    private String from;

    @Column(name = "lt_to")
    private String to;

    @Column(name = "lt_rel")
    private String rel;

    @Column(name = "lt_link_type")
    @Enumerated(EnumType.ORDINAL)
    private LinkType linkType;

    public LinkTuple() {
        super();
    }

    public LinkTuple(String from, LinkType linkType, String to) {
        this.from = from;
        this.linkType = linkType;
        this.to = to;
        this.rel = "";
    }

    public LinkTuple(String from, LinkType linkType, String to, String rel) {
        this.from = from;
        this.linkType = linkType;
        this.to = to;
        this.rel = rel;
    }

    public boolean validate() {
        return StringUtils.isNotBlank(this.from) && StringUtils.isNotBlank(this.to) && this.linkType != null && this.from.length() < 200
                && this.to.length() < 200 && (this.rel == null || this.rel.length() < 120);
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

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public static final String KEY_SPLITTER = "#_##_#";

    public static String toUniqueKey(LinkTuple linkTuple) {
        return linkTuple.linkType + KEY_SPLITTER + linkTuple.rel + KEY_SPLITTER + linkTuple.from + KEY_SPLITTER + linkTuple.to;
    }

    public static LinkTuple fromUniqueKey(String uniqueKey) {
        if (StringUtils.isBlank(uniqueKey)) {
            return null;
        }
        String[] splitteWords = uniqueKey.split(KEY_SPLITTER);
        if (splitteWords.length != 4) {
            return null;
        }

        return new LinkTuple(splitteWords[2], LinkType.valueOf(splitteWords[0]), splitteWords[3], splitteWords[1]);
    }

    public static void main(String[] args) {
        LinkTuple linkTuple = new LinkTuple("denemefrom", LinkType.InferredLink, "deTO", "fuck you");
        System.out.println(LinkTuple.toUniqueKey(linkTuple));

        System.out.println(LinkTuple.fromUniqueKey(LinkTuple.toUniqueKey(linkTuple)));
    }

    @Override
    public int compareTo(LinkTuple o) {
        return LinkTuple.toUniqueKey(this).compareTo(LinkTuple.toUniqueKey(o));
    }
}
