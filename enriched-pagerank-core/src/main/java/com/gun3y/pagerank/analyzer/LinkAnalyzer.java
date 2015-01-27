package com.gun3y.pagerank.analyzer;

import java.util.List;

import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.HtmlTitle;
import com.gun3y.pagerank.entity.LinkTuple;

public interface LinkAnalyzer {

    List<LinkTuple> analyze(EnhancedHtmlPage ePage);

    List<LinkTuple> analyze(EnhancedHtmlPage ePage, HtmlTitle htmlTitle);
}
