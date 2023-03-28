package org.cancermodels.pdcm_etl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchIndexRepository extends JpaRepository<SearchIndex, Long> {

}
