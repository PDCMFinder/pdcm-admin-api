package org.cancermodels.suggestions.index;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.stereotype.Component;

@Component
public class QueryHelper {

  public Query getTermQuery(String field, String value) {
    Term term = new Term(field, value);
    return new TermQuery(term);
  }

  private List<String> tokenize(String field, String text) throws IOException {
    List<String> tokens = new ArrayList<>();
    Analyzer analyzer = new StandardAnalyzer();
    TokenStream tokenStream = analyzer.tokenStream(field, new StringReader(text));
    CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
    tokenStream.reset();

    while(tokenStream.incrementToken()) {
      tokens.add(term.toString());
    }

    return tokens;
  }

  public Query buildBoostedQueryForText(String field, String text, float boost) throws IOException {
    BooleanQuery.Builder builder = new Builder();
    List<String> tokens = tokenize(field, text);
    for (String token : tokens) {
      FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term(field, token));
      builder.add(fuzzyQuery, Occur.SHOULD);
    }
    return new BoostQuery(builder.build(), boost);
  }

}
