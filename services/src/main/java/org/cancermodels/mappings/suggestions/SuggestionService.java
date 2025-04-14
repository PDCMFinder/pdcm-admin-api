package org.cancermodels.mappings.suggestions;

import org.cancer_models.entity2ontology.exceptions.MalformedMappingConfigurationException;
import org.cancer_models.entity2ontology.exceptions.MappingException;
import org.cancer_models.entity2ontology.map.model.MappingRequest;
import org.cancer_models.entity2ontology.map.model.MappingResponse;
import org.cancer_models.entity2ontology.map.model.SourceEntity;
import org.cancer_models.entity2ontology.map.service.MappingRequestService;
import org.cancermodels.pdcm_admin.persistance.*;
import org.cancermodels.util.FileManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class that acts as a bridge to the search functionality in Entity2Ontology.
 * The purpose of the service is to return a list of suggestions for a given {@link MappingEntity}
 */
@Component
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;

    @Value("${lucene_index_dir}")
    private String luceneIndexDir;

    @Value("${number_of_suggested_mappings}")
    private String numberOfSuggestedMappings;

    private static final String MAPPING_CONFIG_FILE = "pdcmMappingConfiguration.json";

    private final MappingRequestService mappingRequestService;
    private final MappingEntityRepository mappingEntityRepository;
    private final E2oSuggestionMapper e2oSuggestionMapper;

    public SuggestionService(
        MappingRequestService mappingRequestService,
        MappingEntityRepository mappingEntityRepository,
        SuggestionRepository suggestionRepository,
        E2oSuggestionMapper e2oSuggestionMapper) {
        this.mappingRequestService = mappingRequestService;
        this.mappingEntityRepository = mappingEntityRepository;
        this.suggestionRepository = suggestionRepository;
        this.e2oSuggestionMapper = e2oSuggestionMapper;
    }

    /**
     * Finds a list of suggestions for a given mapping entity.
     * @param mappingEntity Mapping entity for which the suggestions will be calculated
     * @return List of {@link Suggestion}
     * @throws MalformedMappingConfigurationException if there is an error in the mapping configuration file
     * @throws MappingException if there is an error when mapping the entity
     */
    public List<Suggestion> findSuggestions(MappingEntity mappingEntity)
        throws MalformedMappingConfigurationException, MappingException {

        // This assures we have the correct path even in environments like kubernetes
        String mappingConfFilePath = null;
        try {
            mappingConfFilePath = FileManager.getTmpPathForResource(MAPPING_CONFIG_FILE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MappingRequest mappingRequest = new MappingRequest(
            Integer.parseInt(numberOfSuggestedMappings),
            luceneIndexDir,
            mappingConfFilePath,
            List.of(mappingEntityToSourceEntity(mappingEntity))
        );
        MappingResponse response = mappingRequestService.processMappingRequest(mappingRequest);

        List<org.cancer_models.entity2ontology.map.model.Suggestion> e2oSuggestions
            = response.getMappingsResults().getFirst().getSuggestions();

        return e2oSuggestions.stream()
            .filter(x -> !x.getTargetEntity().id().equals(mappingEntity.getMappingKey()))
            .map(e2oSuggestionMapper::e2oSuggestionToSuggestion).collect(Collectors.toList());
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
     * @param mappingEntity Mapping entity to process
     * @return A sorted list of {@link Suggestion} objects, representing the mapping suggestions.
     * @throws MalformedMappingConfigurationException if there is an error in the mapping configuration file
     * @throws MappingException if there is an error when mapping the entity
     */
    public List<Suggestion> retrieveOrComputeMappingSuggestions(MappingEntity mappingEntity)
        throws MalformedMappingConfigurationException, MappingException {
        List<Suggestion> suggestions = mappingEntity.getSuggestions();
        if (suggestions.isEmpty()) {
            suggestions = regenerateSuggestions(mappingEntity);
        }
        return suggestions;
    }

    /**
     * Deletes any suggestion associated to the entity and calculates again a set of new suggestions.
     * <p>
     * As suggestions are stored in the db, any new recalculation needs to delete the current set
     * of suggestions first. This method deletes all suggestions for the entity in the database and
     * then recalculates the suggestions and updates the database with them.
     * </p>
     *
     * @param mappingEntity Mapping entity to process
     */
    public List<Suggestion> regenerateSuggestions(MappingEntity mappingEntity)
        throws MalformedMappingConfigurationException, MappingException {
        deleteSuggestions(mappingEntity);
        return findAndStoreNewSuggestions(mappingEntity);
    }

    private void deleteSuggestions(MappingEntity mappingEntity) {
        mappingEntity.getSuggestions().clear();
        mappingEntityRepository.save(mappingEntity);
    }

    private List<Suggestion> findAndStoreNewSuggestions(MappingEntity mappingEntity)
        throws MalformedMappingConfigurationException, MappingException {
        List<Suggestion> suggestions = findSuggestions(mappingEntity);
        mappingEntity.getSuggestions().addAll(suggestions);
        suggestionRepository.saveAll(suggestions);
        return suggestions;
    }

    private SourceEntity mappingEntityToSourceEntity(MappingEntity mappingEntity) {
        SourceEntity sourceEntity = new SourceEntity();
        sourceEntity.setId(mappingEntity.getId().toString());
        sourceEntity.setType(mappingEntity.getEntityType().getName());
        sourceEntity.setData(mappingEntity.getValuesAsMap());
        return sourceEntity;
    }

}
