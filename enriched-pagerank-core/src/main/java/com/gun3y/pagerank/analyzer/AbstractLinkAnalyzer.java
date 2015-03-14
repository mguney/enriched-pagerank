package com.gun3y.pagerank.analyzer;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.dao.EnhancedHtmlPageDao;
import com.gun3y.pagerank.dao.HtmlTitleDao;
import com.gun3y.pagerank.dao.LinkTupleDao;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.utils.LangUtils;

abstract class AbstractLinkAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLinkAnalyzer.class);

    protected static final int MAX_STEM_COUNT = 5;

    protected static final int MIN_LINK_OCCURS = 5;

    protected HtmlTitleDao htmlTitleDao;

    protected EnhancedHtmlPageDao htmlPageDao;

    protected LinkTupleDao linkTupleDao;

    private final BlockingQueue<EnhancedHtmlPage> workQueue;

    private final ThreadPoolExecutor service;

    private final int workQueueSize;

    private final int numWorkers;

    private final AtomicInteger counter;

    public AbstractLinkAnalyzer(int numWorkers, HtmlTitleDao htmlTitleDao, EnhancedHtmlPageDao htmlPageDao, LinkTupleDao linkTupleDao) {
        if (numWorkers < 1) {
            throw new RuntimeException("Number of workers cannot be zero or less");
        }

        this.htmlTitleDao = htmlTitleDao;
        this.htmlPageDao = htmlPageDao;
        this.linkTupleDao = linkTupleDao;

        this.numWorkers = numWorkers;
        this.workQueueSize = numWorkers * 5;
        this.counter = new AtomicInteger(numWorkers);

        this.workQueue = new LinkedBlockingQueue<EnhancedHtmlPage>(this.workQueueSize);

        this.service = new ThreadPoolExecutor(numWorkers, numWorkers, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        for (int i = 0; i < this.numWorkers; i++) {
            this.service.submit(this.newLinkWorker(i, this.workQueue, this.counter, linkTupleDao));
        }
    }

    protected abstract LinkWorker newLinkWorker(int id, BlockingQueue<EnhancedHtmlPage> workQueue, AtomicInteger counter,
            LinkTupleDao linkTupleDao);

    public abstract void analyze();

    protected void putPage(EnhancedHtmlPage ePage) {
        try {
            this.workQueue.put(ePage);
        }
        catch (Throwable ex) {
            LOGGER.error(ex.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    protected void waitForWorkQueue() {
        while (!this.workQueue.isEmpty() || this.counter.get() != 0) {
            try {
                Thread.sleep(200);
            }
            catch (Throwable e) {
                LOGGER.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        this.clearQueue();
    }

    protected void clearQueue() {
        this.workQueue.clear();
    }

    protected boolean isInterupted() {
        return this.service.getActiveCount() == 0;
    }

    public void shutdown() {
        this.service.shutdownNow();
    }

    protected String stem(String title) {
        List<String> stemmedWords = LangUtils.extractStemmedWords(title);
        if (stemmedWords.size() >= MAX_STEM_COUNT) {
            LOGGER.debug("[MAX_STEM_COUNT] - " + LangUtils.joinList(stemmedWords));
            return StringUtils.EMPTY;
        }

        return LangUtils.joinList(stemmedWords);
    }

}
