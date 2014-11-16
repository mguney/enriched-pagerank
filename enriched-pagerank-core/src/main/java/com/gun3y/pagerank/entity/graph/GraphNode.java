package com.gun3y.pagerank.entity.graph;

import java.io.Serializable;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Reference;

@Entity
public class GraphNode implements Serializable {

    private static final long serialVersionUID = 869162275352796786L;

    @Id
    @Indexed
    private ObjectId id;

    private double pageRank;

    @Indexed
    private int pageId;

    @Indexed
    private String url;

    @Reference(lazy = true)
    private List<GraphEdge> incomingExplicitLinks;

    @Reference(lazy = true)
    private List<GraphEdge> incomingImplicitLinks;

    @Reference(lazy = true)
    private List<GraphEdge> incomingSemanticLinks;

    private int outgoingExplicitCount;

    private int outgoingImplicitCount;

    private int outgoingSemanticCount;

    public ObjectId getId() {
        return this.id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public double getPageRank() {
        return this.pageRank;
    }

    public void setPageRank(double pageRank) {
        this.pageRank = pageRank;
    }

    public int getPageId() {
        return this.pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<GraphEdge> getIncomingExplicitLinks() {
        return this.incomingExplicitLinks;
    }

    public void setIncomingExplicitLinks(List<GraphEdge> incomingExplicitLinks) {
        this.incomingExplicitLinks = incomingExplicitLinks;
    }

    public List<GraphEdge> getIncomingImplicitLinks() {
        return this.incomingImplicitLinks;
    }

    public void setIncomingImplicitLinks(List<GraphEdge> incomingImplicitLinks) {
        this.incomingImplicitLinks = incomingImplicitLinks;
    }

    public List<GraphEdge> getIncomingSemanticLinks() {
        return this.incomingSemanticLinks;
    }

    public void setIncomingSemanticLinks(List<GraphEdge> incomingSemanticLinks) {
        this.incomingSemanticLinks = incomingSemanticLinks;
    }

    public int getOutgoingExplicitCount() {
        return this.outgoingExplicitCount;
    }

    public void setOutgoingExplicitCount(int outgoingExplicitCount) {
        this.outgoingExplicitCount = outgoingExplicitCount;
    }

    public int getOutgoingImplicitCount() {
        return this.outgoingImplicitCount;
    }

    public void setOutgoingImplicitCount(int outgoingImplicitCount) {
        this.outgoingImplicitCount = outgoingImplicitCount;
    }

    public int getOutgoingSemanticCount() {
        return this.outgoingSemanticCount;
    }

    public void setOutgoingSemanticCount(int outgoingSemanticCount) {
        this.outgoingSemanticCount = outgoingSemanticCount;
    }

}
