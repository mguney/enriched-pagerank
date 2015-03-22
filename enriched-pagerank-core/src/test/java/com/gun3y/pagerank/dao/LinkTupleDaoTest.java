package com.gun3y.pagerank.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.time.StopWatch;

import com.gun3y.pagerank.entity.LinkTuple;
import com.gun3y.pagerank.entity.LinkType;
import com.gun3y.pagerank.utils.HibernateUtils;

/**
 * Created by Mustafa on 01.03.2015.
 */
public class LinkTupleDaoTest {

    public static void main2(String[] args) {
        LinkTupleDao dao = new LinkTupleDao();
        try {

            Random random = new Random();
            StopWatch timer = new StopWatch();
            // dao.removeAll();
            System.out.println(dao.count());
            timer.start();
            int bound = 3;
            List<LinkTuple> lst = new ArrayList<LinkTuple>();
            for (int i = 0; i < 100; i++) {
                lst.add(new LinkTuple("from" + random.nextInt(bound), LinkType.values()[random.nextInt(3)], "to" + random.nextInt(bound),
                        "rel" + random.nextInt(bound)));

            }
            dao.addLinkTuple(lst);
            timer.stop();
            System.out.println("add ends " + timer.getTime());

            timer.reset();
            timer.start();
            LinkTuple linkTuple = new LinkTuple("from5", LinkType.ImplicitLink, "to5", "rel5");
            long count = dao.count(linkTuple);
            System.out.println(count);
            timer.stop();
            System.out.println("count ends " + timer.getTime());

            timer.reset();
            timer.start();
            long applyMinCountFilter = dao.applyMinCountFilter(LinkType.ImplicitLink, 5);
            timer.stop();
            System.out.println(applyMinCountFilter + "  time " + timer.getTime());

            timer.reset();
            timer.start();
            long applySameUrlFilter = dao.applySameUrlFilter(LinkType.ImplicitLink);
            timer.stop();
            System.out.println(applySameUrlFilter + "  time " + timer.getTime());
        }
        finally {
            HibernateUtils.shutdown();
        }

    }

    public static void main(String[] args) throws IOException {
        LinkTupleDao dao = new LinkTupleDao();
        try {
            Random random = new Random();
            StopWatch timer = new StopWatch();
            // dao.removeAll();
            System.out.println(dao.count());
            timer.start();
            int bound = 5;
            List<LinkTuple> lst = new ArrayList<LinkTuple>();
            for (int i = 0; i < 200; i++) {
                lst.add(new LinkTuple("from" + random.nextInt(bound), LinkType.values()[random.nextInt(3)], "to" + random.nextInt(bound),
                        "rel" + random.nextInt(bound)));
            }

            dao.addLinkTuple(lst);
            timer.stop();
            System.out.println("add ends " + timer.getTime());
        }
        finally {
            HibernateUtils.shutdown();
        }
    }
}
