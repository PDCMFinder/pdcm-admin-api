package org.cancermodels.suggestions.index;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class LuceneTester {
  public static void main(String[] args) {
    LuceneTester tester;

    tester = new LuceneTester();

    try {
      tester.displayTokenUsingSimpleAnalyzer();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void displayTokenUsingSimpleAnalyzer() throws IOException {
    String text =
        "Lucene is simple YET , .  powerful java based search library.";
    Analyzer analyzer = new StandardAnalyzer();
    TokenStream tokenStream = analyzer.tokenStream(
        "contents", new StringReader(text));
    CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
    tokenStream.reset();
    while(tokenStream.incrementToken()) {
      System.out.print("[" + term.toString() + "] ");
    }
  }
}
