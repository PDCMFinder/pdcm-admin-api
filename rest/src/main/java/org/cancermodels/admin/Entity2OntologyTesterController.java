package org.cancermodels.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.cancer_models.entity2ontology.exceptions.MalformedMappingConfigurationException;
import org.cancer_models.entity2ontology.exceptions.MappingException;
import org.cancer_models.entity2ontology.map.model.MappingRequest;
import org.cancer_models.entity2ontology.map.model.MappingResponse;
import org.cancer_models.entity2ontology.map.model.SourceEntity;
import org.cancer_models.entity2ontology.map.model.Suggestion;
import org.cancer_models.entity2ontology.map.service.MappingRequestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller to directly call entity2Ontology to test how entries are mapped
 */
@Tag(name = "Entity2Ontology Tester", description = "Endpoints to test raw treatment/diagnosis entries")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/e2o-tester")
public class Entity2OntologyTesterController {

    @Value("${lucene_index_dir}")
    private String luceneIndexDir;

    private static final int numberOfSuggestedMappings = 10;

    private static final String MAPPING_CONFIG_FILE = "conf/pdcmMappingConfiguration.json";

    private final MappingRequestService mappingRequestService;

    public Entity2OntologyTesterController(MappingRequestService mappingRequestService) {
        this.mappingRequestService = mappingRequestService;
    }

    /**
     * Calculates the suggestions for a treatment entry without storing them in the db.
     * @param data Treatment entity data
     * @return list of suggestions
     * @throws MalformedMappingConfigurationException if there is an error in the mapping configuration file
     * @throws MappingException if there is an error when mapping the entity
     */
    @GetMapping("test-treatment")
    List<Suggestion> getSuggestionsForTreatment(@RequestBody Map<String, String> data)
        throws MalformedMappingConfigurationException, MappingException {
        SourceEntity sourceEntity = new SourceEntity();
        sourceEntity.setId("treatment-entry");
        sourceEntity.setType("treatment");
        sourceEntity.setData(data);
        return searchForSuggestions(sourceEntity);
    }

    /**
     * Calculates the suggestions for a diagnosis entry without storing them in the db.
     * @param data Diagnosis entity data
     * @return list of suggestions
     * @throws MalformedMappingConfigurationException if there is an error in the mapping configuration file
     * @throws MappingException if there is an error when mapping the entity
     */
    @GetMapping("test-diagnosis")
    List<Suggestion> getSuggestionsForDiagnosis(@RequestBody Map<String, String> data)
        throws MalformedMappingConfigurationException, MappingException {
        SourceEntity sourceEntity = new SourceEntity();
        sourceEntity.setId("diagnosis-entry");
        sourceEntity.setType("diagnosis");
        sourceEntity.setData(data);
        return searchForSuggestions(sourceEntity);
    }

    private List<Suggestion> searchForSuggestions(SourceEntity sourceEntity)
        throws MalformedMappingConfigurationException, MappingException {
        MappingRequest mappingRequest = new MappingRequest(
            numberOfSuggestedMappings,
            luceneIndexDir,
            MAPPING_CONFIG_FILE,
            List.of(sourceEntity)
        );
        MappingResponse response = mappingRequestService.processMappingRequest(mappingRequest);

        return response.getMappingsResults().getFirst().getSuggestions();
    }
}
