package com.gun3y.pagerank.web;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.entity.html.HtmlPage;
import com.gun3y.pagerank.entity.html.WebUrl;
import com.gun3y.pagerank.mongo.MongoManager;
import com.gun3y.pagerank.web.d3.Node;
import com.gun3y.pagerank.web.d3.NodeLink;
import com.gun3y.pagerank.web.d3.Stat;

public class HtmlPageVisualizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlPageVisualizer.class);

    public static void main(String[] args) throws UnknownHostException {
        MongoManager mongoManager = new MongoManager();
        mongoManager.init();
        final Map<Integer, Pair<Set<Integer>, Set<Integer>>> pageRanks = new HashMap<Integer, Pair<Set<Integer>, Set<Integer>>>();

        final Map<Integer, Pair<String, Integer>> idMap = new HashMap<Integer, Pair<String, Integer>>();

        Iterator<HtmlPage> htmlPageIterator = mongoManager.getHtmlPageIterator();

        while (htmlPageIterator.hasNext()) {
            HtmlPage htmlPage = htmlPageIterator.next();

            WebUrl webUrl = htmlPage.getUrl();

            int docid = webUrl.getDocid();
            idMap.put(docid, new MutablePair<String, Integer>(webUrl.getUrl(), webUrl.getParentDocid()));

            if (!pageRanks.containsKey(docid)) {
                pageRanks.put(docid, new MutablePair<Set<Integer>, Set<Integer>>(new HashSet<Integer>(), new HashSet<Integer>()));
            }

            Pair<Set<Integer>, Set<Integer>> values = pageRanks.get(docid);
            Set<Integer> outgoing = values.getValue();

            Set<WebUrl> outgoingUrls = htmlPage.getHtmlData().getOutgoingUrls();
            if (outgoingUrls != null) {
                for (WebUrl wUrl : outgoingUrls) {
                    if (wUrl.getDocid() > -1) {
                        int oDocid = wUrl.getDocid();

                        idMap.put(oDocid, new MutablePair<String, Integer>(wUrl.getUrl(), wUrl.getParentDocid()));

                        if (!pageRanks.containsKey(oDocid)) {
                            pageRanks.put(oDocid, new MutablePair<Set<Integer>, Set<Integer>>(new HashSet<Integer>(),
                                    new HashSet<Integer>()));
                        }

                        Pair<Set<Integer>, Set<Integer>> oValues = pageRanks.get(oDocid);
                        Set<Integer> incoming = oValues.getKey();
                        incoming.add(docid);

                        outgoing.add(oDocid);

                    }

                }
            }
        }

        List<Node> index = new ArrayList<Node>();
        for (Integer id : idMap.keySet()) {
            index.add(new Node(id));
        }

        // Map<String, NodeLink> nodeLinkMap = new HashMap<String, NodeLink>();

        List<Stat> stats = new ArrayList<Stat>();

        double max = Double.MIN_VALUE;

        double min = Double.MAX_VALUE;

        for (Entry<Integer, Pair<Set<Integer>, Set<Integer>>> entry : pageRanks.entrySet()) {
            Pair<Set<Integer>, Set<Integer>> value = entry.getValue();
            Pair<String, Integer> pair = idMap.get(entry.getKey());

            // extractNodeInfo(nodeLinkMap, entry, value);

            Stat stat = new Stat(entry.getKey(), value.getValue().size(), value.getValue().size());
            if (stat.value > 80d) {
                if (stat.value > max) {
                    max = stat.value;
                }

                if (stat.value < min) {
                    min = stat.value;
                }

                stats.add(stat);
            }

            LOGGER.info("Incoming: {},\t Outgoing: {},\t DocId: {}, \t ParentDocId: {},\t URL: {}", value.getKey().size(), value.getValue()
                    .size(), entry.getKey(), pair.getValue(), pair.getKey());
        }

        BarChartFrame mainFrame = new BarChartFrame(stats, min, max);
        mainFrame.setVisible(true);

        // Gson gson = (new GsonBuilder()).create();
        // System.out.println(gson.toJson(new NodeWrapper(index, new
        // ArrayList<NodeLink>(nodeLinkMap.values()))));
        //

        mongoManager.close();

    }

    private static void extractNodeInfo(Map<String, NodeLink> nodeLinkMap, Entry<Integer, Pair<Set<Integer>, Set<Integer>>> entry,
            Pair<Set<Integer>, Set<Integer>> value) {
        Set<Integer> incomings = value.getKey();
        for (Integer incomingId : incomings) {

            String nodeLinkId = getId(incomingId, entry.getKey());

            if (!nodeLinkMap.containsKey(nodeLinkId)) {
                nodeLinkMap.put(nodeLinkId, new NodeLink(new Node(incomingId), new Node(entry.getKey())));
            }

            NodeLink nodeLink = nodeLinkMap.get(nodeLinkId);
            nodeLink.right = true;
        }

        Set<Integer> outgoings = value.getValue();
        for (Integer outgoingId : outgoings) {

            String nodeLinkId = getId(entry.getKey(), outgoingId);

            if (!nodeLinkMap.containsKey(nodeLinkId)) {
                nodeLinkMap.put(nodeLinkId, new NodeLink(new Node(entry.getKey()), new Node(outgoingId)));
            }

            NodeLink nodeLink = nodeLinkMap.get(nodeLinkId);
            nodeLink.right = true;
        }
    }

    private static String getId(int source, int target) {
        if (source > target) {
            return source + "$" + target;
        }
        else {
            return target + "$" + source;
        }
    }
}
