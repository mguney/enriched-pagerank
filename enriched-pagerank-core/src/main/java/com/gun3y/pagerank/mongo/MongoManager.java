package com.gun3y.pagerank.mongo;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.MapreduceResults;
import org.mongodb.morphia.MapreduceType;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.MapperOptions;
import org.mongodb.morphia.query.MorphiaIterator;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.entity.graph.GraphEdge;
import com.gun3y.pagerank.entity.graph.GraphNode;
import com.gun3y.pagerank.entity.graph.LinkType;
import com.gun3y.pagerank.entity.html.HtmlPage;
import com.gun3y.pagerank.entity.html.WebUrl;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceCommand.OutputType;
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

    public void mp() throws IOException {
        String mapFunction = FileUtils.readFileToString(new File(MongoManager.class.getClassLoader().getResource("map.js").getPath()));
        String reduceFunction = FileUtils
                .readFileToString(new File(MongoManager.class.getClassLoader().getResource("reduce.js").getPath()));
        MapReduceCommand mapReduceCommand = new MapReduceCommand(this.ds.getCollection(GraphNode.class), mapFunction, reduceFunction,
                "TEST", OutputType.INLINE, null);

        MapreduceResults<GraphNode> mapReduce = this.ds.mapReduce(MapreduceType.INLINE, this.ds.createQuery(GraphNode.class),
                GraphNode.class, mapReduceCommand);

        Iterator<GraphNode> inlineResults = mapReduce.getInlineResults();
        while (inlineResults.hasNext()) {
            GraphNode next = inlineResults.next();
            System.out.println(next.getPageId() + " " + next.getPageRank());
        }
    }

    public static void main(String[] args) throws Exception {
        // MongoManager mongoManager = new MongoManager();
        // mongoManager.init();
        // mongoManager.mp();
        // mongoManager.close();

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

    public synchronized void computePageRanks(int numOfIteration) {
        if (this.ds == null) {
            return;
        }

        StopWatch timer = new StopWatch();
        timer.start();

        int count = 0;
        while (numOfIteration > 0) {
            numOfIteration--;
            LOGGER.info("{} iteration start", ++count);
            iteratePageRank();
        }
        timer.stop();
        LOGGER.info("PageRank Computing has been finished in {} ms", timer.getTime());
    }

    private void iteratePageRank() {
        MorphiaIterator<GraphNode, GraphNode> graphNodeIterator = this.ds.find(GraphNode.class).fetch();
        while (graphNodeIterator.hasNext()) {
            StopWatch pageTimer = new StopWatch();
            pageTimer.start();
            GraphNode graphNode = graphNodeIterator.next();

            StopWatch explicitTimer = new StopWatch();
            explicitTimer.start();
            double explicitScores = this.computeIndividualScore(graphNode, LinkType.ExplicitLink);
            explicitTimer.stop();

            StopWatch implicitTimer = new StopWatch();
            implicitTimer.start();
            double implicitScores = this.computeIndividualScore(graphNode, LinkType.ImplicitLink);
            implicitTimer.stop();

            StopWatch semanticTimer = new StopWatch();
            semanticTimer.start();
            double semanticScores = this.computeIndividualScore(graphNode, LinkType.SemanticLink);
            semanticTimer.stop();

            double dumpingFactor = 0.85d;

            double pageRank = (1 - dumpingFactor) + dumpingFactor * (explicitScores + implicitScores + semanticScores);
            graphNode.setPageRank(pageRank);

            this.ds.update(graphNode, this.ds.createUpdateOperations(GraphNode.class).set("pageRank", pageRank));
            pageTimer.stop();

            LOGGER.info("PageRank has been computed for GrapNode:{} in {} ms(Explicit: {}, Implicit: {} Semantic: {}, Update:{})",
                    graphNode.getPageId(), pageTimer.getTime(), explicitTimer.getTime(), implicitTimer.getTime(),
                    semanticTimer.getTime(),
                    (pageTimer.getTime() - explicitTimer.getTime() - implicitTimer.getTime() - semanticTimer.getTime()));
        }
    }

    private double computeIndividualScore(GraphNode graphNode, LinkType linkType) {
        double totalScore = 0d;
        List<GraphEdge> incomingLinks;
        if (linkType == LinkType.ExplicitLink) {
            incomingLinks = graphNode.getIncomingExplicitLinks();
        }
        else if (linkType == LinkType.ImplicitLink) {
            incomingLinks = graphNode.getIncomingImplicitLinks();
        }
        else {
            incomingLinks = graphNode.getIncomingSemanticLinks();
        }

        for (GraphEdge graphEdge : incomingLinks) {
            GraphNode incomingNode = graphEdge.getNodeFrom();

            double outgoingNodeCount = 0;
            if (linkType == LinkType.ExplicitLink) {
                outgoingNodeCount = graphNode.getOutgoingExplicitCount();
            }
            else if (linkType == LinkType.ImplicitLink) {
                outgoingNodeCount = graphNode.getOutgoingImplicitCount();
            }
            else {
                outgoingNodeCount = graphNode.getOutgoingSemanticCount();
            }

            if (outgoingNodeCount == 0) {
                continue;
            }
            double incomingNodePageRank = incomingNode.getPageRank();

            totalScore += incomingNodePageRank / outgoingNodeCount;
        }

        return totalScore;
    }

    public synchronized void cleanWebGraph() {
        if (this.ds == null) {
            return;
        }
        this.ds.getCollection(GraphNode.class).drop();
        this.ds.getCollection(GraphEdge.class).drop();
    }

    public synchronized void transformHtmlPageToWebGraph() {
        if (this.ds == null) {
            return;
        }
        LOGGER.info("Transforming HtmlPages to WebGraph started");
        StopWatch timer = new StopWatch();
        timer.start();
        MorphiaIterator<HtmlPage, HtmlPage> htmlPageIterator = this.ds.find(HtmlPage.class).fetch();
        while (htmlPageIterator.hasNext()) {
            HtmlPage htmlPage = htmlPageIterator.next();
            GraphNode graphNode = MongoUtils.newGraphNode(htmlPage);
            this.ds.save(graphNode);
            LOGGER.info("GraphNode Added: ID:{} URL:{}", graphNode.getPageId(), graphNode.getUrl());

        }
        long nodeCount = this.ds.find(GraphNode.class).countAll();
        timer.stop();
        LOGGER.info("GraphNodes({}) haven added into DB in {} ms", nodeCount, timer.getTime());

        StopWatch rankTimer = new StopWatch();
        rankTimer.start();
        long totalCount = this.ds.find(GraphNode.class).countAll();
        double basePR = 1 / totalCount;
        this.ds.update(this.ds.find(GraphNode.class), this.ds.createUpdateOperations(GraphNode.class).set("pageRank", basePR));
        rankTimer.stop();
        LOGGER.info("Initial PageRanks({}) are applied  in {} ms", basePR, rankTimer.getTime());

        this.transfromLinksToWebGraph();

        LOGGER.info("Transforming HtmlPages to WebGraph ended");
    }

    private void transfromLinksToWebGraph() {
        if (this.ds == null) {
            return;
        }
        StopWatch timer = new StopWatch();
        timer.start();
        this.generateGraphEdges();
        StopWatch nodeUpdateTimer = new StopWatch();
        nodeUpdateTimer.start();
        this.generateGraphNodeStats();
        nodeUpdateTimer.stop();
        LOGGER.info("GraphNodes have been updated in {} ms", nodeUpdateTimer.getTime());

        long edgeCount = this.ds.createQuery(GraphEdge.class).countAll();
        timer.stop();
        LOGGER.info("GraphEdges({}) have been added into DB in {} ms", edgeCount, timer.getTime());
    }

    private void generateGraphNodeStats() {
        MorphiaIterator<GraphNode, GraphNode> graphNodeIterator = this.ds.find(GraphNode.class).fetch();
        while (graphNodeIterator.hasNext()) {
            GraphNode graphNode = graphNodeIterator.next();

            Query<GraphEdge> outExQuery = this.ds.find(GraphEdge.class);
            outExQuery.and(outExQuery.criteria("edgeType").equal(LinkType.ExplicitLink), outExQuery.criteria("nodeFrom").equal(graphNode));
            long outExCount = outExQuery.countAll();

            Query<GraphEdge> outImpQuery = this.ds.find(GraphEdge.class);
            outImpQuery.and(outImpQuery.criteria("edgeType").equal(LinkType.ImplicitLink), outImpQuery.criteria("nodeFrom")
                    .equal(graphNode));
            long outImpCount = outImpQuery.countAll();

            Query<GraphEdge> outSemQuery = this.ds.find(GraphEdge.class);
            outSemQuery.and(outSemQuery.criteria("edgeType").equal(LinkType.SemanticLink), outSemQuery.criteria("nodeFrom")
                    .equal(graphNode));
            long outSemCount = outSemQuery.countAll();

            Query<GraphEdge> incExQuery = this.ds.find(GraphEdge.class);
            incExQuery.and(incExQuery.criteria("edgeType").equal(LinkType.ExplicitLink), incExQuery.criteria("nodeTo").equal(graphNode));
            List<Key<GraphEdge>> incExKeyList = incExQuery.asKeyList();

            Query<GraphEdge> incImpQuery = this.ds.find(GraphEdge.class);
            incImpQuery.and(incImpQuery.criteria("edgeType").equal(LinkType.ImplicitLink), incImpQuery.criteria("nodeTo").equal(graphNode));
            List<Key<GraphEdge>> incImpKeyList = incImpQuery.asKeyList();

            Query<GraphEdge> incSemQuery = this.ds.find(GraphEdge.class);
            incSemQuery.and(incSemQuery.criteria("edgeType").equal(LinkType.SemanticLink), incSemQuery.criteria("nodeTo").equal(graphNode));
            List<Key<GraphEdge>> incSemKeyList = incSemQuery.asKeyList();

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
    }

    private void generateGraphEdges() {

        MorphiaIterator<HtmlPage, HtmlPage> htmlPageIterator = this.ds.find(HtmlPage.class).fetch();
        while (htmlPageIterator.hasNext()) {
            HtmlPage htmlPage = htmlPageIterator.next();

            GraphNode nodeFrom = this.ds.find(GraphNode.class, "pageId", htmlPage.getUrl().getDocid()).get();
            if (nodeFrom == null) {
                nodeFrom = this.addGraphNodeByHtmlPage(htmlPage);
            }
            if (nodeFrom.getPageId() < 0) {
                throw new RuntimeException("PageId cannot be negative!");
            }

            Set<WebUrl> outgoingUrls = htmlPage.getHtmlData().getOutgoingUrls();

            if (outgoingUrls == null || outgoingUrls.isEmpty()) {
                continue;
            }

            for (WebUrl webUrl : outgoingUrls) {
                GraphEdge graphEdge = new GraphEdge();
                graphEdge.setEdgeType(LinkType.ExplicitLink);
                graphEdge.setNodeFrom(nodeFrom);

                GraphNode nodeTo = this.ds.find(GraphNode.class, "pageId", webUrl.getDocid()).get();

                if (nodeTo == null) {
                    if (webUrl.getDocid() < 0) {
                        continue;
                    }
                    nodeTo = this.addGraphNodeByHtmlPage(MongoUtils.newHtmlPage(webUrl));
                }

                if (nodeTo.getPageId() < 0) {
                    throw new RuntimeException("PageId cannot be negative!");
                }
                graphEdge.setNodeTo(nodeTo);

                this.ds.save(graphEdge);
                LOGGER.info("GraphEdge Added: {} -> {}", graphEdge.getNodeFrom().getPageId(), graphEdge.getNodeTo().getPageId());
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
