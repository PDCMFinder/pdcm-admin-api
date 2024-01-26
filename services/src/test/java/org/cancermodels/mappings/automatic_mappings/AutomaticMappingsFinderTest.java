package org.cancermodels.mappings.automatic_mappings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.cancermodels.mappings.suggestions.SimilarityConfigurationReader;
import org.cancermodels.pdcm_admin.EntityTypeName;
import org.cancermodels.general.MappingEntityBuilder;
import org.cancermodels.general.SuggestionBuilder;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.pdcm_admin.persistance.Suggestion;
import org.cancermodels.suggestions.search_engine.SuggestionsSearcher;
import org.cancermodels.pdcm_admin.types.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutomaticMappingsFinderTest {

  @Mock
  private SuggestionsSearcher suggestionsSearcher;

  @Mock
  private SimilarityConfigurationReader similarityConfigurationReader;

  private AutomaticMappingsFinder instance;

  private final MappingEntityBuilder mappingEntityBuilder = new MappingEntityBuilder();
  private final SuggestionBuilder suggestionBuilder = new SuggestionBuilder();

  //Following nomenclature: MethodName_StateUnderTest_ExpectedBehavior

  // To even be considered in the process, a suggestion should have a relative score higher than this.
  private static final int candidateThreshold = 75;

  // A suggestion with a relative score equal or higher than this should be considered a perfect
  // match therefore can be used in an automatic mapping.
  private static final int automaticWithRevisionThreshold = 95;

  private static final int requiredConsensusNumber = 3;

  @BeforeEach
  public void setup()
  {
    instance = new AutomaticMappingsFinder(suggestionsSearcher, similarityConfigurationReader);
  }

  @Test
  void findBestSuggestion_ListSuggestionIsNull_Empty() {
    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
        .build();
    List<Suggestion> suggestions = new ArrayList<>();

    when(suggestionsSearcher.searchTopSuggestions(mappingEntity)).thenReturn(suggestions);

    Optional<Suggestion> suggestionSuitableAutomaticMapping =
        instance.findBestSuggestion(mappingEntity);

    assertEquals(Optional.empty(), suggestionSuitableAutomaticMapping);
  }

  @Test
  void findBestSuggestion_ListSuggestionIsEmpty_Empty() {
    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
        .build();
    List<Suggestion> suggestions = new ArrayList<>();
    when(suggestionsSearcher.searchTopSuggestions(mappingEntity)).thenReturn(suggestions);

    Optional<Suggestion> suggestionSuitableAutomaticMapping =
        instance.findBestSuggestion(mappingEntity);

    assertEquals(Optional.empty(), suggestionSuitableAutomaticMapping);
  }

  @Test
  void findBestSuggestion_SingleSuggestionIsBad_Empty() {
    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
        .build();
    List<Suggestion> suggestions = new ArrayList<>();

    double relativeScore = candidateThreshold - 1;
    Suggestion badSuggestion = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(relativeScore)
        .build();
    suggestions.add(badSuggestion);

    when(suggestionsSearcher.searchTopSuggestions(mappingEntity)).thenReturn(suggestions);
    when(similarityConfigurationReader.getCandidateThreshold()).thenReturn(candidateThreshold);

    Optional<Suggestion> suggestionSuitableAutomaticMapping =
        instance.findBestSuggestion(mappingEntity);

    assertEquals(Optional.empty(), suggestionSuitableAutomaticMapping);
  }

  @Test
  void findBestSuggestion_AllSuggestionsAreBad_Empty() {
    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
        .build();

    List<Suggestion> suggestions = new ArrayList<>();

    double relativeScore1 = candidateThreshold - 1;
    double relativeScore2 = candidateThreshold - 2;

    Suggestion badSuggestion1 = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(relativeScore1)
        .build();
    Suggestion badSuggestion2 = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(relativeScore2)
        .build();
    suggestions.add(badSuggestion1);
    suggestions.add(badSuggestion2);

    when(suggestionsSearcher.searchTopSuggestions(mappingEntity)).thenReturn(suggestions);
    when(similarityConfigurationReader.getCandidateThreshold()).thenReturn(candidateThreshold);

    Optional<Suggestion> suggestionSuitableAutomaticMapping =
        instance.findBestSuggestion(mappingEntity);

    assertEquals(Optional.empty(), suggestionSuitableAutomaticMapping);
  }

  @Test
  void findBestSuggestion_SinglePerfectSuggestion_PerfectSuggestion() {
    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
        .build();
    List<Suggestion> suggestions = new ArrayList<>();

    Suggestion suggestionEqualsToPerfectThreshold = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(automaticWithRevisionThreshold)
        .build();

    suggestions.add(suggestionEqualsToPerfectThreshold);

    when(suggestionsSearcher.searchTopSuggestions(mappingEntity)).thenReturn(suggestions);

    Optional<Suggestion> answer = instance.findBestSuggestion(mappingEntity);

    assertTrue(answer.isPresent());
    assertEquals(suggestionEqualsToPerfectThreshold, answer.get());
  }

  @Test
  void findBestSuggestion_SingleHigherThanPerfectSuggestion_PerfectSuggestion() {
    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
        .build();
    List<Suggestion> suggestions = new ArrayList<>();

    Suggestion suggestionHigherThanPerfectThreshold = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(automaticWithRevisionThreshold + 1)
        .build();

    suggestions.add(suggestionHigherThanPerfectThreshold);

    when(suggestionsSearcher.searchTopSuggestions(mappingEntity)).thenReturn(suggestions);

    Optional<Suggestion> answer = instance.findBestSuggestion(mappingEntity);

    assertTrue(answer.isPresent());
    assertEquals(suggestionHigherThanPerfectThreshold, answer.get());
  }

  @Test
  void findBestSuggestion_BadSuggestionPlusPerfectSuggestion_PerfectSuggestion() {
    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
        .build();
    List<Suggestion> suggestions = new ArrayList<>();

    Suggestion suggestionBadSuggestion = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(candidateThreshold -1)
        .build();

    Suggestion suggestionPerfectThreshold = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(automaticWithRevisionThreshold)
        .build();

    suggestions.add(suggestionBadSuggestion);
    suggestions.add(suggestionPerfectThreshold);

    when(suggestionsSearcher.searchTopSuggestions(mappingEntity)).thenReturn(suggestions);

    Optional<Suggestion> answer = instance.findBestSuggestion(mappingEntity);

    assertTrue(answer.isPresent());
    assertEquals(suggestionPerfectThreshold, answer.get());
  }

  @Test
  void findBestSuggestion_SeveralPerfectSuggestions_HighestRelativeScoreAmongPerfectSuggestions() {
    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
        .build();
    List<Suggestion> suggestions = new ArrayList<>();

    Suggestion perfectSuggestion = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(automaticWithRevisionThreshold)
        .build();

    Suggestion perfectSuggestionHigherRelativeScore = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(automaticWithRevisionThreshold + 1)
        .build();

    suggestions.add(perfectSuggestion);
    suggestions.add(perfectSuggestionHigherRelativeScore);

    when(suggestionsSearcher.searchTopSuggestions(mappingEntity)).thenReturn(suggestions);

    Optional<Suggestion> answer = instance.findBestSuggestion(mappingEntity);

    assertTrue(answer.isPresent());
    assertEquals(perfectSuggestionHigherRelativeScore, answer.get());
  }

  @Test
  void findBestSuggestion_SeveralAcceptableSuggestionsNoConsensus_Empty() {
    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
        .build();
    List<Suggestion> suggestions = new ArrayList<>();

    Suggestion acceptableSuggestion1 = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(candidateThreshold)
        .setSuggestedTermUrl("url1")
        .build();
    Suggestion acceptableSuggestion2 = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(candidateThreshold)
        .setSuggestedTermUrl("url2")
        .build();
    Suggestion acceptableSuggestion3 = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(candidateThreshold)
        .setSuggestedTermUrl("url3")
        .build();

    suggestions.add(acceptableSuggestion1);
    suggestions.add(acceptableSuggestion2);
    suggestions.add(acceptableSuggestion3);

    when(suggestionsSearcher.searchTopSuggestions(mappingEntity)).thenReturn(suggestions);
    when(similarityConfigurationReader.getCandidateThreshold()).thenReturn(candidateThreshold);
    when(similarityConfigurationReader.getAutomaticWithRevisionThreshold()).thenReturn(automaticWithRevisionThreshold);
    when(similarityConfigurationReader.getRequiredConsensusNumber()).thenReturn(requiredConsensusNumber);

    Optional<Suggestion> answer = instance.findBestSuggestion(mappingEntity);

    assertEquals(Optional.empty(), answer);
  }

  @Test
  void findBestSuggestion_SeveralAcceptableSuggestionsConsensus_AnyAcceptableWithConsensus() {
    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
        .build();
    List<Suggestion> suggestions = new ArrayList<>();

    Suggestion acceptableSuggestion1 = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(candidateThreshold)
        .setSuggestedTermUrl("url1")
        .build();
    Suggestion acceptableSuggestion2 = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(candidateThreshold)
        .setSuggestedTermUrl("url1")
        .build();
    Suggestion acceptableSuggestion3 = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(candidateThreshold)
        .setSuggestedTermUrl("url1")
        .build();

    suggestions.add(acceptableSuggestion1);
    suggestions.add(acceptableSuggestion2);
    suggestions.add(acceptableSuggestion3);

    when(suggestionsSearcher.searchTopSuggestions(mappingEntity)).thenReturn(suggestions);

    Optional<Suggestion> answer = instance.findBestSuggestion(mappingEntity);

    assertTrue(answer.isPresent(), "Expected to find a suggestion but found none.");
    assertEquals("url1", answer.get().getSuggestedTermUrl());
  }

  @Test
  void findBestSuggestion_SeveralAcceptableSuggestionsConsensusButOneHighScoreNoConsensus_Empty() {
    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
        .build();
    List<Suggestion> suggestions = new ArrayList<>();

    Suggestion acceptableSuggestion1 = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(candidateThreshold)
        .setSuggestedTermUrl("url1")
        .build();
    Suggestion acceptableSuggestion2 = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(candidateThreshold)
        .setSuggestedTermUrl("url1")
        .build();
    Suggestion acceptableSuggestion3 = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(candidateThreshold)
        .setSuggestedTermUrl("url1")
        .build();
    Suggestion acceptableSuggestion4 = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(candidateThreshold + 1)
        .setSuggestedTermUrl("url2")
        .build();

    suggestions.add(acceptableSuggestion1);
    suggestions.add(acceptableSuggestion2);
    suggestions.add(acceptableSuggestion3);
    suggestions.add(acceptableSuggestion4);

    when(suggestionsSearcher.searchTopSuggestions(mappingEntity)).thenReturn(suggestions);
    when(similarityConfigurationReader.getCandidateThreshold()).thenReturn(candidateThreshold);
    when(similarityConfigurationReader.getAutomaticWithRevisionThreshold()).thenReturn(automaticWithRevisionThreshold);
    when(similarityConfigurationReader.getRequiredConsensusNumber()).thenReturn(requiredConsensusNumber);

    Optional<Suggestion> answer = instance.findBestSuggestion(mappingEntity);

    assertEquals(Optional.empty(), answer);
  }

  @Test
  void findBestSuggestion_NotEnoughConsensus_Empty() {
    MappingEntity mappingEntity = mappingEntityBuilder
        .setEntityType(EntityTypeName.Treatment)
        .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
        .build();
    List<Suggestion> suggestions = new ArrayList<>();

    Suggestion acceptableSuggestion1 = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(candidateThreshold)
        .setSuggestedTermUrl("url1")
        .build();
    Suggestion acceptableSuggestion2 = suggestionBuilder
        .setSourceType(Source.RULE.getLabel())
        .setRelativeScore(candidateThreshold)
        .setSuggestedTermUrl("url1")
        .build();

    suggestions.add(acceptableSuggestion1);
    suggestions.add(acceptableSuggestion2);

    when(suggestionsSearcher.searchTopSuggestions(mappingEntity)).thenReturn(suggestions);
    when(similarityConfigurationReader.getCandidateThreshold()).thenReturn(candidateThreshold);
    when(similarityConfigurationReader.getAutomaticWithRevisionThreshold()).thenReturn(automaticWithRevisionThreshold);
    when(similarityConfigurationReader.getRequiredConsensusNumber()).thenReturn(requiredConsensusNumber);

    Optional<Suggestion> answer = instance.findBestSuggestion(mappingEntity);

    assertEquals(Optional.empty(), answer);
  }

}
