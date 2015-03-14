package com.gun3y.pagerank.analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.common.LineItem;
import com.gun3y.pagerank.dao.HtmlTitleDao;
import com.gun3y.pagerank.dao.LinkTupleDao;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.HtmlTitle;
import com.gun3y.pagerank.entity.LinkTuple;
import com.gun3y.pagerank.entity.LinkType;
import com.gun3y.pagerank.utils.LangUtils;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;

class SemanticLinkWorker extends LinkWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemanticLinkWorker.class);

    private StanfordCoreNLP pipeline;

    private HtmlTitleDao htmlTitleDao;

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

    public SemanticLinkWorker(String workerId, BlockingQueue<EnhancedHtmlPage> workQueue, AtomicInteger counter, LinkTupleDao linkTupleDao,
            HtmlTitleDao htmlTitleDao) {
        super(workerId, workQueue, counter, linkTupleDao);
        this.htmlTitleDao = htmlTitleDao;
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
        this.pipeline = new StanfordCoreNLP(props);
    }

    @Override
    public List<LinkTuple> analyze(EnhancedHtmlPage ePage) {
        List<LinkTuple> retTuples = new ArrayList<LinkTuple>();

        List<LineItem> lines = ePage.getLines();

        if (lines == null || lines.isEmpty()) {
            return retTuples;
        }

        try {
            for (LineItem lineItem : lines) {
                List<LineItem> items = this.extractSentences(lineItem);

                for (LineItem item : items) {
                    List<LinkTuple> tuples = this.executeLineItem(item);
                    retTuples.addAll(tuples);
                }
            }
        }
        catch (Throwable e) {
            Thread.currentThread().interrupt();
            LOGGER.error(e.getMessage(), e);
            return retTuples;
        }

        return retTuples;
    }

    private List<LineItem> extractSentences(LineItem lineItem) {
        List<LineItem> items = new ArrayList<LineItem>();

        Annotation document = this.pipeline.process(lineItem.getLine());

        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {

            String sentenceText = sentence.get(TextAnnotation.class);

            Map<String, String> urls = new HashMap<String, String>();

            List<Pair<String, String>> implicitUrls = this.findImplictUrls(sentence.get(TokensAnnotation.class));

            implicitUrls.stream().forEach(a -> {
                if (sentenceText.contains(a.first)) {
                    urls.put(a.first, a.second);
                }
            });
            lineItem.getUrls().forEach((key, value) -> {
                if (sentenceText.contains(key)) {
                    urls.put(key, value);
                }
            });

            if (urls.size() > 1) {
                items.add(new LineItem(sentenceText, urls));
            }

        }
        return items;
    }

    private List<Pair<String, String>> findImplictUrls(List<CoreLabel> tokens) {
        List<Pair<String, String>> retList = new ArrayList<Pair<String, String>>();

        if (tokens.size() < 2) {
            return retList;
        }

        List<String> lemmaList = tokens.stream().map(a -> a.lemma().toLowerCase(Locale.ENGLISH)).collect(Collectors.toList());
        List<String> textList = tokens.stream().map(a -> a.originalText()).collect(Collectors.toList());

        if (lemmaList.size() != textList.size()) {
            LOGGER.warn("Lemma List and Orginal Text list are not in same size");
            return retList;
        }

        String lemmaText = LangUtils.joinList(lemmaList);

        List<HtmlTitle> htmlTitles = this.htmlTitleDao.findHtmlTitleByTitle(lemmaText);

        for (HtmlTitle htmlTitle : htmlTitles) {
            List<String> titleKeys = Arrays.asList(htmlTitle.getStemmedTitle().split(" "));

            if (titleKeys.size() > textList.size()) {
                continue;
            }

            int indexOfSubList = Collections.indexOfSubList(lemmaList, titleKeys);

            if (indexOfSubList > -1) {
                String titleName = LangUtils.joinList(textList.subList(indexOfSubList, indexOfSubList + titleKeys.size()));
                retList.add(new Pair<String, String>(titleName, htmlTitle.getUrl()));
            }
        }

        return retList;
    }

    private List<LinkTuple> executeLineItem(LineItem lineItem) {
        String sentenceText = lineItem.getLine();
        LOGGER.debug("######################");
        LOGGER.debug(sentenceText);

        Map<String, String> urlMap = lineItem.getUrls();
        for (Entry<String, String> entry : urlMap.entrySet()) {
            LOGGER.debug(entry.getKey() + " -> " + entry.getValue());
        }

        List<PairWord> pairs = this.extractPairs(urlMap);
        List<LinkTuple> retTuples = new ArrayList<LinkTuple>();

        if (pairs.isEmpty()) {
            return retTuples;
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
                    retTuples.add(tuple);
                }

            }

        }

        return retTuples;
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

    private PairWord extractSemanticTriple(SemanticGraph semanticGraph, PairWord pairWord) {
        if (pairWord.firstWord == null || pairWord.secondWord == null) {
            return null;
        }

        PairWord accPairWord = this.newPairWord(pairWord);

        List<SemanticGraphEdge> edges = semanticGraph.getShortestUndirectedPathEdges(accPairWord.firstWord, accPairWord.secondWord);
        LOGGER.debug("From " + accPairWord.firstWord + " to " + accPairWord.secondWord + "  edges ----> " + edges.toString());

        Triple<GrammaticalRelation, IndexedWord, GrammaticalRelation> rootWord = null;

        if (edges == null || edges.size() < 2) {
            return null;
        }

        SemanticGraphEdge fEdge = edges.get(0);

        if (fEdge.getSource().equals(accPairWord.firstWord)) {
            // Root yok. ama ilk gelen VB'yi kullanabiliriz belki
            rootWord = null;
        }
        else if (fEdge.getTarget().equals(accPairWord.firstWord)) {
            // Root var.
            rootWord = this.findRootWord(accPairWord, edges);
        }

        if (rootWord == null) {
            return null;
        }

        return this.createSemanticLink(rootWord, accPairWord);
    }

    private Triple<GrammaticalRelation, IndexedWord, GrammaticalRelation> findRootWord(PairWord accPairWord, List<SemanticGraphEdge> edges) {

        Triple<GrammaticalRelation, IndexedWord, GrammaticalRelation> rootWord = null;

        List<Pair<IndexedWord, GrammaticalRelation>> subjWords = new ArrayList<Pair<IndexedWord, GrammaticalRelation>>();
        List<Pair<IndexedWord, GrammaticalRelation>> objWords = new ArrayList<Pair<IndexedWord, GrammaticalRelation>>();

        boolean firstAccessToRoot = true;

        for (int i = 0; i < edges.size(); i++) {
            SemanticGraphEdge firstEdge = edges.get(i);

            if (edges.size() > (i + 1)) {
                SemanticGraphEdge secondEdge = edges.get(i + 1);
                // Find root
                if (firstEdge.getSource().equals(secondEdge.getSource())) {
                    rootWord = new Triple<GrammaticalRelation, IndexedWord, GrammaticalRelation>(firstEdge.getRelation(),
                            firstEdge.getSource(), secondEdge.getRelation());
                }
            }

            if (rootWord == null) {
                subjWords.add(new Pair<IndexedWord, GrammaticalRelation>(firstEdge.getTarget(), firstEdge.getRelation()));
            }
            else if (firstEdge.getSource().equals(rootWord.second)) {
                if (firstAccessToRoot) {
                    subjWords.add(new Pair<IndexedWord, GrammaticalRelation>(firstEdge.getTarget(), firstEdge.getRelation()));
                    firstAccessToRoot = false;
                }
                else {
                    objWords.add(new Pair<IndexedWord, GrammaticalRelation>(firstEdge.getTarget(), firstEdge.getRelation()));
                }
            }
            else {
                objWords.add(new Pair<IndexedWord, GrammaticalRelation>(firstEdge.getTarget(), firstEdge.getRelation()));
            }
        }

        if (rootWord == null) {
            LOGGER.debug("ROOT not found [" + accPairWord.firstWord + " and " + accPairWord.secondWord + "]");
            return null;
        }
        else if (!rootWord.second.tag().contains("VB")) {
            LOGGER.debug("ROOT(VB) not found [" + accPairWord.firstWord + " and " + accPairWord.secondWord + "]");
            return null;
        }
        else {
            boolean allCheck = true;
            if (rootWord.first.getShortName().contains("dobj")) {
                allCheck &= this.checkObjs(subjWords, accPairWord.firstWord);
                allCheck &= this.checkSubjs(objWords, accPairWord.secondWord);
            }
            else {
                allCheck &= this.checkSubjs(subjWords, accPairWord.firstWord);
                allCheck &= this.checkObjs(objWords, accPairWord.secondWord);
            }

            if (!allCheck) {
                LOGGER.debug("ROOT(checked) not found [" + accPairWord.firstWord + " and " + accPairWord.secondWord + "]");
                return null;
            }
        }

        return rootWord;
    }

    private boolean checkObjs(List<Pair<IndexedWord, GrammaticalRelation>> objWords, IndexedWord secondWord) {
        boolean includesNObj = false;
        boolean includesVB = false;
        boolean includesConj = false;
        boolean includesPrep = false;

        // conj'lara ve prep'lere dikkat et

        for (Pair<IndexedWord, GrammaticalRelation> objPair : objWords) {
            IndexedWord word = objPair.first;
            GrammaticalRelation rel = objPair.second;

            if (rel.getShortName().contains("dobj")) {
                includesNObj = true;
            }

            if (secondWord.equals(word)) {
                continue;
            }

            if (word.tag().contains("VB")) {
                includesVB = true;
            }

            if (rel.getShortName().contains("conj")) {
                includesConj = true;
            }

            if (rel.getShortName().contains("prep")) {
                includesPrep = true;
            }

        }

        return includesNObj && !includesVB && !includesConj && !includesPrep;
    }

    private boolean checkSubjs(List<Pair<IndexedWord, GrammaticalRelation>> subjWords, IndexedWord firstWord) {

        boolean includesNSubj = false;
        boolean includesVB = false;
        boolean includesConj = false;
        boolean includesPrep = false;

        // conj'lara ve prep'lere dikkat et

        for (Pair<IndexedWord, GrammaticalRelation> subjPair : subjWords) {
            IndexedWord word = subjPair.first;
            GrammaticalRelation rel = subjPair.second;

            if (rel.getShortName().contains("nsubj")) {
                includesNSubj = true;
            }

            if (firstWord.equals(word)) {
                continue;
            }

            if (word.tag().contains("VB")) {
                includesVB = true;
            }

            if (rel.getShortName().contains("conj")) {
                includesConj = true;
            }

            if (rel.getShortName().contains("prep")) {
                includesPrep = true;
            }

        }

        return includesNSubj && !includesVB && !includesConj && !includesPrep;
    }

    private PairWord newPairWord(PairWord pairWord) {
        PairWord accPairWord = new PairWord();
        if (pairWord.firstWord.beginPosition() > pairWord.secondWord.beginPosition()) {
            accPairWord.first = pairWord.second;
            accPairWord.firstUrl = pairWord.secondUrl;
            accPairWord.firstWord = pairWord.secondWord;

            accPairWord.second = pairWord.first;
            accPairWord.secondUrl = pairWord.firstUrl;
            accPairWord.secondWord = pairWord.firstWord;
        }
        else {
            accPairWord.first = pairWord.first;
            accPairWord.firstUrl = pairWord.firstUrl;
            accPairWord.firstWord = pairWord.firstWord;

            accPairWord.second = pairWord.second;
            accPairWord.secondUrl = pairWord.secondUrl;
            accPairWord.secondWord = pairWord.secondWord;
        }

        return accPairWord;
    }

    private PairWord createSemanticLink(Triple<GrammaticalRelation, IndexedWord, GrammaticalRelation> rootWord, PairWord pairWord) {
        PairWord retWord = new PairWord();
        String firstRel = rootWord.first().getShortName();
        String secondRel = rootWord.third.getShortName();

        boolean direction = true;

        if (firstRel.equals("nsubjpass") || secondRel.equals("nsubj") || firstRel.contains("dobj")) {
            direction = false;
        }

        if (firstRel.equals("nsubj") || secondRel.contains("dobj")) {
            direction = true;
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

    private List<PairWord> extractPairs(Map<String, String> urls) {
        List<PairWord> retList = new ArrayList<PairWord>();

        if (urls.size() < 2) {
            return retList;
        }

        List<Pair<String, String>> accList = urls.entrySet().stream().map(a -> new Pair<String, String>(a.getKey(), a.getValue()))
                .collect(Collectors.toList());

        for (int i = 0; i < accList.size(); i++) {
            for (int j = i + 1; j < accList.size(); j++) {
                PairWord pairWord = new PairWord();
                pairWord.first = accList.get(i).first;
                pairWord.firstUrl = accList.get(i).second;
                pairWord.second = accList.get(j).first;
                pairWord.secondUrl = accList.get(j).second;

                retList.add(pairWord);
            }
        }

        return retList;
    }

}
