package com.gun3y.pagerank.store;

import java.util.Iterator;

import com.gun3y.pagerank.entity.html.EnhancedHtmlPage;

public interface HtmlEntityManager {

    Iterator<EnhancedHtmlPage> getEnhancedHtmlPageIterator();

    EnhancedHtmlPage getEnhancedHtmlPageByUrl(String url);

    long getEnhancedHtmlPageCount();
}
