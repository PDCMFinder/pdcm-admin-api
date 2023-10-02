package org.cancermodels.admin;

import org.cancermodels.migration.RulesMigratorService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/migration")
public class MigrationController {

  private final RulesMigratorService rulesMigratorService;

  public MigrationController(RulesMigratorService rulesMigratorService) {
    this.rulesMigratorService = rulesMigratorService;
  }

  /**
   * Reloads the ontologies tables in the database using OLS as a source
   */
  @GetMapping("loadOldRules")
  public void loadOldRules() {
    rulesMigratorService.loadOldRulesInDb();
  }
}
