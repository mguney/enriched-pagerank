package com.gun3y.pagerank.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class LangUtils {

    private static StanfordCoreNLP PIPELINE;;

    static {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");

        PIPELINE = new StanfordCoreNLP(props, true);
    }

    public static void main(String[] args) {
        String query = "they're keyboard ' \"";

        List<String> extractStemmedWords = extractStemmedWords(query);

        System.out.println(extractStemmedWords);
    }

    public static String joinList(List<String> list) {
        if (list == null) {
            return StringUtils.EMPTY;
        }

        StringBuilder builder = new StringBuilder(list.size());

        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            builder.append(next);

            if (iterator.hasNext()) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    public static List<String> extractStemmedWords(String content) {
        List<String> results = new ArrayList<String>();
        if (StringUtils.isBlank(content)) {
            return results;
        }

        Annotation document = PIPELINE.process(content);

        for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

                String lemma = token.lemma();
                // TODO: lemma.length() > 1 bu koşulu kaldırdım
                if (StringUtils.isNotBlank(lemma)) {
                    results.add(lemma);
                }
            }
        }

        return results;
    }

    public static String escapeSql(String text) {
        if (text == null) {
            return StringUtils.EMPTY;
        }

        return text.replace("'", "").replace("{", "");
    }

}
