package com.gun3y.pagerank.analyzer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.common.EPRConstants;
import com.gun3y.pagerank.dao.HtmlTitleDao;
import com.gun3y.pagerank.dao.LinkTupleDao;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.LinkType;

import edu.emory.clir.clearnlp.component.AbstractComponent;
import edu.emory.clir.clearnlp.component.mode.dep.DEPConfiguration;
import edu.emory.clir.clearnlp.component.utils.NLPUtils;
import edu.emory.clir.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.util.lang.TLanguage;

public class SemanticLinkAnalyzer extends AbstractLinkAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemanticLinkAnalyzer.class);

    private AbstractComponent[] components;

    private AbstractTokenizer tokenizer;

    public SemanticLinkAnalyzer(int numWorkers, HtmlTitleDao htmlTitleDao, LinkTupleDao linkTupleDao) {
        super(numWorkers, htmlTitleDao, linkTupleDao);

    }

    @Override
    protected LinkWorker newLinkWorker(int id, BlockingQueue<EnhancedHtmlPage> workQueue, AtomicInteger counter, LinkTupleDao linkTupleDao) {

        if (this.components == null || this.tokenizer == null) {
            final String rootLabel = "root"; // root label for dependency parsing

            AbstractComponent morph = NLPUtils.getMPAnalyzer(TLanguage.ENGLISH);
            AbstractComponent tagger = NLPUtils.getPOSTagger(TLanguage.ENGLISH, "general-en-pos.xz");
            AbstractComponent parser = NLPUtils.getDEPParser(TLanguage.ENGLISH, "general-en-dep.xz", new DEPConfiguration(rootLabel));

            this.components = new AbstractComponent[] { tagger, morph, parser };
            this.tokenizer = NLPUtils.getTokenizer(TLanguage.ENGLISH);
        }

        return new SemanticLinkWorker2("SemLinkWorker#" + id, workQueue, counter, linkTupleDao, this.htmlTitleDao, this.tokenizer,
                this.components);
    }

    @Override
    public void push(EnhancedHtmlPage ePage) {
        if (Thread.currentThread().isInterrupted() || this.isInterupted()) {
            LOGGER.error("An error has been occured");
            return;
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

        LOGGER.info("SemanticLinks (Total: {}) has been created in {}ms", this.linkTupleDao.count(LinkType.SemanticLink));

        StopWatch pageTimer = new StopWatch();
        pageTimer.reset();
        pageTimer.start();
        LOGGER.info("Filtering semantic links");
        long minCountFilter = this.linkTupleDao.applyMinCountFilter(LinkType.SemanticLink, EPRConstants.MIN_LINK_OCCURS);
        pageTimer.stop();
        LOGGER.info("MinimumCount filter has been applied. {} links removed in {}ms", minCountFilter, pageTimer.getTime());
        LOGGER.info("SemanticLinks (Total: {}) has been filtered", this.linkTupleDao.count(LinkType.SemanticLink));

    }

}
