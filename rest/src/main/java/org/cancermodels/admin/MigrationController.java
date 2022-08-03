package org.cancermodels.admin;

import org.cancermodels.persistance.OntologyLoadReport;
import org.cancermodels.migration.RulesMigratorService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/migration")
public class MigrationController {

  private RulesMigratorService rulesMigratorService;

  public MigrationController(RulesMigratorService rulesMigratorService) {
    this.rulesMigratorService = rulesMigratorService;
  }

  /**
   * Reloads the ontologies tables in the h2 database using OLS as a source
   * @return {@link OntologyLoadReport} object with a report of the process
   */
  @GetMapping("loadOldRules")
  public void loadOldRules() {
    rulesMigratorService.loadOldRulesInDb();
  }
}
