package org.cancermodels.process_report;

import java.time.LocalDateTime;
import org.cancermodels.persistance.ProcessReport;
import org.cancermodels.persistance.ProcessReportRepository;
import org.cancermodels.types.ProcessReportAttributes;
import org.cancermodels.types.ProcessReportModules;
import org.springframework.stereotype.Service;

@Service
public class ProcessReportService {

  private final ProcessReportRepository processReportRepository;

  public ProcessReportService(
      ProcessReportRepository processReportRepository) {
    this.processReportRepository = processReportRepository;
  }

  /**
   * Register an event in the system. It creates a record in the table ProcessReport.
   * @param module Module of the event (Input data, Ontologies, etc).
   * @param attribute Description of what is going to be logged.
   * @param value A string representation of the value for the attribute.
   */
  public void register(
      ProcessReportModules module, ProcessReportAttributes attribute, String value) {
    ProcessReport processReport = new ProcessReport();
    processReport.setModule(module.getLabel());
    processReport.setAttribute(attribute.getLabel());
    processReport.setValue(value);
    processReport.setDate(LocalDateTime.now());
    processReportRepository.save(processReport);
  }

  /**
   * Gets the last entry fot a specific attribute in a module
   * @param module  Module of the event (Input data, Ontologies, etc).
   * @param attribute Description of what is going to be logged.
   * @return The most recent {@link ProcessReport} object.
   */
  public ProcessReport getLastEntry(ProcessReportModules module, ProcessReportAttributes attribute) {
    return processReportRepository.findTopByModuleAndAttributeOrderByDateDesc(
        module.getLabel(), attribute.getLabel());
  }
}
