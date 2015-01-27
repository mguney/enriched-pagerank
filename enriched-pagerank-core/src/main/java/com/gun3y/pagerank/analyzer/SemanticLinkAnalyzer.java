package com.gun3y.pagerank.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.common.HtmlToText;
import com.gun3y.pagerank.common.LineItem;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.HtmlTitle;
import com.gun3y.pagerank.entity.LinkTuple;
import com.gun3y.pagerank.entity.LinkType;

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
import edu.stanford.nlp.util.Triple;

public class SemanticLinkAnalyzer implements LinkAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemanticLinkAnalyzer.class);

    public static final String IN_SAME_SENTENCE = "__inSameSentence__";

    private StanfordCoreNLP pipeline;

    private StanfordCoreNLP tokenizer;

    public SemanticLinkAnalyzer() {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
        this.pipeline = new StanfordCoreNLP(props);

        Properties tokenProps = new Properties();
        tokenProps.put("annotators", "tokenize, ssplit, pos");
        this.tokenizer = new StanfordCoreNLP(tokenProps);
    }

    public static void main(String[] args) throws IOException {
        SemanticLinkAnalyzer analyzer = new SemanticLinkAnalyzer();

        String html = FileUtils.readFileToString(new File(SemanticLinkAnalyzer.class.getClassLoader().getResource("wiki_sample.html")
                .getPath()));
        String baseUrl = "http://en.wikipedia.org/";

        String text = "\"#ShareIloilo: Miag-ao Church\" \"#ShareIloilo: Camia Balay na Bato\" \"#ShareBaguio Twitter Conversation: Why travel to Baguio?\" \"Revilla bail petition, Pope Francis PH mass, Obama daughters | The wRap\" \"More Stories\" \"The wRap\"";
        Map<String, String> map = new HashMap<String, String>();
        map.put("#ShareIloilo: Miag-ao Church", "link1");
        map.put("#ShareIloilo: Camia Balay na Bato", "link2");
        map.put("#ShareBaguio Twitter Conversation: Why travel to Baguio?", "link3");
        map.put("Revilla bail petition, Pope Francis PH mass, Obama daughters | The wRap", "link4");
        map.put("More Stories", "link5");
        map.put("The wRap", "link6");

        // String text = "\"Ethnic groups\" ( \"2011\" )";
        // Map<String, String> map = new HashMap<String, String>();
        // map.put("Ethnic groups", "ad");
        // map.put("2011", "ad2");

        Document doc = Jsoup.parse(html, baseUrl);
        HtmlToText formatter = new HtmlToText();
        List<LineItem> lines = formatter.getLines(doc);

        LineItem lineItem = new LineItem(text, map);
        // System.out.println(text);
        List<LinkTuple> analyze = analyzer.analyze(Arrays.asList(lineItem));
        // List<PairWord> analyze = analyzer.analyze(html, baseUrl);
        for (LinkTuple linkTuple : analyze) {
            System.out.println(linkTuple);
        }
    }

    @Override
    public List<LinkTuple> analyze(EnhancedHtmlPage ePage) {
        List<LineItem> lines = ePage.getLines();
        return this.analyze(lines);
    }

    @Override
    public List<LinkTuple> analyze(EnhancedHtmlPage ePage, HtmlTitle htmlTitle) {
        return Collections.emptyList();
    }

    private List<LinkTuple> analyze(List<LineItem> lines) {

        List<LinkTuple> tuples = new ArrayList<LinkTuple>();

        if (lines.isEmpty()) {
            return tuples;
        }

        for (LineItem lineItem : lines) {

            if (lineItem.getUrls().size() < 2) {
                continue;
            }
            LOGGER.debug(lineItem.getLine());
            Annotation document = this.pipeline.process(lineItem.getLine());

            List<CoreMap> sentences = document.get(SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {

                String sentenceText = sentence.get(TextAnnotation.class);
                LOGGER.debug(sentenceText);

                List<PairWord> pairs = this.extractPairs(sentenceText, lineItem.getUrls());

                if (pairs.isEmpty()) {
                    continue;
                }

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
                        indexedWordMap.put(pairWord.first, pairWord.firstWord);
                    }

                    if (indexedWordMap.containsKey(pairWord.second)) {
                        pairWord.secondWord = indexedWordMap.get(pairWord.second);
                    }
                    else {
                        pairWord.secondWord = this.findCommonAncestor(semanticGraph, pairWord.second);
                        indexedWordMap.put(pairWord.second, pairWord.secondWord);
                    }

                    PairWord triple = this.extractSemanticTriple(semanticGraph, pairWord);
                    if (triple != null) {
                        LinkTuple tuple = new LinkTuple(triple.firstUrl, LinkType.SemanticLink, triple.secondUrl, triple.predicate);
                        LOGGER.debug(tuple.toString());
                        tuples.add(tuple);
                    }

                }

            }
        }
        return tuples;
    }

    private class PairWord {
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

    private PairWord extractSemanticTriple(SemanticGraph semanticGraph, PairWord pairWord) {
        if (pairWord.firstWord == null || pairWord.secondWord == null) {
            return null;
        }
        List<IndexedWord> firstPath = semanticGraph.getPathToRoot(pairWord.firstWord);
        List<IndexedWord> secondPath = semanticGraph.getPathToRoot(pairWord.secondWord);
        List<SemanticGraphEdge> edges = null;
        boolean reverse = false;
        if (firstPath == null || secondPath == null) {
            LOGGER.error("IndexedWord ({} path is null", pairWord.toString());
            return null;
        }

        if (firstPath.size() > secondPath.size()) {
            reverse = true;
            edges = semanticGraph.getShortestUndirectedPathEdges(pairWord.secondWord, pairWord.firstWord);
        }
        else {
            edges = semanticGraph.getShortestUndirectedPathEdges(pairWord.firstWord, pairWord.secondWord);
        }

        List<Triple<GrammaticalRelation, IndexedWord, GrammaticalRelation>> verbs = new ArrayList<Triple<GrammaticalRelation, IndexedWord, GrammaticalRelation>>();

        Triple<GrammaticalRelation, IndexedWord, GrammaticalRelation> rootWord = null;

        for (int i = 0; i < edges.size(); i++) {
            SemanticGraphEdge firstEdge = edges.get(i);

            if (edges.size() > (i + 1)) {
                SemanticGraphEdge secondEdge = edges.get(i + 1);

                // Find verbs
                if (firstEdge.getTarget().tag().contains("VB")) {
                    verbs.add(new Triple<GrammaticalRelation, IndexedWord, GrammaticalRelation>(firstEdge.getRelation(), firstEdge
                            .getTarget(), secondEdge.getRelation()));
                }

                // Find root
                if (firstEdge.getSource().equals(secondEdge.getSource())) {
                    rootWord = new Triple<GrammaticalRelation, IndexedWord, GrammaticalRelation>(firstEdge.getRelation(),
                            firstEdge.getSource(), secondEdge.getRelation());
                }
            }
        }

        if (rootWord == null && !verbs.isEmpty()) {
            rootWord = verbs.get(0);
        }

        if (rootWord != null) {
            return this.createSemanticLink(rootWord, pairWord, reverse);
        }

        if (!edges.isEmpty()) {

            PairWord pWord = new PairWord();
            pWord.first = pairWord.first;
            pWord.firstUrl = pairWord.firstUrl;
            pWord.second = pairWord.second;
            pWord.secondUrl = pairWord.secondUrl;
            pWord.predicate = IN_SAME_SENTENCE;
            return pWord;
        }

        // TODO: log lazım buraya

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

    private IndexedWord findCommonAncestor(SemanticGraph semanticGraph, String text) {
        IndexedWord ancestorWord = null;

        List<CoreLabel> tokens = this.extractTokens(text);

        if (tokens.isEmpty()) {
            return ancestorWord;
        }

        int i = 0;
        for (; i < tokens.size(); i++) {
            CoreLabel coreLabel = tokens.get(i);
            String tag = coreLabel.tag();
            if (StringUtils.isBlank(tag) || tag.length() < 2 || StringUtil.in(tag, "-LRB-", "-RRB-", "-LCB-", "-RCB-", "-LSB-", "-RSB-")) {
                continue;
            }

            try {
                ancestorWord = semanticGraph.getNodeByWordPattern(coreLabel.originalText());
            }
            catch (Exception e) {
                continue;
            }

            if (ancestorWord != null) {
                i++;
                break;
            }
        }

        for (; i < tokens.size(); i++) {
            CoreLabel coreLabel = tokens.get(i);
            String tag = coreLabel.tag();
            if (StringUtils.isBlank(tag) || tag.length() < 2 || StringUtil.in(tag, "-LRB-", "-RRB-", "-LCB-", "-RCB-", "-LSB-", "-RSB-")) {
                continue;
            }
            IndexedWord tempWord = null;
            try {
                tempWord = semanticGraph.getNodeByWordPattern(coreLabel.originalText());
            }
            catch (PatternSyntaxException e) {
                continue;
            }

            if (tempWord == null) {
                continue;
            }

            List<IndexedWord> pathToRoot = semanticGraph.getPathToRoot(tempWord);
            if (pathToRoot != null && !pathToRoot.contains(ancestorWord)) {
                ancestorWord = tempWord;
            }
        }

        return ancestorWord;
    }

    private List<CoreLabel> extractTokens(String text) {
        Annotation process = this.tokenizer.process(text);
        List<CoreLabel> list = process.get(TokensAnnotation.class);
        return list;
    }

    private List<PairWord> extractPairs(String sentenceText, Map<String, String> urls) {
        List<PairWord> retList = new ArrayList<PairWord>();

        if (StringUtils.isBlank(sentenceText) || urls.isEmpty()) {
            return retList;
        }

        List<String> accList = new ArrayList<String>();
        for (Entry<String, String> entry : urls.entrySet()) {
            if (sentenceText.contains("\"" + entry.getKey() + "\"")) {
                accList.add(entry.getKey());
            }
        }

        if (accList.size() < 2) {
            return retList;
        }

        for (int i = 0; i < accList.size(); i++) {
            for (int j = i + 1; j < accList.size(); j++) {
                PairWord pairWord = new PairWord();
                pairWord.first = accList.get(i);
                pairWord.firstUrl = urls.get(pairWord.first);
                pairWord.second = accList.get(j);
                pairWord.secondUrl = urls.get(pairWord.second);

                retList.add(pairWord);
            }
        }

        return retList;
    }

}
