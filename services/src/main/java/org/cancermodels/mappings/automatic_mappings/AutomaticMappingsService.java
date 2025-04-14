package org.cancermodels.mappings.automatic_mappings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.cancer_models.entity2ontology.exceptions.MalformedMappingConfigurationException;
import org.cancer_models.entity2ontology.exceptions.MappingException;
import org.cancermodels.mappings.suggestions.SimilarityConfigurationReader;
import org.cancermodels.pdcm_admin.EntityTypeName;
import org.cancermodels.mappings.MappingEntityService;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.pdcm_admin.persistance.Suggestion;
import org.cancermodels.process_report.ProcessResponse;
import org.cancermodels.pdcm_admin.types.MappingType;
import org.cancermodels.pdcm_admin.types.Status;
import org.cancermodels.util.Utilities;
import org.springframework.stereotype.Service;

/**
 * A class to manage task related to the automatic mapping of mapping entities.
 */
@Service
@Slf4j
public class AutomaticMappingsService {

    private final MappingEntityService mappingEntityService;
    private final AutomaticMappingsFinder automaticMappingsFinder;

    private final SimilarityConfigurationReader similarityConfigurationReader;

    public AutomaticMappingsService(
        MappingEntityService mappingEntityService,
        AutomaticMappingsFinder automaticMappingsFinder,
        SimilarityConfigurationReader similarityConfigurationReader) {
        this.mappingEntityService = mappingEntityService;
        this.automaticMappingsFinder = automaticMappingsFinder;
        this.similarityConfigurationReader = similarityConfigurationReader;
    }

    /**
     * This is a test/evaluation method to check how good the automatic mapping process is.
     * It's done by taking all mapping entities and calculate the best suggestion (the suggestion).
     * There are 3 scenarios:
     * 1) The suggestion's suggested term url is the same as the one in the mapped term.
     * This is the successful case, showing that the automatic mapping and the actual mapping agree.
     * 2) The suggestion's suggested term url is NOT the same as the one in the mapped term.
     * This is a scenario to check, as there is no agreement between the automatic process and the
     * real mapping.
     * 3) There is not a suggestion.
     * This is another scenario to check, as it indicates that a probably successful mapping was
     * done in the past, but it cannot be replicated with the current logic.
     */
    public Map<String, Integer> evaluateAutomaticMappingsInMappedEntities()
        throws MalformedMappingConfigurationException, MappingException {
        Map<String, Integer> report = new HashMap<>();
        report.put("matching", 0);
        report.put("not_matching", 0);
        report.put("not_suggestion", 0);
        List<List<String>> notMatchingDetails = new ArrayList<>();
        List<List<String>> notSuggestionsDetails = new ArrayList<>();
        List<MappingEntity> mappingEntities =
            mappingEntityService.getAllByStatus(Status.MAPPED.getLabel());

        int counter = 0;


        // Temporal filter to make things faster
//    mappingEntities = mappingEntities.stream().filter(x -> x.getEntityType().getName().equalsIgnoreCase(
//        EntityTypeName.Diagnosis.getLabel())).collect(Collectors.toList());
//    mappingEntities = mappingEntities.subList(0, Math.min(mappingEntities.size(), 500));

        int total = mappingEntities.size();

        for (MappingEntity mappingEntity : mappingEntities) {
            counter++;
            Optional<Suggestion> optionalSuggestion =
                automaticMappingsFinder.findBestSuggestion(mappingEntity);

            if (optionalSuggestion.isPresent()) {
                Suggestion suggestion = optionalSuggestion.get();
                if (suggestion.getSuggestedTermUrl().equalsIgnoreCase(mappingEntity.getMappedTermUrl())) {
                    report.compute("matching", (key, val) -> (val == null) ? 1 : val + 1);
                } else {
                    report.compute("not_matching", (key, val) -> (val == null) ? 1 : val + 1);
                    List<String> details = Arrays.asList(
                        mappingEntity.getId().toString(),
                        suggestion.getSuggestedTermLabel(),
                        Utilities.urlToNCIt(suggestion.getSuggestedTermUrl()),
                        mappingEntity.getMappedTermLabel(),
                        Utilities.urlToNCIt(mappingEntity.getMappedTermUrl()),
                        mappingEntity.getValuesAsMap().toString());
                    notMatchingDetails.add(details);
                }
            } else {
                report.compute("not_suggestion", (key, val) -> (val == null) ? 1 : val + 1);
                List<String> details = Arrays.asList(
                    mappingEntity.getId().toString(),
                    mappingEntity.getMappedTermLabel(),
                    Utilities.urlToNCIt(mappingEntity.getMappedTermUrl()),
                    mappingEntity.getValuesAsMap().toString());
                notSuggestionsDetails.add(details);
            }
            if (counter % 100 == 0 || counter < 100 || total - counter < 100) {
                System.out.println("Processed " + counter + " from " + mappingEntities.size());
            }
        }
        printReportNotMatching(notMatchingDetails);
        printReportNotSuggestion(notSuggestionsDetails);
        return report;
    }

    private void printReportNotMatching(List<List<String>> details) {
        System.out.println("NOT MATCHING ELEMENTS");
        System.out.println("Id|Suggested url|Suggested label|Current url|Current label|data");
        for (List<String> element : details) {
            System.out.println(String.join("|", element));
        }
    }

    private void printReportNotSuggestion(List<List<String>> details) {
        System.out.println("NO SUGGESTIONS FOUND");
        System.out.println("Id|Current url|Current label|data");
        for (List<String> element : details) {
            System.out.println(String.join("|", element));
        }
    }

    /**
     * Takes all unmapped entities and calculates the best mapping suggestion for each one. If any is
     * found, assigns it as an automatic mapping.
     *
     * @return {@link ProcessResponse} object with the count of elements that were mapped by type
     */
    public ProcessResponse assignAutomaticMappings() throws MalformedMappingConfigurationException, MappingException {
        log.info("Init assign Automatic Mappings process");
        Map<String, String> response = new HashMap<>();
        int totalUnmappedTreatment;
        int totalUnmappedDiagnosis;
        int totalAutomaticMappedTreatments = 0;
        int totalAutomaticMappedDiagnosis = 0;

        List<MappingEntity> unmappedEntities = mappingEntityService.getAllByStatus(Status.UNMAPPED.getLabel());
        log.info("Got all unmapped entities. Count: {}", unmappedEntities.size());

        List<MappingEntity> treatmentEntities =
            unmappedEntities.stream().filter(
                    x -> x.getEntityType().getName().equalsIgnoreCase(EntityTypeName.Treatment.getLabel()))
                .toList();
        totalUnmappedTreatment = treatmentEntities.size();
        log.info("Unmapped treatments. Count: {}", unmappedEntities.size());

        List<MappingEntity> diagnosisEntities =
            unmappedEntities.stream().filter(
                    x -> x.getEntityType().getName().equalsIgnoreCase(EntityTypeName.Diagnosis.getLabel()))
                .collect(Collectors.toList());
        totalUnmappedDiagnosis = diagnosisEntities.size();
        log.info("Unmapped diagnosis. Count: {}", unmappedEntities.size());

        log.info("Starts automatic assignation diagnosis");
        totalAutomaticMappedDiagnosis = assignAutomaticMappingsByType(diagnosisEntities);
        log.info("Starts automatic assignation treatments");
        totalAutomaticMappedTreatments = assignAutomaticMappingsByType(treatmentEntities);

        // Save in db
        log.info("Saving into db");
        mappingEntityService.savAll(diagnosisEntities);
        // NOTE: Disabling automatic mappings of treatments as there are several false positive mappings
        // in treatment names that require some changes in code. Maybe setting up a more strict threshold?
        mappingEntityService.savAll(treatmentEntities);
        log.info("Saving into db finished");

        response.put("Treatment", totalAutomaticMappedTreatments + " from " + totalUnmappedTreatment);
        response.put("Diagnosis", totalAutomaticMappedDiagnosis + " from " + totalUnmappedDiagnosis);

        return new ProcessResponse(response);
    }

    public int assignAutomaticMappingsByType(List<MappingEntity> mappingEntities) throws MalformedMappingConfigurationException, MappingException {
        int automaticDirectThreshold = similarityConfigurationReader.getAutomaticDirectThreshold();

        int automaticMappingsCount = 0;
        for (MappingEntity unmapped : mappingEntities) {
            Optional<Suggestion> optionalSuggestion = automaticMappingsFinder.findBestSuggestion(unmapped);
            // Check if a suitable suggestion was found
            if (optionalSuggestion.isPresent()) {
                Suggestion suggestion = optionalSuggestion.get();
                assignMapping(unmapped, suggestion, automaticDirectThreshold);
                automaticMappingsCount++;
                log.info("automatic mapped count: {}", automaticMappingsCount);
            }
        }
        return automaticMappingsCount;
    }

    private void assignMapping(MappingEntity mappingEntity, Suggestion suggestion, int automaticDirectThreshold) {
        String mappedTermUrl = suggestion.getSuggestedTermUrl();
        String mappedTermLabel = suggestion.getSuggestedTermLabel();

        String mappingType = MappingType.AUTOMATIC_REVIEW.getLabel();
        LocalDateTime updateTime = LocalDateTime.now();
        String sourceType = suggestion.getSourceType();

        String status = Status.REVIEW.getLabel();
        // If the relative score is "perfect", move directly to mapped, as it doesn't need to be reviewed
        if (suggestion.getRelativeScore() >= automaticDirectThreshold) {
            mappingType = MappingType.AUTOMATIC_MAPPED.getLabel();
            status = Status.MAPPED.getLabel();
        }

        mappingEntity.setMappedTermUrl(mappedTermUrl);
        mappingEntity.setMappedTermLabel(mappedTermLabel);
        mappingEntity.setSource(sourceType);
        mappingEntity.setMappingType(mappingType);
        mappingEntity.setDateUpdated(updateTime);
        mappingEntity.setStatus(status);
    }

}
