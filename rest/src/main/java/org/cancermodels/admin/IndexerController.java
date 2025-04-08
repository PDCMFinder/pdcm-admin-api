package org.cancermodels.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cancer_models.entity2ontology.exceptions.MalformedMappingConfigurationException;
import org.cancer_models.entity2ontology.exceptions.MappingException;
import org.cancermodels.admin.dtos.SuggestionDTO;
import org.cancermodels.admin.mappers.SuggestionMapper;
import org.cancermodels.exception_handling.ResourceNotFoundException;
import org.cancermodels.mappings.IndexRequestHandler;
import org.cancermodels.mappings.MappingEntityService;
import org.cancermodels.mappings.suggestions.SuggestionService;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.pdcm_admin.persistance.Suggestion;
import org.cancermodels.process_report.ProcessResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller in charge of offering endpoints related to the index process.
 */
@Tag(name = "Indexing", description = "Operations related to the indexing process")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/indexer")
public class IndexerController {
    private final IndexRequestHandler indexRequestHandler;
    private final MappingEntityService mappingEntityService;
    private final SuggestionService suggestionService;
    private final SuggestionMapper suggestionMapper;

    public IndexerController(
        IndexRequestHandler indexRequestHandler, MappingEntityService mappingEntityService, SuggestionService suggestionService,
        SuggestionMapper suggestionMapper) {
        this.indexRequestHandler = indexRequestHandler;
        this.mappingEntityService = mappingEntityService;
        this.suggestionService = suggestionService;
        this.suggestionMapper = suggestionMapper;
    }

    /**
     * Creates a Lucene index with the rules and ontologies defined in a configuration file
     * (Index Request).
     *
     * @return a {@link ProcessResponse} object with information about the index process
     * @throws IOException if an error occurred when trying to index rules and ontologies
     */
    @Operation(
        summary = "Indexes rules and ontologies",
        description = "Creates a Lucene index with the rules and ontologies defined in a configuration file. "
            + "This operation processes the rules and ontologies and stores them in a searchable index."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Indexing completed successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ProcessResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error - An error occurred during indexing")
    })
    @PutMapping("index")
    public ProcessResponse indexAll() throws IOException {
        return indexRequestHandler.index();
    }

    @GetMapping("calculateSuggestions/{id}")
    List<SuggestionDTO> getMappingEntity(@PathVariable int id)
        throws MalformedMappingConfigurationException, MappingException {
        List<SuggestionDTO> suggestionDTOS = new ArrayList<>();
        MappingEntity mappingEntity = mappingEntityService.findById(id).orElseThrow(
            ResourceNotFoundException::new);
        List<Suggestion> results = suggestionService.findSuggestions(mappingEntity);
        results.forEach(x -> suggestionDTOS.add(suggestionMapper.convertToDto(x)));
        return suggestionDTOS;
    }
}
