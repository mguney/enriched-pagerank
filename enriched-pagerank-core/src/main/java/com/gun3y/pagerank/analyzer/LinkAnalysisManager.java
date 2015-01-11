package com.gun3y.pagerank.analyzer;

import java.util.Iterator;
import java.util.List;

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

    // private LinkStorage linkStorage = new LinkStorage();

    private LinkAnalyzer[] analyers = new LinkAnalyzer[] { new ExplicitLinkAnalyzer(), new ImplicitLinkAnalyzer(),
            new SemanticLinkAnalyzer() };

    public LinkAnalysisManager(EnhancedHtmlPageDao htmlPageDao, WebLinkDao webLinkDao) {
        super();
        this.htmlPageDao = htmlPageDao;
        this.webLinkDao = webLinkDao;
    }

    public void analyze() {
        LOGGER.info("Link Analysis has started");

        this.webLinkDao.removeAll();
        LOGGER.info("LinkTuples has been cleaned");

        Iterator<EnhancedHtmlPage> ePageIterator = this.htmlPageDao.getHtmlPageIterator();
        while (ePageIterator.hasNext()) {
            EnhancedHtmlPage ePage = ePageIterator.next();

            Iterator<EnhancedHtmlPage> tempPageIterator = this.htmlPageDao.getHtmlPageIterator();
            while (tempPageIterator.hasNext()) {
                EnhancedHtmlPage tempPage = tempPageIterator.next();

                if (ePage.getPageId() == tempPage.getPageId()) {
                    continue;
                }

                for (LinkAnalyzer linkAnalyzer : this.analyers) {
                    List<LinkTuple> tuples = linkAnalyzer.analyze(ePage, tempPage);
                    System.out.println(linkAnalyzer.getClass() + " " + tuples.size());
                    this.webLinkDao.addLinkTuple(tuples);
                }
            }
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
