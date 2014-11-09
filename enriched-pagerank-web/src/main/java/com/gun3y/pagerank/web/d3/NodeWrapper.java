package com.gun3y.pagerank.web.d3;

import java.util.List;

public class NodeWrapper {

    private List<Node> nodes;

    private List<NodeLink> links;

    public NodeWrapper(List<Node> nodes, List<NodeLink> links) {
        super();
        this.nodes = nodes;
        this.links = links;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<NodeLink> getLinks() {
        return links;
    }

    public void setLinks(List<NodeLink> links) {
        this.links = links;
    }

}
