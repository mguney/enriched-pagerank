package com.gun3y.pagerank;

import com.gun3y.pagerank.aggregator.HtmlAggregator;
import com.gun3y.pagerank.analyzer.LinkAnalysisManager;
import com.gun3y.pagerank.dao.EnhancedHtmlPageDao;
import com.gun3y.pagerank.dao.HtmlTitleDao;
import com.gun3y.pagerank.dao.LinkTupleDao;
import com.gun3y.pagerank.store.MongoHtmlPageDao;
import com.gun3y.pagerank.utils.DBUtils;
import com.sleepycat.je.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class MainApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApp.class);

    private static final String DB_PATH = "data-prdm";

    public static void main22(String[] args) {

        MongoHtmlPageDao mongoHtmlPageDao = new MongoHtmlPageDao("localhost", "PRDB");
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
        LinkTupleDao linkTupleDao = new LinkTupleDao();

        HtmlTitleDao htmlTitleDao = new HtmlTitleDao(env);

        LinkAnalysisManager analysisManager = new LinkAnalysisManager(enhancedHtmlPageDao, linkTupleDao, htmlTitleDao);
        analysisManager.analyze();

        enhancedHtmlPageDao.close();

        htmlTitleDao.close();
        linkTupleDao.close();

        env.close();
        System.out.println(Calendar.getInstance().getTime());
    }
}
