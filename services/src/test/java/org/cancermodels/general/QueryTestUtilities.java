package org.cancermodels.general;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.QueryBuilder;

public class QueryTestUtilities {

  public static Query getDummyQuery() {
    QueryBuilder builder = new QueryBuilder(new StandardAnalyzer());
    Query a = builder.createBooleanQuery("body", "just a test");
    return a;
  }
}
