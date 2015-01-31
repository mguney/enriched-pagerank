package com.gun3y.pagerank.crawler;

import com.gun3y.pagerank.store.MongoHtmlPageDao;

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
        final String crawlStorageFolder = "new_crawldata";

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

        config.setFollowRedirects(true);
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
        controller.addSeed("http://en.wikipedia.org/wiki/The_Matrix");
        controller.addSeed("http://en.wikipedia.org/wiki/Batman:_Arkham_City");
        controller.addSeed("http://en.wikipedia.org/wiki/The_Lord_of_the_Rings");
        controller.addSeed("http://en.wikipedia.org/wiki/Avatar_(2009_film)");
        controller.addSeed("http://en.wikipedia.org/wiki/Star_Wars");
        controller.addSeed("http://en.wikipedia.org/wiki/Battlestar_Galactica_(2004_TV_series)");
        controller.addSeed("http://en.wikipedia.org/wiki/Lost_(TV_series)");
        controller.addSeed("http://en.wikipedia.org/wiki/Prison_Break");
        controller.addSeed("http://en.wikipedia.org/wiki/Supernatural_(U.S._TV_series)");
        controller.addSeed("http://en.wikipedia.org/wiki/Person_of_Interest_(TV_series)");
        controller.addSeed("http://en.wikipedia.org/wiki/Arrow_(TV_series)");
        controller.addSeed("http://en.wikipedia.org/wiki/Turkey");
        controller.addSeed("http://en.wikipedia.org/wiki/Ottoman_Empire");
        controller.addSeed("http://en.wikipedia.org/wiki/England");
        controller.addSeed("http://en.wikipedia.org/wiki/France");
        controller.addSeed("http://en.wikipedia.org/wiki/United_States");
        controller.addSeed("http://en.wikipedia.org/wiki/Space");
        controller.addSeed("http://en.wikipedia.org/wiki/Mathematics");
        controller.addSeed("http://en.wikipedia.org/wiki/Computer");
        controller.addSeed("http://en.wikipedia.org/wiki/Software");
        controller.addSeed("http://en.wikipedia.org/wiki/Manchester");

        MongoHtmlPageDao mongoManager = new MongoHtmlPageDao("localhost", "PRDB");
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
