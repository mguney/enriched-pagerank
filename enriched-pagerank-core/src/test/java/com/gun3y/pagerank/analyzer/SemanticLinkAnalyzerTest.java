package com.gun3y.pagerank.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.gun3y.pagerank.common.HtmlToText;
import com.gun3y.pagerank.common.LineItem;
import com.gun3y.pagerank.dao.HtmlTitleDao;
import com.gun3y.pagerank.entity.LinkTuple;
import com.gun3y.pagerank.utils.HibernateUtils;

public class SemanticLinkAnalyzerTest {
    public static void main(String[] args) throws IOException {
        System.out.println(Calendar.getInstance().getTime());
        HtmlTitleDao htmlTitleDao = new HtmlTitleDao();
        SemanticLinkAnalyzer analyzer = new SemanticLinkAnalyzer(1, null, null, null);

        String html = FileUtils.readFileToString(new File(SemanticLinkAnalyzer.class.getClassLoader().getResource("wiki_sample.html")
                .getPath()));
        String baseUrl = "http://en.wikipedia.org/";

        Document doc = Jsoup.parse(html, baseUrl);
        HtmlToText formatter = new HtmlToText();
        List<LineItem> lines = formatter.getLines(doc);

        List<LinkTuple> analyze = null;// analyzer.analyze(lines);
        System.out.println(Calendar.getInstance().getTime());

        for (LinkTuple linkTuple : analyze) {
            System.out.println(linkTuple);
        }
        System.out.println(analyze.size());
        analyzer.shutdown();
        HibernateUtils.shutdown();
    }

    public static void main22(String[] args) throws IOException {
        System.out.println(Calendar.getInstance().getTime());
        HtmlTitleDao htmlTitleDao = new HtmlTitleDao();

        SemanticLinkAnalyzer analyzer = new SemanticLinkAnalyzer(1, null, null, null);

        String line = "Wikisource has original text related to this article: England portal";

        Map<String, String> map = new HashMap<String, String>();
        map.put("Wikisource", "http://url1");
        map.put("England portal", "http://url2");

        LineItem lineItem = new LineItem(line, map);

        List<LinkTuple> analyze = null;// analyzer.analyze(Arrays.asList(lineItem));
        // List<LinkTuple> analyze = analyzer.analyze(lines);
        System.out.println(Calendar.getInstance().getTime());

        for (LinkTuple linkTuple : analyze) {
            System.out.println(linkTuple);
        }
        System.out.println(analyze.size());
        analyzer.shutdown();
        HibernateUtils.shutdown();
    }
}
