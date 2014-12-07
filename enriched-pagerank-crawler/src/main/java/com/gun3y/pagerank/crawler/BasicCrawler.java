package com.gun3y.pagerank.crawler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.crawler.mapping.BeanMapper;
import com.gun3y.pagerank.entity.html.HtmlData;
import com.gun3y.pagerank.entity.html.HtmlPage;
import com.gun3y.pagerank.mongo.MongoManager;
import com.gun3y.pagerank.utils.HtmlUtils;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class BasicCrawler extends WebCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicCrawler.class);

    private final static Pattern FILTERS = Pattern.compile(HtmlUtils.REGEX_HTML_PAGES);

    private MongoManager mongoManager;

    @Override
    public void onStart() {
        Object customData = this.myController.getCustomData();

        if (customData != null && customData instanceof MongoManager) {
            this.mongoManager = (MongoManager) customData;
        }
    }

    /**
     * You should implement this function to specify whether the given url
     * should be crawled or not (based on your crawling logic).
     */
    @Override
    public boolean shouldVisit(Page page, WebURL url) {
        final String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches();
    }

    /**
     * This function is called when a page is fetched and ready to be processed
     * by your program.
     */
    @Override
    public void visit(Page page) {
        final int docid = page.getWebURL().getDocid();
        final String url = page.getWebURL().getURL();
        final String domain = page.getWebURL().getDomain();
        final String path = page.getWebURL().getPath();
        final String subDomain = page.getWebURL().getSubDomain();
        final String parentUrl = page.getWebURL().getParentUrl();
        final String anchor = page.getWebURL().getAnchor();

        LOGGER.info("Docid: " + docid);
        LOGGER.info("URL: " + url);
        LOGGER.debug("Domain: '" + domain + "'");
        LOGGER.debug("Sub-domain: '" + subDomain + "'");
        LOGGER.debug("Path: '" + path + "'");
        LOGGER.debug("Parent page: " + parentUrl);
        LOGGER.debug("Anchor text: " + anchor);

        if (page.getParseData() instanceof HtmlParseData) {
            final HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            final String text = htmlParseData.getText();
            final String title = htmlParseData.getTitle();
            final String html = htmlParseData.getHtml();
            final Set<WebURL> links = htmlParseData.getOutgoingUrls();

            LOGGER.debug("Title: " + title);
            LOGGER.debug("Text length: " + text.length());
            LOGGER.debug("Html length: " + html.length());
            LOGGER.debug("Number of outgoing links: " + links.size());

            HtmlPage htmlPage = BeanMapper.map(page);
            HtmlData htmlData = htmlPage.getHtmlData();

            // TODO: Mongo için "." yı "_" ile değiştir
            Map<String, String> metaTags = htmlData.getMetaTags();
            if (metaTags != null && !metaTags.isEmpty()) {
                Set<String> keySet = new HashSet<String>(metaTags.keySet());
                for (String key : keySet) {
                    if (key.contains(".")) {
                        LOGGER.debug("MetaTag {} is replaced", key);
                        metaTags.put(key.replace(".", "_"), metaTags.get(key));
                        metaTags.remove(key);
                    }
                }
            }

            if (this.mongoManager != null) {
                this.mongoManager.addHtmlPage(htmlPage);
            }
        }

        final Header[] responseHeaders = page.getFetchResponseHeaders();

        if (responseHeaders != null) {
            LOGGER.debug("Response headers:");
            for (final Header header : responseHeaders) {
                LOGGER.debug("\t" + header.getName() + ": " + header.getValue());
            }
        }

        LOGGER.info("=============");
    }

}
