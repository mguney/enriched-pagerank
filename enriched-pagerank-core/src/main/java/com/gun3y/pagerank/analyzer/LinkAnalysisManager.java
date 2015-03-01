package com.gun3y.pagerank.analyzer;

import com.gun3y.pagerank.dao.EnhancedHtmlPageDao;
import com.gun3y.pagerank.dao.HtmlTitleDao;
import com.gun3y.pagerank.dao.LinkTupleDao;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.HtmlTitle;
import com.gun3y.pagerank.entity.LinkTuple;
import com.gun3y.pagerank.entity.LinkType;
import com.gun3y.pagerank.utils.LangUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class LinkAnalysisManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkAnalysisManager.class);

    private static final int MAX_STEM_COUNT = 5;

    private static final int MIN_LINK_OCCURS = 5;

    private EnhancedHtmlPageDao htmlPageDao;

    private LinkTupleDao linkTupleDao;

    private HtmlTitleDao htmlTitleDao;

    private ExplicitLinkAnalyzer explicitLinkAnalyzer;

    private ImplicitLinkAnalyzer implicitLinkAnalyzer;

    private SemanticLinkAnalyzer semanticLinkAnalyzer;

    public LinkAnalysisManager(EnhancedHtmlPageDao htmlPageDao, LinkTupleDao linkTupleDao, HtmlTitleDao htmlTitleDao) {
        super();
        this.htmlPageDao = htmlPageDao;
        this.linkTupleDao = linkTupleDao;
        this.htmlTitleDao = htmlTitleDao;

        this.explicitLinkAnalyzer = new ExplicitLinkAnalyzer();
        this.implicitLinkAnalyzer = new ImplicitLinkAnalyzer();
        this.semanticLinkAnalyzer = new SemanticLinkAnalyzer(10);
    }

    public void analyze() {
        LOGGER.info("Link Analysis has started");
        StopWatch pageTimer = new StopWatch();
        pageTimer.start();

        this.linkTupleDao.removeAll();
        LOGGER.info("LinkTuples has been cleaned in {}ms", pageTimer.getTime());

        this.analyzeExplicitLinks();

        this.analyzeImplicitLinks();

        this.analyzeSemanticLinks();

        this.semanticLinkAnalyzer.shutdown();

        LOGGER.info("Link Analysis has ended in {}ms", pageTimer.getTime());
    }

    private void analyzeSemanticLinks() {
        StopWatch pageTimer = new StopWatch();

        Iterator<EnhancedHtmlPage> ePageIterator = this.htmlPageDao.getHtmlPageIterator();

        Set<HtmlTitle> uniqueTitles = this.linkTupleDao.findAllTitles();

        while (ePageIterator.hasNext()) {
            EnhancedHtmlPage ePage = ePageIterator.next();

            pageTimer.reset();
            pageTimer.start();
            List<LinkTuple> semanticTuples = this.semanticLinkAnalyzer.analyze(ePage, uniqueTitles);
            this.linkTupleDao.addLinkTuple(semanticTuples);
            pageTimer.stop();
            LOGGER.info("{}: Semantic Links: {} Time: {}", ePage.getUrl(), semanticTuples.size(), pageTimer.getTime());

        }

        LOGGER.info("Filtering semantic links");
        pageTimer.reset();
        pageTimer.start();
        long minCountFilter = this.linkTupleDao.applyMinCountFilter(LinkType.SemanticLink, MIN_LINK_OCCURS);
        pageTimer.stop();
        LOGGER.info("MinimumCount filter has been applied. {} links removed in {}ms", minCountFilter, pageTimer.getTime());
        LOGGER.info("SemanticLinks (Total: {}) has been filtered", this.linkTupleDao.count(LinkType.SemanticLink));
    }

    private void analyzeImplicitLinks() {
        StopWatch pageTimer = new StopWatch();
        pageTimer.start();
        Iterator<EnhancedHtmlPage> ePageIterator = this.htmlPageDao.getHtmlPageIterator();
        while (ePageIterator.hasNext()) {
            EnhancedHtmlPage ePage = ePageIterator.next();

            List<LinkTuple> implicitTuples = this.implicitLinkAnalyzer.analyze(ePage, this.htmlTitleDao.getHtmlTitleSet());
            this.linkTupleDao.addLinkTuple(implicitTuples);

            LOGGER.info("{}: Implicit Links: {}", ePage.getUrl(), implicitTuples.size());
        }
        pageTimer.stop();
        LOGGER.info("ImplicitLinks (Total: {}) has been created in {}ms", this.linkTupleDao.count(LinkType.ImplicitLink),
                pageTimer.getTime());

        LOGGER.info("Filtering implicit links");
        pageTimer.reset();
        pageTimer.start();
        long minCountFilter = this.linkTupleDao.applyMinCountFilter(LinkType.ImplicitLink, MIN_LINK_OCCURS);
        pageTimer.stop();
        LOGGER.info("MinimumCount filter has been applied. {} links removed in {}ms", minCountFilter, pageTimer.getTime());

        pageTimer.reset();
        pageTimer.start();
        long sameUrlFilter = this.linkTupleDao.applySameUrlFilter(LinkType.ImplicitLink);
        pageTimer.stop();
        LOGGER.info("SameURL filter has been applied. {} links removed in {}ms", sameUrlFilter, pageTimer.getTime());
        LOGGER.info("ImplicitLinks (Total: {}) has been filtered", this.linkTupleDao.count(LinkType.ImplicitLink));

    }

    private void analyzeExplicitLinks() {
        StopWatch pageTimer = new StopWatch();

        LOGGER.info("Html titles are preparing");
        pageTimer.reset();
        pageTimer.start();
        this.htmlTitleDao.removeAll();
        pageTimer.stop();
        LOGGER.info("HtmlTitles has been cleaned in {}ms", pageTimer.getTime());

        pageTimer.reset();
        pageTimer.start();
        StopWatch explicitLinkTimer = new StopWatch();
        explicitLinkTimer.start();
        explicitLinkTimer.suspend();
        Iterator<EnhancedHtmlPage> ePageIterator = this.htmlPageDao.getHtmlPageIterator();
        while (ePageIterator.hasNext()) {
            EnhancedHtmlPage ePage = ePageIterator.next();

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

            pageTimer.suspend();
            explicitLinkTimer.resume();
            List<LinkTuple> explicitTuples = this.explicitLinkAnalyzer.analyze(ePage);
            this.linkTupleDao.addLinkTuple(explicitTuples);
            explicitLinkTimer.suspend();
            LOGGER.info("{}: Explicit Links: {}", ePage.getUrl(), explicitTuples.size());
            pageTimer.resume();
        }
        LOGGER.info("Html titles (Total: {}) has been prepared in {}ms", this.htmlTitleDao.count(), pageTimer.getTime());
        LOGGER.info("ExplicitLinks (Total: {}) has been created in {}ms", this.linkTupleDao.count(LinkType.ExplicitLink),
                explicitLinkTimer.getTime());
    }

    private String stem(String title) {
        List<String> stemmedWords = LangUtils.extractStemmedWords(title);
        if (stemmedWords.size() >= MAX_STEM_COUNT) {
            return StringUtils.EMPTY;
        }

        return LangUtils.joinList(stemmedWords);
    }
}
