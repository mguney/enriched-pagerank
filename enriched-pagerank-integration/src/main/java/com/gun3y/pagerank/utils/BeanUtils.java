package com.gun3y.pagerank.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.gun3y.pagerank.common.HtmlToText;
import com.gun3y.pagerank.entity.EnhancedHtmlPage;
import com.gun3y.pagerank.entity.html.HtmlData;
import com.gun3y.pagerank.entity.html.HtmlPage;
import com.gun3y.pagerank.entity.html.WebUrl;

public class BeanUtils {

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
        enhancedHtmlPage.setStemmedText(LangUtils.joinList(LangUtils.extractStemmedWords(text)));
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

}
