package org.cancermodels.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.cancer_models.entity2ontology.exceptions.MalformedMappingConfigurationException;
import org.cancer_models.entity2ontology.exceptions.MappingException;
import org.cancermodels.admin.dtos.SuggestionDTO;
import org.cancermodels.admin.mappers.SuggestionMapper;
import org.cancermodels.mappings.suggestions.OntologySuggestionsService;
import org.cancermodels.pdcm_admin.persistance.Suggestion;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that allows searching exclusively on documents representing ontology terms
 */
@Tag(name = "Ontology searcher", description = "Searches for ontologies only")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ontology")
public class OntologySearcherController {

  private final SuggestionMapper suggestionMapper;
  private final OntologySuggestionsService ontologySuggestionsService;

  public OntologySearcherController(
      SuggestionMapper suggestionMapper, OntologySuggestionsService ontologySuggestionsService) {
    this.suggestionMapper = suggestionMapper;
      this.ontologySuggestionsService = ontologySuggestionsService;
  }

  @GetMapping("/search")
  public List<SuggestionDTO> searchWithDefaultParameters(
      @RequestParam(value = "input") String input,
      @RequestParam(value = "entityTypeName") String entityTypeName)
      throws MappingException, IOException {
    List<SuggestionDTO> suggestionDTOS = new ArrayList<>();
    List<Suggestion> results = ontologySuggestionsService.findOntologySuggestions(input, entityTypeName);
    results.forEach(x -> suggestionDTOS.add(suggestionMapper.convertToDto(x)));
    return suggestionDTOS;
  }
}
