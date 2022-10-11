package org.cancermodels.suggestions.indexers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.cancermodels.process_report.ProcessReportService;
import org.cancermodels.process_report.ProcessResponse;
import org.cancermodels.types.ProcessReportModules;
import org.springframework.stereotype.Component;

/**
 * Indexes the documents needed to have mappings suggestions.
 */
@Component
public class Indexer {

  private final OntologiesIndexer ontologiesIndexer;
  private final RulesIndexer rulesIndexer;
  private final HelperDocumentsIndexer helperDocumentsIndexer;
  private final ProcessReportService processReportService;

  public Indexer(
      OntologiesIndexer ontologiesIndexer,
      RulesIndexer rulesIndexer,
      HelperDocumentsIndexer helperDocumentsIndexer,
      ProcessReportService processReportService) {
    this.ontologiesIndexer = ontologiesIndexer;
    this.rulesIndexer = rulesIndexer;
    this.helperDocumentsIndexer = helperDocumentsIndexer;
    this.processReportService = processReportService;
  }

  /**
   * Indexes rules, ontologies and helper documents.
   * @return {@link ProcessResponse} with the date of the execution.
   * @throws IOException If the index cannot be created.
   */
  public ProcessResponse index() throws IOException {
    rulesIndexer.index();
    ontologiesIndexer.index();
    helperDocumentsIndexer.index();

    Map<String, String> processResult = getProcessResult();

    registerProcess(processResult);
    return createProcessResponse(processResult);
  }

  private Map<String, String> getProcessResult() {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formatDateTime = LocalDateTime.now().format(formatter);

    Map<String, String> result = new HashMap<>();

    result.put("Update date", formatDateTime);

    return result;
  }

  private void registerProcess(Map<String, String> processResult) {
    for (String key : processResult.keySet()) {
      processReportService.register(ProcessReportModules.INDEXER, key, processResult.get(key));
    }
  }

  private ProcessResponse createProcessResponse(Map<String, String> processResult) {
    return new ProcessResponse(processResult);
  }

  public void indexRules() throws IOException {
    rulesIndexer.index();
  }

  public void indexOntologies() throws IOException {
    ontologiesIndexer.index();
  }

  public void indexHelperDocuments() throws IOException {
    helperDocumentsIndexer.index();
  }

}
