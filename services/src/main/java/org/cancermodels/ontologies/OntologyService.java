package org.cancermodels.ontologies;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cancermodels.EntityType;
import org.cancermodels.MappingEntity;
import org.cancermodels.OntologyLoadReport;
import org.cancermodels.OntologyTerm;
import org.springframework.stereotype.Service;

/**
 * This class expose all public methods related to Ontologies
 */
@Service
public class OntologyService {
  private final OntologyLoader ontologyLoader;
  private final OntologyTermManager ontologyTermManager;
  private final OntologyLoadReporter ontologyLoadReporter;

  public OntologyService(OntologyLoader ontologyLoader,
      OntologyTermManager ontologyTermService,
      OntologyLoadReporter ontologyLoadReportService) {
    this.ontologyLoader = ontologyLoader;
    this.ontologyTermManager = ontologyTermService;
    this.ontologyLoadReporter = ontologyLoadReportService;
  }

  public List<OntologyTerm> getAllByType(String type) {
    return ontologyTermManager.getAllByType(type);
  }

  /**
   * Gets all ontology terms as a map, where the key is the type (treatment and regimen grouped
   * as a single one).
   * @return Map with key=type and value=ontology terms. Keys are in lowercase
   */
  public Map<String, List<OntologyTerm>> getOntologyTermsMappedByType() {
    Map<String, List<OntologyTerm>> map = new HashMap<>();

    String diagnosis = OntologyTermType.DIAGNOSIS.getDescription();
    List<OntologyTerm> diagnosisOntologyTerms = getAllByType(diagnosis);
    map.put(diagnosis.toLowerCase(), diagnosisOntologyTerms);

    String treatment = OntologyTermType.TREATMENT.getDescription();
    List<OntologyTerm> treatmentOntologyTerms = getAllByType(treatment);
    String regimen = OntologyTermType.REGIMEN.getDescription();
    List<OntologyTerm> regimenOntologyTerms = getAllByType(regimen);
    treatmentOntologyTerms.addAll(regimenOntologyTerms);
    map.put(treatment.toLowerCase(), treatmentOntologyTerms);

    return map;
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
      countsByType.put(type, ontologyTermManager.getCountByType(type));

    }
    ontologySummary.setTotalCount(ontologyTermManager.count());
    ontologySummary.setCountsByType(countsByType);
    ontologySummary.setCountAddedTermsLatestLoadByType(countAddedTermsLatestLoadByType);
    ontologySummary.setCountAddedTermsPreviousLoadByType(countAddedTermsPreviousLoadByType);
    ontologySummary.setErrors(errors);
    ontologySummary.setLatestLoadingDate(latestLoadDate);
    ontologySummary.setPreviousLoadingDate(previousLoadDate);

    return ontologySummary;
  }
}
