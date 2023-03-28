package org.cancermodels.pdcm_etl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReleaseInfoRepository extends JpaRepository<ReleaseInfo, Long> {

}
