package com.gun3y.pagerank.store;

import java.net.UnknownHostException;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.MapperOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.dao.HtmlPageDao;
import com.gun3y.pagerank.entity.html.HtmlPage;
import com.mongodb.MongoClient;

public class MongoHtmlPageDao implements HtmlPageDao<HtmlPage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoHtmlPageDao.class);

    private MongoClient mongoClient;

    private Datastore ds;

    private String dbName = "PageRankCrawlerDB";

    private String dbHost = "localhost";

    public MongoHtmlPageDao() {
        super();
    }

    public MongoHtmlPageDao(String host, String db) {
        super();
        this.dbHost = host;
        this.dbName = db;
    }

    @PostConstruct
    public void init() {

        try {
            this.mongoClient = new MongoClient(this.dbHost);
        }
        catch (UnknownHostException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalArgumentException("Unkown host!");
        }

        MapperOptions mapperOptions = new MapperOptions();

        Mapper mapper = new Mapper(mapperOptions);
        Morphia morphia = new Morphia(mapper);

        this.ds = morphia.createDatastore(this.mongoClient, this.dbName);
        morphia.map(HtmlPage.class);
    }

    @Override
    @PreDestroy
    public void close() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    @Override
    public synchronized void addHtmlPage(HtmlPage htmlPage) {
        if (this.ds == null || htmlPage == null) {
            return;
        }

        this.ds.save(htmlPage);
        LOGGER.info("HtmlPage Added: ID:{} URL:{}", htmlPage.getUrl().getDocid(), htmlPage.getUrl().getUrl());
    }

    @Override
    public synchronized int getHtmlPageCount() {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        return (int) this.ds.createQuery(HtmlPage.class).countAll();
    }

    @Override
    public synchronized Iterator<HtmlPage> getHtmlPageIterator() {
        if (this.ds == null) {
            throw new IllegalArgumentException("MongoManager not initialized!");
        }

        return this.ds.find(HtmlPage.class).fetch();
    }

    @Override
    public HtmlPage getHtmlPageByUrl(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateHtmlPage(String url, HtmlPage page) {
        // TODO Auto-generated method stub

    }

}
