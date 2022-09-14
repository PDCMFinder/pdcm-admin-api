package org.cancermodels.suggestions.search_engine.query_builder;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.cancermodels.suggestions.search_engine.AnalyzerProvider;
import org.cancermodels.suggestions.search_engine.util.Constants;
import org.springframework.stereotype.Component;

@Component
public class QueryHelper {

  private final AnalyzerProvider analyzerProvider;

  public QueryHelper(AnalyzerProvider analyzerProvider) {
    this.analyzerProvider = analyzerProvider;
  }

  public Query getTermQuery(String field, String value) {
    Term term = new Term(field, value);
    return new TermQuery(term);
  }

  /**
   * Builds a boost fuzzy query for each term/word in {@code text}. Joined by or/should.
   * @param field Name of the field in the query.
   * @param text Text to use to extract the words for the query.
   * @param boost boost value.
   * @return The resulting {@link Query}.
   * @throws IOException if the query cannot be created.
   */
  public Query buildBoostFuzzyQueryByTerm(String field, String text, double boost) throws IOException {
    BooleanQuery.Builder builder = new Builder();
    String[] words = getLimitedNumberOfWords(tokenize(text));
    for (String word : words) {
      FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term(field, word));
      builder.add(fuzzyQuery, Occur.SHOULD);
    }
    return new BoostQuery(builder.build(), (float)boost);
  }

  /**
   * Builds a boost phrase query with the terms/words in {@code phrase}. Joined by or/should.
   * @param field Name of the field in the query.
   * @param text Text to use to extract the words for the query.
   * @param boost boost value.
   * @return The resulting {@link Query}.
   * @throws IOException if the query cannot be created.
   */
  public Query buildBoostPhraseQuery(String field, String text, double boost) throws IOException {
    String[] words = getLimitedNumberOfWords(tokenize(text));
    PhraseQuery phraseQuery = new PhraseQuery(1, field, words);
    return new BoostQuery (phraseQuery, (float)boost);
  }

  public Query joinQueriesShouldMode(List<Query> queries) {
    BooleanQuery.Builder builder = new Builder();
    for (Query query : queries) {
      if (query != null) {
        builder.add(query, Occur.SHOULD);
      }
    }
    return builder.build();
  }

  public Query joinQueriesMustMode(List<Query> queries) {
    BooleanQuery.Builder builder = new Builder();
    for (Query query : queries) {
      if (query != null) {
        builder.add(query, Occur.MUST);
      }
    }
    return builder.build();
  }

  private String[] tokenize(String text) throws IOException {
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

  private String[] getLimitedNumberOfWords(String[] words) {
    if (words.length > Constants.MAX_NUMBER_TERMS_BY_QUERY) {
      return Arrays.copyOfRange(words, 0, Constants.MAX_NUMBER_TERMS_BY_QUERY);
    }
    return words;
  }

}
