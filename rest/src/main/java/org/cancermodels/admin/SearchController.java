package org.cancermodels.admin;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.cancermodels.admin.dtos.MappingEntityDTO;
import org.cancermodels.admin.mappers.MappingEntityMapper;
import org.cancermodels.mappings.search.MappingsFilter;
import org.cancermodels.mappings.search.MappingsFilterBuilder;
import org.cancermodels.mappings.search.SearchService;
import org.cancermodels.pdcm_admin.persistance.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/mappings")
public class SearchController {

  private final SearchService searchService;
  private final MappingEntityMapper mappingEntityMapper;


  public SearchController(SearchService searchService, MappingEntityMapper mappingEntityMapper) {
    this.searchService = searchService;
    this.mappingEntityMapper = mappingEntityMapper;
  }

  /**
   * Allows to find mappings according to several search criteria. It returns the results in pages
   * and with the hateoas format. All searches are case-insensitive.
   * @param pageable Object with the pagination information. It maps the page, size, and sort
   *                 parameters automatically.
   *
   * @param assembler Required for a hateoas representation of the response

   * @param mappingQuery search parameters involving the mapping labels and their values. A label
   *                     and its value are separated by ":".
   *                     Example: mq=DataSource:JAX&TumorType:Primary
   * @param entityTypeNames Name of the entity type we want to retrieve
   * @param status Status of the mapping entity (created, ...)
   * @param mappingTypes Mapping type (Automatic/Manual)
   * @param label Value of the treatment name or diagnosis
   * @return Paginated Mappings that match the search criteria
   */
  @GetMapping("/search")
  public ResponseEntity search(
      Pageable pageable,
      PagedResourcesAssembler assembler,

      @RequestParam(value = "mq", required = false) List<String> mappingQuery,
      @RequestParam(value = "entityType", required = false) List<String> entityTypeNames,
      @RequestParam(value = "status", required = false) List<String> status,
      @RequestParam(value = "mappingType", required = false) List<String> mappingTypes,
      @RequestParam(value = "label", required = false) String label)
  {
    MappingsFilter filter = MappingsFilterBuilder.getInstance()
        .withEntityTypeNames(entityTypeNames)
        .withMappingQuery(mappingQuery)
        .withStatus(status)
        .withMappingType(mappingTypes)
        .withLabel(Collections.singletonList(label))
        .build();

    Page<MappingEntity> mappingEntities = searchService.search(
        pageable, filter);
    Page<MappingEntityDTO> mappingEntityDTOS = mappingEntities.map(
        mappingEntityMapper::convertToDto);

    PagedModel pr =
        assembler.toModel(
            mappingEntityDTOS,
            linkTo(methodOn(SearchController.class)
                .search(
                    pageable, assembler, mappingQuery, entityTypeNames, status, mappingTypes, label))
                .withSelfRel());

    HttpHeaders responseHeaders = new HttpHeaders();
    return new ResponseEntity(pr, responseHeaders, HttpStatus.OK);
  }

  @GetMapping("/statusCounts")
  public Map<String, Long> getCountsByStatus(
      @RequestParam(value = "mq", required = false) List<String> mappingQuery,
      @RequestParam(value = "entityType", required = false) List<String> entityTypeNames,
      @RequestParam(value = "mappingType", required = false) List<String> mappingTypes,
      @RequestParam(value = "label", required = false) String label) {

    MappingsFilter filter = MappingsFilterBuilder.getInstance()
        .withEntityTypeNames(entityTypeNames)
        .withMappingType(mappingTypes)
        .withMappingQuery(mappingQuery)
        .withLabel(Collections.singletonList(label))
        .build();

    return searchService.countStatusWithFilter(filter);
  }

  @GetMapping("/treatmentsAndDiagnosis")
  public List<String> getAllTreatmentsAndDiagnosis() {
    return searchService.getAllTreatmentsAndDiagnosis();
  }

}
