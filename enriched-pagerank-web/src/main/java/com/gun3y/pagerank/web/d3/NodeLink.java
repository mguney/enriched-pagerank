package com.gun3y.pagerank.web.d3;

public class NodeLink {

    public Node source;

    public Node target;

    public boolean left;

    public boolean right;

    public NodeLink(Node source, Node target) {
        super();
        this.source = source;
        this.target = target;
    }

}
