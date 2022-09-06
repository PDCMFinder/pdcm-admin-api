package org.cancermodels.admin.mappers;

import org.cancermodels.mappings.reports.MappingSummaryByTypeAndProvider;
import org.cancermodels.mappings.reports.ReportsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/mappings/reports")
public class ReportsController {

  private final ReportsService reportsService;

  public ReportsController(ReportsService reportsService) {
    this.reportsService = reportsService;
  }

  @GetMapping("/getSummary")
  public MappingSummaryByTypeAndProvider getMappingSummaryByTypeAndProvider(
      @RequestParam(value = "entityTypeName") String entityTypeName) {
    return reportsService.getSummaryByTypeAndProvider(entityTypeName);
  }
}
