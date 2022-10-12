package org.cancermodels.ontologies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cancermodels.persistance.OntologyTerm;
import org.cancermodels.process_report.ProcessResponse;
import org.springframework.stereotype.Service;

/**
 * This class expose all public methods related to Ontologies
 */
@Service
public class OntologyService {
  private final OntologyLoader ontologyLoader;
  private final OntologyTermManager ontologyTermManager;

  public OntologyService(
      OntologyLoader ontologyLoader,
      OntologyTermManager ontologyTermService) {
    this.ontologyLoader = ontologyLoader;
    this.ontologyTermManager = ontologyTermService;
  }

  public List<OntologyTerm> getAllByType(String type) {
    return ontologyTermManager.getAllByType(type);
  }

  public List<OntologyTerm> getAll() {
    return ontologyTermManager.getAll();
  }

  public OntologyTerm getById(int id) {
    return ontologyTermManager.getById(id);
  }

  /**
   * Gets all ontology terms as a map, where the key is the type (treatment and regimen grouped
   * as a single one).
   * @return Map with key=type and value=ontology terms. Keys are in lowercase
   */
  public Map<String, List<OntologyTerm>> getOntologyTermsMappedByType() {
    Map<String, List<OntologyTerm>> map = new HashMap<>();

    String diagnosis = OntologyTermType.DIAGNOSIS.getDescription();
    List<OntologyTerm> diagnosisOntologyTerms = getAllByType(diagnosis);
    map.put(diagnosis.toLowerCase(), diagnosisOntologyTerms);

    String treatment = OntologyTermType.TREATMENT.getDescription();
    List<OntologyTerm> treatmentOntologyTerms = getAllByType(treatment);
    String regimen = OntologyTermType.REGIMEN.getDescription();
    List<OntologyTerm> regimenOntologyTerms = getAllByType(regimen);
    treatmentOntologyTerms.addAll(regimenOntologyTerms);
    map.put(treatment.toLowerCase(), treatmentOntologyTerms);

    return map;
  }

  /**
   * Loads the ontologies using OLS as a source. It loads diagnosis, treatments and regimens
   * ontology terms (as well as their synonyms).
   * @return A {@link ProcessResponse} object with information about number of terms loaded and
   * errors if any
   */
  public ProcessResponse loadOntologies() {
    return ontologyLoader.loadOntologies();
  }

  /**
   * Return the names of the ontology types
   * @return List of strings with the names of the ontology types
   */
  public List<String> getOntologyTypes() {
    List<String> types = new ArrayList<>();
    for (OntologyTermType type : OntologyTermType.values()) {
      types.add(type.getDescription());
    }
    return types;
  }

}
