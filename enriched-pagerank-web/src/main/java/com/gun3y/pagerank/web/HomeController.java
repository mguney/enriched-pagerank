package com.gun3y.pagerank.web;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.gun3y.pagerank.web.response.SearchResponse;

@Path("/")
public class HomeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private SolrService solrService;

    @GET
    @Path("/solr/list")
    @Produces("application/json")
    public List<String> getUrls() {
        return solrService.getSolrUrls();
    }

    @GET
    @Path("/search/{query}")
    @Produces("application/json")
    public SearchResponse search(@PathParam("query") String query) throws SolrServerException {

        return solrService.search(query);
    }

}
