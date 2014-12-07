package com.gun3y.pagerank.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class LangUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(LangUtils.class);

    public static final CharArraySet STOPWORDS_EN = new CharArraySet(20, true);

    private static StanfordCoreNLP PIPELINE;

    static {
        loadStopWords(STOPWORDS_EN);

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");

        PIPELINE = new StanfordCoreNLP(props, true);
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
                String lemma = token.get(LemmaAnnotation.class);

                if (StringUtils.isNotBlank(lemma) && lemma.length() > 1 && !STOPWORDS_EN.contains(lemma)) {
                    results.add(lemma);
                }
            }
        }

        return results;
    }

    // public static List<String> extractStemmedWords(String content) {
    // List<String> results = new ArrayList<String>();
    // if (StringUtils.isBlank(content)) {
    // return results;
    // }
    //
    // StringReader tReader = new StringReader(content);
    //
    // try {
    // EnglishAnalyzer englishAnalyzer = new EnglishAnalyzer(STOPWORDS_EN);
    // TokenStream tStream = englishAnalyzer.tokenStream("contents", tReader);
    // CharTermAttribute term = tStream.addAttribute(CharTermAttribute.class);
    // tStream.reset();
    //
    // while (tStream.incrementToken()) {
    // results.add(term.toString());
    // }
    //
    // tStream.end();
    // tStream.close();
    // englishAnalyzer.close();
    // }
    // catch (IOException e) {
    // LOGGER.error(e.getMessage());
    // }
    //
    // return results;
    // }

    private static void loadStopWords(CharArraySet set) {
        File stopwordFolder = getFile("stopwords");

        if (stopwordFolder.isDirectory()) {
            File[] files = stopwordFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("en.txt");
                }
            });

            if (files == null) {
                return;
            }
            for (File f : files) {
                try {
                    WordlistLoader.getWordSet(new FileReader(f), set);
                }
                catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
            }

        }
    }

    private static File getFile(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("FileName cannot be empty");
        }

        URL url = LangUtils.class.getClassLoader().getResource(fileName);
        return new File(url.getPath());
    }

    public static void main2(String[] args) {
        System.out.println(LangUtils.STOPWORDS_EN);
    }

    public static void main(String[] args) {

        String text = "For a case like this, moving to using a lemmatizer would not completely fix the problem because particular inflectional forms are used in particular collocations: a sentence with the words operate and system is not a good match for the query operating and system. Getting better value from term normalization depends more on pragmatic issues of word use than on formal issues of linguistic morphology.";
        Annotation document = PIPELINE.process(text);

        for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                String word = token.get(TextAnnotation.class);
                System.out.println("word: " + word);
                String lemma = token.get(LemmaAnnotation.class);
                System.out.println("lemmatized version :" + lemma);
            }
        }
    }
}
