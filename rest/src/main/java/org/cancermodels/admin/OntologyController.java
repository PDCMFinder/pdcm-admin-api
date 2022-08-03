package org.cancermodels.admin;

import org.cancermodels.persistance.OntologyLoadReport;
import org.cancermodels.ontologies.OntologyService;
import org.cancermodels.ontologies.OntologySummary;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ontology")
public class OntologyController {

  private final OntologyService ontologyService;

  public OntologyController(OntologyService ontologyService) {
    this.ontologyService = ontologyService;
  }

  /**
   * Reloads the ontologies tables in the h2 database using OLS as a source
   * @return {@link OntologyLoadReport} object with a report of the process
   */
  @GetMapping("loadOntologies")
  public OntologyLoadReport loadOntologies() {
    return ontologyService.loadOntologies();
  }

  /**
   * Gets a summary with the status of the ontologies and the latest loading processes
   * @return {@link OntologySummary} object with the counts of ontology terms and information
   * about the last 2 loading processes
   */
  @GetMapping("getSummary")
  public OntologySummary getSummary() {
    return ontologyService.getOntologySummary();
  }
}
