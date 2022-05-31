package org.cancermodels.admin;

import org.cancermodels.DummyService;
import org.cancermodels.OntologyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
  private DummyService dummyService;
  private OntologyService ontologyService;

  public Controller(DummyService dummyService,
      OntologyService ontologyService) {
    this.dummyService = dummyService;
    this.ontologyService = ontologyService;
  }

  @GetMapping("/setup")
  public void setup() {
    dummyService.setup();
  }

  @GetMapping("/testLoad")
  public void testLoad() {
    dummyService.testLoad();
  }

  @GetMapping("/loadTreatmentTerms")
  public void loadTreatmentTerms() {
    ontologyService.reloadTreatmentTerms();
  }

  @GetMapping("/loadDiagnosisTerms")
  public void loadDiagnosisTerms() {
    ontologyService.reloadDiagnosisTerms();
  }
}
