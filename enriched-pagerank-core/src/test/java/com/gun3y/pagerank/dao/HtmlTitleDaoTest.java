package com.gun3y.pagerank.dao;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gun3y.pagerank.utils.HibernateUtils;

public class HtmlTitleDaoTest {

    static HtmlTitleDao htmlTitleDao;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        htmlTitleDao = new HtmlTitleDao();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        HibernateUtils.shutdown();
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        Assert.assertTrue(htmlTitleDao.count() > -1);
    }

}
