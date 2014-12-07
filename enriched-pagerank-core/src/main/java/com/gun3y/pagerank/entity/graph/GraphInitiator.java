package com.gun3y.pagerank.entity.graph;

import java.net.UnknownHostException;

import com.gun3y.pagerank.mongo.MongoManager;

public class GraphInitiator {

    public static void main2(String[] args) throws UnknownHostException {

        final MongoManager mongoManager = new MongoManager();
        mongoManager.init();
        mongoManager.cleanWebGraph();
        // mongoManager.transformHtmlPageToWebGraph();

        mongoManager.close();
    }

    public static void main(String[] args) throws UnknownHostException {

        final MongoManager mongoManager = new MongoManager();
        mongoManager.init();

        // mongoManager.computePageRanks(10);

        mongoManager.close();
    }

}
