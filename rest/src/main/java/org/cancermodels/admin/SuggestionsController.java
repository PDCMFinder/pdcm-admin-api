package org.cancermodels.admin;

import java.io.IOException;
import org.cancermodels.ontologies.OntologySummary;
import org.cancermodels.suggestions.index.OntologiesIndexer;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/suggestions")
public class SuggestionsController {
  private final OntologiesIndexer ontologiesIndexer;

  public SuggestionsController(
      OntologiesIndexer ontologiesIndexer) {
    this.ontologiesIndexer = ontologiesIndexer;
  }

  @PutMapping("index/ontologies")
  public void indexOntologies() throws IOException {
    ontologiesIndexer.index();
  }
}
