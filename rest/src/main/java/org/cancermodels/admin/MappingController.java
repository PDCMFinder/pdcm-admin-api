package org.cancermodels.admin;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Collections;
import java.util.List;
import org.cancermodels.mappings.suggestions.SuggestionManager;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.mappings.MappingEntityService;
import org.cancermodels.admin.dtos.MappingEntityDTO;
import org.cancermodels.admin.mappers.MappingEntityMapper;
import org.cancermodels.mappings.MappingSummaryByTypeAndProvider;
import org.cancermodels.mappings.search.MappingsFilter;
import org.cancermodels.mappings.search.MappingsFilterBuilder;
import org.cancermodels.persistance.Suggestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/mappings")
public class MappingController {

  private final MappingEntityService mappingEntityService;
  private final MappingEntityMapper mappingEntityMapper;
  private final SuggestionManager suggestionManager;

  public MappingController(MappingEntityService mappingEntityService,
      MappingEntityMapper mappingEntityMapper,
      SuggestionManager suggestionManager) {
    this.mappingEntityService = mappingEntityService;
    this.mappingEntityMapper = mappingEntityMapper;
    this.suggestionManager = suggestionManager;
  }

  @GetMapping("/{id}")
  MappingEntityDTO getMappingEntity(@PathVariable int id) {
    MappingEntity mappingEntity = mappingEntityService.findById(id).orElseThrow(
        ResourceNotFoundException::new);

    return mappingEntityMapper.convertToDto(mappingEntity);
  }

  /**
   * Allows to find mappings according to several search criteria. It returns the results in pages
   * and with the hateoas format. All searches are case insensitive.
   * @param pageable Object with the pagination information. It maps the page, size, and sort
   *                 parameters automatically.
   *
   * @param assembler Required for a hateoas representation of the response

   * @param mappingQuery search parameters involving the mapping labels and their values. A label
   *                     and its value are separated by ":".
   *                     Example: mq=DataSource:JAX&TumorType:Primary
   * @param entityTypeNames Name of the entity type we want to retrieve
   * @param status Status of the mapping entity (created, ...)
   * @return Paginated Mappings that match the search criteria
   */
  @GetMapping("/search")
  public ResponseEntity search(
      Pageable pageable,
      PagedResourcesAssembler assembler,

      @RequestParam(value = "mq", required = false) List<String> mappingQuery,
      @RequestParam(value = "entityType", required = false) List<String> entityTypeNames,
      @RequestParam(value = "status", required = false) List<String> status)
  {

    MappingsFilter filter = MappingsFilterBuilder.getInstance()
        .withEntityTypeNames(entityTypeNames)
        .withMappingQuery(mappingQuery)
        .withStatus(status)
        .build();

    Page<MappingEntity> mappingEntities = mappingEntityService.findPaginatedAndFiltered(
        pageable, filter);
    Page<MappingEntityDTO> mappingEntityDTOS = mappingEntities.map(
        mappingEntityMapper::convertToDto);

    PagedModel pr =
        assembler.toModel(
            mappingEntityDTOS,
            linkTo(methodOn(MappingController.class)
                .search(
                    pageable, assembler, mappingQuery, entityTypeNames, status)).withSelfRel());

    HttpHeaders responseHeaders = new HttpHeaders();
    return new ResponseEntity(pr, responseHeaders, HttpStatus.OK);
  }

  @PutMapping("update")
  public MappingEntityDTO updateMappingEntity( @RequestBody MappingEntity mappingEntity) {

    MappingEntity updated = mappingEntityService.update(mappingEntity).orElseThrow(
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
  List<Suggestion> getSuggestions(@PathVariable int id) {
    MappingEntity mappingEntity = mappingEntityService.findById(id).orElseThrow(
        ResourceNotFoundException::new);
    if (mappingEntity.getSuggestions().isEmpty()) {
      suggestionManager.calculateSuggestions(Collections.singletonList(mappingEntity));
    }
    return mappingEntity.getSuggestions();
  }

  @GetMapping("/getSummary")
  public MappingSummaryByTypeAndProvider getMappingSummaryByTypeAndProvider(
      @RequestParam(value = "entityTypeName") String entityTypeName) {
    return mappingEntityService.getSummaryByTypeAndProvider(entityTypeName);
  }

  @GetMapping("/calculateSuggestions")
  public void getSimilar() {
    mappingEntityService.setMappingSuggestions();
  }

}
