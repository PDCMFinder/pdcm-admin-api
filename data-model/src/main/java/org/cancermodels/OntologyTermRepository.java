package org.cancermodels;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OntologyTermRepository extends JpaRepository<OntologyTerm, Long> {
  long countByType(String type);
}
