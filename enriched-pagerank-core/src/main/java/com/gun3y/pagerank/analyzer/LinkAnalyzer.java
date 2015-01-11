package com.gun3y.pagerank.analyzer;

import java.util.List;

import com.gun3y.pagerank.entity.html.EnhancedHtmlPage;
import com.gun3y.pagerank.store.LinkTuple;


public interface LinkAnalyzer {

    List<LinkTuple> analyze(EnhancedHtmlPage ePage);

    List<LinkTuple> analyze(EnhancedHtmlPage ePage, EnhancedHtmlPage tempPage);
}
