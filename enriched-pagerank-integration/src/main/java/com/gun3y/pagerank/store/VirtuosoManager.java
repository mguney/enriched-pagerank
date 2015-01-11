package com.gun3y.pagerank.store;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jena.driver.VirtBulkUpdateHandler;
import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.gun3y.pagerank.entity.LinkType;
import com.gun3y.pagerank.entity.graph.GraphNode;
import com.gun3y.pagerank.helper.SparqlHelper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class VirtuosoManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirtuosoManager.class);

    private String db = "jdbc:virtuoso://localhost:1111";

    private String userName = "dba";

    private String password = "dba";

    private VirtGraph graph;

    public VirtuosoManager(String graphName) {
        super();
        this.graph = new VirtGraph(graphName, this.db, this.userName, this.password);
    }

    public void addGraphNode(GraphNode graphNode) {
        if (graphNode == null) {
            throw new IllegalArgumentException("GraphNode is mission");
        }

        List<Triple> triples = SparqlHelper.createGraphNodeTriples(graphNode);

        this.addTriplesAsBulk(triples);

        LOGGER.info("GraphNode Added: ID:{} URL:{}", graphNode.getPageId(), graphNode.getUrl());
    }

    public void addGraphEdge(GraphNode from, LinkType linkType, GraphNode to) {
        if (from == null || to == null || linkType == null) {
            throw new IllegalArgumentException("Adding new edge fails: arguments are missing!");
        }
        Node fromNode = this.findPageByUrl(from.getUrl());
        Node toNode = this.findPageByUrl(to.getUrl());

        if (fromNode == null || toNode == null) {
            throw new IllegalStateException("Adding new edge fails: Nodes not found on graph");
        }

        this.graph.add(new Triple(fromNode, SparqlHelper.createLinkPredicate(linkType), toNode));

        LOGGER.info("GraphEdge Added: {} --{}--> {}", from.getPageId(), linkType, to.getPageId());
    }

    private Node findPageByUrl(String url) {

        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url is missing!");
        }

        ExtendedIterator<Triple> iterator = this.graph.find(Node.ANY, NodeFactory.createURI(SparqlHelper.DATA_PROP_URL),
                NodeFactory.createLiteral(url));

        if (iterator.hasNext()) {
            Triple next = iterator.next();

            return next.getSubject();
        }

        return null;
    }

    public long getGraphNodeCount() {
        // TODO eksik
        return 0;
    }

    public void updateGraphNode(double basePR) {
        ExtendedIterator<Triple> iterator = this.graph.find(Node.ANY, NodeFactory.createURI(SparqlHelper.OBJECT_PROP_TYPE),
                NodeFactory.createURI(SparqlHelper.CLASS_PAGE));

        // TODO eksik
    }

    private void addTriplesAsBulk(Collection<Triple> triples) {
        if (triples == null || triples.isEmpty()) {
            return;
        }

        VirtBulkUpdateHandler bulkUpdateHandler = new VirtBulkUpdateHandler(this.graph);
        bulkUpdateHandler.add(triples.toArray(new Triple[triples.size()]));
    }

    public ResultSet executeSelectQuery(String query) {

        if (StringUtils.isBlank(query)) {
            throw new IllegalArgumentException("Sparql Query is missing!");
        }

        try {
            Query sparql = QueryFactory.create(query);

            VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, this.graph);

            return vqe.execSelect();
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }

        return null;
    }

    public boolean executeAskQuery(String query) {
        if (StringUtils.isBlank(query)) {
            throw new IllegalArgumentException("Sparql Query is missing!");
        }

        try {
            Query sparql = QueryFactory.create(query);

            VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, this.graph);

            return vqe.execAsk();
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        return false;
    }

    public AnonId executeAskAnonId(String query) {
        if (StringUtils.isBlank(query)) {
            throw new IllegalArgumentException("Sparql Query is missing!");
        }

        try {
            Query sparql = QueryFactory.create(query);

            VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(sparql, this.graph);

            ResultSet results = vqe.execSelect();
            while (results.hasNext()) {
                QuerySolution result = results.nextSolution();
                RDFNode node = result.get("node");
                if (node.isAnon()) {
                    return node.asResource().getId();
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }

        return null;
    }

}
