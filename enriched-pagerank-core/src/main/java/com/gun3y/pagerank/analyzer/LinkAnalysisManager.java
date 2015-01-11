package com.gun3y.pagerank.analyzer;

import java.util.HashMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.entity.html.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.html.HtmlPage;
import com.gun3y.pagerank.store.LinkStorage;
import com.gun3y.pagerank.store.LinkTuple;
import com.gun3y.pagerank.store.MongoManager;
import com.gun3y.pagerank.utils.BeanUtils;
import com.gun3y.pagerank.utils.LangUtils;

public class LinkAnalysisManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkAnalysisManager.class);

    private static final String LANG_EN = "en";

    private static final String HTML_HREF = "href";

    private MongoManager mongoManager;

    private LinkStorage linkStorage = new LinkStorage();

    private LinkAnalyzer[] analyers = new LinkAnalyzer[] { new ExplicitLinkAnalyzer(), new ImplicitLinkAnalyzer(),
            new SemanticLinkAnalyzer() };

    public LinkAnalysisManager(MongoManager mongoManager) {
        super();
        this.mongoManager = mongoManager;
    }

    public LinkAnalysisManager() {
        super();
        this.mongoManager = new MongoManager();
        this.mongoManager.init();
    }

    public LinkStorage getLinkStorage() {
        return this.linkStorage;
    }



    public void analyze() {
        LOGGER.info("Link Analysis has started");

        this.displayHtmlPageStats();

        this.transformEnhancedHtmlPages();

        Iterator<EnhancedHtmlPage> ePageIterator = this.mongoManager.getEnhancedHtmlPageIterator();
        while (ePageIterator.hasNext()) {
            EnhancedHtmlPage ePage = ePageIterator.next();

            Iterator<EnhancedHtmlPage> tempPageIterator = this.mongoManager.getEnhancedHtmlPageIterator();
            while (tempPageIterator.hasNext()) {
                EnhancedHtmlPage tempPage = tempPageIterator.next();

                if (ePage.getPageId() == tempPage.getPageId()) {
                    continue;
                }

                for (LinkAnalyzer linkAnalyzer : this.analyers) {
                    List<LinkTuple> tuples = linkAnalyzer.analyze(ePage, tempPage);
                    this.linkStorage.addLink(tuples);
                }
            }
        }



        LOGGER.info("Link Analysis has ended");
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

}
