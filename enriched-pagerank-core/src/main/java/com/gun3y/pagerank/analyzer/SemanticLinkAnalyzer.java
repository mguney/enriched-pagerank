package com.gun3y.pagerank.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.gun3y.pagerank.common.HtmlToText;
import com.gun3y.pagerank.common.LineItem;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.HtmlTitle;
import com.gun3y.pagerank.entity.LinkTuple;
import com.gun3y.pagerank.entity.LinkType;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Triple;

public class SemanticLinkAnalyzer implements LinkAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemanticLinkAnalyzer.class);

    public static final String IN_SAME_SENTENCE = "__inSameSentence__";

    private StanfordCoreNLP tokenizer;

    private final BlockingQueue<LineItem> workQueue;

    private final Vector<LinkTuple> tuples;

    private final ExecutorService service;

    private final int workQueueSize;

    private final int numWorkers;

    private final AtomicInteger counter;

    public SemanticLinkAnalyzer() {
        this(1);
    }

    public SemanticLinkAnalyzer(int numWorkers) {

        if (numWorkers < 1) {
            throw new RuntimeException("Number of workers cannot be zero or less");
        }

        this.numWorkers = numWorkers;
        this.workQueueSize = numWorkers * 3;
        this.counter = new AtomicInteger(numWorkers);

        Properties tokenProps = new Properties();
        tokenProps.put("annotators", "tokenize, ssplit, pos");
        this.tokenizer = new StanfordCoreNLP(tokenProps);

        this.workQueue = new LinkedBlockingQueue<LineItem>(this.workQueueSize);
        this.service = Executors.newFixedThreadPool(this.numWorkers);

        this.tuples = new Vector<LinkTuple>();

        for (int i = 0; i < this.numWorkers; i++) {
            this.service.submit(new Worker("worker-" + i, this.workQueue, this.tuples, this.counter));
        }

    }

    public static void main(String[] args) throws IOException {
        System.out.println(Calendar.getInstance().getTime());
        SemanticLinkAnalyzer analyzer = new SemanticLinkAnalyzer(1);

        String html = FileUtils.readFileToString(new File(SemanticLinkAnalyzer.class.getClassLoader().getResource("wiki_sample.html")
                .getPath()));
        String baseUrl = "http://en.wikipedia.org/";

        Document doc = Jsoup.parse(html, baseUrl);
        HtmlToText formatter = new HtmlToText();
        List<LineItem> lines = formatter.getLines(doc);

        List<LinkTuple> analyze = analyzer.analyze(lines);
        System.out.println(Calendar.getInstance().getTime());

        for (LinkTuple linkTuple : analyze) {
            System.out.println(linkTuple);
        }
        System.out.println(analyze.size());
        analyzer.shutdown();
    }

    public static void main22(String[] args) throws IOException {
        System.out.println(Calendar.getInstance().getTime());
        SemanticLinkAnalyzer analyzer = new SemanticLinkAnalyzer(1);

        String line = "NP1 has original text related to this article: NP2";

        Map<String, Pair<String, String>> map = new HashMap<String, Pair<String, String>>();
        map.put("NP1", new MutablePair<String, String>("Wikisource", "http://url1"));
        map.put("NP2", new MutablePair<String, String>("England portal", "http://url2"));

        LineItem lineItem = new LineItem(line, map);

        List<LinkTuple> analyze = analyzer.analyze(Arrays.asList(lineItem));
        // List<LinkTuple> analyze = analyzer.analyze(lines);
        System.out.println(Calendar.getInstance().getTime());

        for (LinkTuple linkTuple : analyze) {
            System.out.println(linkTuple);
        }
        System.out.println(analyze.size());
        analyzer.shutdown();
    }

    @Override
    public synchronized List<LinkTuple> analyze(EnhancedHtmlPage ePage) {
        List<LineItem> lines = ePage.getLines();
        return this.analyze(lines);
    }

    @Override
    public synchronized List<LinkTuple> analyze(EnhancedHtmlPage ePage, HtmlTitle htmlTitle) {
        return Collections.emptyList();
    }

    public void shutdown() {
        this.service.shutdownNow();
    }

    private List<LinkTuple> analyze(List<LineItem> lines) {

        List<LinkTuple> retTuples = new ArrayList<LinkTuple>();

        if (lines.isEmpty()) {
            return retTuples;
        }

        for (LineItem lineItem : lines) {
            if (lineItem.getUrls().size() < 2) {
                continue;
            }

            List<LineItem> items = this.extractSentences(lineItem);

            for (LineItem item : items) {

                try {
                    this.workQueue.put(item);
                }
                catch (InterruptedException ex) {
                    LOGGER.error(ex.getMessage());
                    Thread.currentThread().interrupt();
                }
            }

        }

        while (!this.workQueue.isEmpty() || this.counter.get() != 0) {
            try {
                Thread.sleep(200);
            }
            catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        retTuples.addAll(this.tuples);
        this.workQueue.clear();
        this.tuples.clear();

        return retTuples;
    }

    private List<LineItem> extractSentences(LineItem lineItem) {
        List<LineItem> items = new ArrayList<LineItem>();

        Annotation document = this.tokenizer.process(lineItem.getLine());

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {

            String sentenceText = sentence.get(TextAnnotation.class);
            Map<String, Pair<String, String>> urls = new HashMap<String, Pair<String, String>>();

            for (Entry<String, Pair<String, String>> entry : lineItem.getUrls().entrySet()) {
                if (sentenceText.contains(entry.getKey())) {
                    urls.put(entry.getKey(), entry.getValue());
                }
            }

            if (urls.size() > 1) {
                items.add(new LineItem(sentenceText, urls));
            }

        }
        return items;
    }

    private static class PairWord {
        IndexedWord firstWord;
        IndexedWord secondWord;

        String first;
        String second;

        String firstUrl;
        String secondUrl;

        String predicate;

        @Override
        public String toString() {
            return this.first + "->" + this.predicate + "->" + this.second;
        }
    }

    private static class Worker implements Runnable {

        private StanfordCoreNLP pipeline;

        private final BlockingQueue<LineItem> workQueue;

        private final Vector<LinkTuple> tuples;

        private final AtomicInteger counter;

        private String workerId;

        public Worker(String workerId, BlockingQueue<LineItem> workQueue, Vector<LinkTuple> tuples, AtomicInteger counter) {
            this.workQueue = workQueue;
            this.tuples = tuples;
            this.counter = counter;

            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
            this.pipeline = new StanfordCoreNLP(props);

            this.workerId = workerId;

        }

        @Override
        public void run() {
            MDC.put("logFileName", this.workerId);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    this.counter.decrementAndGet();
                    LineItem lineItem = this.workQueue.take();
                    this.counter.incrementAndGet();

                    this.executeLineItem(lineItem);
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        private void executeLineItem(LineItem lineItem) {
            String sentenceText = this.buildSingleSentence(lineItem);
            LOGGER.debug("######################");
            LOGGER.debug(sentenceText);

            Map<String, Pair<String, String>> urlMap = lineItem.getUrls();
            for (Entry<String, Pair<String, String>> entry : urlMap.entrySet()) {
                LOGGER.debug(entry.getKey() + " -> " + entry.getValue().getKey() + " -> " + entry.getValue().getValue());
            }

            List<PairWord> pairs = this.extractPairs(urlMap);

            if (pairs.isEmpty()) {
                return;
            }

            Annotation document = this.pipeline.process(sentenceText);
            List<CoreMap> sentences = document.get(SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {
                SemanticGraph semanticGraph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);

                if (semanticGraph == null) {
                    continue;
                }

                LOGGER.debug(semanticGraph.toString());

                Map<String, IndexedWord> indexedWordMap = new HashMap<String, IndexedWord>();

                for (PairWord pairWord : pairs) {

                    if (indexedWordMap.containsKey(pairWord.first)) {
                        pairWord.firstWord = indexedWordMap.get(pairWord.first);
                    }
                    else {
                        pairWord.firstWord = this.findCommonAncestor(semanticGraph, pairWord.first);
                        LOGGER.debug(pairWord.first + " -> " + pairWord.firstWord);
                        indexedWordMap.put(pairWord.first, pairWord.firstWord);
                    }

                    if (indexedWordMap.containsKey(pairWord.second)) {
                        pairWord.secondWord = indexedWordMap.get(pairWord.second);
                    }
                    else {
                        pairWord.secondWord = this.findCommonAncestor(semanticGraph, pairWord.second);
                        LOGGER.debug(pairWord.second + " -> " + pairWord.secondWord);
                        indexedWordMap.put(pairWord.second, pairWord.secondWord);
                    }

                    PairWord triple = this.extractSemanticTriple(semanticGraph, pairWord);
                    if (triple != null) {
                        LinkTuple tuple = new LinkTuple(triple.firstUrl, LinkType.SemanticLink, triple.secondUrl, triple.predicate);
                        LOGGER.debug("SemanticLink: " + triple.first + " --[" + triple.predicate + "]--> " + triple.second);
                        LOGGER.info(tuple.toString());
                        this.tuples.add(tuple);
                    }

                }

            }

        }

        private List<IndexedWord> extractTokens(String text) {
            Annotation document = this.pipeline.process(text);
            List<CoreMap> sentences = document.get(SentencesAnnotation.class);
            List<IndexedWord> retList = new ArrayList<IndexedWord>();
            for (CoreMap sentence : sentences) {
                SemanticGraph semanticGraph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);

                if (semanticGraph == null) {
                    continue;
                }

                List<IndexedWord> vertexListSorted = semanticGraph.vertexListSorted();
                retList.addAll(vertexListSorted);
            }

            return retList;
        }

        private IndexedWord findCommonAncestor(SemanticGraph semanticGraph, String text) {
            IndexedWord ancestorWord = null;

            List<IndexedWord> tokenWords = this.extractTokens(text);
            List<String> tokens = tokenWords.stream().map(a -> a.originalText()).collect(Collectors.toList());
            LOGGER.debug("Tokens: " + text + " --> " + tokens);

            if (tokens.isEmpty()) {
                return ancestorWord;
            }

            if (tokenWords.size() == 1 && tokenWords.get(0).tag().contains("VB")) {
                return ancestorWord;
            }

            List<IndexedWord> vertexListSorted = semanticGraph.vertexListSorted();
            if (vertexListSorted.isEmpty()) {
                return ancestorWord;
            }

            List<String> vertexList = vertexListSorted.stream().map(a -> a.originalText()).collect(Collectors.toList());

            int indexOfSubList = Collections.indexOfSubList(vertexList, tokens);

            if (indexOfSubList < 0) {
                return ancestorWord;
            }

            List<IndexedWord> subList = vertexListSorted.subList(indexOfSubList, indexOfSubList + tokens.size());

            return subList.stream()
                    .min((a, b) -> Integer.compare(semanticGraph.getPathToRoot(a).size(), semanticGraph.getPathToRoot(b).size())).get();

        }

        private String buildSingleSentence(LineItem lineItem) {
            String text = new String(lineItem.getLine());

            for (Entry<String, Pair<String, String>> entry : lineItem.getUrls().entrySet()) {
                text = text.replace(entry.getKey(), entry.getValue().getKey());
            }

            return text;
        }

        private PairWord extractSemanticTriple(SemanticGraph semanticGraph, PairWord pairWord) {
            if (pairWord.firstWord == null || pairWord.secondWord == null) {
                return null;
            }
            List<SemanticGraphEdge> edges = null;
            boolean reverse = false;
            if (pairWord.firstWord.beginPosition() > pairWord.secondWord.beginPosition()) {
                reverse = true;
                edges = semanticGraph.getShortestUndirectedPathEdges(pairWord.secondWord, pairWord.firstWord);
                LOGGER.debug("From " + pairWord.secondWord + " to " + pairWord.firstWord + "  edges ----> " + edges.toString());
            }
            else {
                edges = semanticGraph.getShortestUndirectedPathEdges(pairWord.firstWord, pairWord.secondWord);
                LOGGER.debug("From " + pairWord.firstWord + " to " + pairWord.secondWord + "  edges ----> " + edges.toString());
            }

            Triple<GrammaticalRelation, IndexedWord, GrammaticalRelation> rootWord = null;

            for (int i = 0; i < edges.size(); i++) {
                SemanticGraphEdge firstEdge = edges.get(i);

                if (edges.size() > (i + 1)) {
                    SemanticGraphEdge secondEdge = edges.get(i + 1);

                    // Find root
                    if (firstEdge.getSource().equals(secondEdge.getSource()) && firstEdge.getSource().tag().contains("VB")) {
                        rootWord = new Triple<GrammaticalRelation, IndexedWord, GrammaticalRelation>(firstEdge.getRelation(),
                                firstEdge.getSource(), secondEdge.getRelation());

                    }

                }
            }

            // if (rootWord == null && !verbs.isEmpty()) {
            // rootWord = verbs.get(0);
            // }

            if (rootWord != null) {
                return this.createSemanticLink(rootWord, pairWord, reverse);
            }

            // if (!edges.isEmpty()) {
            //
            // PairWord pWord = new PairWord();
            // pWord.first = pairWord.first;
            // pWord.firstUrl = pairWord.firstUrl;
            // pWord.second = pairWord.second;
            // pWord.secondUrl = pairWord.secondUrl;
            // pWord.predicate = IN_SAME_SENTENCE;
            // return pWord;
            // }

            return null;
        }

        private PairWord createSemanticLink(Triple<GrammaticalRelation, IndexedWord, GrammaticalRelation> rootWord, PairWord pairWord,
                boolean reverse) {
            PairWord retWord = new PairWord();
            String firstRel = rootWord.first().getShortName();
            String secondRel = rootWord.third.getShortName();

            boolean direction = !reverse;

            if (firstRel.equals("nsubjpass")) {
                direction = false;
            }
            else if (secondRel.equals("nsubj")) {
                direction = false;
            }

            if (direction) {
                retWord.first = pairWord.first;
                retWord.firstUrl = pairWord.firstUrl;

                retWord.second = pairWord.second;
                retWord.secondUrl = pairWord.secondUrl;
            }
            else {
                retWord.first = pairWord.second;
                retWord.firstUrl = pairWord.secondUrl;

                retWord.second = pairWord.first;
                retWord.secondUrl = pairWord.firstUrl;
            }
            retWord.predicate = rootWord.second.lemma();
            return retWord;
        }

        private List<PairWord> extractPairs(Map<String, Pair<String, String>> urls) {
            List<PairWord> retList = new ArrayList<PairWord>();

            if (urls.isEmpty()) {
                return retList;
            }

            List<Pair<String, String>> accList = new ArrayList<Pair<String, String>>();
            for (Entry<String, Pair<String, String>> entry : urls.entrySet()) {
                accList.add(entry.getValue());
            }

            if (accList.size() < 2) {
                return retList;
            }

            for (int i = 0; i < accList.size(); i++) {
                for (int j = i + 1; j < accList.size(); j++) {
                    PairWord pairWord = new PairWord();
                    pairWord.first = accList.get(i).getKey();
                    pairWord.firstUrl = accList.get(i).getValue();
                    pairWord.second = accList.get(j).getKey();
                    pairWord.secondUrl = accList.get(j).getValue();

                    retList.add(pairWord);
                }
            }

            return retList;
        }
    }

}
