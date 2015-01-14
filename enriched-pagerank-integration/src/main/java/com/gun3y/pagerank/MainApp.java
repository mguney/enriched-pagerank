package com.gun3y.pagerank;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.aggregator.HtmlAggregator;
import com.gun3y.pagerank.analyzer.LinkAnalysisManager;
import com.gun3y.pagerank.dao.EnhancedHtmlPageDao;
import com.gun3y.pagerank.dao.WebLinkDao;
import com.gun3y.pagerank.store.MongoHtmlPageDao;
import com.gun3y.pagerank.utils.DBUtils;
import com.sleepycat.je.Environment;

public class MainApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApp.class);

    private static final String DB_PATH = "data";

    public static void main2(String[] args) {

        MongoHtmlPageDao mongoHtmlPageDao = new MongoHtmlPageDao();
        mongoHtmlPageDao.init();

        DBUtils.deleteFolderContents(new File(DB_PATH));
        LOGGER.info("DB Path has been cleaned");

        Environment env = DBUtils.newEnvironment(DB_PATH);
        LOGGER.info("New Environment has been created");

        EnhancedHtmlPageDao enhancedHtmlPageDao = new EnhancedHtmlPageDao(env);

        HtmlAggregator aggregator = new HtmlAggregator(mongoHtmlPageDao, enhancedHtmlPageDao);

        aggregator.transformHtmlPages();

        enhancedHtmlPageDao.close();

        mongoHtmlPageDao.close();

        env.close();

    }

    public static void main(String[] args) throws IOException {
        System.out.println(Calendar.getInstance().getTime());
        Environment env = DBUtils.newEnvironment(DB_PATH);
        LOGGER.info("New Environment has been created");

        EnhancedHtmlPageDao enhancedHtmlPageDao = new EnhancedHtmlPageDao(env);
        WebLinkDao webLinkDao = new WebLinkDao(env);

        // EnhancedHtmlPage htmlPageByUrl = enhancedHtmlPageDao
        // .getHtmlPageByUrl("http://www.bbc.com/future/story/20141201-the-myths-about-ptsd");
        //
        // SemanticLinkAnalyzer analyzer = new SemanticLinkAnalyzer();
        // analyzer.analyze(htmlPageByUrl);
        // StopWatch stopWatch = new StopWatch();
        // stopWatch.start();
        // // System.out.println(webLinkDao.getLinkTupleCount());
        // // System.out.println(webLinkDao.getLinkTupleIterator());
        // webLinkDao.addLinkTuple(new LinkTuple("asd", LinkType.ExplicitLink,
        // "asd"));
        // stopWatch.stop();
        // System.out.println(stopWatch.getTime());

        LinkAnalysisManager analysisManager = new LinkAnalysisManager(enhancedHtmlPageDao, webLinkDao);
        analysisManager.analyze();

        // System.out.println(webLinkDao.getLinkTupleCount());
        // List<LinkTuple> exp = new ArrayList<LinkTuple>();
        // List<LinkTuple> imp = new ArrayList<LinkTuple>();
        // List<LinkTuple> sem = new ArrayList<LinkTuple>();
        // Iterator<LinkTuple> linkTupleIterator =
        // webLinkDao.getLinkTupleIterator();
        // while (linkTupleIterator.hasNext()) {
        // LinkTuple next = linkTupleIterator.next();
        // if (next.getLinkType() == LinkType.ExplicitLink) {
        // exp.add(next);
        // }
        // else if (next.getLinkType() == LinkType.ImplicitLink) {
        // imp.add(next);
        // }
        // else if (next.getLinkType() == LinkType.SemanticLink) {
        // sem.add(next);
        // }
        // }
        // FileUtils.writeLines(new File("exp.txt"), exp);
        // FileUtils.writeLines(new File("imp.txt"), imp);
        // FileUtils.writeLines(new File("sem.txt"), sem);

        enhancedHtmlPageDao.close();

        webLinkDao.close();

        env.close();
        System.out.println(Calendar.getInstance().getTime());
    }
    // public static void main(String[] args) {
    //
    // }
}
