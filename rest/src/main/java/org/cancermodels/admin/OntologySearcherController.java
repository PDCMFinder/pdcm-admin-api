package org.cancermodels.admin;

import java.io.IOException;
import java.util.List;
import org.cancermodels.persistance.Suggestion;
import org.cancermodels.suggestions.search_engine.OntologySearcherByText;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ontology")
public class OntologySearcherController {

private final OntologySearcherByText ontologySearcherByText;

  public OntologySearcherController(OntologySearcherByText ontologySearcherByText) {
    this.ontologySearcherByText = ontologySearcherByText;
  }

  @GetMapping("/search")
  public List<Suggestion> searchWithDefaultParameters(@RequestParam(value = "input") String input)
      throws IOException {
    return ontologySearcherByText.searchWithDefaultParameters(input);
  }
}
