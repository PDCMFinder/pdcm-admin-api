package org.cancermodels.suggestions.index;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.apache.lucene.search.Query;
import org.junit.jupiter.api.Test;

class QueryHelperTest {

  private final QueryHelper queryHelper = new QueryHelper();

  @Test
  void buildBoostedQueryForTextSingleWord() throws IOException {
    String expected = "(field:word~2)^0.0";

    Query query = queryHelper.buildBoostFuzzyQueryByTerm("field", "word", 0.0f);

    assertEquals(expected, query.toString());
  }

  @Test
  void buildBoostedQueryForTextSeveralWords() throws IOException {
    String expected = "(field:word1~2 field:word2~2 field:word3~2)^0.0";

    Query query = queryHelper.buildBoostFuzzyQueryByTerm("field", "word1 word2 word3", 0.0f);

    assertEquals(expected, query.toString());
  }

  @Test
  void buildBoostedQueryForTextSeveralWordsWithCharacters() throws IOException {
    String expected = "(field:word1~2 field:word2~2 field:word3~2)^0.0";

    Query query = queryHelper.buildBoostFuzzyQueryByTerm("field", "word1 += word2 ;.<>/ word3", 0.0f);

    assertEquals(expected, query.toString());
  }
}