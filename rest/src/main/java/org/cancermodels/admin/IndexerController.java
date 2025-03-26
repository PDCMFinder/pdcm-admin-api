package org.cancermodels.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.cancermodels.admin.dtos.SuggestionDTO;
import org.cancermodels.admin.mappers.SuggestionMapper;
import org.cancermodels.exception_handling.ResourceNotFoundException;
import org.cancermodels.exceptions.SearchException;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.pdcm_admin.persistance.Suggestion;
import org.cancermodels.mappings.MappingEntityService;
import org.cancermodels.mappings.suggestions.SuggestionManager;
import org.cancermodels.process_report.ProcessResponse;
import org.cancermodels.suggestions.indexers.Indexer;
import org.cancermodels.suggestions.search_engine.SuggestionsSearcher;
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
  private final Indexer indexer;
  private final MappingEntityService mappingEntityService;
  private final SuggestionsSearcher suggestionsSearcher;

  private final SuggestionManager suggestionManager;
  private final SuggestionMapper suggestionMapper;

  public IndexerController(
      Indexer indexer,
      MappingEntityService mappingEntityService,
      SuggestionsSearcher suggestionsSearcher,
      SuggestionManager suggestionManager,
      SuggestionMapper suggestionMapper) {
    this.indexer = indexer;
    this.mappingEntityService = mappingEntityService;
    this.suggestionsSearcher = suggestionsSearcher;
    this.suggestionManager = suggestionManager;
    this.suggestionMapper = suggestionMapper;
  }

  @PutMapping("index")
  public ProcessResponse indexAll() throws IOException {
    return indexer.index();
  }

  @PutMapping("index/ontologies")
  public void indexOntologies() throws IOException {
    indexer.indexOntologies();
  }

  @PutMapping("index/rules")
  public void indexRules() throws IOException {
    indexer.indexRules();
  }

  @PutMapping("index/helperDocuments")
  public void indexHelperDocuments() throws IOException {
    indexer.indexHelperDocuments();
  }

  @GetMapping("calculateSuggestions/{id}")
  List<SuggestionDTO> getMappingEntity(@PathVariable int id) throws IOException {
    List<SuggestionDTO> suggestionDTOS = new ArrayList<>();
    MappingEntity mappingEntity = mappingEntityService.findById(id).orElseThrow(
        ResourceNotFoundException::new);
    List<Suggestion> results = suggestionsSearcher.searchTopSuggestions(mappingEntity);
    results.forEach(x -> suggestionDTOS.add(suggestionMapper.convertToDto(x)));
    return suggestionDTOS;
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

  @GetMapping("getHelperSuggestion/{id}")
  public Suggestion getHelperDoc(@PathVariable int id) throws SearchException {
    MappingEntity mappingEntity = mappingEntityService.findById(id).orElseThrow(
        ResourceNotFoundException::new);
    return suggestionsSearcher.getHelperSuggestion(mappingEntity);
  }

}
