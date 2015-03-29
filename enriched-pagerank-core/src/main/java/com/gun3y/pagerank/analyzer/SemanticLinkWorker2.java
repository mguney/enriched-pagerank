package com.gun3y.pagerank.analyzer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.common.HtmlToText;
import com.gun3y.pagerank.common.LineItem;
import com.gun3y.pagerank.dao.HtmlTitleDao;
import com.gun3y.pagerank.dao.LinkTupleDao;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.HtmlTitle;
import com.gun3y.pagerank.entity.LinkTuple;
import com.gun3y.pagerank.entity.LinkType;
import com.gun3y.pagerank.utils.HibernateUtils;
import com.gun3y.pagerank.utils.LangUtils;

import edu.emory.clir.clearnlp.component.AbstractComponent;
import edu.emory.clir.clearnlp.component.mode.dep.DEPConfiguration;
import edu.emory.clir.clearnlp.component.utils.NLPUtils;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.util.lang.TLanguage;
import edu.stanford.nlp.util.Pair;

class SemanticLinkWorker2 extends LinkWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemanticLinkWorker2.class);

    private AbstractComponent[] components;

    private AbstractTokenizer tokenizer;

    private HtmlTitleDao htmlTitleDao;

    private static class PairWord {
        DEPNode firstWord;
        DEPNode secondWord;

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

    public SemanticLinkWorker2(String workerId, BlockingQueue<EnhancedHtmlPage> workQueue, AtomicInteger counter,
            LinkTupleDao linkTupleDao, HtmlTitleDao htmlTitleDao, AbstractTokenizer tokenizer, AbstractComponent[] components) {
        super(workerId, workQueue, counter, linkTupleDao);
        this.htmlTitleDao = htmlTitleDao;

        this.components = components;
        this.tokenizer = tokenizer;
    }

    public static void main(String[] args) throws Exception {
        LinkTupleDao linkTupleDao = new LinkTupleDao();
        HtmlTitleDao htmlTitleDao = new HtmlTitleDao();

        final String rootLabel = "root"; // root label for dependency parsing

        AbstractComponent morph = NLPUtils.getMPAnalyzer(TLanguage.ENGLISH);
        AbstractComponent tagger = NLPUtils.getPOSTagger(TLanguage.ENGLISH, "general-en-pos.xz");
        AbstractComponent parser = NLPUtils.getDEPParser(TLanguage.ENGLISH, "general-en-dep.xz", new DEPConfiguration(rootLabel));

        AbstractComponent[] components = new AbstractComponent[] { tagger, morph, parser };
        AbstractTokenizer tokenizer = NLPUtils.getTokenizer(TLanguage.ENGLISH);

        SemanticLinkWorker2 linkWorker2 = new SemanticLinkWorker2(null, null, null, linkTupleDao, htmlTitleDao, tokenizer, components);

        EnhancedHtmlPage enhancedHtmlPage = new EnhancedHtmlPage();

        String html = FileUtils.readFileToString(new File(SemanticLinkAnalyzer.class.getClassLoader().getResource("wiki_sample.html")
                .getPath()));
        String baseUrl = "http://en.wikipedia.org/";

        Document doc = Jsoup.parse(html, baseUrl);
        HtmlToText formatter = new HtmlToText();
        enhancedHtmlPage.setLines(formatter.getLines(doc));

        enhancedHtmlPage.setHtml(html);

        linkWorker2.analyze(enhancedHtmlPage);

        HibernateUtils.shutdown();
    }

    @Override
    public List<LinkTuple> analyze(EnhancedHtmlPage ePage) throws Exception {
        List<LinkTuple> retTuples = new ArrayList<LinkTuple>();

        List<LineItem> lines = ePage.getLines();

        if (lines == null || lines.isEmpty()) {
            return retTuples;
        }

        for (LineItem lineItem : lines) {

            List<LineItem> items = this.extractSentences(lineItem);

            for (LineItem item : items) {
                List<LinkTuple> tuples = this.executeLineItem(item);
                retTuples.addAll(tuples);
            }
        }

        return retTuples;
    }

    private List<LineItem> extractSentences(LineItem lineItem) throws IOException {
        List<LineItem> items = new ArrayList<LineItem>();

        InputStream in = IOUtils.toInputStream(lineItem.getLine(), "UTF-8");
        List<List<String>> sentences = this.tokenizer.segmentize(in);

        for (List<String> sentence : sentences) {
            DEPTree tree = new DEPTree(sentence);

            for (AbstractComponent component : this.components) {
                component.process(tree);
            }

            String sentenceText = LangUtils.joinList(sentence);

            Map<String, String> urls = new HashMap<String, String>();

            List<Pair<String, String>> implicitUrls = this.findImplictUrls(tree);

            implicitUrls.stream().forEach(a -> {
                if (sentence.contains(a.first)) {
                    urls.put(a.first, a.second);
                }
            });
            lineItem.getUrls().forEach((key, value) -> {
                if (sentence.contains(key)) {
                    urls.put(key, value);
                }
            });

            if (urls.size() > 1) {
                items.add(new LineItem(sentenceText, urls, tree));
            }

        }
        return items;
    }

    private List<Pair<String, String>> findImplictUrls(DEPTree tree) {
        List<Pair<String, String>> retList = new ArrayList<Pair<String, String>>();

        if (tree == null || tree.size() < 2) {
            return retList;
        }

        List<String> lemmaList = new ArrayList<String>();
        tree.forEach(a -> lemmaList.add(a.getLemma().toLowerCase(Locale.ENGLISH)));

        List<String> textList = new ArrayList<String>();
        tree.forEach(a -> textList.add(a.getWordForm()));

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

        DEPTree tree = lineItem.getTree();

        if (pairs.isEmpty() || tree == null) {
            return retTuples;
        }

        LOGGER.debug(this.print(tree.getFirstRoot()));

        Map<String, DEPNode> indexedWordMap = new HashMap<String, DEPNode>();

        for (PairWord pairWord : pairs) {

            if (indexedWordMap.containsKey(pairWord.first)) {
                pairWord.firstWord = indexedWordMap.get(pairWord.first);
            }
            else {
                pairWord.firstWord = this.findCommonAncestor(tree, pairWord.first);
                LOGGER.debug(pairWord.first + " -> " + this.printDepNode(pairWord.firstWord));
                indexedWordMap.put(pairWord.first, pairWord.firstWord);
            }

            if (indexedWordMap.containsKey(pairWord.second)) {
                pairWord.secondWord = indexedWordMap.get(pairWord.second);
            }
            else {
                pairWord.secondWord = this.findCommonAncestor(tree, pairWord.second);
                LOGGER.debug(pairWord.second + " -> " + this.printDepNode(pairWord.secondWord));
                indexedWordMap.put(pairWord.second, pairWord.secondWord);
            }

            PairWord triple = this.extractSemanticTriple(tree, pairWord);
            if (triple != null) {
                LinkTuple tuple = new LinkTuple(triple.firstUrl, LinkType.SemanticLink, triple.secondUrl, triple.predicate);
                LOGGER.debug("SemanticLink: " + triple.first + " --[" + triple.predicate + "]--> " + triple.second);
                LOGGER.info(tuple.toString());
                retTuples.add(tuple);
            }

        }

        return retTuples;
    }

    private List<DEPNode> extractTokens(String text) {
        List<String> tokens = this.tokenizer.tokenize(text);
        DEPTree tree = new DEPTree(tokens);

        for (AbstractComponent component : this.components) {
            component.process(tree);
        }

        List<DEPNode> retList = new ArrayList<DEPNode>();

        tree.forEach(a -> retList.add(a));

        return retList;
    }

    private DEPNode findCommonAncestor(DEPTree tree, String text) {

        List<DEPNode> tokenWords = this.extractTokens(text);
        List<String> tokens = tokenWords.stream().map(a -> a.getWordForm()).collect(Collectors.toList());
        LOGGER.debug("Tokens: " + text + " --> " + tokens);

        if (tokens.isEmpty()) {
            return null;
        }

        List<DEPNode> vertexListSorted = new ArrayList<DEPNode>();
        tree.forEach(a -> vertexListSorted.add(a));

        List<String> vertexList = vertexListSorted.stream().map(a -> a.getWordForm()).collect(Collectors.toList());

        int indexOfSubList = Collections.indexOfSubList(vertexList, tokens);

        if (indexOfSubList < 0) {
            return null;
        }

        List<DEPNode> subList = vertexListSorted.subList(indexOfSubList, indexOfSubList + tokens.size());

        return subList.stream().map(a -> {
            Set<DEPNode> pathToRoot = a.getAncestorSet();
            if (pathToRoot == null) {
                return new Pair<DEPNode, Integer>(a, Integer.MAX_VALUE);
            }
            else {
                return new Pair<DEPNode, Integer>(a, pathToRoot.size());
            }
        }).min((a, b) -> Integer.compare(a.second, b.second)).get().first;

    }

    private PairWord extractSemanticTriple(DEPTree tree, PairWord pairWord) {
        if (pairWord.firstWord == null || pairWord.secondWord == null) {
            return null;
        }

        PairWord accPairWord = this.newPairWord(pairWord);

        DEPNode rootNode = accPairWord.firstWord.getLowestCommonAncestor(accPairWord.secondWord);

        if (rootNode == accPairWord.firstWord || rootNode == accPairWord.secondWord) {
            LOGGER.debug("From " + this.printDepNode(accPairWord.firstWord) + " to " + this.printDepNode(accPairWord.secondWord)
                    + "  ----> NO COMMON ROOT !");
            return null;
        }
        else if (!rootNode.getPOSTag().contains("VB")) {
            LOGGER.debug("From " + this.printDepNode(accPairWord.firstWord) + " to " + this.printDepNode(accPairWord.secondWord)
                    + "  ----> " + this.printDepNode(rootNode) + "NO COMMON ROOT(VB) !");
            return null;
        }

        List<DEPNode> subjects = this.getParentNodes(accPairWord.firstWord, rootNode);

        List<DEPNode> objects = this.getParentNodes(accPairWord.secondWord, rootNode);

        if (subjects.isEmpty() || objects.isEmpty()) {
            LOGGER.debug("From " + this.printDepNode(accPairWord.firstWord) + " to " + this.printDepNode(accPairWord.secondWord)
                    + "  ----> NO EDGE !");
            return null;
        }
        LOGGER.debug("From " + this.printDepNode(accPairWord.firstWord) + " to " + this.printDepNode(accPairWord.secondWord)
                + "  edges ----> " + this.printDepNode(subjects) + "[" + this.printDepNode(rootNode) + "]" + this.printDepNode(objects));

        boolean allCheck = true;
        if (subjects.get(subjects.size() - 1).getLabel().contains("dobj")) {
            allCheck &= this.checkObjs(subjects, accPairWord.firstWord);
            allCheck &= this.checkSubjs(objects, accPairWord.secondWord);
        }
        else {
            allCheck &= this.checkSubjs(subjects, accPairWord.firstWord);
            allCheck &= this.checkObjs(objects, accPairWord.secondWord);
        }

        if (!allCheck) {
            LOGGER.debug("From " + this.printDepNode(accPairWord.firstWord) + " to " + this.printDepNode(accPairWord.secondWord)
                    + "  ----> NO COMMON ROOT(Checked) !");
            return null;
        }

        return this.createSemanticLink(rootNode, subjects.get(subjects.size() - 1), objects.get(objects.size() - 1), accPairWord);
    }

    private boolean checkObjs(List<DEPNode> objWords, DEPNode node) {
        boolean includesNObj = false;
        boolean includesVB = false;
        boolean includesConj = false;
        boolean includesPrep = false;

        // conj'lara ve prep'lere dikkat et

        for (DEPNode accNode : objWords) {
            String label = accNode.getLabel();
            String tag = accNode.getPOSTag();

            if (label.contains("dobj")) {
                includesNObj = true;
            }

            if (node.equals(accNode)) {
                continue;
            }

            if (tag.contains("VB")) {
                includesVB = true;
            }

            if (label.contains("conj")) {
                includesConj = true;
            }

            if (label.contains("prep")) {
                includesPrep = true;
            }

        }

        return includesNObj && !includesVB && !includesConj && !includesPrep;
    }

    private boolean checkSubjs(List<DEPNode> subjWords, DEPNode node) {

        boolean includesNSubj = false;
        boolean includesVB = false;
        boolean includesConj = false;
        boolean includesPrep = false;

        // conj'lara ve prep'lere dikkat et

        for (DEPNode accNode : subjWords) {
            String label = accNode.getLabel();
            String tag = accNode.getPOSTag();

            if (label.contains("nsubj")) {
                includesNSubj = true;
            }

            if (node.equals(accNode)) {
                continue;
            }

            if (tag.contains("VB")) {
                includesVB = true;
            }

            if (label.contains("conj")) {
                includesConj = true;
            }

            if (label.contains("prep")) {
                includesPrep = true;
            }

        }

        return includesNSubj && !includesVB && !includesConj && !includesPrep;
    }

    private PairWord newPairWord(PairWord pairWord) {
        PairWord accPairWord = new PairWord();
        if (pairWord.firstWord.getID() > pairWord.secondWord.getID()) {
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

    private PairWord createSemanticLink(DEPNode rootNode, DEPNode subjectNode, DEPNode objectNode, PairWord pairWord) {
        PairWord retWord = new PairWord();
        String firstRel = subjectNode.getLabel();
        String secondRel = objectNode.getLabel();

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
        retWord.predicate = rootNode.getLemma();
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

    public String print(DEPNode node) {
        return this.print(node, "", true);
    }

    private String print(DEPNode node, String prefix, boolean isTail) {
        StringBuilder builder = new StringBuilder();
        builder.append(prefix + (isTail ? "└── " : "├── ") + this.printDepNode(node)).append("\n");
        for (int i = 0; i < node.getDependentSize() - 1; i++) {
            builder.append(this.print(node.getDependent(i), prefix + (isTail ? "    " : "│   "), false));
        }
        if (node.getDependentSize() > 0) {
            builder.append(this.print(node.getDependent(node.getDependentSize() - 1), prefix + (isTail ? "    " : "│   "), true));
        }
        return builder.toString();
    }

    private List<DEPNode> getParentNodes(DEPNode node, DEPNode common) {
        List<DEPNode> nodes = new ArrayList<DEPNode>();

        if (common == node) {
            return nodes;
        }

        nodes.add(node);
        while (node.getHead() != common) {
            nodes.add(node.getHead());
            node = node.getHead();
        }

        return nodes;
    }

    private String printDepNode(DEPNode node) {
        if (node == null) {
            return "[NULL]";
        }
        return node.getWordForm() + "(" + node.getID() + ":" + node.getPOSTag() + ")";
    }

    private String printDepNode(List<DEPNode> nodes) {
        StringBuilder builder = new StringBuilder();
        if (nodes != null) {
            for (DEPNode node : nodes) {
                builder.append(node.getWordForm()).append("[").append(node.getPOSTag()).append(":").append(node.getLabel()).append("]")
                .append(" ");
            }
        }
        return builder.toString();
    }

}
