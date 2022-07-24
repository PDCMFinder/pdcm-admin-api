package org.cancermodels.suggestions.index;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class TokenizerHelper {
  public static List<String> tokenize(String field, String text) throws IOException {
    List<String> tokens = new ArrayList<>();
    Analyzer analyzer = new EnglishAnalyzer();
    TokenStream tokenStream = analyzer.tokenStream(field, new StringReader(text));
    CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
    tokenStream.reset();

    while(tokenStream.incrementToken()) {
      tokens.add(term.toString());
    }

    return tokens;
  }
}
