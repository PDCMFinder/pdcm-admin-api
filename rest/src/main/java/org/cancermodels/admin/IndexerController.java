package org.cancermodels.admin;

import java.io.IOException;
import java.util.List;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.Suggestion;
import org.cancermodels.mappings.MappingEntityService;
import org.cancermodels.mappings.suggestions.SuggestionManager;
import org.cancermodels.suggestions.indexers.OntologiesIndexer;
import org.cancermodels.suggestions.indexers.RulesIndexer;
import org.cancermodels.suggestions.search_engine.SuggestionsSearcher;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/indexer")
public class IndexerController {
  private final OntologiesIndexer ontologiesIndexer;
  private final RulesIndexer rulesIndexer;
  private final MappingEntityService mappingEntityService;
  private final SuggestionsSearcher suggestionsSearcher;

  private final SuggestionManager suggestionManager;

  public IndexerController(
      OntologiesIndexer ontologiesIndexer,
      RulesIndexer rulesIndexer,
      MappingEntityService mappingEntityService,
      SuggestionsSearcher suggestionsSearcher,
      SuggestionManager suggestionManager) {
    this.ontologiesIndexer = ontologiesIndexer;
    this.rulesIndexer = rulesIndexer;
    this.mappingEntityService = mappingEntityService;
    this.suggestionsSearcher = suggestionsSearcher;
    this.suggestionManager = suggestionManager;
  }

  @PutMapping("index/ontologies")
  public void indexOntologies() throws IOException {
    ontologiesIndexer.index();
  }

  @PutMapping("index/rules")
  public void indexRules() throws IOException {
    rulesIndexer.index();
  }

  @GetMapping("calculateSuggestions/{id}")
  List<Suggestion> getMappingEntity(@PathVariable int id) throws IOException {
    MappingEntity mappingEntity = mappingEntityService.findById(id).orElseThrow(
        ResourceNotFoundException::new);
    List<Suggestion> results = suggestionsSearcher.searchTopSuggestions(mappingEntity);
    return results;
  }

  @GetMapping("executeSuggestionsReport")
  void getMappingEntity() throws IOException {
    List<MappingEntity> treatments = mappingEntityService.getAllByTypeName("treatment");
    List<MappingEntity> diagnosis = mappingEntityService.getAllByTypeName("diagnosis");

    System.out.println("Report for treatments");
    for (MappingEntity mappingEntity : treatments) {
      suggestionManager.runSuggestionReportForEntity(mappingEntity);
    }

    System.out.println("Report for diagnosis");
    for (MappingEntity mappingEntity : treatments) {
      suggestionManager.runSuggestionReportForEntity(mappingEntity);
    }
  }

}
