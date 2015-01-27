package com.gun3y.pagerank.dao;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.gun3y.pagerank.entity.HtmlTitle;
import com.sleepycat.je.Environment;

public class HtmlTitleDao {
    // private static final Logger LOGGER =
    // LoggerFactory.getLogger(HtmlTitleDao.class);

    // private static final String HTMLTITLE_DB = "HtmlTitleDB";

    // private static final String HTMLTITLE_COUNT_DB = "HtmlTitleCountDB";
    //
    // protected Database htmlTitleDB = null;
    //
    // protected Database htmlTitleCountDB = null;
    //
    // protected Environment env;
    //
    // protected StoredClassCatalog catalog;
    //
    // protected StoredMap<String, HtmlTitle> htmlTitleMap;
    //
    // protected StoredMap<String, Integer> htmlTitleCountMap;

    protected Map<String, HtmlTitle> htmlTitleMap = new HashMap<String, HtmlTitle>();

    public HtmlTitleDao(Environment env) {

        // this.env = env;
        //
        // DatabaseConfig dbConfig = new DatabaseConfig();
        //
        // // db will be created if not exits
        // dbConfig.setAllowCreate(true);
        // dbConfig.setTransactional(true);
        //
        // Database catalogDb = this.env.openDatabase(null,
        // EnhancedHtmlPageDao.CLASS_CATALOG, dbConfig);
        // this.catalog = new StoredClassCatalog(catalogDb);
        //
        // this.htmlTitleDB = this.env.openDatabase(null, HTMLTITLE_DB,
        // dbConfig);
        //
        // this.htmlTitleCountDB = this.env.openDatabase(null,
        // HTMLTITLE_COUNT_DB, dbConfig);
        //
        // this.htmlTitleMap = new StoredMap<String,
        // HtmlTitle>(this.htmlTitleDB, new SerialBinding<String>(this.catalog,
        // String.class),
        // new SerialBinding<HtmlTitle>(this.catalog, HtmlTitle.class), true);
        //
        // this.htmlTitleCountMap = new StoredMap<String,
        // Integer>(this.htmlTitleCountDB,
        // new SerialBinding<String>(this.catalog, String.class), new
        // SerialBinding<Integer>(this.catalog, Integer.class), true);
    }

    public void addHtmlTitle(HtmlTitle htmlTitle) {
        if (htmlTitle == null || StringUtils.isBlank(htmlTitle.getStemmedTitle()) || StringUtils.isBlank(htmlTitle.getUrl())) {
            return;
        }

        String uniqueId = htmlTitle.getUniqueId();

        if (!this.htmlTitleMap.containsKey(uniqueId)) {
            this.htmlTitleMap.put(uniqueId, htmlTitle);
        }

        // if (this.htmlTitleCountMap.containsKey(uniqueId)) {
        // this.htmlTitleCountMap.put(uniqueId,
        // this.htmlTitleCountMap.get(uniqueId) + count);
        // }
        // else {
        // this.htmlTitleCountMap.put(uniqueId, 1);
        // this.htmlTitleMap.put(uniqueId, htmlTitle);
        // }
    }

    public Iterator<HtmlTitle> getHtmlTitleIterator() {
        return this.htmlTitleMap.values().iterator();
    }

    public void removeAll() {
        this.htmlTitleMap.clear();
        // this.htmlTitleCountMap.clear();
    }

    public void close() {
        // try {
        // this.htmlTitleDB.close();
        // this.htmlTitleCountDB.close();
        // this.catalog.close();
        // }
        // catch (DatabaseException e) {
        // LOGGER.error(e.getMessage());
        // }
    }

}
