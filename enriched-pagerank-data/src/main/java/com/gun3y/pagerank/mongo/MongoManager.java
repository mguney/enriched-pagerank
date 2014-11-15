package com.gun3y.pagerank.mongo;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.MapperOptions;
import org.mongodb.morphia.query.MorphiaIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.graph.GraphEdge;
import com.gun3y.pagerank.graph.GraphNode;
import com.gun3y.pagerank.graph.LinkType;
import com.gun3y.pagerank.page.HtmlPage;
import com.gun3y.pagerank.page.WebUrl;
import com.mongodb.MongoClient;

public class MongoManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoManager.class);

    private MongoClient mongoClient;

    private Datastore ds;

    private String dbName = "EnrichedPageRankDB";

    private String dbHost = "localhost";

    public MongoManager() {
        super();
    }

    public MongoManager(String host, String db) {
        super();
        this.dbHost = host;
        this.dbName = db;
    }

    @PostConstruct
    public void init() throws UnknownHostException {

        this.mongoClient = new MongoClient(this.dbHost);

        MapperOptions mapperOptions = new MapperOptions();

        Mapper mapper = new Mapper(mapperOptions);
        Morphia morphia = new Morphia(mapper);

        this.ds = morphia.createDatastore(this.mongoClient, this.dbName);
        morphia.map(HtmlPage.class);
    }

    @PreDestroy
    public void close() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    public synchronized void runOnHtmlPages(HtmlPageRunner pageRunner) {
        if (this.ds == null) {
            return;
        }

        MorphiaIterator<HtmlPage, HtmlPage> htmlPageIterator = this.ds.find(HtmlPage.class).fetch();
        while (htmlPageIterator.hasNext()) {
            HtmlPage htmlPage = htmlPageIterator.next();
            pageRunner.run(htmlPage);
        }
    }

    public synchronized void addGraphNodesByHtmlPages() {
        if (this.ds == null) {
            return;
        }

        MorphiaIterator<HtmlPage, HtmlPage> htmlPageIterator = this.ds.find(HtmlPage.class).fetch();
        while (htmlPageIterator.hasNext()) {
            HtmlPage htmlPage = htmlPageIterator.next();
            GraphNode graphNode = MongoUtils.newGraphNode(htmlPage);
            this.ds.save(graphNode);
            LOGGER.info("GraphNode Added: ID:{}\t URL:{}", graphNode.getPageId(), graphNode.getUrl());

        }
    }

    public synchronized void addGraphEdgesByHtmlLinks() {
        if (this.ds == null) {
            return;
        }

        MorphiaIterator<HtmlPage, HtmlPage> htmlPageIterator = this.ds.find(HtmlPage.class).fetch();
        while (htmlPageIterator.hasNext()) {
            HtmlPage htmlPage = htmlPageIterator.next();
            Set<WebUrl> outgoingUrls = htmlPage.getHtmlData().getOutgoingUrls();

            if (outgoingUrls == null || outgoingUrls.isEmpty()) {
                continue;
            }
            for (WebUrl webUrl : outgoingUrls) {
                GraphEdge graphEdge = new GraphEdge();

                graphEdge.setEdgeType(LinkType.ExplicitLink);

                GraphNode incomingNode = this.ds.find(GraphNode.class, "pageId", htmlPage.getUrl().getDocid()).get();
                if (incomingNode == null) {
                    incomingNode = this.addGraphNodeByHtmlPage(htmlPage);
                }
                if (incomingNode.getPageId() < 0) {
                    throw new RuntimeException("PageId cannot be negative!");
                }
                graphEdge.setIncomingNode(incomingNode);

                GraphNode outgoingNode = this.ds.find(GraphNode.class, "pageId", webUrl.getDocid()).get();

                if (outgoingNode == null) {
                    if (webUrl.getDocid() < 0) {
                        continue;
                    }
                    outgoingNode = this.addGraphNodeByHtmlPage(MongoUtils.newHtmlPage(webUrl));
                }
                if (incomingNode.getPageId() < 0) {
                    throw new RuntimeException("PageId cannot be negative!");
                }
                graphEdge.setOutgoingNode(outgoingNode);

                this.ds.save(graphEdge);
                LOGGER.info("GraphEdge Added: {} -> {}", graphEdge.getIncomingNode().getPageId(), graphEdge.getOutgoingNode().getPageId());
            }

        }
    }

    private GraphNode addGraphNodeByHtmlPage(HtmlPage htmlPage) {
        GraphNode graphNode = MongoUtils.newGraphNode(htmlPage);
        Key<GraphNode> grapNodeKey = this.ds.save(graphNode);
        LOGGER.info("GraphNode Added: ID:{}\t URL:{}", graphNode.getPageId(), graphNode.getUrl());
        return this.ds.getByKey(GraphNode.class, grapNodeKey);
    }

    public synchronized void add(GraphNode graphNode) {
        if (this.ds == null || graphNode == null) {
            return;
        }

        this.ds.save(graphNode);
    }

    public synchronized void add(HtmlPage htmlPage) {
        if (this.ds == null || htmlPage == null) {
            return;
        }

        this.ds.save(htmlPage);
    }

    public synchronized <T> void removeAll(Class<T> clazz) {
        if (this.ds == null || clazz == null) {
            return;
        }
        this.ds.getCollection(clazz).drop();
    }

    public synchronized void remove(Collection<HtmlPage> htmlPages) {
        if (this.ds == null || htmlPages == null) {
            return;
        }

        for (HtmlPage htmlPage : htmlPages) {
            this.ds.delete(htmlPage);
        }
    }

    public synchronized <T> long getCount(Class<T> clazz) {
        if (this.ds == null) {
            return -1;
        }
        return this.ds.getCount(clazz);
    }

}
