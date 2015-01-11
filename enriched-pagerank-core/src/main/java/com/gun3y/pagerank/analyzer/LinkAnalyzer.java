package com.gun3y.pagerank.analyzer;

import java.util.List;

import com.gun3y.pagerank.dao.LinkTuple;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;


public interface LinkAnalyzer {

    List<LinkTuple> analyze(EnhancedHtmlPage ePage);

    List<LinkTuple> analyze(EnhancedHtmlPage ePage, EnhancedHtmlPage tempPage);
}
