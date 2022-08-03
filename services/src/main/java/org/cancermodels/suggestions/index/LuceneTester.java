package org.cancermodels.suggestions.index;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.QueryBuilder;

public class LuceneTester {
  public static void main(String[] args) {
    LuceneTester tester;

    QueryBuilder queryBuilder = new QueryBuilder(new StandardAnalyzer());
    Query a = queryBuilder.createBooleanQuery("body", "just a test");
    Query b = queryBuilder.createPhraseQuery("body", "another test");
    Query c = queryBuilder.createMinShouldMatchQuery("body", "another test", 0.6f);
    System.out.println(a);
    System.out.println(b);
    System.out.println(c);

    tester = new LuceneTester();

    try {
      tester.displayTokenUsingSimpleAnalyzer();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void displayTokenUsingSimpleAnalyzer() throws IOException {
    String text =
        "Etoposide/Fluorouracil/Folinic Acid";
    List<String> words = Arrays.asList("the", "of");
    CharArraySet charArraySet = new CharArraySet(words, true);
    Analyzer analyzer = new StandardAnalyzer(charArraySet);

    AnalyzerProvider analyzerProvider = new AnalyzerProvider();

    analyzer = new EnglishAnalyzer();
    analyzer = analyzerProvider.getAnalyzer();
    TokenStream tokenStream = analyzer.tokenStream(
        "contents", new StringReader(text));
    CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
    tokenStream.reset();
    while(tokenStream.incrementToken()) {
      System.out.print("[" + term.toString() + "] ");
    }
  }
}
