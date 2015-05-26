package com.gun3y.pagerank.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.dao.HtmlTitleDao;
import com.gun3y.pagerank.dao.LinkTupleDao;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.HtmlTitle;
import com.gun3y.pagerank.entity.LinkTuple;
import com.gun3y.pagerank.entity.LinkType;

public class ExplicitLinkAnalyzer extends AbstractLinkAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExplicitLinkAnalyzer.class);

    public ExplicitLinkAnalyzer(int numWorkers, HtmlTitleDao htmlTitleDao, LinkTupleDao linkTupleDao) {
        super(numWorkers, htmlTitleDao, linkTupleDao);
    }

    @Override
    public void push(EnhancedHtmlPage ePage) {

        String title = ePage.getTitle();

        String stemmedTitle = this.stem(title);

        String url = ePage.getUrl();

        if (StringUtils.isNotBlank(stemmedTitle)) {
            this.htmlTitleDao.addHtmlTitle(new HtmlTitle(stemmedTitle, url));
        }

        Set<String> anchors = ePage.getAnchors();

        if (anchors != null) {
            for (String anchor : anchors) {
                String stemmedAnchor = this.stem(anchor);
                if (StringUtils.isNotBlank(stemmedAnchor)) {
                    this.htmlTitleDao.addHtmlTitle(new HtmlTitle(stemmedAnchor, url));
                }
            }
        }

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
        int removeDuplicates = this.htmlTitleDao.removeDuplicates(5);

        LOGGER.info("Html titles (Total: {} Removed: {}) has been prepared", this.htmlTitleDao.count(), removeDuplicates);
        LOGGER.info("ExplicitLinks (Total: {}) has been created", this.linkTupleDao.count(LinkType.ExplicitLink));

    }

    @Override
    protected LinkWorker newLinkWorker(int id, BlockingQueue<EnhancedHtmlPage> workQueue, AtomicInteger counter, LinkTupleDao linkTupleDao) {
        return new LinkWorker("ExLinkWorker#" + id, workQueue, counter, linkTupleDao) {

            @Override
            public List<LinkTuple> analyze(EnhancedHtmlPage ePage) {
                List<LinkTuple> tuples = new ArrayList<LinkTuple>();

                if (ePage == null || StringUtils.isBlank(ePage.getUrl())) {
                    return tuples;
                }

                Set<String> outgoingUrls = ePage.getOutgoingUrls();

                if (outgoingUrls == null || outgoingUrls.isEmpty()) {
                    return tuples;
                }

                //TODO: Explicit link'lere ihtiyaç olmadığı için kapatıldı.
                //                for (String url : outgoingUrls) {
                //                    tuples.add(new LinkTuple(ePage.getUrl(), LinkType.ExplicitLink, url));
                //                }

                return tuples;
            }
        };
    }

}
