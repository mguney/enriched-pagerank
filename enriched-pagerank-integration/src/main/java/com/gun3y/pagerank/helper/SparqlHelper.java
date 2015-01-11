package com.gun3y.pagerank.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.gun3y.pagerank.entity.LinkType;
import com.gun3y.pagerank.entity.graph.GraphNode;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;

public class SparqlHelper {

    public static final String PREFIX_PRS = "PREFIX prs: <http://www.gun3y.com/pagerank/schema/>";
    public static final String PREFIX_PRK = "PREFIX wrp: <http://www.gun3y.com/pagerank/knowledge/>";
    public static final String PREFIX_RDF = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";

    public static final String ONTOLOGY = "http://www.gun3y.com/ontology/pagerank";

    public static final String KNOWLEDGE_BASE = "http://www.gun3y.com/pagerank/knowledge/";
    public static final String SCHEMA_BASE = "http://www.gun3y.com/pagerank/schema/";

    public static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
    public static final String OWL = "http://www.w3.org/2002/07/owl#";
    public static final String XSD = "http://www.w3.org/2001/XMLSchema#";

    public static final String OBJECT_PROP_EXPLICIT = SCHEMA_BASE + "givesAnExplicitLink";

    public static final String OBJECT_PROP_IMPLICIT = SCHEMA_BASE + "givesAnImplicitLink";

    public static final String OBJECT_PROP_SEMANTIC = SCHEMA_BASE + "givesASemanticLink";

    public static final String OBJECT_PROP_INFERRED = SCHEMA_BASE + "givesAnInferredLink";

    public static final String OBJECT_PROP_TYPE = RDF + "type";

    public static final String CLASS_PAGE = SCHEMA_BASE + "Page";

    public static final String DATA_PROP_URL = SCHEMA_BASE + "url";

    public static final String DATA_PROP_PAGE_RANK = SCHEMA_BASE + "pageRank";

    public static Node createLinkPredicate(LinkType linkType) {

        switch (linkType) {
            case ExplicitLink:
                return NodeFactory.createURI(OBJECT_PROP_EXPLICIT);
            case ImplicitLink:
                return NodeFactory.createURI(OBJECT_PROP_IMPLICIT);
            case SemanticLink:
                return NodeFactory.createURI(OBJECT_PROP_SEMANTIC);
            case InferredLink:
                return NodeFactory.createURI(OBJECT_PROP_INFERRED);
            default:
                return NodeFactory.createURI(OBJECT_PROP_EXPLICIT);
        }
    }

    public static List<Triple> createGraphNodeTriples(GraphNode graphNode) {

        List<Triple> triples = new ArrayList<Triple>();

        if (graphNode == null) {
            return triples;
        }

        Node gNode = NodeFactory.createAnon(new AnonId(KNOWLEDGE_BASE + "Page/" + UUID.randomUUID().toString()));
        triples.add(new Triple(gNode, NodeFactory.createURI(OBJECT_PROP_TYPE), NodeFactory.createURI(CLASS_PAGE)));
        triples.add(new Triple(gNode, NodeFactory.createURI(DATA_PROP_PAGE_RANK), NodeFactory.createLiteral(graphNode.getPageRank() + "")));
        triples.add(new Triple(gNode, NodeFactory.createURI(DATA_PROP_URL), NodeFactory.createLiteral(graphNode.getUrl())));

        return triples;
    }

}
