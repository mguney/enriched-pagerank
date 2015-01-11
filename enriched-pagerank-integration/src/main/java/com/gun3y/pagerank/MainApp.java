package com.gun3y.pagerank;

import java.io.File;

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

    public static void main(String[] args) {
        Environment env = DBUtils.newEnvironment(DB_PATH);
        LOGGER.info("New Environment has been created");

        EnhancedHtmlPageDao enhancedHtmlPageDao = new EnhancedHtmlPageDao(env);
        WebLinkDao webLinkDao = new WebLinkDao(env);

        LinkAnalysisManager analysisManager = new LinkAnalysisManager(enhancedHtmlPageDao, webLinkDao);
        System.out.println(enhancedHtmlPageDao.getHtmlPageCount());
        analysisManager.analyze();

        enhancedHtmlPageDao.close();

        webLinkDao.close();

        env.close();
    }

    // public static void main(String[] args) {
    //
    // }
}
