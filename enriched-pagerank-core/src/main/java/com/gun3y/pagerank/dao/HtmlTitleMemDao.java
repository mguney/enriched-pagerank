package com.gun3y.pagerank.dao;

import com.gun3y.pagerank.entity.HtmlTitle;
import com.sleepycat.je.Environment;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class HtmlTitleMemDao {

    protected Set<HtmlTitle> htmlTitleSet = new HashSet<HtmlTitle>();

    public HtmlTitleMemDao(Environment env) {

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

    public Set<HtmlTitle> getHtmlTitleSet() {
        return this.htmlTitleSet;
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
