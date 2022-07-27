package org.cancermodels.suggestions.index;

import static org.cancermodels.general.MappingEntityCreator.createMappingEntityTestInstance;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.apache.lucene.search.Query;
import org.cancermodels.MappingEntity;
import org.cancermodels.general.MappingEntityCreator;
import org.junit.jupiter.api.Test;

class RulesQueryBuilderTest {

  private final MappingValueConfHelper keySearchConfService = new MappingValueConfHelper();
  private final AnalyzerProvider analyzerProvider = new AnalyzerProvider();
  private final QueryHelper queryHelper = new QueryHelper(analyzerProvider);

  private final RulesQueryBuilder instance = new RulesQueryBuilder(keySearchConfService, queryHelper);

  @Test
  void buildRulesQuery() throws IOException {
    MappingEntity mappingEntity = createMappingEntityTestInstance();
    Query query = instance.buildRulesQuery(mappingEntity.getMappingValues());

    String expected = getExpectedRule();

    assertEquals(expected, query.toString());
    System.out.println(query);
  }

  private String getExpectedRule() {
    double boost1 =
        MappingEntityCreator.WEIGHT_2 * QueryConstants.RULE_MULTIPLIER *
            QueryConstants.TERM_RELEVANCE_MULTIPLIER;
    double boost2 =
        MappingEntityCreator.WEIGHT_3 * QueryConstants.RULE_MULTIPLIER *
            QueryConstants.TERM_RELEVANCE_MULTIPLIER;
    double boost3 =
        MappingEntityCreator.WEIGHT_4 * QueryConstants.RULE_MULTIPLIER *
            QueryConstants.TERM_RELEVANCE_MULTIPLIER;
    double boost4 =
        MappingEntityCreator.WEIGHT_2 * QueryConstants.RULE_MULTIPLIER *
            QueryConstants.PHRASE_RELEVANCE_MULTIPLIER;
    double boost5 =
        MappingEntityCreator.WEIGHT_2 * QueryConstants.RULE_MULTIPLIER *
            QueryConstants.MULTI_TERM_RELEVANCE_MULTIPLIER;
    double boost6 =
        MappingEntityCreator.WEIGHT_2 * QueryConstants.RULE_MULTIPLIER *
            QueryConstants.MULTI_TERM_PHRASE_RELEVANCE_MULTIPLIER;

    String expected =
        "(rule.value.sampleDiagnosis:central~2 rule.value.sampleDiagnosis:nervous~2 "
            + "rule.value.sampleDiagnosis:system~2 rule.value.sampleDiagnosis:cancer~2)^[BOOST1] "
            + "(rule.value.originTissue:central~2 rule.value.originTissue:nervous~2 "
            + "rule.value.originTissue:system~2)^[BOOST2] "
            + "(rule.value.tumourType:primary~2)^[BOOST3] "
            + "(rule.value.sampleDiagnosis:\"central nervous system cancer\"~1)^[BOOST4] "
            + "(rule.value.sampleDiagnosis:central~2 rule.value.sampleDiagnosis:nervous~2 "
            + "rule.value.sampleDiagnosis:system~2 rule.value.sampleDiagnosis:central~2 "
            + "rule.value.sampleDiagnosis:nervous~2 rule.value.sampleDiagnosis:system~2 "
            + "rule.value.sampleDiagnosis:cancer~2)^[BOOST5] "
            + "(rule.value.sampleDiagnosis:\"central nervous system central nervous system cancer\"~1)^[BOOST6]";

    expected = expected.replace("[BOOST1]", boost1 + "");
    expected = expected.replace("[BOOST2]", boost2 + "");
    expected = expected.replace("[BOOST3]", boost3 + "");
    expected = expected.replace("[BOOST4]", boost4 + "");
    expected = expected.replace("[BOOST5]", boost5 + "");
    expected = expected.replace("[BOOST6]", boost6 + "");

    return expected;
  }
}