package com.gun3y.pagerank.crawler.mapping;

import org.dozer.DozerBeanMapper;

import com.gun3y.pagerank.page.HtmlPage;

import edu.uci.ics.crawler4j.crawler.Page;

public class BeanMapper {

    private static DozerBeanMapper MAPPER = new DozerBeanMapper();

    static {
        MAPPER.addMapping(BeanMapper.class.getClassLoader().getResourceAsStream("dozer-mapping.xml"));
    }

    public static HtmlPage map(Page page) {
        return MAPPER.map(page, HtmlPage.class);
    }

}
