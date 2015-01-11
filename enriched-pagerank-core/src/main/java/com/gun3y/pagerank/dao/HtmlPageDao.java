package com.gun3y.pagerank.dao;

import java.util.Iterator;

public interface HtmlPageDao<T> {

    Iterator<T> getHtmlPageIterator();

    T getHtmlPageByUrl(String url);

    void addHtmlPage(T page);

    void updateHtmlPage(String url, T page);

    int getHtmlPageCount();

    void close();
}
