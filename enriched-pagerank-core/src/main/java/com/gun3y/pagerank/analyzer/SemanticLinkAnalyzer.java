package com.gun3y.pagerank.analyzer;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.dao.EnhancedHtmlPageDao;
import com.gun3y.pagerank.dao.HtmlTitleDao;
import com.gun3y.pagerank.dao.LinkTupleDao;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.LinkType;

public class SemanticLinkAnalyzer extends AbstractLinkAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemanticLinkAnalyzer.class);

    public SemanticLinkAnalyzer(int numWorkers, HtmlTitleDao htmlTitleDao, EnhancedHtmlPageDao htmlPageDao, LinkTupleDao linkTupleDao) {
        super(numWorkers, htmlTitleDao, htmlPageDao, linkTupleDao);
    }

    @Override
    protected LinkWorker newLinkWorker(int id, BlockingQueue<EnhancedHtmlPage> workQueue, AtomicInteger counter, LinkTupleDao linkTupleDao) {
        return new SemanticLinkWorker("SemLinkWorker#" + id, workQueue, counter, linkTupleDao, this.htmlTitleDao);
    }

    @Override
    public void analyze() {
        LOGGER.info("Analyzing semantic links...");
        StopWatch pageTimer = new StopWatch();
        pageTimer.start();

        Iterator<EnhancedHtmlPage> ePageIterator = this.htmlPageDao.getHtmlPageIterator();

        int retCode = 0;
        while (ePageIterator.hasNext()) {
            if (Thread.currentThread().isInterrupted() || this.isInterupted()) {
                LOGGER.error("An error has been occured");
                return;
            }
            EnhancedHtmlPage ePage = ePageIterator.next();
            retCode = this.putPage(ePage);
            if (retCode < 0) {
                LOGGER.error("An error has been occured");
                return;
            }
        }
        retCode = this.waitForWorkQueue();
        if (retCode < 0) {
            LOGGER.error("An error has been occured");
            return;
        }
        pageTimer.stop();
        LOGGER.info("SemanticLinks (Total: {}) has been created in {}ms", this.linkTupleDao.count(LinkType.SemanticLink),
                pageTimer.getTime());

        LOGGER.info("Filtering semantic links");
        pageTimer.reset();
        pageTimer.start();
        long minCountFilter = this.linkTupleDao.applyMinCountFilter(LinkType.SemanticLink, MIN_LINK_OCCURS);
        pageTimer.stop();
        LOGGER.info("MinimumCount filter has been applied. {} links removed in {}ms", minCountFilter, pageTimer.getTime());
        LOGGER.info("SemanticLinks (Total: {}) has been filtered", this.linkTupleDao.count(LinkType.SemanticLink));
    }

}
