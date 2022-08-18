package org.cancermodels.mappings.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cancermodels.Status;
import org.cancermodels.mappings.reports.MappingSummaryByTypeAndProvider.SummaryEntry;
import org.cancermodels.persistance.MappingEntityRepository;
import org.springframework.stereotype.Service;

@Service
public class ReportsService {

  private final MappingEntityRepository mappingEntityRepository;

  public ReportsService(
      MappingEntityRepository mappingEntityRepository) {
    this.mappingEntityRepository = mappingEntityRepository;
  }

  public MappingSummaryByTypeAndProvider getSummaryByTypeAndProvider(String entityTypeName) {
    MappingSummaryByTypeAndProvider mappingSummaryByTypeAndProvider = new MappingSummaryByTypeAndProvider();
    List<SummaryEntry> summaryEntries = new ArrayList<>();
    mappingSummaryByTypeAndProvider.setEntityTypeName(entityTypeName);

    Map<String, Map<String, Integer>> data = new HashMap<>();

    String mappedKey = Status.MAPPED.getLabel();
    String unmappedKey = Status.UNMAPPED.getLabel();

    List<Object[]> list = mappingEntityRepository.countEntityTypeStatusByProvider(entityTypeName);
    for (Object[] row : list) {
      String dataSource = row[0].toString();
      String status  = row[1].toString();
      int count = Integer.parseInt(row[2].toString());
      if (!data.containsKey(dataSource)) {
        data.put(dataSource, new HashMap<>());
      }
      data.get(dataSource).put(status, count);
    }

    for (String dataSource : data.keySet()) {
      SummaryEntry summaryEntry = new SummaryEntry();
      summaryEntry.setDataSource(dataSource);
      Map<String, Integer> countByDataSource = data.get(dataSource);

      if (countByDataSource.containsKey(mappedKey)) {
        summaryEntry.setMapped(countByDataSource.get(mappedKey));
      }
      if (countByDataSource.containsKey(unmappedKey)) {
        summaryEntry.setUnmapped(countByDataSource.get(unmappedKey));
      }
      int totalTerms = summaryEntry.getMapped() + summaryEntry.getUnmapped();
      summaryEntry.setTotalTerms(totalTerms);
      summaryEntry.setProgress(summaryEntry.getMapped()*1.0 / totalTerms );
      summaryEntries.add(summaryEntry);
    }

    mappingSummaryByTypeAndProvider.setSummaryEntries(summaryEntries);
    return mappingSummaryByTypeAndProvider;
  }
}
