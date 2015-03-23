package com.gun3y.pagerank.dao;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.ParameterMode;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.procedure.ProcedureCall;

import com.gun3y.pagerank.entity.HtmlTitle;
import com.gun3y.pagerank.entity.LinkTuple;
import com.gun3y.pagerank.entity.LinkType;
import com.gun3y.pagerank.utils.HibernateUtils;

public class LinkTupleDao {

    public LinkTupleDao() {
    }

    public synchronized LinkTuple addLinkTuple(LinkTuple linkTuple) {
        if (linkTuple == null || !linkTuple.validate()) {
            return null;
        }
        this.executeInsert(linkTuple);
        return linkTuple;
    }

    public synchronized void addLinkTuple(Collection<LinkTuple> linkTuples) {
        if (linkTuples == null || linkTuples.isEmpty()) {
            return;
        }

        this.executeBulkInsert(linkTuples);
    }

    public synchronized long count() {
        return this.executeCount("select count(*) from LinkTuple");
    }

    public synchronized long count(LinkType linkType) {
        if (linkType == null) {
            return -1;
        }

        return this.executeCount("select count(*) from LinkTuple where lt_link_type = " + linkType.ordinal());
    }

    public synchronized long count(LinkTuple linkTuple) {
        if (linkTuple == null) {
            return -1;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("select count(*) from LinkTuple where ").append("lt_from ='").append(linkTuple.getFrom()).append("' and ")
                .append("lt_to ='").append(linkTuple.getTo()).append("' and ").append("lt_link_type= ")
                .append(linkTuple.getLinkType().ordinal()).append(" and lt_rel = '")
        .append(StringUtils.replace(linkTuple.getRel(), "'", "''")).append("'");

        return this.executeCount(builder.toString());
    }

    public synchronized long applySameUrlFilter(LinkType linkType) {
        if (linkType == null) {
            return -1;
        }

        Session session = HibernateUtils.getCurrentSession();
        int retults;
        try {
            session.getTransaction().begin();

            ProcedureCall spCall = session.createStoredProcedureCall("APPLY_SAME_URL_FILTER");
            spCall.registerParameter("linkType", Integer.class, ParameterMode.IN).bindValue(linkType.ordinal());
            spCall.registerParameter("removedRows", Integer.class, ParameterMode.OUT);

            retults = (int) spCall.getOutputs().getOutputParameterValue("removedRows");

            session.getTransaction().commit();
        }
        catch (RuntimeException e) {
            session.getTransaction().rollback();
            throw e;
        }

        return retults;
    }

    public synchronized long applyMinCountFilter(LinkType linkType, int minOccurs) {
        if (linkType == null || minOccurs < 1) {
            return -1;
        }

        String query = "delete a from link_tuple as a join " + "(select lt_from, lt_to, lt_rel from link_tuple where lt_link_type = "
                + linkType.ordinal() + " group by lt_from, lt_to, lt_rel having count(*) < " + minOccurs + ") as b "
                + "on a.lt_from = b.lt_from and a.lt_to = b.lt_to and a.lt_rel = b.lt_rel;";

        return this.executeSqlQuery(query);
        // Session session =
        // HibernateUtils.getSessionFactory().getCurrentSession();
        // int retults;
        // try {
        // session.getTransaction().begin();
        //
        // ProcedureCall spCall =
        // session.createStoredProcedureCall("APPLY_MIN_COUNT_FILTER");
        // spCall.registerParameter("linkType", Integer.class,
        // ParameterMode.IN).bindValue(linkType.ordinal());
        // spCall.registerParameter("minOccurs", Integer.class,
        // ParameterMode.IN).bindValue(minOccurs);
        // spCall.registerParameter("removedRows", Integer.class,
        // ParameterMode.OUT);
        //
        // retults = (int)
        // spCall.getOutputs().getOutputParameterValue("removedRows");
        //
        // session.getTransaction().commit();
        // }
        // catch (RuntimeException e) {
        // session.getTransaction().rollback();
        // throw e;
        // }

        // return retults;
    }

    @SuppressWarnings("unchecked")
    public synchronized Set<HtmlTitle> findAllTitles() {

        Set<HtmlTitle> titleSet = new HashSet<HtmlTitle>();

        List<LinkTuple> tuples = (List<LinkTuple>) this.executeSelect("from LinkTuple where lt_link_type = "
                + LinkType.ImplicitLink.ordinal());

        tuples.forEach(a -> titleSet.add(new HtmlTitle(a.getRel(), a.getTo())));

        return titleSet;
    }

    /*
     * @SuppressWarnings("unchecked") public List<String> findAllVerbs() {
     * return (List<String>)
     * this.executeSelect("select distinct rel from LinkTuple where LT_LINK_TYPE = "
     * + LinkType.SemanticLink.ordinal()); }
     *
     * @SuppressWarnings("unchecked") public List<LinkTuple>
     * getLinkTuples(LinkType linkType) { return (List<LinkTuple>)
     * this.executeSelect("from LinkTuple where LT_LINK_TYPE =" +
     * linkType.ordinal()); }
     */
    public synchronized void removeAll() {
        this.executeSqlQuery("truncate table link_tuple");
        // this.executeSqlQuery("truncate table LINK_TUPLE_COUNT");
    }

    private void executeBulkInsert(Collection<LinkTuple> tuples) {
        StatelessSession statelessSession = HibernateUtils.getSessionFactory().openStatelessSession();

        try {
            statelessSession.beginTransaction();

            for (LinkTuple linkTuple : tuples) {
                if (linkTuple.validate()) {
                    statelessSession.insert(linkTuple);
                }
            }

            statelessSession.getTransaction().commit();
        }
        catch (RuntimeException e) {
            statelessSession.getTransaction().rollback();
            throw e;
        }
        statelessSession.close();
    }

    private void executeInsert(LinkTuple linkTuple) {
        Session session = HibernateUtils.getCurrentSession();
        try {
            session.getTransaction().begin();

            session.save(linkTuple);

            session.getTransaction().commit();
        }
        catch (RuntimeException e) {
            session.getTransaction().rollback();
            throw e;
        }
    }

    private int executeSqlQuery(String query) {
        Session session = HibernateUtils.getCurrentSession();
        int retults;
        try {
            session.getTransaction().begin();

            retults = session.createSQLQuery(query).executeUpdate();

            session.getTransaction().commit();
        }
        catch (RuntimeException e) {
            session.getTransaction().rollback();
            throw e;
        }

        return retults;
    }

    private List<?> executeSelect(String query) {
        Session session = HibernateUtils.getCurrentSession();
        List<?> retults;
        try {
            session.getTransaction().begin();

            retults = session.createQuery(query).list();

            session.getTransaction().commit();
        }
        catch (RuntimeException e) {
            session.getTransaction().rollback();
            throw e;
        }

        return retults;
    }

    private long executeCount(String query) {
        Session session = HibernateUtils.getCurrentSession();
        long count;
        try {
            session.getTransaction().begin();

            count = (long) session.createQuery(query).uniqueResult();

            session.getTransaction().commit();
        }
        catch (RuntimeException e) {
            session.getTransaction().rollback();
            throw e;
        }

        return count;
    }
}
