package org.cancermodels.ontologies;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cancermodels.OntologyLoadReport;
import org.springframework.stereotype.Service;

/**
 * This class expose all public methods related to Ontologies
 */
@Service
public class OntologyService {
  private final OntologyLoader ontologyLoader;
  private final OntologyTermService ontologyTermService;
  private final OntologyLoadReporter ontologyLoadReporter;

  public OntologyService(OntologyLoader ontologyLoader,
      OntologyTermService ontologyTermService,
      OntologyLoadReporter ontologyLoadReportService) {
    this.ontologyLoader = ontologyLoader;
    this.ontologyTermService = ontologyTermService;
    this.ontologyLoadReporter = ontologyLoadReportService;
  }

  /**
   * Loads the ontologies using OLS as a source. It loads diagnosis, treatments and regimens
   * ontology terms (as well as their synonyms).
   * @return A {@link OntologyLoadReport} object with information about number of terms loaded and
   * errors if any
   */
  public OntologyLoadReport loadOntologies() {
    return ontologyLoader.loadOntologies();
  }

  /**
   * Return the names of the ontology types
   * @return List of strings with the names of the ontology types
   */
  public List<String> getOntologyTypes() {
    List<String> types = new ArrayList<>();
    for (OntologyTermType type : OntologyTermType.values()) {
      types.add(type.getDescription());
    }
    return types;
  }

  /**
   * Returns the number of ontology terms by type as well as information about the loading process.
   * @return {@link OntologySummary} object with stats related to the ontologies and the loading
   * process.
   */
  public OntologySummary getOntologySummary() {
    OntologySummary ontologySummary = new OntologySummary();

    List<OntologyLoadReport> twoLastReports = ontologyLoadReporter.getLastNReports(2);
    OntologyLoadReport latestReport = null;
    OntologyLoadReport previousReport = null;
    if (!twoLastReports.isEmpty()) {
      latestReport = twoLastReports.get(0);
      if (twoLastReports.size() == 2) {
        previousReport = twoLastReports.get(1);
      }
    }

    Map<String, Long> countsByType = new HashMap<>();
    Map<String, Long> countAddedTermsLatestLoadByType = new HashMap<>();
    Map<String, Long> countAddedTermsPreviousLoadByType = new HashMap<>();
    String errors = null;
    LocalDateTime latestLoadDate = null;
    LocalDateTime previousLoadDate = null;

    if (latestReport != null) {
      countAddedTermsLatestLoadByType = ontologyLoadReporter.countsByType(latestReport);
      errors = latestReport.getErrorMessage();
      latestLoadDate = latestReport.getLoadingDateTime();
    }
    if (previousReport != null) {
      countAddedTermsPreviousLoadByType = ontologyLoadReporter.countsByType(previousReport);
      previousLoadDate = previousReport.getLoadingDateTime();
    }

    for (String type : getOntologyTypes()) {
      countsByType.put(type, ontologyTermService.getCountByType(type));

    }
    ontologySummary.setTotalCount(ontologyTermService.count());
    ontologySummary.setCountsByType(countsByType);
    ontologySummary.setCountAddedTermsLatestLoadByType(countAddedTermsLatestLoadByType);
    ontologySummary.setCountAddedTermsPreviousLoadByType(countAddedTermsPreviousLoadByType);
    ontologySummary.setErrors(errors);
    ontologySummary.setLatestLoadingDate(latestLoadDate);
    ontologySummary.setPreviousLoadingDate(previousLoadDate);

    return ontologySummary;
  }
}
