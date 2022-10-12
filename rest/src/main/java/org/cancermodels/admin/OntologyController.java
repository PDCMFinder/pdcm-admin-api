package org.cancermodels.admin;

import org.cancermodels.ontologies.OntologyService;
import org.cancermodels.process_report.ProcessResponse;
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
   * @return {@link ProcessResponse} object with a report of the process
   */
  @GetMapping("loadOntologies")
  public ProcessResponse loadOntologies() {
    return ontologyService.loadOntologies();
  }

}
