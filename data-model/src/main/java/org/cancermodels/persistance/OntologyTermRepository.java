package org.cancermodels.persistance;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OntologyTermRepository extends JpaRepository<OntologyTerm, Integer> {
  long countByType(String type);

  List<OntologyTerm> findAllByTypeIgnoreCase(String type);

  OntologyTerm findByKey(String key);
}
