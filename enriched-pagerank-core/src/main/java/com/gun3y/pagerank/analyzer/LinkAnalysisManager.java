package com.gun3y.pagerank.analyzer;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.dao.EnhancedHtmlPageDao;
import com.gun3y.pagerank.dao.LinkTuple;
import com.gun3y.pagerank.dao.WebLinkDao;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;

public class LinkAnalysisManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkAnalysisManager.class);

    private EnhancedHtmlPageDao htmlPageDao;

    private WebLinkDao webLinkDao;

    private LinkAnalyzer explicitLinkAnalyzer = new ExplicitLinkAnalyzer();

    private LinkAnalyzer implicitLinkAnalyzer = new ImplicitLinkAnalyzer();

    private LinkAnalyzer semanticLinkAnalyzer = new SemanticLinkAnalyzer();

    public LinkAnalysisManager(EnhancedHtmlPageDao htmlPageDao, WebLinkDao webLinkDao) {
        super();
        this.htmlPageDao = htmlPageDao;
        this.webLinkDao = webLinkDao;
    }

    public void analyze() {
        LOGGER.info("Link Analysis has started");
        StopWatch pageTimer = new StopWatch();

        pageTimer.start();
        this.webLinkDao.removeAll();
        pageTimer.stop();
        LOGGER.info("LinkTuples has been cleaned in {}ms", pageTimer.getTime());

        Iterator<EnhancedHtmlPage> ePageIterator = this.htmlPageDao.getHtmlPageIterator();
        while (ePageIterator.hasNext()) {
            EnhancedHtmlPage ePage = ePageIterator.next();

            pageTimer.reset();
            pageTimer.start();
            List<LinkTuple> explicitTuples = this.explicitLinkAnalyzer.analyze(ePage);
            this.webLinkDao.addLinkTuple(explicitTuples);
            pageTimer.stop();
            LOGGER.info("{}: Explicit Links: {} Time: {}", ePage.getUrl(), explicitTuples.size(), pageTimer.getTime());

            pageTimer.reset();
            pageTimer.start();
            List<LinkTuple> semanticTuples = this.semanticLinkAnalyzer.analyze(ePage);
            this.webLinkDao.addLinkTuple(semanticTuples);
            pageTimer.stop();
            LOGGER.info("{}: Semantic Links: {} Time: {}", ePage.getUrl(), semanticTuples.size(), pageTimer.getTime());

            int total = 0;
            int totalLinks = 0;
            pageTimer.reset();
            pageTimer.start();
            Iterator<EnhancedHtmlPage> tempPageIterator = this.htmlPageDao.getHtmlPageIterator();
            while (tempPageIterator.hasNext()) {
                EnhancedHtmlPage tempPage = tempPageIterator.next();

                if (ePage.getPageId() == tempPage.getPageId()) {
                    continue;
                }

                List<LinkTuple> implicitTuples = this.implicitLinkAnalyzer.analyze(ePage, tempPage);
                this.webLinkDao.addLinkTuple(implicitTuples);

                totalLinks += implicitTuples.size();
                total++;
            }
            pageTimer.stop();
            LOGGER.info("{}: Implicit Links: {} in {} page Time: {}", ePage.getUrl(), totalLinks, total, pageTimer.getTime());
        }

        // this.linkStorage.reduceLinks(5);
        //
        // for (LinkTuple linkTuple : this.linkStorage.getExplicitLinks()) {
        // LOGGER.info(linkTuple.toString());
        // }
        //
        // for (LinkTuple linkTuple : this.linkStorage.getImplicitLinks()) {
        // LOGGER.info(linkTuple.toString());
        // }
        //
        // for (LinkTuple linkTuple : this.linkStorage.getSemanticLinks()) {
        // LOGGER.info(linkTuple.toString());
        // }

        LOGGER.info("Link Analysis has ended");
    }
}
