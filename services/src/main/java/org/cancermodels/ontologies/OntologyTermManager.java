package org.cancermodels.ontologies;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.cancermodels.persistance.OntologyTerm;
import org.cancermodels.persistance.OntologyTermRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class OntologyTermManager {
  private final OntologyTermRepository ontologyTermRepository;
  private static final Logger LOG = LoggerFactory.getLogger(OntologyTermManager.class);

  public OntologyTermManager(OntologyTermRepository ontologyTermRepository) {
    this.ontologyTermRepository = ontologyTermRepository;
  }

  public OntologyTerm createOntologyTerm(
      String ncit,
      String url,
      String termLabel,
      String type,
      String description,
      Set<String> synonyms) {

    String updatedTermLabel = termLabel;

    if ("diagnosis".equalsIgnoreCase(type)) {
      updatedTermLabel = updateTermLabel(updatedTermLabel);
    }

    OntologyTerm ontologyTerm = new OntologyTerm(
        ncit, url, updatedTermLabel, type, description );
    ontologyTerm.setSynonyms(synonyms);
    return ontologyTerm;
  }

  private String updateTermLabel(String termLabel){
    // Changes Malignant * Neoplasm to * Cancer
    String pattern = "(.*)Malignant(.*)Neoplasm(.*)";
    String updatedTermLabel = termLabel;

    if (termLabel.matches(pattern)) {
      updatedTermLabel = (termLabel.replaceAll(pattern, "\t$1$2Cancer$3")).trim();
      LOG.trace("Replacing term label '{}' with '{}'", termLabel, updatedTermLabel);
    }
    return updatedTermLabel;
  }

  public List<OntologyTerm> getAll() {
    return ontologyTermRepository.findAll();
  }

  public OntologyTerm getById(int id) {
    return ontologyTermRepository.getById(id);
  }

  public List<OntologyTerm> getAllByType(String type) {
    return ontologyTermRepository.findAllByTypeIgnoreCase(type);
  }

  public void saveOntologyTerms(Set<OntologyTerm> ontologyTerms) {
    LOG.info("Saving {} ontology terms to db", ontologyTerms.size());
    ontologyTermRepository.saveAll(ontologyTerms);
    LOG.info("Terms saved");
  }

  public void deleteAll() {
    ontologyTermRepository.deleteAll();
  }
  public long count() {
    return ontologyTermRepository.count();
  }

  public long getCountByType(String type) {
    return ontologyTermRepository.countByType(type);
  }
}
