package com.gun3y.pagerank.web;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gun3y.pagerank.web.response.SearchResponse;

@Service
public class SolrService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrService.class);

    @Value("#{T(java.util.Arrays).asList('${solr.urls}')}")
    private List<String> solrUrls;

    private List<SolrServer> servers;

    @PostConstruct
    public void initServers() {
        LOGGER.info("initializing Solr Server ...");
        servers = new ArrayList<SolrServer>();
        for (String url : solrUrls) {
            SolrServer solrServer = new HttpSolrServer(url);
            LOGGER.info("Solr Url: " + url);
            servers.add(solrServer);
        }
        LOGGER.info("Solr Servers has bean initialized!");

    }

    public List<String> getSolrUrls() {
        return solrUrls;
    }

    public SearchResponse search(String query) throws SolrServerException {

        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(query);

        QueryResponse rsp = servers.get(0).query(solrQuery);
        SolrDocumentList results = rsp.getResults();
        SearchResponse response = new SearchResponse();
        response.setSearchResults(results);
        return response;
    }

}
