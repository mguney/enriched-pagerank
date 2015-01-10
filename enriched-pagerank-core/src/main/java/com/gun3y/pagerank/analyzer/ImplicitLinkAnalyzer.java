package com.gun3y.pagerank.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.gun3y.pagerank.entity.graph.LinkType;
import com.gun3y.pagerank.entity.html.EnhancedHtmlPage;
import com.gun3y.pagerank.store.LinkTuple;

public class ImplicitLinkAnalyzer implements LinkAnalyzer {

    @Override
    public List<LinkTuple> analyze(EnhancedHtmlPage ePage, EnhancedHtmlPage tempPage) {
        List<LinkTuple> tuples = new ArrayList<LinkTuple>();

        if (ePage == null || tempPage == null || ePage.getPageId() == tempPage.getPageId()) {
            return tuples;
        }

        Set<String> stemmedTitles = new HashSet<String>();
        String stemmedTitle = ePage.getStemmedTitle();

        if (StringUtils.isNotBlank(stemmedTitle)) {
            stemmedTitles.add(stemmedTitle);
        }

        Set<String> stemmedAnchorTitles = ePage.getStemmedAnchorTitles();
        if (stemmedAnchorTitles != null) {
            stemmedTitles.addAll(stemmedAnchorTitles);
        }

        if (stemmedTitles.isEmpty()) {
            return tuples;
        }

        String stemmedText = tempPage.getStemmedText();
        if (StringUtils.isBlank(stemmedText)) {
            return tuples;
        }
        for (String key : stemmedTitles) {
            if (stemmedText.contains(key.toLowerCase(Locale.ENGLISH))) {
                tuples.add(new LinkTuple(tempPage.getUrl(), LinkType.ImplicitLink, ePage.getUrl()));
            }
        }

        return tuples;

    }

    @Override
    public List<LinkTuple> analyze(EnhancedHtmlPage ePage) {
        return Collections.emptyList();
    }
}
