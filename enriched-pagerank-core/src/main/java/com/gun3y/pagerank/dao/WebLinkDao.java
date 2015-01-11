package com.gun3y.pagerank.dao;

import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.StoredList;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;

public class WebLinkDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebLinkDao.class);

    private static final String WEBLINK_DB = "WebLinkDB";

    protected Database weblinkDB = null;

    protected Environment env;

    protected StoredClassCatalog catalog;

    protected StoredList<LinkTuple> webLinkList;

    public WebLinkDao(Environment env) {

        this.env = env;

        DatabaseConfig dbConfig = new DatabaseConfig();

        // db will be created if not exits
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);

        Database catalogDb = this.env.openDatabase(null, EnhancedHtmlPageDao.CLASS_CATALOG, dbConfig);
        this.catalog = new StoredClassCatalog(catalogDb);

        this.weblinkDB = this.env.openDatabase(null, WEBLINK_DB, dbConfig);

        // TODO: burası çalışmıyor
        this.webLinkList = new StoredList<LinkTuple>(this.weblinkDB, new SerialBinding<LinkTuple>(this.catalog, LinkTuple.class), true);
    }

    public void addLinkTuple(LinkTuple linkTuple) {
        if (linkTuple == null) {
            return;
        }

        this.webLinkList.add(linkTuple);

    }

    public void addLinkTuple(Collection<LinkTuple> linkTuples) {
        if (linkTuples == null) {
            return;
        }

        this.webLinkList.addAll(linkTuples);

    }

    public int getLinkTupleCount() {
        return this.webLinkList.size();
    }

    public Iterator<LinkTuple> getLinkTupleIterator() {
        return this.webLinkList.iterator();
    }

    public void removeAll() {
        this.webLinkList.clear();
    }

    public void close() {
        try {
            this.weblinkDB.close();
            this.catalog.close();
        }
        catch (DatabaseException e) {
            LOGGER.error(e.getMessage());
        }
    }

}
