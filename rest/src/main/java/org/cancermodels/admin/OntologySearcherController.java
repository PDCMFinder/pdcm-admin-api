package org.cancermodels.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.cancermodels.admin.dtos.SuggestionDTO;
import org.cancermodels.admin.mappers.SuggestionMapper;
import org.cancermodels.persistance.Suggestion;
import org.cancermodels.suggestions.search_engine.OntologySearcherByText;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ontology")
@Slf4j
public class OntologySearcherController {

private final OntologySearcherByText ontologySearcherByText;
  private final SuggestionMapper suggestionMapper;

  public OntologySearcherController(OntologySearcherByText ontologySearcherByText,
      SuggestionMapper suggestionMapper) {
    this.ontologySearcherByText = ontologySearcherByText;
    this.suggestionMapper = suggestionMapper;
  }

  @GetMapping("/search")
  public List<SuggestionDTO> searchWithDefaultParameters(
      @RequestParam(value = "input") String input, @RequestParam(value = "entityTypeName") String entityTypeName) {
    log.info("Ontology search for: [{}]", input);
    List<SuggestionDTO> suggestionDTOS = new ArrayList<>();
    List<Suggestion> results = ontologySearcherByText.searchWithDefaultParameters(input, entityTypeName);
    log.info("Found: {} suggestions", results.size());
    results.forEach(x -> suggestionDTOS.add(suggestionMapper.convertToDto(x)));
    log.info("Returning: {} results", suggestionDTOS.size());
    return suggestionDTOS;
  }
}
