package org.cancermodels.pdcm_admin.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OntologyTermRepository extends JpaRepository<OntologyTerm, Integer> {
    Optional<OntologyTerm> findByKey(String key);
}
