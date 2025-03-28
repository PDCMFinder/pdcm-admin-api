package org.cancermodels.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.cancermodels.admin.dtos.MappingEntityDTO;
import org.cancermodels.admin.mappers.MappingEntityMapper;
import org.cancermodels.exception_handling.ResourceNotFoundException;
import org.cancermodels.mappings.MappingEntityService;
import org.cancermodels.mappings.discovery.UnmappedTermsDiscoverService;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.pdcm_admin.types.MappingType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/mappings")
public class MappingController {

    private final MappingEntityService mappingEntityService;
    private final MappingEntityMapper mappingEntityMapper;
    private final UnmappedTermsDiscoverService unmappedTermsDiscoverService;

    public MappingController(MappingEntityService mappingEntityService,
                             MappingEntityMapper mappingEntityMapper,
                             UnmappedTermsDiscoverService newMappingsDetectorService) {
        this.mappingEntityService = mappingEntityService;
        this.mappingEntityMapper = mappingEntityMapper;
        this.unmappedTermsDiscoverService = newMappingsDetectorService;
    }

    /**
     * Retrieves the DTO representation of a {@link MappingEntity}.
     * <p>
     * This endpoint fetches a mapping entity by its ID and converts it into a DTO representation.
     * If the specified entity does not exist, a {@link ResourceNotFoundException} is thrown.
     * </p>
     *
     * @param id Internal ID of the mapping entity.
     * @return {@link MappingEntityDTO} containing the details of the mapping entity.
     * @throws ResourceNotFoundException if the mapping entity is not found.
     */
    @Operation(
        summary = "Get mapping entity by ID",
        description = "Retrieves a mapping entity based on the provided ID and returns its DTO representation. "
            + "If the entity does not exist, a 404 error is returned."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the mapping entity"),
        @ApiResponse(responseCode = "404", description = "Mapping entity not found")
    })
    @GetMapping("/{id}")
    MappingEntityDTO getMappingEntity(@PathVariable int id) {
        MappingEntity mappingEntity = mappingEntityService.findById(id).orElseThrow(
            ResourceNotFoundException::new);

        return mappingEntityMapper.convertToDto(mappingEntity);
    }

    /**
     * Get the DTO representation of a {@link MappingEntity}.
     *
     * @param key key of the mapping entity.
     * @return {@link MappingEntityDTO} object.
     */
    @GetMapping("/get-by-key/{key}")
    MappingEntityDTO getMappingEntityByKey(@PathVariable String key) {
        MappingEntity mappingEntity = mappingEntityService.findByKey(key).orElseThrow(
            ResourceNotFoundException::new);

        return mappingEntityMapper.convertToDto(mappingEntity);
    }

    /**
     * Updates a mapping entity.
     *
     * @param id            The id of the {@link MappingEntity}.
     * @param mappingEntity the {@link MappingEntity} with the changes.
     * @return {@link MappingEntityDTO} with a DTO representation of the new mapping entity.
     */
    @PutMapping("/{id}")
    public MappingEntityDTO updateMappingEntity(
        @PathVariable int id, @RequestBody MappingEntity mappingEntity) {

        MappingEntity updated = mappingEntityService.update(id, mappingEntity, MappingType.MANUAL).orElseThrow(
            ResourceNotFoundException::new);

        return mappingEntityMapper.convertToDto(updated);
    }

    /**
     * Detects and identifies new unmapped terms in treatment and diagnosis data.
     * <p>
     * This method scans through existing treatment and diagnosis data to find terms
     * that have not been previously mapped to an ontology term.
     * When such terms are found, it creates placeholder mapping entities
     * to facilitate later curation (manual or automatic).
     *
     * @return A map where the keys are the type of entity (treatment/diagnosis)
     * and the values represent the count of occurrences for each term.
     */
    @Operation(
        summary = "Detect New Unmapped Terms",
        description = "Scans treatment and diagnosis data to identify terms without existing mappings",
        tags = {"Unmapped Terms"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully detected and recorded new unmapped terms"
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error during term detection"
    )
    @PutMapping("/detect-new-mappings")
    public Map<String, Integer> detectNewMappings() {
        return unmappedTermsDiscoverService.detectNewUnmappedTerms();
    }

}
