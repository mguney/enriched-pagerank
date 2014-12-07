package com.gun3y.pagerank.algorithm;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.tartarus.snowball.ext.PorterStemmer;

import com.gun3y.pagerank.utils.LangUtils;

public class WordAnalyzer {

    public static void main(String[] args) throws Exception {
        StopAnalyzer stopAnalyzer = new StopAnalyzer();
        PorterStemmer porterStemmer = new PorterStemmer();

        System.out.println(LangUtils.extractStemmedWords(FileUtils.readFileToString(new File(
                "C:\\Users\\Mustafa Guney\\Desktop\\agreement-chato.txt"))));
    }
}
