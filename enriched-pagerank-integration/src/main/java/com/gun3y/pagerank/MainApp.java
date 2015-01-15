package com.gun3y.pagerank;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.aggregator.HtmlAggregator;
import com.gun3y.pagerank.analyzer.LinkAnalysisManager;
import com.gun3y.pagerank.dao.EnhancedHtmlPageDao;
import com.gun3y.pagerank.dao.LinkTuple;
import com.gun3y.pagerank.dao.WebLinkDao;
import com.gun3y.pagerank.entity.LinkType;
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

    public static void mainasd(String[] args) throws IOException {
        System.out.println(Calendar.getInstance().getTime());
        Environment env = DBUtils.newEnvironment(DB_PATH);
        LOGGER.info("New Environment has been created");

        EnhancedHtmlPageDao enhancedHtmlPageDao = new EnhancedHtmlPageDao(env);
        WebLinkDao webLinkDao = new WebLinkDao(env);

        LinkAnalysisManager analysisManager = new LinkAnalysisManager(enhancedHtmlPageDao, webLinkDao);
        analysisManager.analyze();

        enhancedHtmlPageDao.close();

        webLinkDao.close();

        env.close();
        System.out.println(Calendar.getInstance().getTime());
    }

    public static void main(String[] args) throws IOException {
        System.out.println(Calendar.getInstance().getTime());
        Environment env = DBUtils.newEnvironment(DB_PATH);
        LOGGER.info("New Environment has been created");

        WebLinkDao webLinkDao = new WebLinkDao(env);

        System.out.println(webLinkDao.getLinkTupleCount());
        List<LinkTuple> exp = new ArrayList<LinkTuple>();
        List<LinkTuple> imp = new ArrayList<LinkTuple>();
        List<LinkTuple> sem = new ArrayList<LinkTuple>();
        Iterator<LinkTuple> linkTupleIterator = webLinkDao.getLinkTupleIterator();
        while (linkTupleIterator.hasNext()) {
            LinkTuple next = linkTupleIterator.next();
            if (next.getLinkType() == LinkType.ExplicitLink) {
                exp.add(next);
            }
            else if (next.getLinkType() == LinkType.ImplicitLink) {
                imp.add(next);
            }
            else if (next.getLinkType() == LinkType.SemanticLink) {
                sem.add(next);
            }
        }
        FileUtils.writeLines(new File("exp.txt"), exp);
        FileUtils.writeLines(new File("imp.txt"), imp);
        FileUtils.writeLines(new File("sem.txt"), sem);

        webLinkDao.close();

        env.close();
        System.out.println(Calendar.getInstance().getTime());
    }
}
