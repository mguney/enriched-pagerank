package com.gun3y.pagerank.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.common.EPRConstants;
import com.gun3y.pagerank.dao.HtmlTitleDao;
import com.gun3y.pagerank.dao.LinkTupleDao;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.HtmlTitle;
import com.gun3y.pagerank.entity.LinkTuple;
import com.gun3y.pagerank.entity.LinkType;

public class ImplicitLinkAnalyzer extends AbstractLinkAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImplicitLinkAnalyzer.class);

    public ImplicitLinkAnalyzer(int numWorkers, HtmlTitleDao htmlTitleDao, LinkTupleDao linkTupleDao) {
        super(numWorkers, htmlTitleDao, linkTupleDao);
        this.titleSet = Collections.synchronizedSet(this.htmlTitleDao.getHtmlTitleSet());
    }

    private Set<HtmlTitle> titleSet;

    @Override
    public void push(EnhancedHtmlPage ePage) {
        int retCode = this.putPage(ePage);
        if (retCode < 0) {
            LOGGER.error("An error has been occured");
            return;
        }

    }

    @Override
    public void finish() {
        int retCode = this.waitForWorkQueue();
        if (retCode < 0) {
            LOGGER.error("An error has been occured");
            return;
        }

        LOGGER.info("ImplicitLinks (Total: {}) has been created", this.linkTupleDao.count(LinkType.ImplicitLink));

        StopWatch pageTimer = new StopWatch();
        LOGGER.info("Filtering implicit links");
        pageTimer.reset();
        pageTimer.start();
        long minCountFilter = this.linkTupleDao.applyMinCountFilter(LinkType.ImplicitLink, EPRConstants.MIN_LINK_OCCURS);
        pageTimer.stop();
        LOGGER.info("MinimumCount filter has been applied. {} links removed in {}ms", minCountFilter, pageTimer.getTime());

        pageTimer.reset();
        pageTimer.start();
        long sameUrlFilter = this.linkTupleDao.applySameUrlFilter(LinkType.ImplicitLink);
        pageTimer.stop();
        LOGGER.info("SameURL filter has been applied. {} links removed in {}ms", sameUrlFilter, pageTimer.getTime());
        LOGGER.info("ImplicitLinks (Total: {}) has been filtered", this.linkTupleDao.count(LinkType.ImplicitLink));

    }

    @Override
    protected LinkWorker newLinkWorker(int id, BlockingQueue<EnhancedHtmlPage> workQueue, AtomicInteger counter, LinkTupleDao linkTupleDao) {
        return new LinkWorker("ImLinkWorker#" + id, workQueue, counter, linkTupleDao) {

            @Override
            public List<LinkTuple> analyze(EnhancedHtmlPage ePage) {
                List<LinkTuple> tuples = new ArrayList<LinkTuple>();

                if (ePage == null) {
                    return tuples;
                }

                String stemmedText = ePage.getStemmedText();
                if (StringUtils.isBlank(stemmedText)) {
                    return tuples;
                }

                for (HtmlTitle htmlTitle : ImplicitLinkAnalyzer.this.titleSet) {
                    if (htmlTitle.validate() && !ePage.getUrl().equals(htmlTitle.getUrl())) {
                        int countMatches = StringUtils.countMatches(stemmedText, htmlTitle.getStemmedTitle());
                        while (countMatches > 0) {
                            tuples.add(new LinkTuple(ePage.getUrl(), LinkType.ImplicitLink, htmlTitle.getUrl(), htmlTitle.getStemmedTitle()));
                            countMatches--;
                        }
                    }
                }

                return tuples;

            }

        };
    }

}
