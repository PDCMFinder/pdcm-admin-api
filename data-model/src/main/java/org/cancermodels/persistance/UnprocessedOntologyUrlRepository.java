package org.cancermodels.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnprocessedOntologyUrlRepository extends JpaRepository<UnprocessedOntologyUrl, Long> {
  UnprocessedOntologyUrl findByUrl(String url);
}
