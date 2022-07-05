package org.cancermodels;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OntologyTermRepository extends JpaRepository<OntologyTerm, Long> {
  long countByType(String type);

  List<OntologyTerm> findAllByTypeIgnoreCase(String type);
}
