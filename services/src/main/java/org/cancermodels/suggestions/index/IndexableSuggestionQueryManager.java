package org.cancermodels.suggestions.index;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.stereotype.Component;

@Component
public class IndexableSuggestionQueryManager {

  public Query getTermQuery(String field, String value) {
    Term term = new Term(field, value);
    return new TermQuery(term);
  }
}
