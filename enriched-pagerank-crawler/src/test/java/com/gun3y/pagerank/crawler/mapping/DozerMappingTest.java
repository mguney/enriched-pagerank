package com.gun3y.pagerank.crawler.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.dozer.DozerBeanMapper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gun3y.pagerank.entity.html.HtmlData;
import com.gun3y.pagerank.entity.html.HtmlPage;
import com.gun3y.pagerank.entity.html.WebUrl;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class DozerMappingTest {

    private DozerBeanMapper mapper;

    private Page page;

    private HtmlPage htmlPage;

    private static final WebURL webURL = createWebURL("1");
    private static final WebUrl webUrl = createWebUrl("1");
    private static final boolean redirect = true;
    private static final String redirectedToUrl = "http://dozer.sourceforge.net/documentation/contextmapping.html";
    private static final int statusCode = 200;
    private static final String contentType = "content type";
    private static final String contentEncoding = "content encoding";
    private static final String contentCharset = "content charset";
    private static final String language = "language en";
    private static final String html = "html source";
    private static final String text = "txt txt txt";
    private static final String title = "title title title";
    private static final Map<String, String> metaTags = createMetaTags();
    private static final Set<WebURL> outgoingURLs = createOutgoinURLs();
    private static final Set<WebUrl> outgoingUrls = createOutgoinUrls();

    private static final String url = "http://dozer.sourceforge.net/documentation/deepmapping.html";
    private static final int docid = 2;
    private static final int parentDocid = 3;
    private static final String parentUrl = "http://dozer.sourceforge.net/documentation/gettingstarted.html";
    private static final short depth = (short) 3;
    private static final String domain = "sourceforge.net";
    private static final String subDomain = "dozer";
    private static final String path = "/documentation/deepmapping.html";
    private static final String anchor = "anchorrr";
    private static final byte priority = (byte) 2;
    private static final String tag = "tagssss";

    @BeforeClass
    public static final void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static final void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        this.mapper = new DozerBeanMapper();
        this.mapper.addMapping(DozerMappingTest.class.getClassLoader().getResourceAsStream("dozer-mapping.xml"));

        this.page = new Page(DozerMappingTest.webURL);
        this.page.setContentCharset(DozerMappingTest.contentCharset);
        this.page.setContentEncoding(DozerMappingTest.contentEncoding);
        this.page.setContentType(DozerMappingTest.contentType);
        this.page.setLanguage(DozerMappingTest.language);
        this.page.setRedirect(DozerMappingTest.redirect);
        this.page.setRedirectedToUrl(DozerMappingTest.redirectedToUrl);
        this.page.setStatusCode(DozerMappingTest.statusCode);
        this.page.setParseData(createParseData());

        this.htmlPage = new HtmlPage();
        this.htmlPage.setContentCharset(DozerMappingTest.contentCharset);
        this.htmlPage.setContentEncoding(DozerMappingTest.contentEncoding);
        this.htmlPage.setContentType(DozerMappingTest.contentType);
        this.htmlPage.setHtmlData(createHtmlData());
        this.htmlPage.setLanguage(DozerMappingTest.language);
        this.htmlPage.setRedirect(DozerMappingTest.redirect);
        this.htmlPage.setRedirectedToUrl(DozerMappingTest.redirectedToUrl);
        this.htmlPage.setStatusCode(DozerMappingTest.statusCode);
        this.htmlPage.setUrl(DozerMappingTest.webUrl);

    }

    private static HtmlData createHtmlData() {
        HtmlData htmlData = new HtmlData();
        htmlData.setHtml(DozerMappingTest.html);
        htmlData.setMetaTags(DozerMappingTest.metaTags);
        htmlData.setOutgoingUrls(DozerMappingTest.outgoingUrls);
        htmlData.setText(DozerMappingTest.text);
        htmlData.setTitle(DozerMappingTest.title);
        return htmlData;
    }

    private static ParseData createParseData() {
        HtmlParseData htmlParseData = new HtmlParseData();
        htmlParseData.setHtml(DozerMappingTest.html);
        htmlParseData.setText(DozerMappingTest.text);
        htmlParseData.setTitle(DozerMappingTest.title);
        htmlParseData.setMetaTags(DozerMappingTest.metaTags);
        htmlParseData.setOutgoingUrls(DozerMappingTest.outgoingURLs);
        return htmlParseData;
    }

    private static WebURL createWebURL(String key) {
        WebURL webURL = new WebURL();
        webURL.setURL(DozerMappingTest.url + key);
        webURL.setAnchor(DozerMappingTest.anchor + key);
        webURL.setDepth(DozerMappingTest.depth);
        webURL.setDocid(DozerMappingTest.docid);
        webURL.setParentDocid(DozerMappingTest.parentDocid);
        webURL.setParentUrl(DozerMappingTest.parentUrl + key);
        webURL.setPath(DozerMappingTest.path + key);
        webURL.setPriority(DozerMappingTest.priority);
        webURL.setTag(DozerMappingTest.tag + key);

        return webURL;
    }

    private static WebUrl createWebUrl(String key) {
        WebUrl webURL = new WebUrl();
        webURL.setUrl(DozerMappingTest.url + key);
        webURL.setAnchor(DozerMappingTest.anchor + key);
        webURL.setDepth(DozerMappingTest.depth);
        webURL.setDocid(DozerMappingTest.docid);
        webURL.setParentDocid(DozerMappingTest.parentDocid);
        webURL.setParentUrl(DozerMappingTest.parentUrl + key);
        webURL.setPath(DozerMappingTest.path + key);
        webURL.setPriority(DozerMappingTest.priority);
        webURL.setTag(DozerMappingTest.tag + key);
        webURL.setDomain(DozerMappingTest.domain);
        webURL.setSubDomain(DozerMappingTest.subDomain);

        return webURL;
    }

    private static Map<String, String> createMetaTags() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        return map;
    }

    private static Set<WebURL> createOutgoinURLs() {
        Set<WebURL> set = new HashSet<WebURL>();
        set.add(createWebURL("2"));
        set.add(createWebURL("3"));
        set.add(createWebURL("4"));
        return set;
    }

    private static Set<WebUrl> createOutgoinUrls() {
        Set<WebUrl> set = new HashSet<WebUrl>();
        set.add(createWebUrl("2"));
        set.add(createWebUrl("3"));
        set.add(createWebUrl("4"));
        return set;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void mapping() {
        HtmlPage mappedHtmlPage = this.mapper.map(this.page, HtmlPage.class);

        Assert.assertTrue(deepEquals(this.htmlPage, mappedHtmlPage));
    }

    private static boolean deepEquals(HtmlPage page1, HtmlPage page2) {
        if (page1 == page2) {
            return true;
        }

        if (page1 == null || page2 == null) {
            return false;
        }

        EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(page1.getStatusCode(), page2.getStatusCode());
        equalsBuilder.append(page1.getContentCharset(), page2.getContentCharset());
        equalsBuilder.append(page1.getContentEncoding(), page2.getContentEncoding());
        equalsBuilder.append(page1.getContentType(), page2.getContentType());
        equalsBuilder.append(page1.getLanguage(), page2.getLanguage());
        equalsBuilder.append(page1.getRedirectedToUrl(), page2.getRedirectedToUrl());
        equalsBuilder.append(page1.isRedirect(), page2.isRedirect());
        equalsBuilder.append(true, deepEquals(page1.getUrl(), page2.getUrl()));
        equalsBuilder.append(true, deepEquals(page1.getHtmlData(), page2.getHtmlData()));

        return equalsBuilder.isEquals();

    }

    private static boolean deepEquals(WebUrl webUrl1, WebUrl webUrl2) {
        if (webUrl1 == webUrl2) {
            return true;
        }

        if (webUrl1 == null || webUrl2 == null) {
            return false;
        }
        return EqualsBuilder.reflectionEquals(webUrl1, webUrl2, true);

    }

    private static boolean deepEquals(HtmlData htmlData1, HtmlData htmlData2) {
        if (htmlData1 == htmlData2) {
            return true;
        }

        if (htmlData1 == null || htmlData2 == null) {
            return false;
        }

        EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(htmlData1.getHtml(), htmlData2.getHtml());
        equalsBuilder.append(htmlData1.getText(), htmlData2.getText());
        equalsBuilder.append(htmlData1.getTitle(), htmlData2.getTitle());

        equalsBuilder.append(true, deepEquals(htmlData1.getMetaTags(), htmlData2.getMetaTags()));
        equalsBuilder.append(true, deepEquals(htmlData1.getOutgoingUrls(), htmlData2.getOutgoingUrls()));

        return true;

    }

    private static boolean deepEquals(Map<?, ?> map1, Map<?, ?> map2) {
        if (map1 == map2) {
            return true;
        }

        if (map1 == null || map2 == null) {
            return false;
        }

        if (map1.size() != map2.size()) {
            return false;
        }

        for (Entry<?, ?> entry : map1.entrySet()) {
            if (!map2.containsKey(entry.getKey())) {
                return false;
            }
            Object val = map2.get(entry.getKey());
            if (entry.getValue() == null && val == null) {
                continue;
            }
            else if (entry.getValue() != null && !entry.getValue().equals(val)) {
                return false;
            }
            else if (!val.equals(entry.getValue())) {
                return false;
            }
        }

        return true;

    }

    private static boolean deepEquals(Set<?> set1, Set<?> set2) {
        if (set1 == set2) {
            return true;
        }

        if (set1 == null || set2 == null) {
            return false;
        }

        if (set1.size() != set2.size()) {
            return false;
        }
        List<String> ar1 = new ArrayList<String>();
        for (Object obj : set1) {
            ar1.add(obj.toString());
        }

        List<String> ar2 = new ArrayList<String>();
        for (Object obj : set2) {
            ar2.add(obj.toString());
        }
        Collections.sort(ar1);
        Collections.sort(ar2);

        for (int i = 0; i < ar1.size(); i++) {
            if (!ar1.get(i).equals(ar2.get(i))) {
                return false;
            }
        }
        return true;
    }
}
