package com.gun3y.pagerank;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.aggregator.HtmlAggregator;
import com.gun3y.pagerank.analyzer.ExplicitLinkAnalyzer;
import com.gun3y.pagerank.analyzer.ImplicitLinkAnalyzer;
import com.gun3y.pagerank.analyzer.SemanticLinkAnalyzer;
import com.gun3y.pagerank.crawler.BasicCrawlController;
import com.gun3y.pagerank.dao.EnhancedHtmlPageDao;
import com.gun3y.pagerank.dao.HtmlTitleDao;
import com.gun3y.pagerank.dao.LinkTupleDao;
import com.gun3y.pagerank.store.HtmlPageDao;
import com.gun3y.pagerank.utils.DBUtils;
import com.gun3y.pagerank.utils.HibernateUtils;
import com.sleepycat.je.Environment;

public class MainApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApp.class);

    public enum OP {
        Explicit, Implicit, Semantic, All, EnhanceHtml, Test, Crawl
    }

    @Option(name = "-data-html", usage = "data path for html")
    private String dataPathHtml = "PR-DATA/data_htmlpage";

    @Option(name = "-data", usage = "data path")
    private String dataPath = "PR-DATA/data_prdm";

    @Option(name = "-op", usage = "operation code", required = true)
    private OP op;

    @Option(name = "-thread", usage = "thread size")
    private int thread = 5;

    @Option(name = "-seed", usage = "seed list")
    private File seedFile = new File(".");

    public static void main(String[] args) throws IOException {
        MainApp app = new MainApp();
        app.doMain(args);
    }

    public void doMain(String[] args) throws IOException {
        System.out.println(Calendar.getInstance().getTime());
        ParserProperties defaults = ParserProperties.defaults().withUsageWidth(100);

        CmdLineParser parser = new CmdLineParser(this, defaults);

        try {
            parser.parseArgument(args);
        }
        catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.out);
            return;
        }
        try {

            System.out.println("Data     : " + this.dataPath);
            System.out.println("Data-Html: " + this.dataPathHtml);
            System.out.println("OP       : " + this.op);
            System.out.println("Thread   : " + this.thread);
            System.out.println("SeedFile : " + this.seedFile.getAbsolutePath());

            if (this.op == OP.Crawl) {
                DBUtils.deleteFolderContents(new File(this.dataPathHtml));
                LOGGER.info("DB Path for Html has been cleaned");
            }

            Environment envHtml = DBUtils.newEnvironment(this.dataPathHtml);

            if (this.op == OP.EnhanceHtml) {
                DBUtils.deleteFolderContents(new File(this.dataPath));
                LOGGER.info("DB Path has been cleaned");
            }

            Environment env = DBUtils.newEnvironment(this.dataPath);
            LOGGER.info("New Environment has been created");

            EnhancedHtmlPageDao eHtmlPageDao = new EnhancedHtmlPageDao(env);
            LinkTupleDao linkTupleDao = new LinkTupleDao();
            HtmlTitleDao htmlTitleDao = new HtmlTitleDao();

            HtmlPageDao htmlPageDao = new HtmlPageDao(envHtml);

            switch (this.op) {
                case Explicit:
                    ExplicitLinkAnalyzer ex1 = new ExplicitLinkAnalyzer(this.thread, htmlTitleDao, eHtmlPageDao, linkTupleDao);
                    ex1.analyze();
                    ex1.shutdown();
                    break;
                case Implicit:
                    ImplicitLinkAnalyzer im1 = new ImplicitLinkAnalyzer(this.thread, htmlTitleDao, eHtmlPageDao, linkTupleDao);
                    im1.analyze();
                    im1.shutdown();
                    break;
                case Semantic:
                    SemanticLinkAnalyzer sem1 = new SemanticLinkAnalyzer(this.thread, htmlTitleDao, eHtmlPageDao, linkTupleDao);
                    sem1.analyze();
                    sem1.shutdown();
                    break;
                case All:
                    ExplicitLinkAnalyzer ex2 = new ExplicitLinkAnalyzer(this.thread, htmlTitleDao, eHtmlPageDao, linkTupleDao);
                    ex2.analyze();
                    ex2.shutdown();

                    ImplicitLinkAnalyzer im2 = new ImplicitLinkAnalyzer(this.thread, htmlTitleDao, eHtmlPageDao, linkTupleDao);
                    im2.analyze();
                    im2.shutdown();

                    SemanticLinkAnalyzer sem3 = new SemanticLinkAnalyzer(this.thread, htmlTitleDao, eHtmlPageDao, linkTupleDao);
                    sem3.analyze();
                    sem3.shutdown();

                    break;
                case EnhanceHtml:

                    HtmlAggregator aggregator = new HtmlAggregator(htmlPageDao, eHtmlPageDao);

                    aggregator.transformHtmlPages();
                    break;
                case Test:
                    System.out.println("Test");
                    break;
                case Crawl:
                    BasicCrawlController.startCrawler(this.seedFile, htmlPageDao);
                    break;
            }

            eHtmlPageDao.close();
            htmlPageDao.close();
            env.close();
            envHtml.close();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        finally {
            HibernateUtils.getSessionFactory().close();
        }
        System.out.println(Calendar.getInstance().getTime());
    }
}
