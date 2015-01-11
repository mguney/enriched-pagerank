package com.gun3y.pagerank.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.gun3y.pagerank.entity.graph.LinkType;

public class LinkStorage {

    private List<LinkTuple> explicitLinks = new LinkedList<LinkTuple>();

    private List<LinkTuple> implicitLinks = new LinkedList<LinkTuple>();

    private List<LinkTuple> semanticLinks = new LinkedList<LinkTuple>();

    public void reduceLinks(int minOccurs) {
        this.reduceLinks(this.implicitLinks, minOccurs);
        this.reduceLinks(this.semanticLinks, minOccurs);
    }

    private void reduceLinks(List<LinkTuple> linkTuples, int minOccurs) {

        final Map<String, Integer> counts = new HashMap<String, Integer>();
        for (LinkTuple linkTuple : linkTuples) {
            if (StringUtils.isNotBlank(linkTuple.rel)) {
                Integer co = counts.get(linkTuple.rel);
                if (co == null) {
                    counts.put(linkTuple.rel, 1);
                }
                else {
                    counts.put(linkTuple.rel, co + 1);
                }

            }
        }

        final Set<String> cSet = new HashSet<String>();
        for (Entry<String, Integer> entry : counts.entrySet()) {
            String key = entry.getKey();
            Integer count = entry.getValue();

            if (count < minOccurs) {
                cSet.add(key);
            }
        }

        linkTuples.removeIf(new Predicate<LinkTuple>() {
            @Override
            public boolean test(LinkTuple t) {
                return cSet.contains(t.rel);
            }
        });

    }

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
