package org.cancermodels.suggestions.search_engine.query_builder;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.cancermodels.suggestions.search_engine.AnalyzerProvider;
import org.cancermodels.suggestions.search_engine.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryHelperTest {

  private final AnalyzerProvider analyzerProvider = new AnalyzerProvider();
  private QueryHelper instance;

  @BeforeEach
  void setUp() {
    instance = new QueryHelper(analyzerProvider);
  }

  @Test
  void getTermQuery_ValidValues_QueryWithOneTerm() {
    Query query = instance.getTermQuery("field", "word");

    String expected = "field:word";
    assertEquals(expected, query.toString());
  }

  @Test
  void buildBoostFuzzyQueryByTerm_OneWord_QueryWithOneTerm()
      throws IOException {
    Query query = instance.buildBoostFuzzyQueryByTerm("field", "word", 0.5f);

    String expected = "(field:word~2)^0.5";
    assertEquals(expected, query.toString());
  }

  @Test
  void buildBoostFuzzyQueryByTerm_SeveralWord_QueryWithSeveralTerms() throws IOException {
    Query query = instance.buildBoostFuzzyQueryByTerm("field", "word1 word2 word3", 0.5f);

    String expected = "(field:word1~2 field:word2~2 field:word3~2)^0.5";
    assertEquals(expected, query.toString());
  }

  @Test
  void buildBoostFuzzyQueryByTerm_Symbols_QueryWithoutSymbols() throws IOException {
    Query query = instance.buildBoostFuzzyQueryByTerm("field", "+=word1 £ word2%%% & word3", 0.5f);

    String expected = "(field:word1~2 field:word2~2 field:word3~2)^0.5";
    assertEquals(expected, query.toString());
  }

  @Test
  void buildBoostPhraseQuery_LessThanLimitWords_PhraseQueryWithAllWords() throws IOException {
    Query query = instance.buildBoostPhraseQuery("field", "word", 1.5);

    String expected = "(field:\"word\"~1)^1.5";
    assertEquals(expected, query.toString());
  }

  @Test
  void buildBoostPhraseQuery_MoreThanLimitWords_PhraseQueryWithTruncateNumberWords()
      throws IOException {
    List<String> words = new ArrayList<>();
    for (int i = 1 ; i <= Constants.MAX_NUMBER_TERMS_BY_QUERY + 1; i++) {
      words.add("word" + i);
    }
    String allWords = String.join(" ", words);
    Query query = instance.buildBoostPhraseQuery("field", allWords, 1.5);

    String truncatedWords = String.join(" ", words.subList(0, Constants.MAX_NUMBER_TERMS_BY_QUERY));
    String expected =
        "(field:\"" + truncatedWords + "\"~1)^1.5";
    assertEquals(expected, query.toString());
  }

  @Test
  void joinQueriesShouldMode_2Queries_2JoinedQueries() {
    Term term1 = new Term("field1", "word1");
    Query query1 = new TermQuery(term1);
    Term term2 = new Term("field2", "word2");
    Query query2 = new TermQuery(term2);
    Query query = instance.joinQueriesShouldMode(Arrays.asList(query1, query2));

    String expected = "field1:word1 field2:word2";
    assertEquals(expected, query.toString());
  }

  @Test
  void joinQueriesMustMode_2Queries_2JoinedQueries() {
    Term term1 = new Term("field1", "word1");
    Query query1 = new TermQuery(term1);
    Term term2 = new Term("field2", "word2");
    Query query2 = new TermQuery(term2);
    Query query = instance.joinQueriesMustMode(Arrays.asList(query1, query2));

    String expected = "+field1:word1 +field2:word2";
    assertEquals(expected, query.toString());
  }

  @Test
  void joinQueriesDisjunctionMaxQueryZeroTie_2Queries_2JoinedDisjQueries() {
    Term term1 = new Term("field1", "word1");
    Query query1 = new TermQuery(term1);
    Term term2 = new Term("field2", "word2");
    Query query2 = new TermQuery(term2);
    Query query = instance.joinQueriesDisjunctionMaxQueryZeroTie(
        Arrays.asList(query1, query2));

    String expected = "(field1:word1 | field2:word2)";
    assertEquals(expected, query.toString());
  }

}