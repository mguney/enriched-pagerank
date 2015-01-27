package com.gun3y.pagerank.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.entity.LinkTuple;
import com.gun3y.pagerank.entity.LinkType;
import com.gun3y.pagerank.utils.DBUtils;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityJoin;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.ForwardCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;

public class LinkTupleDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkTupleDao.class);

    private static final String LINKTUPLE_STORE = "LinkTupleStore";

    private static final String LINKTUPLE_COUNT_DB = "LinkTupleCountDB";

    private Database linkTupleCountDB = null;

    private StoredMap<String, Integer> linkTupleCountMap;

    private Environment env;

    private StoredClassCatalog catalog;

    private PrimaryIndex<Integer, LinkTuple> linkTupleById;

    private SecondaryIndex<String, Integer, LinkTuple> linkTupleByFrom;

    private SecondaryIndex<String, Integer, LinkTuple> linkTupleByTo;

    private SecondaryIndex<String, Integer, LinkTuple> linkTupleByRel;

    private SecondaryIndex<LinkType, Integer, LinkTuple> linkTupleByLinkType;

    private EntityStore linkTupleStore;

    public LinkTupleDao(Environment env) {

        this.env = env;

        DatabaseConfig dbConfig = new DatabaseConfig();

        // db will be created if not exits
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);

        Database catalogDb = this.env.openDatabase(null, EnhancedHtmlPageDao.CLASS_CATALOG, dbConfig);
        this.catalog = new StoredClassCatalog(catalogDb);

        this.linkTupleCountDB = this.env.openDatabase(null, LINKTUPLE_COUNT_DB, dbConfig);

        this.linkTupleCountMap = new StoredMap<String, Integer>(this.linkTupleCountDB,
                new SerialBinding<String>(this.catalog, String.class), new SerialBinding<Integer>(this.catalog, Integer.class), true);

        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);
        storeConfig.setTransactional(true);

        this.linkTupleStore = new EntityStore(env, LINKTUPLE_STORE, storeConfig);
        this.linkTupleById = this.linkTupleStore.getPrimaryIndex(Integer.class, LinkTuple.class);
        this.linkTupleByFrom = this.linkTupleStore.getSecondaryIndex(this.linkTupleById, String.class, "from");
        this.linkTupleByTo = this.linkTupleStore.getSecondaryIndex(this.linkTupleById, String.class, "to");
        this.linkTupleByRel = this.linkTupleStore.getSecondaryIndex(this.linkTupleById, String.class, "rel");
        this.linkTupleByLinkType = this.linkTupleStore.getSecondaryIndex(this.linkTupleById, LinkType.class, "linkType");

    }

    public static void main(String[] args) {
        Environment env = DBUtils.newEnvironment("test-linktupledao2");
        LinkTupleDao dao = new LinkTupleDao(env);
        Random random = new Random();
        StopWatch timer = new StopWatch();
        // dao.removeAll();
        System.out.println(dao.count());
        timer.start();
        int bound = 50;
        for (int i = 0; i < 1000000; i++) {
            LinkTuple addLinkTuple = dao.addLinkTuple(new LinkTuple("from" + random.nextInt(bound), LinkType.values()[random.nextInt(3)],
                    "to" + random.nextInt(bound), "rel" + random.nextInt(bound)));

            if (addLinkTuple != null) {
                System.out.println(addLinkTuple.getLinkTupleId());
            }
        }
        timer.stop();
        System.out.println("add ends " + timer.getTime());

        timer.reset();
        timer.start();
        System.out.println(dao.count(new LinkTuple("from1", LinkType.ExplicitLink, "to1", "rel1")));
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
        env.close();
    }

    public LinkTuple addLinkTuple(LinkTuple linkTuple) {
        if (linkTuple == null || !linkTuple.validate()) {
            return null;
        }

        LinkType linkType = linkTuple.getLinkType();

        if (linkType == LinkType.ImplicitLink || linkType == LinkType.SemanticLink) {
            String uniqueKey = LinkTuple.toUniqueKey(linkTuple);

            if (this.linkTupleCountMap.containsKey(uniqueKey)) {
                this.linkTupleCountMap.put(uniqueKey, this.linkTupleCountMap.get(uniqueKey) + 1);
            }
            else {
                this.linkTupleCountMap.put(uniqueKey, 1);
            }
        }

        return this.linkTupleById.put(linkTuple);
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

    public long count() {
        return this.linkTupleById.count();
    }

    public int count(LinkTuple linkTuple) {
        if (linkTuple == null) {
            return -1;
        }
        EntityJoin<Integer, LinkTuple> entityJoin = new EntityJoin<Integer, LinkTuple>(this.linkTupleById);
        entityJoin.addCondition(this.linkTupleByFrom, linkTuple.getFrom());
        entityJoin.addCondition(this.linkTupleByTo, linkTuple.getTo());
        entityJoin.addCondition(this.linkTupleByRel, linkTuple.getRel());
        entityJoin.addCondition(this.linkTupleByLinkType, linkTuple.getLinkType());

        ForwardCursor<Integer> keys = entityJoin.keys();
        int count = 0;
        while (true) {
            Integer next = keys.next();
            if (next == null) {
                break;
            }
            count++;
        }

        keys.close();

        return count;
    }

    private int removeLinkTuple(LinkTuple linkTuple) {
        if (linkTuple == null) {
            return -1;
        }
        EntityJoin<Integer, LinkTuple> entityJoin = new EntityJoin<Integer, LinkTuple>(this.linkTupleById);
        entityJoin.addCondition(this.linkTupleByFrom, linkTuple.getFrom());
        entityJoin.addCondition(this.linkTupleByTo, linkTuple.getTo());
        entityJoin.addCondition(this.linkTupleByRel, linkTuple.getRel());
        entityJoin.addCondition(this.linkTupleByLinkType, linkTuple.getLinkType());
        ForwardCursor<Integer> keys = entityJoin.keys();

        List<Integer> removeList = new ArrayList<Integer>();
        while (true) {
            Integer next = keys.next();
            if (next == null) {
                break;
            }
            removeList.add(next);
        }

        keys.close();

        return this.removeLinkTuple(removeList);
    }

    private int removeLinkTuple(Collection<Integer> keyIds) {
        int count = 0;
        for (Integer key : keyIds) {
            boolean delete = this.linkTupleById.delete(key);
            count++;
            if (!delete) {
                count--;
                LOGGER.error("LinkTuple ({}) couldn't removed!", key);
            }
        }

        return count;
    }

    public ForwardCursor<Integer> findLinkTuple(LinkTuple linkTuple) {
        if (linkTuple == null) {
            return null;
        }
        EntityJoin<Integer, LinkTuple> entityJoin = new EntityJoin<Integer, LinkTuple>(this.linkTupleById);
        entityJoin.addCondition(this.linkTupleByFrom, linkTuple.getFrom());
        entityJoin.addCondition(this.linkTupleByTo, linkTuple.getTo());
        entityJoin.addCondition(this.linkTupleByRel, linkTuple.getRel());
        entityJoin.addCondition(this.linkTupleByLinkType, linkTuple.getLinkType());

        return entityJoin.keys();
    }

    public int applySameUrlFilter(LinkType linkType) {
        if (linkType == null) {
            return -1;
        }

        Set<Integer> removeList = new HashSet<Integer>();

        Set<String> titleSet = this.findAllTitles(linkType, removeList);

        Set<String> removeTitleList = this.findRemovalTitles(linkType, titleSet);

        this.updateRemoveList(linkType, removeTitleList, removeList);

        return this.removeLinkTuple(removeList);
    }

    private Set<String> findAllTitles(LinkType linkType, Set<Integer> removeList) {
        // Find all titles
        EntityCursor<LinkTuple> linkTupleCursor = this.linkTupleByLinkType.entities(linkType, true, linkType, true);
        Set<String> titleSet = new HashSet<String>();
        while (true) {
            LinkTuple nextLinkTuple = linkTupleCursor.next();
            if (nextLinkTuple == null) {
                break;
            }
            String title = nextLinkTuple.getRel();
            if (StringUtils.isBlank(title)) {
                removeList.add(nextLinkTuple.getLinkTupleId());
            }

            titleSet.add(title);
        }
        return titleSet;
    }

    private Set<String> findRemovalTitles(LinkType linkType, Set<String> titleSet) {

        Set<String> removeTitleList = new HashSet<String>();

        for (String title : titleSet) {
            EntityJoin<Integer, LinkTuple> entityJoin = new EntityJoin<Integer, LinkTuple>(this.linkTupleById);
            entityJoin.addCondition(this.linkTupleByRel, title);
            entityJoin.addCondition(this.linkTupleByLinkType, linkType);

            ForwardCursor<LinkTuple> joinedLinkTupleCursor = entityJoin.entities();

            LinkTuple accLinkTuple = joinedLinkTupleCursor.next();

            if (accLinkTuple == null) {
                continue;
            }

            while (true) {
                LinkTuple tempLinkTuple = joinedLinkTupleCursor.next();

                if (tempLinkTuple == null) {
                    break;
                }

                // It shouldn't be same link tuple
                boolean overallCheck = tempLinkTuple.getLinkTupleId() != accLinkTuple.getLinkTupleId();

                // should have same title
                overallCheck &= title.equals(tempLinkTuple.getRel());

                // should have same link type
                overallCheck &= tempLinkTuple.getLinkType() == linkType;

                // shouldn't have same to url
                overallCheck &= !tempLinkTuple.getTo().equals(accLinkTuple.getTo());

                if (overallCheck) {
                    removeTitleList.add(title);
                    break;
                }

            }
            joinedLinkTupleCursor.close();
        }

        return removeTitleList;
    }

    private void updateRemoveList(LinkType linkType, Set<String> removeTitleList, Set<Integer> removeList) {
        for (String title : removeTitleList) {
            EntityJoin<Integer, LinkTuple> entityJoin = new EntityJoin<Integer, LinkTuple>(this.linkTupleById);
            entityJoin.addCondition(this.linkTupleByRel, title);
            entityJoin.addCondition(this.linkTupleByLinkType, linkType);

            ForwardCursor<Integer> joinedKeyCursor = entityJoin.keys();

            while (true) {
                Integer tempKey = joinedKeyCursor.next();

                if (tempKey == null) {
                    break;
                }

                removeList.add(tempKey);
            }
            joinedKeyCursor.close();
        }
    }

    public int applyMinCountFilter(LinkType linkType, int minOccurs) {
        if (linkType == null || minOccurs < 1) {
            return -1;
        }
        List<String> removeList = new ArrayList<String>();
        Set<Entry<String, Integer>> entrySet = this.linkTupleCountMap.entrySet();
        for (Entry<String, Integer> entry : entrySet) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            if (value < minOccurs && key.contains(LinkTuple.KEY_SPLITTER + linkType + LinkTuple.KEY_SPLITTER)) {
                removeList.add(key);
            }
        }

        for (String key : removeList) {
            LinkTuple fromUniqueKey = LinkTuple.fromUniqueKey(key);
            this.linkTupleCountMap.remove(key);
            this.removeLinkTuple(fromUniqueKey);
        }

        return removeList.size();
    }

    public EntityCursor<LinkTuple> getLinkTupleIterator(LinkType linkType) {
        return this.linkTupleByLinkType.entities(linkType, true, linkType, true);
    }

    public void removeAll() {
        this.linkTupleById.sortedMap().clear();
        this.linkTupleCountMap.clear();
    }

    public void close() {
        try {
            this.linkTupleCountDB.close();
            this.linkTupleStore.close();
            this.catalog.close();
        }
        catch (DatabaseException e) {
            LOGGER.error(e.getMessage());
        }
    }

}
