package org.cancermodels.suggestions.index;

import java.io.IOException;
import java.util.List;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.stereotype.Component;

@Component
public class QueryHelper {

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
  public Query buildBoostFuzzyQueryByTerm(String field, String text, float boost) throws IOException {
    BooleanQuery.Builder builder = new Builder();
    List<String> tokens = TokenizerHelper.tokenize(field, text);
    for (String token : tokens) {
      FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term(field, token));
      builder.add(fuzzyQuery, Occur.SHOULD);
    }
    return new BoostQuery(builder.build(), boost);
  }

  /**
   * Builds a boost phrase query with the terms/words in {@code phrase}. Joined by or/should.
   * @param field Name of the field in the query.
   * @param text Text to use to extract the words for the query.
   * @param boost boost value.
   * @return The resulting {@link Query}.
   * @throws IOException if the query cannot be created.
   */
  public Query buildBoostPhraseQuery(String field, String text, float boost) throws IOException {
    List<String> tokens = TokenizerHelper.tokenize(field, text);
    PhraseQuery phraseQuery = new PhraseQuery(1, field, tokens.toArray(new String[0]));
    return new BoostQuery (phraseQuery, boost);
  }

}
