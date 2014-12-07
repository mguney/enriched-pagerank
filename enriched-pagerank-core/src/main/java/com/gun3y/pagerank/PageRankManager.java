package com.gun3y.pagerank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mongodb.morphia.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.entity.graph.GraphEdge;
import com.gun3y.pagerank.entity.graph.GraphNode;
import com.gun3y.pagerank.entity.graph.LinkType;
import com.gun3y.pagerank.entity.html.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.html.HtmlPage;
import com.gun3y.pagerank.mongo.MongoManager;
import com.gun3y.pagerank.utils.BeanUtils;
import com.gun3y.pagerank.utils.LangUtils;

public class PageRankManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageRankManager.class);

    private static final String LANG_EN = "en";

    private static final String HTML_HREF = "href";

    private MongoManager mongoManager;

    public PageRankManager(MongoManager mongoManager) {
        super();
        this.mongoManager = mongoManager;
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

    public static void main(String[] args) {
        MongoManager mongoManager = new MongoManager();
        mongoManager.init();
        PageRankManager pageRankManager = new PageRankManager(mongoManager);
        pageRankManager.transform();

    }

    public void transform() {

        LOGGER.info("Transforming HtmlPages to WebGraph started");

        this.displayHtmlPageStats();

        this.transformEnhancedHtmlPages();

        this.transformGraphNodes();

        this.transformInitialPageRanks();

        this.transformLinks();

        LOGGER.info("Transforming HtmlPages to WebGraph ended");
    }

    private void displayHtmlPageStats() {
        StopWatch timer = new StopWatch();
        timer.start();
        Iterator<HtmlPage> htmlPageIterator = this.mongoManager.getHtmlPageIterator();
        Map<String, Integer> languages = new HashMap<String, Integer>();
        int count = 0;
        while (htmlPageIterator.hasNext()) {
            HtmlPage htmlPage = htmlPageIterator.next();

            String lang = htmlPage.getLanguage();

            if (StringUtils.isBlank(lang)) {
                count++;
                continue;
            }

            if (!languages.containsKey(lang)) {
                languages.put(lang, 0);
            }
            languages.put(lang, languages.get(lang) + 1);
        }
        for (Entry<String, Integer> langEntry : languages.entrySet()) {
            LOGGER.info(langEntry.getKey() + ": " + langEntry.getValue());
        }
        LOGGER.info("No Lang: " + count);
        timer.stop();
        long htmlPages = this.mongoManager.getHtmlPageCount();
        LOGGER.info("HtmlPage({}) Stats are executed in {} ms", htmlPages, timer.getTime());

    }

    private void transformEnhancedHtmlPages() {
        StopWatch timer = new StopWatch();
        timer.start();
        Iterator<HtmlPage> htmlPageIterator = this.mongoManager.getHtmlPageIterator();
        while (htmlPageIterator.hasNext()) {
            HtmlPage htmlPage = htmlPageIterator.next();
            if (!LANG_EN.equals(htmlPage.getLanguage())) {
                continue;
            }

            EnhancedHtmlPage enhancedHtmlPage = BeanUtils.newEnhancedHtmlPage(htmlPage);
            this.mongoManager.addEnhancedHtmlPage(enhancedHtmlPage);

        }
        long nodeCount = this.mongoManager.getEnhancedHtmlPageCount();
        LOGGER.info("EnhancedHtmlPages({}) haven added into DB in {} ms", nodeCount, timer.getTime());

        timer.reset();
        timer.start();
        Iterator<EnhancedHtmlPage> enhancedHtmlPageIterator = this.mongoManager.getEnhancedHtmlPageIterator();
        while (enhancedHtmlPageIterator.hasNext()) {
            EnhancedHtmlPage enhancedHtmlPage = enhancedHtmlPageIterator.next();

            this.updateAnchors(enhancedHtmlPage.getHtml(), enhancedHtmlPage.getUrl());
        }
        LOGGER.info("EnhancedHtmlPages({}) have been updated in {} ms", nodeCount, timer.getTime());
    }

    private void updateAnchors(String html, String url) {
        Document doc = Jsoup.parse(html);
        doc.setBaseUri(url);
        Elements links = doc.select("a[href]");
        for (Element element : links) {

            String href = element.attr(HTML_HREF);
            String absUrl = element.absUrl(HTML_HREF);
            String text = element.text();
            String title = element.attr("title");

            if (StringUtils.isBlank(href)) {
                continue;
            }
            href = href.toLowerCase(Locale.ENGLISH);

            if (href.startsWith("#") || href.startsWith("mailto:") || href.startsWith("javascript:")) {
                continue;
            }

            if (StringUtils.isBlank(absUrl)) {
                LOGGER.warn("Irrelevant Href:" + href);
                continue;
            }

            EnhancedHtmlPage nextEnhancedHtmlPage = this.mongoManager.getEnhancedHtmlPageByUrl(absUrl);
            if (nextEnhancedHtmlPage == null) {
                LOGGER.warn("Missing EnhancedHtmlPage: " + absUrl);
            }
            else {
                String anchor = StringUtils.EMPTY;
                if (StringUtils.isNotBlank(text)) {
                    anchor = text;
                }
                if (StringUtils.isNotBlank(title) && title.length() > anchor.length()) {
                    anchor = title;
                }

                if (StringUtils.isNotBlank(anchor)) {
                    String stemmedAnchor = LangUtils.joinList(LangUtils.extractStemmedWords(anchor));
                    Set<String> stemmedAnchorTitles = nextEnhancedHtmlPage.getStemmedAnchorTitles();

                    if (StringUtils.isNotBlank(stemmedAnchor)) {
                        stemmedAnchorTitles.add(stemmedAnchor);
                        this.mongoManager.updateEnhancedHtmlPage(nextEnhancedHtmlPage, stemmedAnchorTitles);
                    }
                }
            }
        }
    }

    private void transformGraphNodes() {

        StopWatch timer = new StopWatch();
        timer.start();
        Iterator<EnhancedHtmlPage> enhancedHtmlPageIterator = this.mongoManager.getEnhancedHtmlPageIterator();
        while (enhancedHtmlPageIterator.hasNext()) {
            EnhancedHtmlPage enhancedHtmlPage = enhancedHtmlPageIterator.next();
            GraphNode graphNode = BeanUtils.newGraphNode(enhancedHtmlPage);
            this.mongoManager.addGraphNode(graphNode);
        }
        long nodeCount = this.mongoManager.getGraphNodeCount();
        timer.stop();
        LOGGER.info("GraphNodes({}) haven added into DB in {} ms", nodeCount, timer.getTime());

    }

    private void transformInitialPageRanks() {
        StopWatch rankTimer = new StopWatch();
        rankTimer.start();
        long totalCount = this.mongoManager.getGraphNodeCount();
        double basePR = 1 / totalCount;
        this.mongoManager.updateGraphNode(basePR);
        rankTimer.stop();
        LOGGER.info("Initial PageRanks({}) have been applied  in {} ms", basePR, rankTimer.getTime());

    }

    private void transformLinks() {
        StopWatch timer = new StopWatch();
        timer.start();

        this.transformExplicitLinks();

        this.transformImplicitLinks();

        this.transformSemanticLinks();

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

    private void transformSemanticLinks() {
        StopWatch timer = new StopWatch();
        timer.start();
        // TODO: code here
        timer.stop();
        LOGGER.info("SemanticEdges have been created in {} ms", timer.getTime());

    }

    private void transformImplicitLinks() {
        StopWatch timer = new StopWatch();
        timer.start();

        Iterator<EnhancedHtmlPage> enhancedHtmlPageIterator = this.mongoManager.getEnhancedHtmlPageIterator();
        while (enhancedHtmlPageIterator.hasNext()) {
            EnhancedHtmlPage enhancedHtmlPage = enhancedHtmlPageIterator.next();
            GraphNode nodeTo = this.mongoManager.getGraphNodeById(enhancedHtmlPage.getPageId());

            Set<String> stemmedTitles = new HashSet<String>();
            String stemmedTitle = enhancedHtmlPage.getStemmedTitle();

            if (StringUtils.isNotBlank(stemmedTitle)) {
                stemmedTitles.add(stemmedTitle);
            }

            Set<String> stemmedAnchorTitles = enhancedHtmlPage.getStemmedAnchorTitles();
            if (stemmedAnchorTitles != null) {
                stemmedTitles.addAll(stemmedAnchorTitles);
            }

            if (stemmedTitles.isEmpty()) {
                continue;
            }

            this.updateImplicitLinks(enhancedHtmlPage, nodeTo, stemmedTitles);

        }

        timer.stop();
        LOGGER.info("ImplicitEdges have been created in {} ms", timer.getTime());
    }

    public void updateImplicitLinks(EnhancedHtmlPage enhancedHtmlPage, GraphNode nodeTo, Set<String> stemmedTitles) {
        Iterator<EnhancedHtmlPage> nextEnhancedHtmlPageIterator = this.mongoManager.getEnhancedHtmlPageIterator();
        while (nextEnhancedHtmlPageIterator.hasNext()) {
            EnhancedHtmlPage nextEnhancedHtmlPage = nextEnhancedHtmlPageIterator.next();

            if (nextEnhancedHtmlPage.getPageId() == enhancedHtmlPage.getPageId()) {
                continue;
            }

            String stemmedText = nextEnhancedHtmlPage.getStemmedText();
            if (StringUtils.isBlank(stemmedText)) {
                continue;
            }
            for (String key : stemmedTitles) {
                if (stemmedText.contains(key.toLowerCase(Locale.ENGLISH))) {
                    GraphNode nodeFrom = this.mongoManager.getGraphNodeById(nextEnhancedHtmlPage.getPageId());
                    this.mongoManager.addGraphEdge(nodeFrom, nodeTo, LinkType.ImplicitLink);
                }
            }

        }

    }

    private void transformExplicitLinks() {
        StopWatch timer = new StopWatch();
        timer.start();

        Iterator<EnhancedHtmlPage> enhancedHtmlPageIterator = this.mongoManager.getEnhancedHtmlPageIterator();
        while (enhancedHtmlPageIterator.hasNext()) {
            EnhancedHtmlPage enhancedHtmlPage = enhancedHtmlPageIterator.next();
            GraphNode nodeFrom = this.mongoManager.getGraphNodeById(enhancedHtmlPage.getPageId());

            if (nodeFrom == null || nodeFrom.getPageId() < 0) {
                throw new RuntimeException("GraphNode(" + enhancedHtmlPage.getPageId() + ") not Found!");
            }

            Set<String> outgoingUrls = enhancedHtmlPage.getOutgoingUrls();

            if (outgoingUrls == null || outgoingUrls.isEmpty()) {
                continue;
            }

            for (String url : outgoingUrls) {

                GraphNode nodeTo = this.mongoManager.getGraphNodeByUrl(url);

                if (nodeTo == null || nodeTo.getPageId() < 0) {
                    continue;
                }

                this.mongoManager.addGraphEdge(nodeFrom, nodeTo, LinkType.ExplicitLink);
            }
        }

        timer.stop();
        LOGGER.info("ExplicitEdges have been created in {} ms", timer.getTime());
    }

    public MongoManager getMongoManager() {
        return this.mongoManager;
    }

    public void setMongoManager(MongoManager mongoManager) {
        this.mongoManager = mongoManager;
    }

}
