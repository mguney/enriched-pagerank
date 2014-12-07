package com.gun3y.pagerank.mongo;

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.MapperOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.entity.graph.GraphEdge;
import com.gun3y.pagerank.entity.graph.GraphNode;
import com.gun3y.pagerank.entity.graph.LinkType;
import com.gun3y.pagerank.entity.html.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.html.HtmlPage;
import com.mongodb.MongoClient;

public class MongoManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoManager.class);

    private MongoClient mongoClient;

    private Datastore ds;

    private String dbName = "PageRankCrawlerDB";

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
    public void init() {

        try {
            this.mongoClient = new MongoClient(this.dbHost);
        }
        catch (UnknownHostException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalArgumentException("Unkown host!");
        }

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

    // public void mp() throws IOException {
    // String mapFunction = FileUtils.readFileToString(new
    // File(MongoManager.class.getClassLoader().getResource("map.js").getPath()));
    // String reduceFunction = FileUtils
    // .readFileToString(new
    // File(MongoManager.class.getClassLoader().getResource("reduce.js").getPath()));
    // MapReduceCommand mapReduceCommand = new
    // MapReduceCommand(this.ds.getCollection(GraphNode.class), mapFunction,
    // reduceFunction,
    // "TEST", OutputType.INLINE, null);
    //
    // MapreduceResults<GraphNode> mapReduce =
    // this.ds.mapReduce(MapreduceType.INLINE,
    // this.ds.createQuery(GraphNode.class),
    // GraphNode.class, mapReduceCommand);
    //
    // Iterator<GraphNode> inlineResults = mapReduce.getInlineResults();
    // while (inlineResults.hasNext()) {
    // GraphNode next = inlineResults.next();
    // System.out.println(next.getPageId() + " " + next.getPageRank());
    // }
    // }

    // private GraphNode addGraphNodeByHtmlPage(HtmlPage htmlPage) {
    // GraphNode graphNode = MongoUtils.newGraphNode(htmlPage);
    // Key<GraphNode> grapNodeKey = this.ds.save(graphNode);
    // LOGGER.info("GraphNode Added: ID:{}\t URL:{}", graphNode.getPageId(),
    // graphNode.getUrl());
    // return this.ds.getByKey(GraphNode.class, grapNodeKey);
    // }

    // public synchronized void computePageRanks(int numOfIteration) {
    // if (this.ds == null) {
    // return;
    // }
    //
    // StopWatch timer = new StopWatch();
    // timer.start();
    //
    // int count = 0;
    // while (numOfIteration > 0) {
    // numOfIteration--;
    // LOGGER.info("{} iteration start", ++count);
    // this.iteratePageRank();
    // }
    // timer.stop();
    // LOGGER.info("PageRank Computing has been finished in {} ms",
    // timer.getTime());
    // }

    public synchronized void cleanWebGraph() {
        if (this.ds == null) {
            return;
        }
        this.ds.getCollection(GraphNode.class).drop();
        this.ds.getCollection(GraphEdge.class).drop();
    }

    public synchronized void addHtmlPage(HtmlPage htmlPage) {
        if (this.ds == null || htmlPage == null) {
            return;
        }

        this.ds.save(htmlPage);
        LOGGER.info("HtmlPage Added: ID:{} URL:{}", htmlPage.getUrl().getDocid(), htmlPage.getUrl().getUrl());
    }

    public synchronized void addGraphNode(GraphNode graphNode) {
        if (this.ds == null || graphNode == null) {
            return;
        }

        this.ds.save(graphNode);
        LOGGER.info("GraphNode Added: ID:{} URL:{}", graphNode.getPageId(), graphNode.getUrl());
    }

    public synchronized void addEnhancedHtmlPage(EnhancedHtmlPage enhancedHtmlPage) {
        if (this.ds == null || enhancedHtmlPage == null) {
            return;
        }

        this.ds.save(enhancedHtmlPage);
        LOGGER.info("EnhancedHtmlPage Added: ID:{} URL:{}", enhancedHtmlPage.getPageId(), enhancedHtmlPage.getUrl());
    }

    public synchronized void addGraphEdge(GraphNode nodeFrom, GraphNode nodeTo, LinkType linkType) {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        if (nodeFrom == null || nodeTo == null) {
            throw new IllegalArgumentException("GraphNode not Found!");
        }
        if (linkType == null) {
            throw new IllegalArgumentException("LinkType not Found!");
        }

        GraphEdge graphEdge = new GraphEdge();
        graphEdge.setEdgeType(linkType);
        graphEdge.setNodeFrom(nodeFrom);
        graphEdge.setNodeTo(nodeTo);
        this.ds.save(graphEdge);
        LOGGER.info("GraphEdge Added: {} --{}--> {}", graphEdge.getNodeFrom().getPageId(), graphEdge.getEdgeType(), graphEdge.getNodeTo()
                .getPageId());
    }

    public synchronized GraphNode getGraphNodeById(int docid) {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        return this.ds.find(GraphNode.class, "pageId", docid).get();
    }

    public synchronized GraphNode getGraphNodeByUrl(String url) {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        return this.ds.find(GraphNode.class, "url", url).get();
    }

    public synchronized EnhancedHtmlPage getEnhancedHtmlPageById(int docid) {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        return this.ds.find(EnhancedHtmlPage.class, "pageId", docid).get();
    }

    public synchronized EnhancedHtmlPage getEnhancedHtmlPageByUrl(String url) {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        return this.ds.find(EnhancedHtmlPage.class, "url", url).get();
    }

    public synchronized long getEnhancedHtmlPageCount() {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        return this.ds.createQuery(EnhancedHtmlPage.class).countAll();
    }

    public synchronized long getGraphNodeCount() {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        return this.ds.createQuery(GraphNode.class).countAll();
    }

    public synchronized long getHtmlPageCount() {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        return this.ds.createQuery(HtmlPage.class).countAll();
    }

    public synchronized long getEdgeCount() {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        return this.ds.createQuery(GraphEdge.class).countAll();
    }

    public synchronized long getEdgeCount(LinkType linkType, GraphNode nodeFrom) {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        Query<GraphEdge> outExQuery = this.ds.find(GraphEdge.class);
        outExQuery.and(outExQuery.criteria("edgeType").equal(linkType), outExQuery.criteria("nodeFrom").equal(nodeFrom));
        return outExQuery.countAll();
    }

    public synchronized List<Key<GraphEdge>> getGraphEdgeKeys(LinkType linkType, GraphNode nodeTo) {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        Query<GraphEdge> incExQuery = this.ds.find(GraphEdge.class);
        incExQuery.and(incExQuery.criteria("edgeType").equal(linkType), incExQuery.criteria("nodeTo").equal(nodeTo));
        return incExQuery.asKeyList();
    }

    public synchronized Iterator<HtmlPage> getHtmlPageIterator() {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        return this.ds.find(HtmlPage.class).fetch();
    }

    public synchronized Iterator<GraphNode> getGraphNodeIterator() {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        return this.ds.find(GraphNode.class).fetch();
    }

    public synchronized Iterator<EnhancedHtmlPage> getEnhancedHtmlPageIterator() {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        return this.ds.find(EnhancedHtmlPage.class).fetch();
    }

    public synchronized void updateGraphNode(GraphNode graphNode, long outExCount, long outImpCount, long outSemCount,
            List<Key<GraphEdge>> incExKeyList, List<Key<GraphEdge>> incImpKeyList, List<Key<GraphEdge>> incSemKeyList) {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        UpdateOperations<GraphNode> graphUpdateCommand = this.ds.createUpdateOperations(GraphNode.class)
                .set("incomingExplicitLinks", incExKeyList).set("incomingImplicitLinks", incImpKeyList)
                .set("incomingSemanticLinks", incSemKeyList).set("outgoingExplicitCount", outExCount)
                .set("outgoingImplicitCount", outImpCount).set("outgoingSemanticCount", outSemCount);

        this.ds.update(graphNode, graphUpdateCommand);

        LOGGER.info(
                "GraphNode Updated: ID:{} IncExpLinks:{} IncImpLinks: {}, IncSemLinks: {}, OutExpLinks: {}, OutImpLinks: {}, OutImpLinks: {}",
                graphNode.getPageId(), incExKeyList.size(), incImpKeyList.size(), incSemKeyList.size(), outExCount, outImpCount,
                outSemCount);

    }

    public synchronized void updateGraphNode(double pr) {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        this.ds.update(this.ds.find(GraphNode.class), this.ds.createUpdateOperations(GraphNode.class).set("pageRank", pr));
    }

    public synchronized void updateGraphNode(GraphNode graphNode, double pr) {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }
        this.ds.update(graphNode, this.ds.createUpdateOperations(GraphNode.class).set("pageRank", pr));

    }

    public void updateEnhancedHtmlPage(EnhancedHtmlPage enhancedHtmlPage, Set<String> stemmedAnchorTitles) {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }
        this.ds.update(enhancedHtmlPage,
                this.ds.createUpdateOperations(EnhancedHtmlPage.class).set("stemmedAnchorTitles", stemmedAnchorTitles));

        LOGGER.info("EnhancedHtmlPage Updated for Anchor Titles: URL:{}", enhancedHtmlPage.getUrl());

    }

}
