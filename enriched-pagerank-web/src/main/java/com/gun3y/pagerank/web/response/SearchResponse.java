package com.gun3y.pagerank.web.response;

import org.apache.solr.common.SolrDocumentList;

public class SearchResponse {

    private SolrDocumentList searchResults;

    public void setSearchResults(SolrDocumentList searchResults) {
        this.searchResults = searchResults;
    }

    public SolrDocumentList getSearchResults() {
        return this.searchResults;
    }

}
