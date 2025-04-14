package org.cancermodels.mappings.suggestions;

import org.cancer_models.entity2ontology.exceptions.MalformedMappingConfigurationException;
import org.cancer_models.entity2ontology.exceptions.MappingException;
import org.cancer_models.entity2ontology.map.model.MappingConfiguration;
import org.cancer_models.entity2ontology.map.model.MappingRequest;
import org.cancer_models.entity2ontology.map.model.MappingResponse;
import org.cancer_models.entity2ontology.map.model.SourceEntity;
import org.cancer_models.entity2ontology.map.service.MappingIO;
import org.cancer_models.entity2ontology.map.service.MappingRequestService;
import org.cancer_models.entity2ontology.map.service.OntologySuggestionsFinder;
import org.cancer_models.entity2ontology.map.service.SuggestionsFinder;
import org.cancermodels.pdcm_admin.persistance.Suggestion;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service class that acts as a bridge to the search functionality in Entity2Ontology.
 * The purpose of the service is to return a list of suggestions for an {@code input}, which is expected
 * to be a label of an ontology term.
 * Only matches that correspond to ontology terms are considered.
 */
@Component
public class OntologySuggestionsService {

    @Value("${lucene_index_dir}")
    private String luceneIndexDir;

    @Value("${number_of_suggested_mappings}")
    private String numberOfSuggestedMappings;

    private static final String MAPPING_CONFIG_FILE = "conf/pdcmMappingConfiguration.json";

    private final MappingRequestService mappingRequestService;

    private final E2oSuggestionMapper e2oSuggestionMapper;

    private final SuggestionsFinder suggestionsFinder;


    public OntologySuggestionsService(MappingRequestService mappingRequestService, E2oSuggestionMapper e2oSuggestionMapper, @Qualifier("ontologySuggestionsFinder") SuggestionsFinder suggestionsFinder) {
        this.mappingRequestService = mappingRequestService;
        this.e2oSuggestionMapper = e2oSuggestionMapper;
        this.suggestionsFinder = suggestionsFinder;
    }

    /**
     * Finds a list of suggestions for a given `input`.
     * @param input A string to use when querying for ontology terms
     * @return List of {@link Suggestion}
     * @throws MalformedMappingConfigurationException if there is an error in the mapping configuration file
     * @throws MappingException if there is an error when mapping the entity
     */
    public List<Suggestion> findOntologySuggestions(String input, String entityType)
        throws MalformedMappingConfigurationException, MappingException, IOException {
        SourceEntity sourceEntity = createSourceEntity(input, entityType);
        MappingConfiguration mappingConfiguration = MappingIO.readMappingConfiguration(MAPPING_CONFIG_FILE);

        List<org.cancer_models.entity2ontology.map.model.Suggestion> e2oSuggestions
            = suggestionsFinder.findSuggestions(sourceEntity, luceneIndexDir, 50, mappingConfiguration);

        return e2oSuggestions.stream()
            .map(e2oSuggestionMapper::e2oSuggestionToSuggestion).collect(Collectors.toList());
    }

    private SourceEntity createSourceEntity(String input, String entityType) {
        SourceEntity sourceEntity = new SourceEntity();
        sourceEntity.setId("free-text-entry");
        sourceEntity.setType(entityType);
        Map<String, String> data = new HashMap<>();
        if ("treatment".equalsIgnoreCase(entityType)) {
            data.put("TreatmentName", input);
        } else {
            data.put("SampleDiagnosis", input);
        }
        sourceEntity.setData(data);
        return sourceEntity;
    }

    private MappingRequest getMappingRequest(String input, String entityType) {
        SourceEntity sourceEntity = new SourceEntity();
        sourceEntity.setId("free-text-entry");
        sourceEntity.setType(entityType);
        Map<String, String> data = new HashMap<>();
        if ("treatment".equalsIgnoreCase(entityType)) {
            data.put("TreatmentName", input);
        } else {
            data.put("SampleDiagnosis", input);
        }
        sourceEntity.setData(data);

        return new MappingRequest(
            Integer.parseInt(numberOfSuggestedMappings), luceneIndexDir, MAPPING_CONFIG_FILE, List.of(sourceEntity));
    }
}
