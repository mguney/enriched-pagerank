package com.gun3y.pagerank.analyzer;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.gun3y.pagerank.dao.LinkTupleDao;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.LinkTuple;

abstract class LinkWorker implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkWorker.class);

    private final BlockingQueue<EnhancedHtmlPage> workQueue;

    private final AtomicInteger counter;

    private LinkTupleDao linkTupleDao;

    private String workerId;

    public LinkWorker(String workerId, BlockingQueue<EnhancedHtmlPage> workQueue, AtomicInteger counter, LinkTupleDao linkTupleDao) {
        this.workQueue = workQueue;
        this.counter = counter;
        this.linkTupleDao = linkTupleDao;
        this.workerId = workerId;

    }

    public abstract List<LinkTuple> analyze(EnhancedHtmlPage htmlPage);

    @Override
    public void run() {
        MDC.put("logFileName", this.workerId);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                this.counter.decrementAndGet();
                EnhancedHtmlPage htmlPage = this.workQueue.take();
                this.counter.incrementAndGet();

                List<LinkTuple> linkTuples = this.analyze(htmlPage);

                this.linkTupleDao.addLinkTuple(linkTuples);
                LOGGER.info("{}: Links: {}", htmlPage.getUrl(), linkTuples.size());
            }
            catch (Throwable ex) {
                Thread.currentThread().interrupt();
                LOGGER.error(ex.getMessage());
                break;
            }
        }
    }
}
