package org.cancermodels.admin;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.cancermodels.MappingEntity;
import org.cancermodels.OntologySuggestion;
import org.cancermodels.OntologyTerm;
import org.cancermodels.mappings.MappingEntityService;
import org.cancermodels.admin.dtos.MappingEntityDTO;
import org.cancermodels.admin.mappers.MappingEntityMapper;
import org.cancermodels.mappings.MappingSummaryByTypeAndProvider;
import org.cancermodels.mappings.search.MappingsFilter;
import org.cancermodels.mappings.search.MappingsFilterBuilder;
import org.cancermodels.mappings.suggestions.OntologySuggestionManager;
import org.cancermodels.mappings.suggestions.MappingEntitiesSuggestionManager;
import org.cancermodels.ontologies.OntologyService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/mappings")
public class MappingController {

  private final MappingEntityService mappingEntityService;
  private final MappingEntityMapper mappingEntityMapper;
  private final MappingEntitiesSuggestionManager suggestionsManager;
  private final OntologyService ontologyService;
  private final OntologySuggestionManager ontologySuggestionManager;

  public MappingController(MappingEntityService mappingEntityService,
      MappingEntityMapper mappingEntityMapper,
      MappingEntitiesSuggestionManager suggestionsManager,
      OntologyService ontologyService,
      OntologySuggestionManager ontologySuggestionManager) {
    this.mappingEntityService = mappingEntityService;
    this.mappingEntityMapper = mappingEntityMapper;
    this.suggestionsManager = suggestionsManager;
    this.ontologyService = ontologyService;
    this.ontologySuggestionManager = ontologySuggestionManager;
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
   * @param entityTypeName Name of the entity type we want to retrieve
   * @param status Status of the mapping entity (created, ...)
   * @return Paginated Mappings that match the search criteria
   */
  @GetMapping("/search")
  public ResponseEntity search(
      Pageable pageable,
      PagedResourcesAssembler assembler,

      @RequestParam(value = "mq", required = false) List<String> mappingQuery,
      @RequestParam(value = "entityType", required = false) String entityTypeName,
      @RequestParam(value = "status", required = false) List<String> status)
  {

    MappingsFilter filter = MappingsFilterBuilder.getInstance()
        .withEntityTypeName(entityTypeName)
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
                    pageable, assembler, mappingQuery, entityTypeName, status)).withSelfRel());

    HttpHeaders responseHeaders = new HttpHeaders();
    return new ResponseEntity(pr, responseHeaders, HttpStatus.OK);
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

  // This is a testing endpoint
  @GetMapping("/testSuggestion")
  public void testSuggestion() {
    Optional<MappingEntity> mappingEntity = mappingEntityService.findById(819699);
    var all = mappingEntityService.getAllByTypeName("treatment");
    suggestionsManager.testOne(mappingEntity.get(), all);
    System.out.println("end");
  }

  // This is a testing endpoint
  @GetMapping("/testOntoSuggestion")
  public Map<MappingEntity, List<OntologySuggestion>> testOntoSuggestion() {
    Optional<MappingEntity> mappingEntity = mappingEntityService.findById(820905);
    List<MappingEntity> list = Arrays.asList(mappingEntity.get());
    List<OntologyTerm> ontologyTerms = ontologyService.getAllByType("treatment");
//    var all = mappingEntityService.getAllByTypeName("treatment");
//    suggestionsManager.testOne(mappingEntity.get(), all);
    var rest = ontologySuggestionManager.calculateSuggestions(list, ontologyTerms, "treatment");
    System.out.println(rest);
    return rest;
  }
}
