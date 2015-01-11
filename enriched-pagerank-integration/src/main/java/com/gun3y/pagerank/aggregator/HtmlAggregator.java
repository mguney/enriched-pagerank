package com.gun3y.pagerank.aggregator;

import java.util.HashMap;
import java.util.Iterator;
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

import com.gun3y.pagerank.dao.EnhancedHtmlPageDao;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.html.HtmlPage;
import com.gun3y.pagerank.store.MongoHtmlPageDao;
import com.gun3y.pagerank.utils.BeanUtils;
import com.gun3y.pagerank.utils.HtmlUtils;
import com.gun3y.pagerank.utils.LangUtils;

public class HtmlAggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlAggregator.class);

    private static final String LANG_EN = "en";

    private static final String HTML_HREF = "href";

    private MongoHtmlPageDao mongoHtmlPageDao;

    private EnhancedHtmlPageDao enhancedHtmlPageDao;

    public HtmlAggregator(MongoHtmlPageDao mongoHtmlPageDao, EnhancedHtmlPageDao enhancedHtmlPageDao) {
        this.mongoHtmlPageDao = mongoHtmlPageDao;
        this.enhancedHtmlPageDao = enhancedHtmlPageDao;
    }

    public void transformHtmlPages() {
        LOGGER.info("Transforming HtmlPages has started");

        this.displayHtmlPageStats();

        this.transformEnhancedHtmlPages();

        LOGGER.info("Transforming HtmlPages has ended");
    }

    private void displayHtmlPageStats() {
        StopWatch timer = new StopWatch();
        timer.start();
        Iterator<HtmlPage> htmlPageIterator = this.mongoHtmlPageDao.getHtmlPageIterator();
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
        int htmlPages = this.mongoHtmlPageDao.getHtmlPageCount();
        LOGGER.info("HtmlPage({}) Stats are executed in {} ms", htmlPages, timer.getTime());

    }

    private void transformEnhancedHtmlPages() {
        StopWatch timer = new StopWatch();
        timer.start();
        Iterator<HtmlPage> htmlPageIterator = this.mongoHtmlPageDao.getHtmlPageIterator();
        while (htmlPageIterator.hasNext()) {
            HtmlPage htmlPage = htmlPageIterator.next();
            if (!LANG_EN.equals(htmlPage.getLanguage())) {
                continue;
            }

            EnhancedHtmlPage enhancedHtmlPage = BeanUtils.newEnhancedHtmlPage(htmlPage);
            this.enhancedHtmlPageDao.addHtmlPage(enhancedHtmlPage);

        }
        long nodeCount = this.enhancedHtmlPageDao.getHtmlPageCount();
        LOGGER.info("EnhancedHtmlPages({}) haven added into DB in {} ms", nodeCount, timer.getTime());

        timer.reset();
        timer.start();
        Iterator<EnhancedHtmlPage> enhancedHtmlPageIterator = this.enhancedHtmlPageDao.getHtmlPageIterator();
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

            if (!HtmlUtils.checkUrl(absUrl)) {
                continue;
            }

            EnhancedHtmlPage nextEnhancedHtmlPage = this.enhancedHtmlPageDao.getHtmlPageByUrl(absUrl);
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
                        this.enhancedHtmlPageDao.updateHtmlPage(nextEnhancedHtmlPage.getUrl(), nextEnhancedHtmlPage);
                    }
                }
            }
        }
    }
}
