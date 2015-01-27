package com.gun3y.pagerank.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import com.gun3y.pagerank.entity.LinkTuple;
import com.gun3y.pagerank.entity.LinkType;
import com.gun3y.pagerank.utils.DBUtils;
import com.sleepycat.je.Environment;

public class LinkTupleMemDao {

    private Map<LinkTuple, Integer> linkTupleCountMap = new HashMap<LinkTuple, Integer>();

    private List<LinkTuple> explicitLinks = new ArrayList<LinkTuple>();
    private List<LinkTuple> implicitLinks = new ArrayList<LinkTuple>();
    private List<LinkTuple> semanticLinks = new ArrayList<LinkTuple>();

    public static void main(String[] args) {
        Environment env = DBUtils.newEnvironment("test-mem");
        LinkTupleMemDao dao = new LinkTupleMemDao(env);
        Random random = new Random();
        StopWatch timer = new StopWatch();
        // dao.removeAll();
        System.out.println(dao.count());
        timer.start();
        int bound = 30;
        for (int i = 0; i < 2000000; i++) {
            dao.addLinkTuple(new LinkTuple("from" + random.nextInt(bound), LinkType.values()[random.nextInt(3)], "to"
                    + random.nextInt(bound), "rel" + random.nextInt(500)));

        }
        timer.stop();
        System.out.println("add ends " + timer.getTime());

        timer.reset();
        timer.start();
        LinkTuple linkTuple = new LinkTuple("from5", LinkType.ImplicitLink, "to5", "rel5");
        int count = dao.count(linkTuple);
        System.out.println(count);
        timer.stop();
        System.out.println("count ends " + timer.getTime());

        timer.reset();
        timer.start();
        int applyMinCountFilter = dao.applyMinCountFilter(LinkType.ImplicitLink, 5);
        timer.stop();
        System.out.println(applyMinCountFilter + "  time " + timer.getTime());

        timer.reset();
        timer.start();
        int applySameUrlFilter = dao.applySameUrlFilter(LinkType.ImplicitLink);
        timer.stop();
        System.out.println(applySameUrlFilter + "  time " + timer.getTime());

        dao.close();
    }

    public LinkTupleMemDao(Environment env) {
        File home = env.getHome();

    }

    public LinkTuple addLinkTuple(LinkTuple linkTuple) {
        if (linkTuple == null || !linkTuple.validate()) {
            return null;
        }

        LinkType linkType = linkTuple.getLinkType();

        if (linkType == LinkType.ImplicitLink || linkType == LinkType.SemanticLink) {

            if (this.linkTupleCountMap.containsKey(linkTuple)) {
                this.linkTupleCountMap.put(linkTuple, this.linkTupleCountMap.get(linkTuple) + 1);
            }
            else {
                this.linkTupleCountMap.put(linkTuple, 1);
            }
        }
        switch (linkTuple.getLinkType()) {
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

        return linkTuple;
    }

    public List<LinkTuple> addLinkTuple(Collection<LinkTuple> linkTuples) {
        if (linkTuples == null || linkTuples.isEmpty()) {
            return Collections.emptyList();
        }

        List<LinkTuple> retList = new ArrayList<LinkTuple>();
        for (LinkTuple linkTuple : linkTuples) {
            LinkTuple insertedLT = this.addLinkTuple(linkTuple);
            if (insertedLT != null) {
                retList.add(insertedLT);
            }
        }

        return retList;
    }

    public int count() {
        return this.explicitLinks.size() + this.implicitLinks.size() + this.semanticLinks.size();
    }

    public int count(LinkType linkType) {
        if (linkType == null) {
            return -1;
        }

        switch (linkType) {
            case ExplicitLink:
                return this.explicitLinks.size();
            case ImplicitLink:
                return this.implicitLinks.size();
            case SemanticLink:
                return this.semanticLinks.size();
            default:
                return -1;
        }
    }

    public int count(LinkTuple linkTuple) {
        if (linkTuple == null) {
            return -1;
        }

        switch (linkTuple.getLinkType()) {
            case ExplicitLink:
                return Collections.frequency(this.explicitLinks, linkTuple);
            case ImplicitLink:
                return Collections.frequency(this.implicitLinks, linkTuple);
            case SemanticLink:
                return Collections.frequency(this.semanticLinks, linkTuple);
            default:
                return -1;
        }
    }

    private void removeLinkTuple(Set<LinkTuple> tupleSet, LinkType linkType) {
        if (tupleSet == null || tupleSet.isEmpty()) {
            return;
        }

        switch (linkType) {
            case ExplicitLink:
                this.explicitLinks.removeIf(a -> tupleSet.contains(a));
                break;
            case ImplicitLink:
                this.implicitLinks.removeIf(a -> tupleSet.contains(a));
                break;
            case SemanticLink:
                this.semanticLinks.removeIf(a -> tupleSet.contains(a));
                break;
            default:
                return;
        }
    }

    public List<LinkTuple> findLinkTuple(LinkTuple linkTuple) {
        if (linkTuple == null) {
            return Collections.emptyList();
        }

        switch (linkTuple.getLinkType()) {
            case ExplicitLink:
                return this.explicitLinks.stream().filter(a -> a.equals(linkTuple)).collect(Collectors.<LinkTuple> toList());
            case ImplicitLink:
                this.implicitLinks.stream().filter(a -> a.equals(linkTuple)).collect(Collectors.<LinkTuple> toList());
            case SemanticLink:
                this.semanticLinks.stream().filter(a -> a.equals(linkTuple)).collect(Collectors.<LinkTuple> toList());
            default:
                return Collections.emptyList();
        }
    }

    public int applySameUrlFilter(LinkType linkType) {
        if (linkType == null) {
            return -1;
        }

        Set<String> titleSet = this.findAllTitles(linkType);

        Set<String> findRemovalTitles = this.findRemovalTitles(linkType, titleSet);

        return this.removeSameUrlTitles(linkType, findRemovalTitles);
    }

    private int removeSameUrlTitle(List<LinkTuple> tuples, Set<String> titles) {
        int count = tuples.size();

        boolean removeRet = tuples.removeIf(a -> titles.contains(a.getRel()));
        if (!removeRet) {
            return -1;
        }
        return count - tuples.size();
    }

    private int removeSameUrlTitles(LinkType linkType, Set<String> findRemovalTitles) {
        if (linkType == null) {
            return -1;
        }
        switch (linkType) {
            case ExplicitLink:
                return this.removeSameUrlTitle(this.explicitLinks, findRemovalTitles);

            case ImplicitLink:
                return this.removeSameUrlTitle(this.implicitLinks, findRemovalTitles);

            case SemanticLink:
                return this.removeSameUrlTitle(this.semanticLinks, findRemovalTitles);

            default:
                return -1;

        }

    }

    private Set<String> findAllTitles(LinkType linkType) {

        if (linkType == null) {
            return Collections.emptySet();
        }

        switch (linkType) {
            case ExplicitLink:
                return this.explicitLinks.stream().map(a -> a.getRel()).collect(Collectors.<String> toSet());
            case ImplicitLink:
                return this.implicitLinks.stream().map(a -> a.getRel()).collect(Collectors.<String> toSet());
            case SemanticLink:
                return this.semanticLinks.stream().map(a -> a.getRel()).collect(Collectors.<String> toSet());
            default:
                return Collections.emptySet();
        }

    }

    private Set<String> findRemovalTitles(LinkType linkType, Set<String> titleSet) {

        Set<String> removeSet = new HashSet<String>();

        for (String title : titleSet) {
            if (StringUtils.isBlank(title)) {
                continue;
            }

            List<LinkTuple> tempList = new ArrayList<LinkTuple>();

            switch (linkType) {
                case ExplicitLink:
                    tempList.addAll(this.explicitLinks.stream().filter(a -> title.equals(a.getRel()))
                            .collect(Collectors.<LinkTuple> toList()));
                    break;
                case ImplicitLink:
                    tempList.addAll(this.implicitLinks.stream().filter(a -> title.equals(a.getRel()))
                            .collect(Collectors.<LinkTuple> toList()));
                    break;
                case SemanticLink:
                    tempList.addAll(this.semanticLinks.stream().filter(a -> title.equals(a.getRel()))
                            .collect(Collectors.<LinkTuple> toList()));
                    break;
                default:
                    return removeSet;
            }

            if (tempList.size() < 2) {
                return removeSet;
            }

            Iterator<LinkTuple> iterator = tempList.iterator();

            LinkTuple accLinkTuple = iterator.next();

            if (accLinkTuple == null) {
                continue;
            }

            while (iterator.hasNext()) {
                LinkTuple tempLinkTuple = iterator.next();

                if (tempLinkTuple == null) {
                    break;
                }

                if (!tempLinkTuple.getTo().equals(accLinkTuple.getTo())) {
                    removeSet.add(title);
                    break;
                }

            }
        }

        return removeSet;
    }

    public int applyMinCountFilter(LinkType linkType, int minOccurs) {
        if (linkType == null || minOccurs < 1) {
            return -1;
        }
        Set<LinkTuple> removeList = new HashSet<LinkTuple>();
        Set<Entry<LinkTuple, Integer>> entrySet = this.linkTupleCountMap.entrySet();
        for (Entry<LinkTuple, Integer> entry : entrySet) {
            LinkTuple key = entry.getKey();
            Integer value = entry.getValue();

            if (value < minOccurs && key.getLinkType() == linkType) {
                removeList.add(key);
            }
        }

        int count = this.count(linkType);

        for (LinkTuple key : removeList) {
            this.linkTupleCountMap.remove(key);
        }

        this.removeLinkTuple(removeList, linkType);

        return count - this.count(linkType);
    }

    public List<LinkTuple> getLinkTuples(LinkType linkType) {
        switch (linkType) {
            case ExplicitLink:
                return this.explicitLinks;
            case ImplicitLink:
                return this.explicitLinks;
            case SemanticLink:
                return this.explicitLinks;
            default:
                return Collections.emptyList();
        }
    }

    public void removeAll() {
        this.explicitLinks.clear();
        this.implicitLinks.clear();
        this.semanticLinks.clear();
        this.linkTupleCountMap.clear();
    }

    public void close() {

    }
}
