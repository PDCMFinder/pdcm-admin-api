package org.cancermodels.suggestions.index;

import static org.cancermodels.general.MappingEntityCreator.createMappingEntityTestInstance;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.apache.lucene.search.Query;
import org.cancermodels.MappingEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MappingEntityQueryBuilderTest {

  @Spy
  QueryHelper queryHelper;

  @InjectMocks
  private MappingEntityQueryBuilder instance;

  @Test
  void test() throws IOException {
    MappingEntity mappingEntity = createMappingEntityTestInstance();
    MappingEntityQueryBuilder spyInstance = spy(instance);
    spyInstance.buildSuggestionQuery(mappingEntity);

    Query rulesQuery = spyInstance.buildRulesQuery(mappingEntity.getMappingValues());
    Query ontologiesQuery = spyInstance.buildOntologyQuery(mappingEntity.getMappingValues());

    verify(spyInstance).ensembleFinalQuery(rulesQuery, ontologiesQuery );
  }

  @Test
  void testBuildRulesQuery() throws IOException {
    MappingEntity mappingEntity = createMappingEntityTestInstance();
    String expectedQuery =
        "(rule.value.sampleDiagnosis:central~2 rule.value.sampleDiagnosis:nervous~2 "
            + "rule.value.sampleDiagnosis:system~2 rule.value.sampleDiagnosis:cancer~2)^8.1 "
            + "(rule.value.originTissue:central~2 rule.value.originTissue:nervous~2 "
            + "rule.value.originTissue:system~2)^0.72 (rule.value.tumourType:primary~2)^0.18";

    Query rulesQuery = instance.buildRulesQuery(mappingEntity.getMappingValues());

    verify(queryHelper, never()).buildBoostFuzzyQueryByTerm(eq("rule.dataSource"), anyString(), anyFloat());
    assertEquals(expectedQuery, rulesQuery.toString());
  }

  @Test
  void testBuildOntologyQuery() throws IOException {
    MappingEntity mappingEntity = createMappingEntityTestInstance();
    String expectedQuery =
        "(ontology.label:central~2 ontology.label:nervous~2 ontology.label:system~2 "
            + "ontology.label:central~2 ontology.label:nervous~2 ontology.label:system~2 "
            + "ontology.label:cancer~2)^1.0 "
            + "(ontology.definition:central~2 ontology.definition:nervous~2 "
            + "ontology.definition:system~2 ontology.definition:central~2 "
            + "ontology.definition:nervous~2 ontology.definition:system~2 "
            + "ontology.definition:cancer~2)^1.0 (ontology.synonym:central~2 "
            + "ontology.synonym:nervous~2 ontology.synonym:system~2 "
            + "ontology.synonym:central~2 ontology.synonym:nervous~2 "
            + "ontology.synonym:system~2 ontology.synonym:cancer~2)^1.0";

    Query ontologiesQuery = instance.buildOntologyQuery(mappingEntity.getMappingValues());

    // Queries should only be built for Sample Diagnosis value
    verify(queryHelper, never()).buildBoostFuzzyQueryByTerm(anyString(), eq("crl"), anyFloat());
    verify(
        queryHelper, never()).buildBoostFuzzyQueryByTerm(anyString(), eq("central nervous system"), anyFloat());
    verify(queryHelper, never()).buildBoostFuzzyQueryByTerm(anyString(), eq("primary"), anyFloat());
    assertEquals(expectedQuery, ontologiesQuery.toString());
  }
}