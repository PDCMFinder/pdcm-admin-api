package org.cancermodels.ontologies;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cancermodels.OntologyLoadReport;
import org.cancermodels.OntologyLoadReportRepository;
import org.cancermodels.OntologyLoadReport_;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Service
class OntologyLoadReporter {
  private final OntologyLoadReportRepository ontologyLoadReportRepository;

  public OntologyLoadReporter(OntologyLoadReportRepository ontologyLoadReportRepository) {
    this.ontologyLoadReportRepository = ontologyLoadReportRepository;
  }

  public OntologyLoadReport createReport(
      int newNumberDiagnosisTerms,
      int newNumberTreatmentTerms,
      int newNumberRegimenTerms,
      String errorMessage) {

    OntologyLoadReport newReport = new OntologyLoadReport();

    newReport.setLoadingDateTime(LocalDateTime.now());
    newReport.setNumberDiagnosisTerms(newNumberDiagnosisTerms);
    newReport.setNumberTreatmentTerms(newNumberTreatmentTerms);
    newReport.setNumberRegimenTerms(newNumberRegimenTerms);
    newReport.setErrorMessage(errorMessage);

    ontologyLoadReportRepository.save(newReport);

    return newReport;
  }

  public List<OntologyLoadReport> getLastNReports(int n) {
    Sort sort = Sort.by(Direction.DESC, OntologyLoadReport_.LOADING_DATE_TIME);
    Pageable paging = PageRequest.of(0, n, sort);
    return ontologyLoadReportRepository.findAll(paging).getContent();
  }

  /**
   *  Converts the information of counts that is in columns into a representation of a map, where
   *  the key is the ontology type name and the value is the count of loaded terms for that type.
   * @param ontologyLoadReport Report where the information will be taken from
   * @return Map in the format {type: count}.
   */
  Map<String, Long> countsByType(OntologyLoadReport ontologyLoadReport) {
    Map<String, Long> counterByType = new HashMap<>();

    for (OntologyTermType type : OntologyTermType.values()) {
      long counter = 0;
      switch (type) {
        case DIAGNOSIS:
          counter = ontologyLoadReport.getNumberDiagnosisTerms();
          break;
        case TREATMENT:
          counter = ontologyLoadReport.getNumberTreatmentTerms();
          break;
        case REGIMEN:
          counter = ontologyLoadReport.getNumberRegimenTerms();
          break;
      }
      counterByType.put(type.getDescription(), counter);
    }
    return counterByType;
  }
}
