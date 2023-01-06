package org.cancermodels.mappings.suggestions;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingEntityRepository;
import org.cancermodels.persistance.Suggestion;
import org.cancermodels.persistance.SuggestionRepository;
import org.cancermodels.suggestions.search_engine.SuggestionsSearcher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SuggestionManager {

  private final SuggestionsSearcher suggestionsSearcher;
  private final SuggestionRepository suggestionRepository;
  private final MappingEntityRepository mappingEntityRepository;

  public SuggestionManager(
      SuggestionsSearcher suggestionsSearcher,
      SuggestionRepository setSuggestionsByMappingEntities,
      MappingEntityRepository mappingEntityRepository) {
    this.suggestionsSearcher = suggestionsSearcher;
    this.suggestionRepository = setSuggestionsByMappingEntities;
    this.mappingEntityRepository = mappingEntityRepository;
  }

  public void calculateSuggestions(List<MappingEntity> mappingEntities) throws IOException {
    log.info("\nInit suggestion calculation process for {} entities", mappingEntities.size());
    resetData(mappingEntities);

    setSuggestions(mappingEntities);
  }

  private void resetData(List<MappingEntity> toProcess) {
    log.info("Resetting data");

    for (MappingEntity mappingEntity : toProcess) {
      mappingEntity.getSuggestions().clear();
    }
    // Save all entities to execute the deletion on db
    log.info("Resetting data: [init] save all entities after deleting suggestions.");
    mappingEntityRepository.saveAll(toProcess);
    log.info("Resetting data: [end] save all entities after deleting suggestions.");
  }

  /** Sets the suggestions based on similar mapping entities. */
  private void setSuggestions(List<MappingEntity> toProcess) throws IOException {

    log.info("Init mapping entity suggestions ({} entities)", toProcess.size());
    Set<Suggestion> allSuggestions = new HashSet<>();

    Map<MappingEntity, List<Suggestion>> suggestionsByEntity = getSuggestionsByEntity(toProcess);

    for (MappingEntity mappingEntity : suggestionsByEntity.keySet()) {
      // Children are saved explicitly
      List<Suggestion> suggestions = suggestionsByEntity.get(mappingEntity);
      mappingEntity.getSuggestions().addAll(suggestions);
      allSuggestions.addAll(suggestions);
    }

    suggestionRepository.saveAll(allSuggestions);
    log.info("Finish mapping entity suggestions");
  }

  public Map<MappingEntity, List<Suggestion>> getSuggestionsByEntity(
      List<MappingEntity> mappingEntities) throws IOException {
    Map<MappingEntity, List<Suggestion>> suggestionsByEntity = new HashMap<>();
    for (MappingEntity mappingEntity : mappingEntities) {
      List<Suggestion> suggestions =
          suggestionsSearcher.searchTopSuggestions(mappingEntity);
      suggestionsByEntity.put(mappingEntity, suggestions);
    }

    return suggestionsByEntity;

  }

  public void runSuggestionReportForEntity(MappingEntity mappingEntity) {
    System.out.println("----------------------------------------------------------");
    System.out.println("Entity id: " + mappingEntity.getId());
    System.out.println("Entity values: " + mappingEntity.getValuesAsMap());
    System.out.println("Current mapping: " + mappingEntity.getMappedTermUrl());

    List<Suggestion> suggestionsResults = suggestionsSearcher.searchTopSuggestions(mappingEntity);
    for (Suggestion suggestion : suggestionsResults) {

      System.out.print("\n" + suggestion.getSourceType() + "| ");
      System.out.print(suggestion.getSuggestedTermUrl() + "| ");
      System.out.print(suggestion.getSuggestedTermLabel() + "| ");
      System.out.println("\n");
    }
  }
}
