package com.gun3y.pagerank.graph;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Reference;

@Entity
public class GraphEdge {

    @Reference
    @Indexed
    private GraphNode incomingNode;

    @Reference
    @Indexed
    private GraphNode outgoingNode;

    @Indexed
    private LinkType edgeType;

    @Id
    @Indexed
    ObjectId id;

    public GraphNode getIncomingNode() {
        return this.incomingNode;
    }

    public void setIncomingNode(GraphNode incomingNode) {
        this.incomingNode = incomingNode;
    }

    public GraphNode getOutgoingNode() {
        return this.outgoingNode;
    }

    public void setOutgoingNode(GraphNode outgoingNode) {
        this.outgoingNode = outgoingNode;
    }

    public LinkType getEdgeType() {
        return this.edgeType;
    }

    public void setEdgeType(LinkType edgeType) {
        this.edgeType = edgeType;
    }

    public ObjectId getId() {
        return this.id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

}
