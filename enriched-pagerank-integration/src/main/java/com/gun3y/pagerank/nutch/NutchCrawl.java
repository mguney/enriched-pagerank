package com.gun3y.pagerank.nutch;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapFileOutputFormat;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.fetcher.Fetcher;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.scoring.webgraph.LinkDatum;
import org.apache.nutch.util.FSUtils;
import org.apache.nutch.util.HadoopFSUtil;
import org.apache.nutch.util.LockUtil;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.apache.nutch.util.TimingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.analyzer.ExplicitLinkAnalyzer;
import com.gun3y.pagerank.analyzer.ImplicitLinkAnalyzer;
import com.gun3y.pagerank.analyzer.SemanticLinkAnalyzer;
import com.gun3y.pagerank.dao.HtmlTitleDao;
import com.gun3y.pagerank.dao.LinkTupleDao;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.helper.BeanHelper;

public class NutchCrawl extends Configured implements Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(NutchCrawl.class);

    private static final String LOCK_NAME = ".locked";

    private static final String CURRENT = "current";

    private static final String OLD = "old";

    public static void main(String[] args) throws Exception {
        //        int res = ToolRunner.run(NutchConfiguration.create(), new WebGraph(), args);
        //        System.exit(res);

        Configuration conf = NutchConfiguration.create();

        NutchCrawl nutchCrawl = null;// = new NutchCrawl();

        String base = "C:/Users/Mustafa/Desktop/NUTCH_CRAWL/";

        String crawldDb = base + "crawldb";
        String integrateDb = base + "integratedb";
        String segments = base + "segments";

        ToolRunner.run(conf, nutchCrawl, new String[] { "-segmentDir", segments, "-integratedb", integrateDb, "-crawldb", crawldDb });
    }

    private static ExplicitLinkAnalyzer EX_LINK_ANALYZER;
    private static ImplicitLinkAnalyzer IMP_LINK_ANALYZER;
    private static SemanticLinkAnalyzer SEM_LINK_ANALYZER;

    static {
        HtmlTitleDao htmlTitleDao = new HtmlTitleDao();
        LinkTupleDao linkTupleDao = new LinkTupleDao();

        int numOfWorker = 10;

        EX_LINK_ANALYZER = new ExplicitLinkAnalyzer(numOfWorker, htmlTitleDao, linkTupleDao);
        IMP_LINK_ANALYZER = new ImplicitLinkAnalyzer(numOfWorker, htmlTitleDao, linkTupleDao);
        SEM_LINK_ANALYZER = new SemanticLinkAnalyzer(numOfWorker, htmlTitleDao, linkTupleDao);
    }

    @Override
    public int run(String[] args) throws Exception {
        // boolean options
        Option helpOpt = new Option("h", "help", false, "show this help message");

        // argument options
        @SuppressWarnings("static-access")
        Option nutchOpt = OptionBuilder.withArgName("integratedb").hasArg().withDescription("nutch integrate folder").create("integratedb");
        @SuppressWarnings("static-access")
        Option segOpt = OptionBuilder.withArgName("segment").hasArgs().withDescription("the segment(s) to use").create("segment");
        @SuppressWarnings("static-access")
        Option segDirOpt = OptionBuilder.withArgName("segmentDir").hasArgs().withDescription("the segment directory to use")
        .create("segmentDir");
        @SuppressWarnings("static-access")
        Option crawlDirOpt = OptionBuilder.withArgName("crawldb").hasArgs().withDescription("crawl db").create("crawldb");
        @SuppressWarnings("static-access")
        Option opOpt = OptionBuilder.withArgName("op").hasArgs().withDescription("operation").create("op");

        // create the options
        Options options = new Options();
        options.addOption(helpOpt);
        options.addOption(nutchOpt);
        options.addOption(segOpt);
        options.addOption(segDirOpt);
        options.addOption(crawlDirOpt);
        options.addOption(opOpt);

        CommandLineParser parser = new GnuParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("help") || !line.hasOption("integratedb") || !line.hasOption("crawldb")
                    || (!line.hasOption("segment") && !line.hasOption("segmentDir"))) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("NutchCrawl", options, true);
                return -1;
            }

            String integratedb = line.getOptionValue("integratedb");

            String crawldb = line.getOptionValue("crawldb");

            Path[] segPaths = null;

            // Handle segment option
            if (line.hasOption("segment")) {
                String[] segments = line.getOptionValues("segment");
                segPaths = new Path[segments.length];
                for (int i = 0; i < segments.length; i++) {
                    segPaths[i] = new Path(segments[i]);
                }
            }

            // Handle segmentDir option
            if (line.hasOption("segmentDir")) {
                Path dir = new Path(line.getOptionValue("segmentDir"));
                FileSystem fs = dir.getFileSystem(this.getConf());
                FileStatus[] fstats = fs.listStatus(dir, HadoopFSUtil.getPassDirectoriesFilter(fs));
                segPaths = HadoopFSUtil.getPaths(fstats);
            }

            this.integrate(new Path(crawldb), new Path(integratedb), segPaths);
            return 0;
        }
        catch (Exception e) {
            LOGGER.error("NutchCrawl: " + StringUtils.stringifyException(e));
            return -2;
        }
    }

    private void integrate(Path crawlDb, Path integrateDb, Path[] segments) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long start = System.currentTimeMillis();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("WebGraphDb: starting at " + sdf.format(start));
            LOGGER.info("WebGraphDb: integratedb: " + integrateDb);
        }

        Configuration conf = this.getConf();
        FileSystem fs = FileSystem.get(conf);

        // lock an existing webgraphdb to prevent multiple simultaneous updates
        Path lock = new Path(integrateDb, LOCK_NAME);
        if (!fs.exists(integrateDb)) {
            fs.mkdirs(integrateDb);
        }

        LockUtil.createLockFile(fs, lock, false);

        // outlink and temp outlink database paths
        Path current = new Path(integrateDb, CURRENT);
        Path old = new Path(integrateDb, OLD);

        if (!fs.exists(current)) {
            fs.mkdirs(current);
        }

        Path tempCurrent = new Path(current + "-" + Integer.toString(new Random().nextInt(Integer.MAX_VALUE)));
        JobConf integrateJob = new NutchJob(conf);
        integrateJob.setJobName("IntegrateDb: " + current);

        // get the parse data and crawl fetch data for all segments
        if (segments != null) {
            for (final Path segment : segments) {
                LOGGER.info("NutchCrawl: adding segment: " + segment);
                FileInputFormat.addInputPath(integrateJob, new Path(segment, ParseData.DIR_NAME));
                FileInputFormat.addInputPath(integrateJob, new Path(segment, Fetcher.CONTENT_REDIR));
            }
        }

        // add the crawldb
        FileInputFormat.addInputPath(integrateJob, new Path(crawlDb, CrawlDb.CURRENT_NAME));

        // add the existing integrateDB
        LOGGER.info("NutchCrawl: adding input: " + current);
        FileInputFormat.addInputPath(integrateJob, current);

        //Set outputpath
        FileOutputFormat.setOutputPath(integrateJob, tempCurrent);
        integrateJob.setInputFormat(SequenceFileInputFormat.class);
        integrateJob.setMapperClass(IntegrateDb.class);
        integrateJob.setReducerClass(IntegrateDb.class);
        integrateJob.setMapOutputKeyClass(Text.class);
        integrateJob.setMapOutputValueClass(EnrichedPageRankWriteable.class);
        integrateJob.setOutputKeyClass(Text.class);
        integrateJob.setOutputValueClass(EnrichedPageRankWriteable.class);
        integrateJob.setOutputFormat(MapFileOutputFormat.class);

        // run the outlinkdb job and replace any old outlinkdb with the new one
        try {
            LOGGER.info("NutchCrawl: running");
            JobClient.runJob(integrateJob);
            LOGGER.info("NutchCrawl: installing " + current);
            FSUtils.replace(fs, old, current, true);
            FSUtils.replace(fs, current, tempCurrent, true);
            LOGGER.info("NutchCrawl: finished");
        }
        catch (IOException e) {

            // remove lock file and and temporary directory if an error occurs
            LockUtil.removeLockFile(fs, lock);
            if (fs.exists(tempCurrent)) {
                fs.delete(tempCurrent, true);
            }
            LOGGER.error(StringUtils.stringifyException(e));
            throw e;
        }

        // remove the lock file for the webgraph
        LockUtil.removeLockFile(fs, lock);

        long end = System.currentTimeMillis();
        LOGGER.info("WebGraphDb: finished at " + sdf.format(end) + ", elapsed: " + TimingUtil.elapsedTime(start, end));

    }

    public static class IntegrateDb extends Configured implements Mapper<Text, Writable, Text, EnrichedPageRankWriteable>,
    Reducer<Text, EnrichedPageRankWriteable, Text, EnrichedPageRankWriteable> {

        // url normalizers, filters and job configuration
        private URLNormalizers urlNormalizers;
        private URLFilters filters;
        private JobConf conf;

        /**
         * Normalizes and trims extra whitespace from the given url.
         *
         * @param url
         *            The url to normalize.
         *
         * @return The normalized url.
         */
        private String normalizeUrl(String url) {

            String normalized = null;
            if (this.urlNormalizers != null) {
                try {

                    // normalize and trim the url
                    normalized = this.urlNormalizers.normalize(url, URLNormalizers.SCOPE_DEFAULT);
                    normalized = normalized.trim();
                }
                catch (Exception e) {
                    LOGGER.warn("Skipping " + url + ":" + e);
                    normalized = null;
                }
            }
            return normalized;
        }

        /**
         * Filters the given url.
         *
         * @param url
         *            The url to filter.
         *
         * @return The filtered url or null.
         */
        private String filterUrl(String url) {

            try {
                url = this.filters.filter(url);
            }
            catch (Exception e) {
                url = null;
            }

            return url;
        }

        /**
         * Default constructor.
         */
        public IntegrateDb() {
        }

        /**
         * Configurable constructor.
         */
        public IntegrateDb(Configuration conf) {
            this.setConf(conf);

        }

        /**
         * Configures the OutlinkDb job. Sets up internal links and link limiting.
         */
        @Override
        public void configure(JobConf conf) {
            this.conf = conf;

            this.urlNormalizers = new URLNormalizers(conf, URLNormalizers.SCOPE_DEFAULT);
            this.filters = new URLFilters(conf);

        }

        /**
         * Passes through existing LinkDatum objects from an existing OutlinkDb and maps out new LinkDatum objects from new crawls ParseData.
         */
        @Override
        public void map(Text key, Writable value, OutputCollector<Text, EnrichedPageRankWriteable> output, Reporter reporter)
                throws IOException {

            // normalize url, stop processing if null
            String url = this.normalizeUrl(key.toString());
            if (url == null) {
                return;
            }

            // filter url
            if (this.filterUrl(url) == null) {
                return;
            }

            // Overwrite the key with the normalized URL
            key.set(url);

            if (value instanceof CrawlDatum) {
                CrawlDatum datum = (CrawlDatum) value;

                if (datum.getStatus() == CrawlDatum.STATUS_FETCH_REDIR_TEMP || datum.getStatus() == CrawlDatum.STATUS_FETCH_REDIR_PERM
                        || datum.getStatus() == CrawlDatum.STATUS_FETCH_GONE) {

                    // Tell the reducer to get rid of all instances of this key
                    output.collect(key, new EnrichedPageRankWriteable(new BooleanWritable(true)));
                }
            }
            else if (value instanceof ParseData) {
                // get the parse data and the outlinks from the parse data, along with
                // the fetch time for those links
                ParseData data = (ParseData) value;
                Outlink[] outlinkAr = data.getOutlinks();
                Map<String, String> outlinkMap = new LinkedHashMap<String, String>();

                // normalize urls and put into map
                if (outlinkAr != null && outlinkAr.length > 0) {
                    for (int i = 0; i < outlinkAr.length; i++) {
                        Outlink outlink = outlinkAr[i];
                        String toUrl = this.normalizeUrl(outlink.getToUrl());

                        if (this.filterUrl(toUrl) == null) {
                            continue;
                        }

                        // only put into map if the url doesn't already exist in the map or
                        // if it does and the anchor for that link is null, will replace if
                        // url is existing
                        boolean existingUrl = outlinkMap.containsKey(toUrl);
                        if (toUrl != null && (!existingUrl || (existingUrl && outlinkMap.get(toUrl) == null))) {
                            outlinkMap.put(toUrl, outlink.getAnchor());
                        }
                    }
                }

                //collect parse data
                output.collect(new Text(key), new EnrichedPageRankWriteable(data));

                // collect the outlinks under the fetch time
                for (String outlinkUrl : outlinkMap.keySet()) {
                    String anchor = outlinkMap.get(outlinkUrl);
                    LinkDatum datum = new LinkDatum(outlinkUrl, anchor, 0L);
                    output.collect(new Text(outlinkUrl), new EnrichedPageRankWriteable(datum));

                }
            }
            else if (value instanceof LinkDatum) {
                LinkDatum datum = (LinkDatum) value;
                String linkDatumUrl = this.normalizeUrl(datum.getUrl());

                if (this.filterUrl(linkDatumUrl) != null) {
                    datum.setUrl(linkDatumUrl);
                    // collect existing outlinks from existing OutlinkDb
                    output.collect(key, new EnrichedPageRankWriteable(datum));
                }
            }
            else if (value instanceof Content) {
                output.collect(key, new EnrichedPageRankWriteable(value));
            }
            else {
                System.out.println("burda bi bokluk var !");
            }
        }

        @Override
        public void reduce(Text key, Iterator<EnrichedPageRankWriteable> values, OutputCollector<Text, EnrichedPageRankWriteable> output,
                Reporter reporter) throws IOException {

            Content content = null;
            ParseData data = null;
            List<String> anchors = new ArrayList<String>();
            while (values.hasNext()) {
                Writable value = values.next().get();

                if (value instanceof LinkDatum) {
                    // loop through, change out most recent timestamp if needed
                    LinkDatum next = (LinkDatum) value;
                    anchors.add(next.getAnchor());
                }
                else if (value instanceof BooleanWritable) {
                    BooleanWritable delete = (BooleanWritable) value;
                    // Actually, delete is always true, otherwise we don't emit it in the
                    // mapper in the first place
                    if (delete.get() == true) {
                        // This page is gone, do not emit it's outlinks
                        return;
                    }
                }
                else if (value instanceof ParseData) {
                    data = (ParseData) value;
                }
                else if (value instanceof Content) {
                    content = (Content) value;
                }
            }

            String url = content.getUrl();
            String html = new String(content.getContent());
            String title = data.getTitle();

            EnhancedHtmlPage ePage = BeanHelper.newEnhancedHtmlPage(url, html, title, anchors);

            if (ePage != null) {
                EX_LINK_ANALYZER.push(ePage);
                IMP_LINK_ANALYZER.push(ePage);
                SEM_LINK_ANALYZER.push(ePage);
            }
        }

        @Override
        public void close() {
        }
    }
}
