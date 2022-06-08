package org.cancermodels.ontologies;

import java.time.LocalDateTime;
import org.cancermodels.OntologyLoadReport;
import org.cancermodels.OntologyLoadReportRepository;
import org.springframework.stereotype.Service;

@Service
public class OntologyLoadReportService {
  private final OntologyLoadReportRepository ontologyLoadReportRepository;

  public OntologyLoadReportService(OntologyLoadReportRepository ontologyLoadReportRepository) {
    this.ontologyLoadReportRepository = ontologyLoadReportRepository;
  }

  public void updateLoadingReport(
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
  }
}
