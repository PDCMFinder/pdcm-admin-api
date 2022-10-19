package org.cancermodels.suggestions.search_engine.query_builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;
import org.cancermodels.exceptions.SearchException;
import org.springframework.stereotype.Component;

@Component
public class SearchInputQueryBuilder {

  private final QueryHelper queryHelper;

  public SearchInputQueryBuilder(QueryHelper queryHelper) {
    this.queryHelper = queryHelper;
  }

  public Query buildQuery(SearchInput searchInput) throws SearchException {
    Query finalQuery;
    Query shouldPart = null;
    try {
      shouldPart = buildShouldPart(searchInput);
      Query mustNotPart = buildMustNotPart(searchInput);

      if (mustNotPart == null) {
        finalQuery = shouldPart;

      } else {
        BooleanQuery.Builder builder = new Builder();
        finalQuery = builder.add(shouldPart, Occur.SHOULD).add(mustNotPart, Occur.MUST_NOT).build();
      }
    } catch (IOException ioException) {
      throw new SearchException(ioException);
    }


    return finalQuery;
  }

  private Query buildShouldPart(SearchInput searchInput) throws IOException {
    Query shouldQuery;
    List<Query> queries = new ArrayList<>();
    List<Query> boostFuzzyQueries = buildBoostFuzzyQueryByTerm(searchInput);
    List<Query> boostPhraseQueries = buildBoostPhraseQuery(searchInput);
    queries.addAll(boostFuzzyQueries);
    queries.addAll(boostPhraseQueries);

    if (searchInput.isDisjunctionMaxQuery()) {
      shouldQuery = queryHelper.joinQueriesDisjunctionMaxQueryZeroTie(queries);
    }
    else {
      shouldQuery = queryHelper.joinQueriesShouldMode(queries);
    }

    return shouldQuery;
  }

  private Query buildMustNotPart(SearchInput searchInput) {
    List<Query> queries = new ArrayList<>();
    Query query = null;
    for (SearchInputEntry searchInputEntry : searchInput.getFieldsToExclude()) {
      queries.add(buildSimpleTermQuery(searchInputEntry));
    }

    if (!queries.isEmpty()) {
      query = queryHelper.joinQueriesShouldMode(queries);
    }

    return query;
  }

  private Query buildSimpleTermQuery(SearchInputEntry searchInputEntry) {
    return queryHelper.getTermQuery(searchInputEntry.getFieldName(), searchInputEntry.getText());
  }

  private List<Query> buildBoostFuzzyQueryByTerm(SearchInput searchInput) throws IOException {
    List<Query> queries = new ArrayList<>();
    double multiplier = searchInput.getCommonParameters().getTermMultiplier();
    for (SearchInputEntry entry : searchInput.getFieldsToInclude()) {

      String fieldName = entry.getFieldName();
      String text = entry.getText();
      double boost = entry.getWeight() * multiplier;

      Query query = queryHelper.buildBoostFuzzyQueryByTerm(fieldName, text,  boost);

      queries.add(query);
    }
    return queries;
  }

  private List<Query> buildBoostPhraseQuery(SearchInput searchInput) throws IOException {
    List<Query> queries = new ArrayList<>();
    double multiplier = searchInput.getCommonParameters().getPhraseMultiplier();
    for (SearchInputEntry entry : searchInput.getFieldsToInclude()) {

      String fieldName = entry.getFieldName();
      String text = entry.getText();
      double boost = entry.getWeight() * multiplier;

      Query query = queryHelper.buildBoostPhraseQuery(fieldName, text,  boost);

      queries.add(query);
    }
    return queries;
  }

}
