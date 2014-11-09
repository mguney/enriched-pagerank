package com.gun3y.pagerank.mongo;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.MapperOptions;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.page.HtmlPage;
import com.mongodb.MongoClient;

public class MongoManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoManager.class);

    private MongoClient mongoClient;

    private Datastore ds;

    private String dbName = "htmlPageDB";

    private String dbHost = "localhost";

    public MongoManager() {
        super();
    }

    public MongoManager(String host, String db) {
        super();
        this.dbHost = host;
        this.dbName = db;
    }

    @PostConstruct
    public void init() throws UnknownHostException {

        this.mongoClient = new MongoClient(this.dbHost);

        MapperOptions mapperOptions = new MapperOptions();

        Mapper mapper = new Mapper(mapperOptions);
        Morphia morphia = new Morphia(mapper);

        this.ds = morphia.createDatastore(this.mongoClient, this.dbName);
        morphia.map(HtmlPage.class);
    }

    @PreDestroy
    public void close() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

    public synchronized List<HtmlPage> getHtmlPages() {
        if (this.ds == null) {
            return Collections.emptyList();
        }
        Query<HtmlPage> find = this.ds.find(HtmlPage.class);

        return find.asList();
    }

    public synchronized void add(HtmlPage htmlPage) {
        if (this.ds == null || htmlPage == null) {
            return;
        }

        this.ds.save(htmlPage);
    }

    public synchronized void remove(Collection<HtmlPage> htmlPages) {
        if (this.ds == null || htmlPages == null) {
            return;
        }

        for (HtmlPage htmlPage : htmlPages) {
            this.ds.delete(htmlPage);
        }
    }

    public synchronized long getCount() {
        if (this.ds == null) {
            return -1;
        }
        return this.ds.getCount(HtmlPage.class);
    }

    public static void main(String[] args) throws UnknownHostException {
        MongoManager mongoManager = new MongoManager();
        mongoManager.init();
        HtmlPage as = new HtmlPage();
        as.setStatusCode(200);
        // mongoManager.add(as);
        List<HtmlPage> htmlPages = mongoManager.getHtmlPages();

        for (HtmlPage htmlPage : htmlPages) {
            LOGGER.info(htmlPage.toString());
        }

        // mongoManager.remove(htmlPages);
        mongoManager.close();

    }
}
