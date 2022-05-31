package org.cancermodels;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OntologyTermRepository extends JpaRepository<OntologyTerm, Long> {
  List<OntologyTerm> findAllByType(String type);

  void deleteAllByType(String type);
}
