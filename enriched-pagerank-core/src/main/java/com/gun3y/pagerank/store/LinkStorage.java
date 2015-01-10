package com.gun3y.pagerank.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.gun3y.pagerank.entity.graph.LinkType;

public class LinkStorage {

    private List<LinkTuple> explicitLinks = new ArrayList<LinkTuple>();

    private List<LinkTuple> implicitLinks = new ArrayList<LinkTuple>();

    private List<LinkTuple> semanticLinks = new ArrayList<LinkTuple>();

    public synchronized void addLink(String from, LinkType linkType, String to, String rel) {
        this.addLink(new LinkTuple(from, linkType, to, rel));
    }

    public synchronized void addLink(String from, LinkType linkType, String to) {
        this.addLink(new LinkTuple(from, linkType, to, StringUtils.EMPTY));
    }

    public synchronized void addLink(Collection<LinkTuple> linkTuples) {
        if (linkTuples == null) {
            return;
        }

        for (LinkTuple linkTuple : linkTuples) {
            this.addLink(linkTuple);
        }
    }

    public synchronized void addLink(LinkTuple linkTuple) {
        if (linkTuple == null || !linkTuple.validate()) {
            return;
        }
        switch (linkTuple.linkType) {
            case ExplicitLink:
                this.explicitLinks.add(linkTuple);
                break;
            case ImplicitLink:
                this.implicitLinks.add(linkTuple);
                break;
            case SemanticLink:
                this.semanticLinks.add(linkTuple);
                break;
            default:
                break;
        }
    }
}
