package org.cancermodels.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessReportRepository extends JpaRepository<ProcessReport, Long> {

  ProcessReport findTopByModuleAndAttributeOrderByDateDesc(String module, String attribute);
}
