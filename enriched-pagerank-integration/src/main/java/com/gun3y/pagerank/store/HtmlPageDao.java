package com.gun3y.pagerank.store;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.dao.EnhancedHtmlPageDao;
import com.gun3y.pagerank.entity.html.HtmlPage;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;

public class HtmlPageDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlPageDao.class);

    private static final String HTML_PAGE_DB = "HtmlPageDB";

    protected Database htmlPageDB = null;

    protected Environment env;

    protected StoredClassCatalog catalog;

    protected StoredMap<String, HtmlPage> htmlPageMap;

    public HtmlPageDao(Environment env) {
        this.env = env;

        DatabaseConfig dbConfig = new DatabaseConfig();

        // db will be created if not exits
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);

        Database catalogDb = this.env.openDatabase(null, EnhancedHtmlPageDao.CLASS_CATALOG, dbConfig);
        this.catalog = new StoredClassCatalog(catalogDb);

        this.htmlPageDB = this.env.openDatabase(null, HTML_PAGE_DB, dbConfig);

        this.htmlPageMap = new StoredMap<String, HtmlPage>(this.htmlPageDB, new SerialBinding<String>(this.catalog, String.class),
                new SerialBinding<HtmlPage>(this.catalog, HtmlPage.class), true);

    }

    public void addHtmlPage(HtmlPage page) {
        if (page == null) {
            return;
        }

        if (page.getUrl() == null || StringUtils.isBlank(page.getUrl().getUrl())) {
            LOGGER.warn("HtmlPage URL is missing");
        }
        else {
            this.htmlPageMap.put(page.getUrl().getUrl(), page);
        }

    }

    public Iterator<HtmlPage> getHtmlPageIterator() {
        return this.htmlPageMap.values().iterator();
    }

    public HtmlPage getHtmlPageByUrl(String url) {
        return this.htmlPageMap.get(url);
    }

    public int getHtmlPageCount() {
        return this.htmlPageMap.size();
    }

    public void close() {
        try {
            this.htmlPageDB.close();
            this.catalog.close();
        }
        catch (DatabaseException e) {
            LOGGER.error(e.getMessage());
        }
    }

}
