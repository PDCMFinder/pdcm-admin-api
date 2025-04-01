package org.cancermodels.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cancermodels.admin.dtos.SuggestionDTO;
import org.cancermodels.admin.mappers.SuggestionMapper;
import org.cancermodels.exception_handling.ResourceNotFoundException;
import org.cancermodels.mappings.MappingEntityService;
import org.cancermodels.mappings.automatic_mappings.AutomaticMappingsService;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.pdcm_admin.persistance.Suggestion;
import org.cancermodels.process_report.ProcessResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Controller in charge of the mapping process task
 */
@Tag(name = "Mapping process", description = "Operations related to the mapping process")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/mappings")
public class MappingProcessController {

    private final AutomaticMappingsService automaticMappingsService;
    private final MappingEntityService mappingEntityService;
    //private final SuggestionManager suggestionManager;
    private final SuggestionMapper suggestionMapper;

    public MappingProcessController(
        AutomaticMappingsService automaticMappingsService,
        MappingEntityService mappingEntityService,
        //SuggestionManager suggestionManager,
        SuggestionMapper suggestionMapper) {
        this.automaticMappingsService = automaticMappingsService;
        this.mappingEntityService = mappingEntityService;
        //this.suggestionManager = suggestionManager;
        this.suggestionMapper = suggestionMapper;
    }

    /**
     * Processes unmapped entities and assigns automatic ontology mappings.
     * <p>
     * This endpoint evaluates all unmapped entities and determines the most suitable ontology term
     * for each one. If a match is found, the entity is automatically assigned a mapping.
     * The response includes a count of mapped entities categorized by type.
     * </p>
     *
     * @return {@link ProcessResponse} containing the count of automatically mapped entities by type.
     */
    @Operation(
        summary = "Assign automatic ontology mappings",
        description = "Processes all unmapped entities and attempts to find the best ontology term "
            + "for each one. If a suitable match is identified, it is automatically assigned. "
            + "Returns a response with the count of successfully mapped entities by type."
    )
    @PutMapping("/auto-assign-mappings")
    public ProcessResponse assignAutomaticMappings() {
        return automaticMappingsService.assignAutomaticMappings();
    }

    /**
     * Retrieves a list of mapping suggestions for a given mapping entity.
     * <p>
     * If the mapping entity already has precomputed suggestions, they are returned directly.
     * Otherwise, the suggestion calculation process is triggered to generate new suggestions,
     * which are then returned. The results are sorted in descending order based on their
     * relative score.
     * </p>
     *
     * @param id Internal ID of the mapping entity.
     * @return A sorted list of {@link SuggestionDTO} objects, representing the mapping suggestions.
     * @throws IOException If an error occurs during suggestion retrieval.
     */
    @Operation(
        summary = "Get mapping suggestions for an entity",
        description = "Retrieves a list of mapping suggestions for a specified mapping entity. "
            + "If the entity already has precomputed suggestions, they are returned. "
            + "Otherwise, new suggestions are calculated and returned. The results "
            + "are sorted in descending order by relative score."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved suggestions"),
        @ApiResponse(responseCode = "404", description = "Mapping entity not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error during processing")
    })
    @PostMapping("/{id}/suggestions")
    public List<SuggestionDTO> getSuggestions(@PathVariable int id) throws IOException {
        List<SuggestionDTO> suggestionDTOS = new ArrayList<>();
        MappingEntity mappingEntity = mappingEntityService.findById(id)
            .orElseThrow(ResourceNotFoundException::new);

//        if (mappingEntity.getSuggestions().isEmpty()) {
//            suggestionManager.calculateSuggestions(Collections.singletonList(mappingEntity));
//        }

        List<Suggestion> suggestions = mappingEntity.getSuggestions();
        suggestions.forEach(suggestion -> suggestionDTOS.add(suggestionMapper.convertToDto(suggestion)));

        suggestionDTOS.sort(Comparator.comparing(SuggestionDTO::getRelativeScore).reversed());
        return suggestionDTOS;
    }
}
