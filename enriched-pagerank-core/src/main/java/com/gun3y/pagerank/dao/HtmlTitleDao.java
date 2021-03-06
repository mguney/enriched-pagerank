package com.gun3y.pagerank.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;

import com.gun3y.pagerank.entity.HtmlTitle;
import com.gun3y.pagerank.utils.HibernateUtils;
import com.gun3y.pagerank.utils.LangUtils;

public class HtmlTitleDao {

    public HtmlTitleDao() {

    }

    public synchronized void addHtmlTitle(HtmlTitle htmlTitle) {
        if (htmlTitle == null || StringUtils.isBlank(htmlTitle.getStemmedTitle()) || StringUtils.isBlank(htmlTitle.getUrl())) {
            return;
        }

        this.executeInsert(htmlTitle);
    }

    @SuppressWarnings("unchecked")
    public Set<HtmlTitle> getHtmlTitleSet() {
        return new HashSet<HtmlTitle>((List<HtmlTitle>) this.executeSelect("from HtmlTitle"));
    }

    public long count() {
        return this.executeCount("select count(*) from HtmlTitle");
    }

    public synchronized void removeAll() {
        this.executeSqlQuery("truncate table html_title");
    }

    public static void main(String[] args) {
        HtmlTitleDao dao = new HtmlTitleDao();
        dao.findHtmlTitleByTitle("pass a category by parameter <includeonly> -lsb- -lsb- category :{ -lcb- -lcb- cat | default -rcb- -rcb- -rcb- -rsb- -rsb- </includeonly> or <includeonly> -lcb- -lcb- -lcb- cat | -lsb- -lsb- category : default -rsb- -rsb- -rcb- -rcb- -rcb- </includeonly>");

        HibernateUtils.shutdown();
    }

    public List<HtmlTitle> findHtmlTitleByTitle(String title) {
        if (StringUtils.isBlank(title)) {
            return Collections.emptyList();
        }
        String query = "select ht_title as stemmedTitle, ht_url as url from html_title where instr('" + LangUtils.escapeSql(title)
                + "', ht_title) and ht_title in (select distinct lt_rel from link_tuple where lt_link_type = 1);";

        return this.executeSqlSelect(query);
    }

    public synchronized int removeDuplicates(int minOccurs) {
        StringBuilder builder = new StringBuilder();
        builder.append("delete a from html_title as a join ")
                .append("(SELECT ht_title FROM html_title group by ht_title having count(*) > " + minOccurs + ") as b ")
                .append("on b.ht_title= a.ht_title;");
        return this.executeSqlQuery(builder.toString());
    }

    private void executeInsert(HtmlTitle htmlTitle) {
        Session session = HibernateUtils.getCurrentSession();
        try {
            session.getTransaction().begin();

            session.save(htmlTitle);

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

    @SuppressWarnings("unchecked")
    private List<HtmlTitle> executeSqlSelect(String query) {
        Session session = HibernateUtils.getCurrentSession();
        List<HtmlTitle> retults;
        try {
            session.getTransaction().begin();
            retults = session.createSQLQuery(query).setResultTransformer(Transformers.aliasToBean(HtmlTitle.class)).list();
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
