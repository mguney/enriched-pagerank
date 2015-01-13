package com.gun3y.pagerank.dao;

import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.StoredMap;
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

    protected StoredMap<Integer, LinkTuple> weblinkMap;

    public WebLinkDao(Environment env) {

        this.env = env;

        DatabaseConfig dbConfig = new DatabaseConfig();

        // db will be created if not exits
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);

        Database catalogDb = this.env.openDatabase(null, EnhancedHtmlPageDao.CLASS_CATALOG, dbConfig);
        this.catalog = new StoredClassCatalog(catalogDb);

        this.weblinkDB = this.env.openDatabase(null, WEBLINK_DB, dbConfig);

        this.weblinkMap = new StoredMap<Integer, LinkTuple>(this.weblinkDB, new SerialBinding<Integer>(this.catalog, Integer.class),
                new SerialBinding<LinkTuple>(this.catalog, LinkTuple.class), true);
    }

    public void addLinkTuple(LinkTuple linkTuple) {
        if (linkTuple == null) {
            return;
        }

        this.weblinkMap.put(this.weblinkMap.size() + 1, linkTuple);

    }

    public void addLinkTuple(Collection<LinkTuple> linkTuples) {
        if (linkTuples == null) {
            return;
        }

        for (LinkTuple linkTuple : linkTuples) {
            this.addLinkTuple(linkTuple);
        }
    }

    public int getLinkTupleCount() {
        return this.weblinkMap.size();
    }

    public Iterator<LinkTuple> getLinkTupleIterator() {
        return this.weblinkMap.values().iterator();
    }

    public void removeAll() {
        this.weblinkMap.clear();
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
