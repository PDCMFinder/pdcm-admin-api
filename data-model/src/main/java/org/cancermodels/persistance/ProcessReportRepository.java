package org.cancermodels.persistance;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProcessReportRepository extends JpaRepository<ProcessReport, Long> {

  ProcessReport findTopByModuleAndAttributeOrderByDateDesc(String module, String attribute);

  @Query(
      value =
          "select * from PROCESS_REPORT "
              + "where (module, attribute, date) in (\n"
              + "select module, attribute, max(date)  "
              + "from PROCESS_REPORT where module=:module group by module, attribute)",
      nativeQuery = true)
  List<ProcessReport> findLatestReportsByModule(String module);
}
