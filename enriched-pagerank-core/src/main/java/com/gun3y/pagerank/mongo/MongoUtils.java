package com.gun3y.pagerank.mongo;

import com.gun3y.pagerank.entity.graph.GraphNode;
import com.gun3y.pagerank.entity.html.HtmlPage;
import com.gun3y.pagerank.entity.html.WebUrl;

public class MongoUtils {

    public static GraphNode newGraphNode(HtmlPage htmlPage) {
        GraphNode graphNode = new GraphNode();

        WebUrl webUrl = htmlPage.getUrl();
        graphNode.setPageId(webUrl.getDocid());
        graphNode.setUrl(webUrl.getUrl());
        graphNode.setPageRank(0.15d);

        return graphNode;
    }

    public static HtmlPage newHtmlPage(WebUrl webUrl) {
        HtmlPage htmlPage = new HtmlPage();
        htmlPage.setUrl(webUrl);
        return htmlPage;
    }

}
