package com.gun3y.pagerank;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.mongodb.morphia.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.entity.graph.GraphEdge;
import com.gun3y.pagerank.entity.graph.GraphNode;
import com.gun3y.pagerank.entity.graph.LinkType;
import com.gun3y.pagerank.entity.html.EnhancedHtmlPage;
import com.gun3y.pagerank.store.MongoManager;
import com.gun3y.pagerank.store.VirtuosoManager;
import com.gun3y.pagerank.utils.BeanUtils;

public class PageRankManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageRankManager.class);

    private MongoManager mongoManager;

    private VirtuosoManager virtuosoManager;

    public PageRankManager(MongoManager mongoManager, VirtuosoManager virtuosoManager) {
        super();
        this.mongoManager = mongoManager;
        this.virtuosoManager = virtuosoManager;
    }

    public PageRankManager() {
        super();
        this.mongoManager = new MongoManager();
        this.mongoManager.init();
    }

    public void compute(int numOfIteration) {
        StopWatch timer = new StopWatch();
        timer.start();

        int count = 0;
        while (numOfIteration > 0) {
            numOfIteration--;
            LOGGER.info("{} iteration start", ++count);
            this.iteratePageRank();
        }
        timer.stop();
        LOGGER.info("PageRank Computing has been finished in {} ms", timer.getTime());
    }

    private void iteratePageRank() {
        Iterator<GraphNode> graphNodeIterator = this.mongoManager.getGraphNodeIterator();
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

            this.mongoManager.updateGraphNode(graphNode, pageRank);
            pageTimer.stop();

            LOGGER.info("PageRank has been computed for GrapNode:{} in {} ms(Explicit: {}, Implicit: {} Semantic: {}, Update:{})",
                    graphNode.getPageId(), pageTimer.getTime(), explicitTimer.getTime(), implicitTimer.getTime(), semanticTimer.getTime(),
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

    public void transform() {

        LOGGER.info("Transforming HtmlPages to WebGraph started");

        this.transformGraphNodes();

        this.transformInitialPageRanks();

        this.transformLinks();

        LOGGER.info("Transforming HtmlPages to WebGraph ended");
    }

    private void transformGraphNodes() {

        StopWatch timer = new StopWatch();
        timer.start();
        Iterator<EnhancedHtmlPage> enhancedHtmlPageIterator = this.mongoManager.getEnhancedHtmlPageIterator();
        while (enhancedHtmlPageIterator.hasNext()) {
            EnhancedHtmlPage enhancedHtmlPage = enhancedHtmlPageIterator.next();
            GraphNode graphNode = BeanUtils.newGraphNode(enhancedHtmlPage);
            this.virtuosoManager.addGraphNode(graphNode);
        }
        long nodeCount = this.virtuosoManager.getGraphNodeCount();
        timer.stop();
        LOGGER.info("GraphNodes({}) haven added into DB in {} ms", nodeCount, timer.getTime());

    }

    private void transformInitialPageRanks() {
        StopWatch rankTimer = new StopWatch();
        rankTimer.start();
        long totalCount = this.virtuosoManager.getGraphNodeCount();
        double basePR = 1 / totalCount;
        this.virtuosoManager.updateGraphNode(basePR);
        rankTimer.stop();
        LOGGER.info("Initial PageRanks({}) have been applied  in {} ms", basePR, rankTimer.getTime());

    }

    private void transformLinks() {
        StopWatch timer = new StopWatch();
        timer.start();

        this.transformGraphNodeStats();

        long edgeCount = this.mongoManager.getEdgeCount();
        timer.stop();
        LOGGER.info("GraphEdges({}) have been added into DB in {} ms", edgeCount, timer.getTime());
    }

    private void transformGraphNodeStats() {
        StopWatch timer = new StopWatch();
        timer.start();
        Iterator<GraphNode> graphNodeIterator = this.mongoManager.getGraphNodeIterator();
        while (graphNodeIterator.hasNext()) {
            GraphNode graphNode = graphNodeIterator.next();

            long outExCount = this.mongoManager.getEdgeCount(LinkType.ExplicitLink, graphNode);
            long outImpCount = this.mongoManager.getEdgeCount(LinkType.ImplicitLink, graphNode);
            long outSemCount = this.mongoManager.getEdgeCount(LinkType.SemanticLink, graphNode);

            List<Key<GraphEdge>> incExKeyList = this.mongoManager.getGraphEdgeKeys(LinkType.ExplicitLink, graphNode);
            List<Key<GraphEdge>> incImpKeyList = this.mongoManager.getGraphEdgeKeys(LinkType.ImplicitLink, graphNode);
            List<Key<GraphEdge>> incSemKeyList = this.mongoManager.getGraphEdgeKeys(LinkType.SemanticLink, graphNode);
            this.mongoManager.updateGraphNode(graphNode, outExCount, outImpCount, outSemCount, incExKeyList, incImpKeyList, incSemKeyList);
        }
        timer.stop();
        LOGGER.info("GraphStats have been computed in {} ms", timer.getTime());
    }

    public MongoManager getMongoManager() {
        return this.mongoManager;
    }

    public void setMongoManager(MongoManager mongoManager) {
        this.mongoManager = mongoManager;
    }

}
