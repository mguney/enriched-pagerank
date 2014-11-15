package com.gun3y.pagerank.graph;

import java.net.UnknownHostException;

import com.gun3y.pagerank.mongo.MongoManager;

public class GraphInitiator {

    public static void main(String[] args) throws UnknownHostException {

        final MongoManager mongoManager = new MongoManager();
        mongoManager.init();
        mongoManager.removeAll(GraphNode.class);
        mongoManager.removeAll(GraphEdge.class);
        mongoManager.addGraphNodesByHtmlPages();
        mongoManager.addGraphEdgesByHtmlLinks();

        mongoManager.close();
    }

}
