package org.cancermodels.mappings.automatic_mappings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.cancermodels.mappings.MappingEntityService;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.Suggestion;
import org.cancermodels.types.Status;
import org.cancermodels.util.Utilities;
import org.springframework.stereotype.Service;

/**
 * A class to manage task related to the automatic mapping of mapping entities.
 */
@Service
public class AutomaticMappingsService {

  private final MappingEntityService mappingEntityService;
  private final AutomaticMappingsFinder automaticMappingsFinder;

  public AutomaticMappingsService(
      MappingEntityService mappingEntityService,
      AutomaticMappingsFinder automaticMappingsFinder) {
    this.mappingEntityService = mappingEntityService;
    this.automaticMappingsFinder = automaticMappingsFinder;
  }

  /**
   * This is a test/evaluation method to check how good the automatic mapping process is.
   * It's done by taking all mapping entities and calculate the best suggestion (the suggestion).
   * There are 3 scenarios:
   * 1) The suggestion's suggested term url is the same as the one in the mapped term.
   *    This is the successful case, showing that the automatic mapping and the actual mapping agree.
   * 2) The suggestion's suggested term url is NOT the same as the one in the mapped term.
   *    This is a scenario to check, as there is no agreement between the automatic process and the
   *    real mapping.
   * 3) There is not a suggestion.
   *    This is another scenario to check, as it indicates that a probably successful mapping was
   *    done in the past, but it cannot be replicated with the current logic.
   */
  public Map<String, Integer> evaluateAutomaticMappingsInMappedEntities() {
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
}
