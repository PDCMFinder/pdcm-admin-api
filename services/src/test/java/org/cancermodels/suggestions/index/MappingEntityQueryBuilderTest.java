package org.cancermodels.suggestions.index;

import static org.cancermodels.general.MappingEntityCreator.createMappingEntityTestInstance;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.cancermodels.MappingEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MappingEntityQueryBuilderTest {

  @Mock
  private RulesQueryBuilder rulesQueryBuilder;
  @Mock
  private OntologyQueryBuilder ontologyQueryBuilder;

  @InjectMocks
  private MappingEntityQueryBuilder instance;

  @Test
  void testBuildSuggestionQuery() throws IOException {
    MappingEntity mappingEntity = createMappingEntityTestInstance();
    instance.buildSuggestionQuery(mappingEntity);
    verify(rulesQueryBuilder).buildRulesQuery(mappingEntity.getMappingValues());
    verify(ontologyQueryBuilder).buildOntologiesQuery(mappingEntity.getMappingValues());
  }

}