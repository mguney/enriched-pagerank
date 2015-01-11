package com.gun3y.pagerank.dao;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;

public class EnhancedHtmlPageDao implements HtmlPageDao<EnhancedHtmlPage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancedHtmlPageDao.class);

    private static final String ENHANCED_HTML_PAGE_DB = "EnhancedHtmlPageDB";

    public static final String CLASS_CATALOG = "EPR_CATALOG";

    protected Database htmlPageDB = null;

    protected Environment env;

    protected StoredClassCatalog catalog;

    protected StoredMap<String, EnhancedHtmlPage> enhancedHtmlPageMap;

    public EnhancedHtmlPageDao(Environment env) {
        this.env = env;

        DatabaseConfig dbConfig = new DatabaseConfig();

        // db will be created if not exits
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);

        Database catalogDb = this.env.openDatabase(null, CLASS_CATALOG, dbConfig);
        this.catalog = new StoredClassCatalog(catalogDb);

        this.htmlPageDB = this.env.openDatabase(null, ENHANCED_HTML_PAGE_DB, dbConfig);

        this.enhancedHtmlPageMap = new StoredMap<String, EnhancedHtmlPage>(this.htmlPageDB, new SerialBinding<String>(this.catalog,
                String.class), new SerialBinding<EnhancedHtmlPage>(this.catalog, EnhancedHtmlPage.class), true);

    }

    @Override
    public void addHtmlPage(EnhancedHtmlPage page) {
        if (page == null) {
            return;
        }

        if (StringUtils.isBlank(page.getUrl())) {
            LOGGER.warn("HtmlPage URL is missing");
        }
        else {
            this.enhancedHtmlPageMap.put(page.getUrl(), page);
        }

    }

    @Override
    public Iterator<EnhancedHtmlPage> getHtmlPageIterator() {
        return this.enhancedHtmlPageMap.values().iterator();
    }

    @Override
    public EnhancedHtmlPage getHtmlPageByUrl(String url) {
        return this.enhancedHtmlPageMap.get(url);
    }

    @Override
    public int getHtmlPageCount() {
        return this.enhancedHtmlPageMap.size();
    }

    @Override
    public void close() {
        try {
            this.htmlPageDB.close();
            this.catalog.close();
        }
        catch (DatabaseException e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void updateHtmlPage(String url, EnhancedHtmlPage page) {
        if (StringUtils.isBlank(url)) {
            LOGGER.error("Update URL is missing");
            return;
        }

        if (page == null) {
            LOGGER.error("Page is missing");
            return;
        }

        if (StringUtils.isBlank(page.getUrl())) {
            LOGGER.warn("Page URL is missin");
        }

        this.enhancedHtmlPageMap.put(url, page);
    }
}
