package org.cancermodels.mappings.automatic_mappings;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.cancer_models.entity2ontology.exceptions.MalformedMappingConfigurationException;
import org.cancer_models.entity2ontology.exceptions.MappingException;
import org.cancermodels.mappings.suggestions.SimilarityConfigurationReader;
import org.cancermodels.pdcm_admin.EntityTypeName;
import org.cancermodels.general.MappingEntityBuilder;
import org.cancermodels.general.SuggestionBuilder;
import org.cancermodels.mappings.MappingEntityService;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.pdcm_admin.persistance.Suggestion;
import org.cancermodels.pdcm_admin.types.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutomaticMappingsServiceTest {

  private AutomaticMappingsService instance;

  @Mock
  private MappingEntityService mappingEntityService;

  @Mock
  private AutomaticMappingsFinder automaticMappingsFinder;

  @Mock
  private SimilarityConfigurationReader similarityConfigurationReader;

  private final MappingEntityBuilder mappingEntityBuilder = new MappingEntityBuilder();
  private final SuggestionBuilder suggestionBuilder = new SuggestionBuilder();

  @BeforeEach
  public void setup()
  {
    instance = new AutomaticMappingsService(mappingEntityService, automaticMappingsFinder, similarityConfigurationReader);
  }

  @Test
  void evaluateAutomaticMappingsInMappedEntities_OneEntitySuccessful_MapWithOneSuccessfulEntry() throws MalformedMappingConfigurationException, MappingException {

    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
        .setMappedTermUrl("url1")
        .build();

    List<MappingEntity> mappingEntities = new ArrayList<>();
    mappingEntities.add(mappingEntity);

    when(mappingEntityService.getAllByStatus(Status.MAPPED.getLabel())).thenReturn(mappingEntities);

    Suggestion suggestion = suggestionBuilder.setId(1).setSuggestedTermUrl("url1").build();

    when(automaticMappingsFinder.findBestSuggestion(mappingEntity)).thenReturn(Optional.of(suggestion));

    Map<String, Integer> findings = instance.evaluateAutomaticMappingsInMappedEntities();

    assertNotNull(findings);
    assertEquals(1, findings.get("matching"));
    assertEquals(0, findings.get("not_matching"));
    assertEquals(0, findings.get("not_suggestion"));
  }

  @Test
  void evaluateAutomaticMappingsInMappedEntities_OneEntityNotMatching_MapWithOneSuccessfulEntry() throws MalformedMappingConfigurationException, MappingException {

    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
        .setMappedTermUrl("url1")
        .build();

    List<MappingEntity> mappingEntities = new ArrayList<>();
    mappingEntities.add(mappingEntity);

    when(mappingEntityService.getAllByStatus(Status.MAPPED.getLabel())).thenReturn(mappingEntities);

    Suggestion suggestion = suggestionBuilder.setId(1).setSuggestedTermUrl("url2").build();

    when(automaticMappingsFinder.findBestSuggestion(mappingEntity)).thenReturn(Optional.of(suggestion));

    Map<String, Integer> findings = instance.evaluateAutomaticMappingsInMappedEntities();

    assertNotNull(findings);
    assertEquals(0, findings.get("matching"));
    assertEquals(1, findings.get("not_matching"));
    assertEquals(0, findings.get("not_suggestion"));
  }

  @Test
  void evaluateAutomaticMappingsInMappedEntities_OneEntityNotSuggestion_MapWithOneSuccessfulEntry() throws MalformedMappingConfigurationException, MappingException {

    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
        .setMappedTermUrl("url1")
        .build();

    List<MappingEntity> mappingEntities = new ArrayList<>();
    mappingEntities.add(mappingEntity);

    when(mappingEntityService.getAllByStatus(Status.MAPPED.getLabel())).thenReturn(mappingEntities);

    when(automaticMappingsFinder.findBestSuggestion(mappingEntity)).thenReturn(Optional.empty());

    Map<String, Integer> findings = instance.evaluateAutomaticMappingsInMappedEntities();

    assertNotNull(findings);
    assertEquals(0, findings.get("matching"));
    assertEquals(0, findings.get("not_matching"));
    assertEquals(1, findings.get("not_suggestion"));
  }

  @Test
  void evaluateAutomaticMappingsInMappedEntities_OneEachOne_MapWith1CounterForEachEntry() throws MalformedMappingConfigurationException, MappingException {

    MappingEntity mappingEntity1 = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setMappingKey("key1")
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "treatment1"))
        .setMappedTermUrl("url1")
        .build();

    MappingEntity mappingEntity2 = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setMappingKey("key2")
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "treatment2"))
        .setMappedTermUrl("url2")
        .build();

    MappingEntity mappingEntity3 = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setMappingKey("key3")
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "treatment3"))
        .setMappedTermUrl("url3")
        .build();

    List<MappingEntity> mappingEntities = new ArrayList<>();
    mappingEntities.add(mappingEntity1);
    mappingEntities.add(mappingEntity2);
    mappingEntities.add(mappingEntity3);

    when(mappingEntityService.getAllByStatus(Status.MAPPED.getLabel())).thenReturn(mappingEntities);

    Suggestion suggestion1 = suggestionBuilder.setId(1).setSuggestedTermUrl("url1").build();
    Suggestion suggestion2 = suggestionBuilder.setId(1).setSuggestedTermUrl("urlx").build();

    when(automaticMappingsFinder.findBestSuggestion(mappingEntity1)).thenReturn(Optional.of(suggestion1));
    when(automaticMappingsFinder.findBestSuggestion(mappingEntity2)).thenReturn(Optional.of(suggestion2));
    when(automaticMappingsFinder.findBestSuggestion(mappingEntity3)).thenReturn(Optional.empty());

    Map<String, Integer> findings = instance.evaluateAutomaticMappingsInMappedEntities();

    assertNotNull(findings);
    assertEquals(1, findings.get("matching"));
    assertEquals(1, findings.get("not_matching"));
    assertEquals(1, findings.get("not_suggestion"));
  }
}