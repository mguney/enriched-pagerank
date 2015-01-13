package com.gun3y.pagerank.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.gun3y.pagerank.dao.LinkTuple;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.LinkType;

public class ExplicitLinkAnalyzer implements LinkAnalyzer {

    @Override
    public List<LinkTuple> analyze(EnhancedHtmlPage ePage, EnhancedHtmlPage tempPage) {
        return Collections.emptyList();
    }

    @Override
    public List<LinkTuple> analyze(EnhancedHtmlPage ePage) {
        List<LinkTuple> tuples = new ArrayList<LinkTuple>();

        if (ePage == null || StringUtils.isBlank(ePage.getUrl())) {
            return tuples;
        }

        Set<String> outgoingUrls = ePage.getOutgoingUrls();

        if (outgoingUrls == null || outgoingUrls.isEmpty()) {
            return tuples;
        }

        for (String url : outgoingUrls) {
            tuples.add(new LinkTuple(ePage.getUrl(), LinkType.ExplicitLink, url));
        }

        return tuples;
    }
}
