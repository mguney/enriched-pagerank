package com.gun3y.pagerank.crawler;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.store.HtmlPageDao;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class BasicCrawlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicCrawlController.class);

    public static void startCrawler(File seedFile, HtmlPageDao htmlPageDao) throws Exception {
        InputStream resourceAsStream = BasicCrawlController.class.getClassLoader().getResourceAsStream("crawler.properties");

        Properties crawlerProp = new Properties();
        crawlerProp.load(resourceAsStream);

        List<String> seedList = FileUtils.readLines(seedFile);

        LOGGER.debug("Crawler Config:");
        for (Entry<Object, Object> entry : crawlerProp.entrySet()) {
            LOGGER.debug(entry.getKey() + ": " + entry.getValue());
        }

        LOGGER.debug("Seeds:");
        for (String seed : seedList) {
            LOGGER.debug(seed);
        }

        final CrawlConfig config = new CrawlConfig();

        config.setConnectionTimeout(Integer.parseInt(crawlerProp.getProperty("crawler.connection.timeout")));
        config.setCrawlStorageFolder(crawlerProp.getProperty("crawler.storage"));
        config.setFollowRedirects(Boolean.parseBoolean(crawlerProp.getProperty("crawler.followRedirects")));
        config.setIncludeBinaryContentInCrawling(Boolean.parseBoolean(crawlerProp.getProperty("crawler.include.binary")));
        config.setIncludeHttpsPages(Boolean.parseBoolean(crawlerProp.getProperty("crawler.include.https")));
        config.setMaxConnectionsPerHost(Integer.parseInt(crawlerProp.getProperty("crawler.max.connection.host")));
        config.setMaxDepthOfCrawling(Integer.parseInt(crawlerProp.getProperty("crawler.max.depth")));
        config.setMaxDownloadSize(Integer.parseInt(crawlerProp.getProperty("crawler.max.downloadSize")));
        config.setMaxOutgoingLinksToFollow(Integer.parseInt(crawlerProp.getProperty("crawler.max.outgoingLinks")));
        config.setMaxPagesToFetch(Integer.parseInt(crawlerProp.getProperty("crawler.max.pages")));
        config.setMaxTotalConnections(Integer.parseInt(crawlerProp.getProperty("crawler.max.connection.total")));
        config.setPolitenessDelay(Integer.parseInt(crawlerProp.getProperty("crawler.connection.politeness")));
        config.setResumableCrawling(Boolean.parseBoolean(crawlerProp.getProperty("crawler.resumable")));
        config.setSocketTimeout(Integer.parseInt(crawlerProp.getProperty("crawler.connection.socket")));
        config.setUserAgentString(crawlerProp.getProperty("crawler.connection.userAgent"));

        final PageFetcher pageFetcher = new PageFetcher(config);
        final RobotstxtConfig robotstxtConfig = new RobotstxtConfig();

        final RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        final CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        for (String seed : seedList) {
            if (StringUtils.isNotBlank(seed)) {
                controller.addSeed(seed);
            }
        }

        controller.setCustomData(htmlPageDao);
        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(BasicCrawler.class, Integer.parseInt(crawlerProp.getProperty("crawler.thread")));

    }
}
