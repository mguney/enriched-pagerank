package com.gun3y.pagerank.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import com.gun3y.pagerank.utils.HtmlUtils;

public class HtmlToText {

    public static void main(String[] args) {
        String html = "<p>The <a href=\"/wiki/Kingdom_of_England\" title=\"Kingdom of England\">Kingdom of England</a> – which <a href=\"/wiki/Statute_of_Rhuddlan\" title=\"Statute of Rhuddlan\">after 1284</a> included Wales – was a <a href=\"/wiki/Sovereign_state\" title=\"Sovereign state\">sovereign state</a> until 1 May 1707, when the <a href=\"/wiki/Acts_of_Union_1707\" title=\"Acts of Union 1707\">Acts of Union</a> put into effect the terms agreed in the <a href=\"/wiki/Treaty_of_Union\" title=\"Treaty of Union\">Treaty of Union</a> the previous year, resulting in a political union with the <a href=\"/wiki/Kingdom_of_Scotland\" title=\"Kingdom of Scotland\">Kingdom of Scotland</a> to create the <a href=\"/wiki/Kingdom_of_Great_Britain\" title=\"Kingdom of Great Britain\">Kingdom of Great Britain</a>.<sup id=\"cite_ref-10\" class=\"reference\"><a href=\"#cite_note-10\"><span>[</span>9<span>]</span></a></sup><sup id=\"cite_ref-11\" class=\"reference\"><a href=\"#cite_note-11\"><span>[</span>10<span>]</span></a></sup> In 1801, Great Britain was united with the <a href=\"/wiki/Kingdom_of_Ireland\" title=\"Kingdom of Ireland\">Kingdom of Ireland</a> through another <a href=\"/wiki/Act_of_Union_1800\" title=\"Act of Union 1800\" class=\"mw-redirect\">Act of Union</a> to become the <a href=\"/wiki/United_Kingdom_of_Great_Britain_and_Ireland\" title=\"United Kingdom of Great Britain and Ireland\">United Kingdom of Great Britain and Ireland</a>. In 1922 the <a href=\"/wiki/Irish_Free_State\" title=\"Irish Free State\">Irish Free State</a> seceded from the United Kingdom, leading to the latter being <a href=\"/wiki/Royal_and_Parliamentary_Titles_Act_1927\" title=\"Royal and Parliamentary Titles Act 1927\">renamed</a> the United Kingdom of Great Britain and Northern Ireland.</p>";
        String baseUrl = "http://en.wikipedia.org/";
        Document doc = Jsoup.parse(html, baseUrl);

        HtmlToText formatter = new HtmlToText();
        List<LineItem> lines = formatter.getLines(doc);
        for (LineItem lineItem : lines) {
            System.out.println(lineItem.line);
            System.out.println(lineItem.urls);
        }
    }

    public List<LineItem> getLines(Element element) {
        FormattingVisitor formatter = new FormattingVisitor();
        NodeTraversor traversor = new NodeTraversor(formatter);
        traversor.traverse(element);
        return formatter.lines;
    }

    public String getText(List<LineItem> lines) {
        StringBuilder builder = new StringBuilder();
        if (lines != null) {
            for (LineItem lineItem : lines) {
                builder.append(lineItem.line).append("\n");
            }
        }
        return builder.toString();
    }

    // the formatting rules, implemented in a breadth-first DOM traverse
    private class FormattingVisitor implements NodeVisitor {

        List<LineItem> lines = new ArrayList<LineItem>();

        Map<String, String> urlMap = new HashMap<String, String>();

        StringBuilder accLine = new StringBuilder();

        Stack<Integer> stack = new Stack<Integer>();

        @Override
        public void head(Node node, int depth) {
            String name = node.nodeName();

            if (node instanceof TextNode) {
                String text = StringEscapeUtils.unescapeHtml4(((TextNode) node).text()).trim();
                if (StringUtils.isNotBlank(text)) {
                    this.accLine.append(text).append(" ");
                }
            }
            else if (StringUtil.in(name, "li", "meta", "body", "html", "link", "table", "tr", "th", "td", "tbody")
                    && this.accLine.length() > 0) {
                this.lines.add(new LineItem(this.accLine.toString().trim(), this.urlMap));
                this.accLine = new StringBuilder();
                this.urlMap = new HashMap<String, String>();
                this.stack = new Stack<Integer>();
            }
            else if (this.checkUrl(node)) {
                this.accLine.append("\"");
                this.stack.push(this.accLine.length());
            }
        }

        // hit when all of the node's children (if any) have been visited
        @Override
        public void tail(Node node, int depth) {
            String name = node.nodeName();

            if (StringUtil.in(name, "br", "h1", "h2", "h3", "h4", "h5", "li", "p", "li", "meta", "body", "html", "link", "table", "tr",
                    "th", "td", "tbody") && this.accLine.length() > 0) {
                this.lines.add(new LineItem(this.accLine.toString().trim(), this.urlMap));
                this.accLine = new StringBuilder();
                this.urlMap = new HashMap<String, String>();
                this.stack = new Stack<Integer>();
            }
            else if (this.checkUrl(node) && !this.stack.isEmpty()) {

                Integer index = this.stack.pop();

                if (index == this.accLine.length()) {
                    this.accLine.replace(this.accLine.length() - 1, this.accLine.length(), "");
                }
                else {
                    String absUrl = node.absUrl("href");
                    String linkText = this.accLine.substring(index).trim();

                    if (linkText.length() < 2) {
                        this.accLine.replace(index - 1, index, "");
                    }
                    else {

                        this.urlMap.put(linkText, absUrl);
                        this.accLine.replace(this.accLine.length() - 1, this.accLine.length(), "");
                        this.accLine.append("\" ");
                    }
                }

            }
        }

        private boolean checkUrl(Node node) {

            if (!node.nodeName().equals("a")) {
                return false;
            }

            String absUrl = node.absUrl("href");

            return HtmlUtils.checkUrl(absUrl);
        }

        //
        // @Override
        // public String toString() {
        // String content = this.accum.toString();
        // return content.replaceAll("(\r?\n)+", "#newline#").replaceAll("\\s+",
        // " ")
        // .replaceAll("(#newline#)*( )?(#newline#)( )?(#newline#)*",
        // "\n").trim();
        // }
    }
}
