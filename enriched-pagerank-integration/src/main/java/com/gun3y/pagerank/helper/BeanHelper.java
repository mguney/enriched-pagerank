package com.gun3y.pagerank.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.gun3y.pagerank.common.HtmlToText;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.html.HtmlData;
import com.gun3y.pagerank.entity.html.HtmlPage;
import com.gun3y.pagerank.entity.html.WebUrl;
import com.gun3y.pagerank.utils.HtmlUtils;
import com.gun3y.pagerank.utils.LangUtils;

public class BeanHelper {

    private static final Pattern URL_FILTER = Pattern.compile(HtmlUtils.REGEX_HTML_PAGES);

    public static HtmlPage newHtmlPage(WebUrl webUrl) {
        HtmlPage htmlPage = new HtmlPage();
        htmlPage.setUrl(webUrl);
        return htmlPage;
    }

    public static EnhancedHtmlPage newEnhancedHtmlPage(HtmlPage htmlPage) {
        EnhancedHtmlPage enhancedHtmlPage = new EnhancedHtmlPage();
        WebUrl url = htmlPage.getUrl();
        HtmlData htmlData = htmlPage.getHtmlData();

        String html = htmlData.getHtml();
        String title = htmlData.getTitle();

        Document doc = Jsoup.parse(html, url.getUrl());
        String text = doc.text();

        HtmlToText formatter = new HtmlToText();
        enhancedHtmlPage.setLines(formatter.getLines(doc));

        enhancedHtmlPage.setHtml(html);

        enhancedHtmlPage.setTitle(title);
        enhancedHtmlPage.setStemmedText(LangUtils.joinList(LangUtils.extractStemmedWords(text)).toLowerCase(Locale.ENGLISH));
        enhancedHtmlPage.setUrl(url.getUrl());

        Set<String> urls = new HashSet<String>();
        Set<WebUrl> outgoingUrls = htmlData.getOutgoingUrls();
        if (outgoingUrls != null) {
            for (WebUrl webUrl : outgoingUrls) {
                if (!URL_FILTER.matcher(webUrl.getUrl().toLowerCase()).matches()) {
                    urls.add(webUrl.getUrl());
                }
            }
        }
        enhancedHtmlPage.setOutgoingUrls(urls);

        return enhancedHtmlPage;
    }

    public static EnhancedHtmlPage newEnhancedHtmlPage(String url, String html, String title, List<String> anchors) {
        EnhancedHtmlPage enhancedHtmlPage = new EnhancedHtmlPage();

        if (StringUtils.isBlank(html) || StringUtils.isBlank(url)) {
            return null;
        }

        Document doc = Jsoup.parse(html, url);
        String text = doc.text();

        HtmlToText formatter = new HtmlToText();
        enhancedHtmlPage.setLines(formatter.getLines(doc));

        enhancedHtmlPage.setHtml(html);

        enhancedHtmlPage.setTitle(title);
        enhancedHtmlPage.setStemmedText(LangUtils.joinList(LangUtils.extractStemmedWords(text)).toLowerCase(Locale.ENGLISH));
        enhancedHtmlPage.setUrl(url);

        if (anchors == null) {
            anchors = new ArrayList<String>();
        }
        enhancedHtmlPage.setAnchors(new HashSet<String>(anchors));

        return enhancedHtmlPage;
    }

}
