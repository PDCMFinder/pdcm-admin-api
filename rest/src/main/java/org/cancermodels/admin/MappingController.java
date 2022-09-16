package org.cancermodels.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.cancermodels.admin.dtos.SuggestionDTO;
import org.cancermodels.admin.mappers.SuggestionMapper;
import org.cancermodels.types.MappingType;
import org.cancermodels.reader.MissingMappingsService;
import org.cancermodels.mappings.suggestions.SuggestionManager;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.mappings.MappingEntityService;
import org.cancermodels.admin.dtos.MappingEntityDTO;
import org.cancermodels.admin.mappers.MappingEntityMapper;
import org.cancermodels.persistance.Suggestion;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/mappings")
public class MappingController {

  private final MappingEntityService mappingEntityService;
  private final MappingEntityMapper mappingEntityMapper;
  private final SuggestionManager suggestionManager;
  private final MissingMappingsService missingMappingsService;
  private final SuggestionMapper suggestionMapper;

  public MappingController(MappingEntityService mappingEntityService,
      MappingEntityMapper mappingEntityMapper,
      SuggestionManager suggestionManager,
      MissingMappingsService newMappingsDetectorService,
      SuggestionMapper suggestionMapper) {
    this.mappingEntityService = mappingEntityService;
    this.mappingEntityMapper = mappingEntityMapper;
    this.suggestionManager = suggestionManager;
    this.missingMappingsService = newMappingsDetectorService;
    this.suggestionMapper = suggestionMapper;
  }

  @GetMapping("/{id}")
  MappingEntityDTO getMappingEntity(@PathVariable int id) {
    MappingEntity mappingEntity = mappingEntityService.findById(id).orElseThrow(
        ResourceNotFoundException::new);

    return mappingEntityMapper.convertToDto(mappingEntity);
  }

  @PutMapping("/{id}")
  public MappingEntityDTO updateMappingEntity(
      @PathVariable int id, @RequestBody MappingEntity mappingEntity) {

    MappingEntity updated = mappingEntityService.update(id, mappingEntity, MappingType.MANUAL).orElseThrow(
        ResourceNotFoundException::new);

    return mappingEntityMapper.convertToDto(updated);
  }

  /**
   * Return a list of suggestion for the given Mapping Entity.
   * The mapping entity can already have calculated suggestions, in which case those are
   * returned directly. If the mapping entity does not have any calculated suggestion, then
   * the suggestion calculation process is called and the found suggestions retuned.
   * @param id Internal id of the mapping entity.
   * @return List of suggestions
   */
  @PostMapping("/{id}/suggestions")
  List<SuggestionDTO> getSuggestions(@PathVariable int id) throws IOException {
    List<SuggestionDTO> suggestionDTOS = new ArrayList<>();
    MappingEntity mappingEntity = mappingEntityService.findById(id).orElseThrow(
        ResourceNotFoundException::new);
    if (mappingEntity.getSuggestions().isEmpty()) {
      suggestionManager.calculateSuggestions(Collections.singletonList(mappingEntity));
    }
    List<Suggestion> suggestions = mappingEntity.getSuggestions();
    suggestions.forEach(x -> suggestionDTOS.add(suggestionMapper.convertToDto(x)));

    suggestionDTOS.sort(Comparator.comparing(SuggestionDTO::getRelativeScore).reversed());
    return suggestionDTOS;
  }

  @GetMapping("/calculateSuggestions")
  public void getSimilar() throws IOException {
    mappingEntityService.setMappingSuggestions();
  }

  /**
   * Reads treatment and diagnosis data and detects terms that are unmapped, creating the
   * corresponding mapping entities (unmapped) so the curator can map them later.
   * @return a map with the counts of the new detected terms.
   */
  @PutMapping("/detectNewMappings")
  public Map<String, Integer> detectNewMappings() {
    return missingMappingsService.detectNewUnmappedTerms();
  }

}
