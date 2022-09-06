package org.cancermodels.suggestions.search_engine;

import static org.cancermodels.general.MappingEntityCreator.createMappingEntityTestInstance;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.apache.lucene.search.Query;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.suggestions.search_engine.util.QueryConstants;
import org.junit.jupiter.api.Test;

class OntologyQueryBuilderTest {

  private final MappingValueConfHelper keySearchConfService = new MappingValueConfHelper();
  private final AnalyzerProvider analyzerProvider = new AnalyzerProvider();
  private final QueryHelper queryHelper = new QueryHelper(analyzerProvider);

  private final OntologyQueryBuilder instance = new OntologyQueryBuilder(keySearchConfService, queryHelper);


  @Test
  void buildOntologiesQuery() throws IOException {
    MappingEntity mappingEntity = createMappingEntityTestInstance();
    Query query = instance.buildOntologiesQuery(mappingEntity.getMappingValues());
    System.out.println(query);

    String expected = getExpectedRule();

    assertEquals(expected, query.toString());
    System.out.println(query);
  }

  private String getExpectedRule() {
    double boost1 =
        QueryConstants.ONTOLOGY_SEARCH_LABEL_WEIGHT * QueryConstants.TERM_RELEVANCE_MULTIPLIER;
    double boost2 =
        QueryConstants.ONTOLOGY_SEARCH_DEFINITION_WEIGHT * QueryConstants.TERM_RELEVANCE_MULTIPLIER;
    double boost3 =
        QueryConstants.ONTOLOGY_SEARCH_SYNONYM_WEIGHT * QueryConstants.TERM_RELEVANCE_MULTIPLIER;
    double boost4 =
        QueryConstants.ONTOLOGY_SEARCH_LABEL_WEIGHT * QueryConstants.PHRASE_RELEVANCE_MULTIPLIER;
    double boost5 =
        QueryConstants.ONTOLOGY_SEARCH_DEFINITION_WEIGHT *
            QueryConstants.PHRASE_RELEVANCE_MULTIPLIER;
    double boost6 =
        QueryConstants.ONTOLOGY_SEARCH_SYNONYM_WEIGHT *
            QueryConstants.PHRASE_RELEVANCE_MULTIPLIER;
    double boost7 =
        QueryConstants.ONTOLOGY_SEARCH_LABEL_WEIGHT *
            QueryConstants.MULTI_TERM_RELEVANCE_MULTIPLIER;
    double boost8 =
        QueryConstants.ONTOLOGY_SEARCH_DEFINITION_WEIGHT *
            QueryConstants.MULTI_TERM_RELEVANCE_MULTIPLIER;
    double boost9 =
        QueryConstants.ONTOLOGY_SEARCH_SYNONYM_WEIGHT *
            QueryConstants.MULTI_TERM_RELEVANCE_MULTIPLIER;
    double boost10 =
        QueryConstants.ONTOLOGY_SEARCH_LABEL_WEIGHT *
            QueryConstants.MULTI_TERM_PHRASE_RELEVANCE_MULTIPLIER;
    double boost11 =
        QueryConstants.ONTOLOGY_SEARCH_DEFINITION_WEIGHT *
            QueryConstants.MULTI_TERM_PHRASE_RELEVANCE_MULTIPLIER;
    double boost12 =
        QueryConstants.ONTOLOGY_SEARCH_SYNONYM_WEIGHT *
            QueryConstants.MULTI_TERM_PHRASE_RELEVANCE_MULTIPLIER;

    String expected =
        "(ontology.label:central~2 ontology.label:nervous~2 ontology.label:system~2 "
            + "ontology.label:cancer~2)^[BOOST1] "
            + "(ontology.definition:central~2 ontology.definition:nervous~2 "
            + "ontology.definition:system~2 ontology.definition:cancer~2)^[BOOST2] ("
            + "ontology.synonym:central~2 ontology.synonym:nervous~2 ontology.synonym:system~2 "
            + "ontology.synonym:cancer~2)^[BOOST3] "
            + "(ontology.label:\"central nervous system cancer\"~1)^[BOOST4] "
            + "(ontology.definition:\"central nervous system cancer\"~1)^[BOOST5] "
            + "(ontology.synonym:\"central nervous system cancer\"~1)^[BOOST6] "
            + "(ontology.label:central~2 ontology.label:nervous~2 ontology.label:system~2 "
            + "ontology.label:central~2 ontology.label:nervous~2 ontology.label:system~2 "
            + "ontology.label:cancer~2)^[BOOST7] "
            + "(ontology.definition:central~2 ontology.definition:nervous~2 "
            + "ontology.definition:system~2 ontology.definition:central~2 "
            + "ontology.definition:nervous~2 ontology.definition:system~2 "
            + "ontology.definition:cancer~2)^[BOOST8] "
            + "(ontology.synonym:central~2 ontology.synonym:nervous~2 "
            + "ontology.synonym:system~2 ontology.synonym:central~2 "
            + "ontology.synonym:nervous~2 ontology.synonym:system~2 "
            + "ontology.synonym:cancer~2)^[BOOST9] "
            + "(ontology.label:\"central nervous system central nervous system cancer\"~1)^[BOOST10] "
            + "(ontology.definition:\"central nervous system central nervous system cancer\"~1)^[BOOST11] "
            + "(ontology.synonym:\"central nervous system central nervous system cancer\"~1)^[BOOST12]";

    expected = expected.replace("[BOOST1]", boost1 + "");
    expected = expected.replace("[BOOST2]", boost2 + "");
    expected = expected.replace("[BOOST3]", boost3 + "");
    expected = expected.replace("[BOOST4]", boost4 + "");
    expected = expected.replace("[BOOST5]", boost5 + "");
    expected = expected.replace("[BOOST6]", boost6 + "");
    expected = expected.replace("[BOOST7]", boost7 + "");
    expected = expected.replace("[BOOST8]", boost8 + "");
    expected = expected.replace("[BOOST9]", boost9 + "");
    expected = expected.replace("[BOOST10]", boost10 + "");
    expected = expected.replace("[BOOST11]", boost11 + "");
    expected = expected.replace("[BOOST12]", boost12 + "");

    return expected;
  }
}