package com.gun3y.pagerank.analyzer;

import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.HtmlTitle;
import com.gun3y.pagerank.entity.LinkTuple;

import java.util.List;
import java.util.Set;

public interface LinkAnalyzer {

    List<LinkTuple> analyze(EnhancedHtmlPage ePage);

    List<LinkTuple> analyze(EnhancedHtmlPage ePage, Set<HtmlTitle> titleSet);
}
