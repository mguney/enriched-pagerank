package com.gun3y.pagerank.crawler;

import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.crawler.mapping.BeanMapper;
import com.gun3y.pagerank.entity.html.HtmlPage;
import com.gun3y.pagerank.mongo.MongoManager;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class BasicCrawler extends WebCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicCrawler.class);

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4"
            + "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    private MongoManager mongoManager;

    @Override
    public void onStart() {
        Object customData = myController.getCustomData();

        if (customData != null && customData instanceof MongoManager) {
            mongoManager = (MongoManager) customData;
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
        LOGGER.info("Domain: '" + domain + "'");
        LOGGER.info("Sub-domain: '" + subDomain + "'");
        LOGGER.info("Path: '" + path + "'");
        LOGGER.info("Parent page: " + parentUrl);
        LOGGER.info("Anchor text: " + anchor);

        if (page.getParseData() instanceof HtmlParseData) {
            final HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            final String text = htmlParseData.getText();
            final String html = htmlParseData.getHtml();
            final Set<WebURL> links = htmlParseData.getOutgoingUrls();

            LOGGER.info("Text length: " + text.length());
            LOGGER.info("Html length: " + html.length());
            LOGGER.info("Number of outgoing links: " + links.size());

            HtmlPage htmlPage = BeanMapper.map(page);
            mongoManager.add(htmlPage);
        }

        final Header[] responseHeaders = page.getFetchResponseHeaders();

        if (responseHeaders != null) {
            LOGGER.info("Response headers:");
            for (final Header header : responseHeaders) {
                LOGGER.info("\t" + header.getName() + ": " + header.getValue());
            }
        }

        LOGGER.info("=============");
    }
}
