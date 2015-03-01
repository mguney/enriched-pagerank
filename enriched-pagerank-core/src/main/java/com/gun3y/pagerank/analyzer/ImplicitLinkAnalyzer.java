package com.gun3y.pagerank.analyzer;

import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.HtmlTitle;
import com.gun3y.pagerank.entity.LinkTuple;
import com.gun3y.pagerank.entity.LinkType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ImplicitLinkAnalyzer implements LinkAnalyzer {

    @Override
    public List<LinkTuple> analyze(EnhancedHtmlPage ePage, Set<HtmlTitle> titleSet) {
        List<LinkTuple> tuples = new ArrayList<LinkTuple>();

        if (ePage == null || titleSet == null || titleSet.isEmpty()) {
            return tuples;
        }

        String stemmedText = ePage.getStemmedText();
        if (StringUtils.isBlank(stemmedText)) {
            return tuples;
        }

        for (HtmlTitle htmlTitle : titleSet) {
            if (!htmlTitle.validate() || ePage.getUrl().equals(htmlTitle.getUrl())) {
                continue;
            }

            int countMatches = StringUtils.countMatches(stemmedText, htmlTitle.getStemmedTitle());
            while (countMatches > 0) {
                tuples.add(new LinkTuple(ePage.getUrl(), LinkType.ImplicitLink, htmlTitle.getUrl(), htmlTitle.getStemmedTitle()));
                countMatches--;
            }
        }

        return tuples;

    }

    @Override
    public List<LinkTuple> analyze(EnhancedHtmlPage ePage) {
        return Collections.emptyList();
    }

}
