package com.gun3y.pagerank.entity.graph;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Reference;

@Entity
public class GraphEdge implements Serializable {

    private static final long serialVersionUID = -4575506561438964598L;

    @Id
    @Indexed
    ObjectId id;

    @Reference(lazy = true)
    @Indexed
    private GraphNode nodeFrom;

    @Reference(lazy = true)
    @Indexed
    private GraphNode nodeTo;

    @Indexed
    private LinkType edgeType;

    public ObjectId getId() {
        return this.id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public GraphNode getNodeFrom() {
        return this.nodeFrom;
    }

    public void setNodeFrom(GraphNode nodeFrom) {
        this.nodeFrom = nodeFrom;
    }

    public GraphNode getNodeTo() {
        return this.nodeTo;
    }

    public void setNodeTo(GraphNode nodeTo) {
        this.nodeTo = nodeTo;
    }

    public LinkType getEdgeType() {
        return this.edgeType;
    }

    public void setEdgeType(LinkType edgeType) {
        this.edgeType = edgeType;
    }

}
