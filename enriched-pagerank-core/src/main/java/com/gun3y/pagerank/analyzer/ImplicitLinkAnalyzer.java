package com.gun3y.pagerank.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.HtmlTitle;
import com.gun3y.pagerank.entity.LinkTuple;
import com.gun3y.pagerank.entity.LinkType;

public class ImplicitLinkAnalyzer implements LinkAnalyzer {

    @Override
    public List<LinkTuple> analyze(EnhancedHtmlPage ePage, HtmlTitle htmlTitle) {
        List<LinkTuple> tuples = new ArrayList<LinkTuple>();

        if (ePage == null || htmlTitle == null || !htmlTitle.validate()) {
            return tuples;
        }

        String stemmedText = ePage.getStemmedText();
        if (StringUtils.isBlank(stemmedText)) {
            return tuples;
        }

        int countMatches = StringUtils.countMatches(stemmedText, htmlTitle.getStemmedTitle());
        while (countMatches > 0) {
            tuples.add(new LinkTuple(ePage.getUrl(), LinkType.ImplicitLink, htmlTitle.getUrl(), htmlTitle.getStemmedTitle()));
            countMatches--;
        }

        return tuples;

    }

    @Override
    public List<LinkTuple> analyze(EnhancedHtmlPage ePage) {
        return Collections.emptyList();
    }

}
