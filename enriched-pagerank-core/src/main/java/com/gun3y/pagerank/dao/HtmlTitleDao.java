package com.gun3y.pagerank.dao;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.gun3y.pagerank.entity.HtmlTitle;
import com.sleepycat.je.Environment;

public class HtmlTitleDao {

    protected Set<HtmlTitle> htmlTitleSet = new HashSet<HtmlTitle>();

    public HtmlTitleDao(Environment env) {

    }

    public void addHtmlTitle(HtmlTitle htmlTitle) {
        if (htmlTitle == null || StringUtils.isBlank(htmlTitle.getStemmedTitle()) || StringUtils.isBlank(htmlTitle.getUrl())) {
            return;
        }

        this.htmlTitleSet.add(htmlTitle);
    }

    public Iterator<HtmlTitle> getHtmlTitleIterator() {
        return this.htmlTitleSet.iterator();
    }

    public int count() {
        return this.htmlTitleSet.size();
    }

    public void removeAll() {
        this.htmlTitleSet.clear();
    }

    public void close() {

    }

}
