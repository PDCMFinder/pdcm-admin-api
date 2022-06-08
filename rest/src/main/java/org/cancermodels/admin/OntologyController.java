package org.cancermodels.admin;

import org.cancermodels.ontologies.OntologyLoaderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ontology")
public class OntologyController {

  private OntologyLoaderService ontologyLoaderService;

  public OntologyController(OntologyLoaderService ontologyLoaderService) {
    this.ontologyLoaderService = ontologyLoaderService;
  }

  /**
   * Reloads the ontologies tables in the h2 database using OLS as a source
   * @return
   */
  @GetMapping("loadOntologies")
  public void loadOntologies() {
    ontologyLoaderService.loadOntologies();
  }
}
