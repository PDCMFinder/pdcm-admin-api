package org.cancermodels.admin;

import java.util.List;
import java.util.Map;
import org.cancermodels.process_report.ProcessReportService;
import org.cancermodels.types.ProcessReportModules;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class providing endpoints to query the information about executed processes.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/processReport")
public class ProcessReportController {

  private final ProcessReportService processReportService;

  public ProcessReportController(
      ProcessReportService processReportService) {
    this.processReportService = processReportService;
  }

  @GetMapping
  public Map<String, String> getInputDataReport(@RequestParam(value = "module") String module) {
    return processReportService.getLatestReportByModule(ProcessReportModules.getByName(module));
  }
}
