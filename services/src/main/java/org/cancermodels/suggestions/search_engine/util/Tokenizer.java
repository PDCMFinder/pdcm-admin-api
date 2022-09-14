package org.cancermodels.suggestions.search_engine.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.cancermodels.suggestions.search_engine.AnalyzerProvider;
import org.springframework.stereotype.Component;

@Component
public class Tokenizer {

  private final AnalyzerProvider analyzerProvider;

  public Tokenizer(AnalyzerProvider analyzerProvider) {
    this.analyzerProvider = analyzerProvider;
  }

  public String[] tokenize(String text) throws IOException {
    List<String> tokens = new ArrayList<>();
    // New analyzer needs to be created because if the same as the on for the indexer is used
    // this will fail (it cannot be reused).
    Analyzer analyzer = analyzerProvider.generateNewAnalyzer();
    TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(text));
    CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
    tokenStream.reset();

    while(tokenStream.incrementToken()) {
      tokens.add(term.toString());
    }

    return tokens.toArray(String[]::new);
  }
}
