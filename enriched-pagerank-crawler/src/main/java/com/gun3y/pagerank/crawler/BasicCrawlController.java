package com.gun3y.pagerank.crawler;

import com.gun3y.pagerank.mongo.MongoManager;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class BasicCrawlController {

    public static void main(String[] args) throws Exception {

        /*
         * crawlStorageFolder is a folder where intermediate crawl data is
         * stored.
         */
        final String crawlStorageFolder = "crawlData4";

        /*
         * numberOfCrawlers shows the number of concurrent threads that should
         * be initiated for crawling.
         */
        final int numberOfCrawlers = 5;

        final CrawlConfig config = new CrawlConfig();

        config.setCrawlStorageFolder(crawlStorageFolder);

        /*
         * Be polite: Make sure that we don't send more than 1 request per
         * second (1000 milliseconds between requests).
         */
        config.setPolitenessDelay(1000);

        /*
         * You can set the maximum crawl depth here. The default value is -1 for
         * unlimited depth
         */
        // config.setMaxDepthOfCrawling(2);

        /*
         * You can set the maximum number of pages to crawl. The default value
         * is -1 for unlimited number of pages
         */
        config.setMaxPagesToFetch(1000);

        /*
         * Do you need to set a proxy? If so, you can use:
         * config.setProxyHost("proxyserver.example.com");
         * config.setProxyPort(8080);
         *
         * If your proxy also needs authentication:
         * config.setProxyUsername(username); config.getProxyPassword(password);
         */

        /*
         * This config parameter can be used to set your crawl to be resumable
         * (meaning that you can resume the crawl from a previously
         * interrupted/crashed crawl). Note: if you enable resuming feature and
         * want to start a fresh crawl, you need to delete the contents of
         * rootFolder manually.
         */
        config.setResumableCrawling(false);

        /*
         * Instantiate the controller for this crawl.
         */
        final PageFetcher pageFetcher = new PageFetcher(config);
        final RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        // robotstxtConfig
        // .setUserAgentName("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/537.36");

        final RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        final CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        controller.addSeed("http://www.ics.uci.edu/");
        controller.addSeed("http://www.ics.uci.edu/~lopes/");
        controller.addSeed("http://www.ics.uci.edu/~welling/");

        MongoManager mongoManager = new MongoManager();
        mongoManager.init();

        controller.setCustomData(mongoManager);
        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(BasicCrawler.class, numberOfCrawlers);

        mongoManager.close();

    }

}
