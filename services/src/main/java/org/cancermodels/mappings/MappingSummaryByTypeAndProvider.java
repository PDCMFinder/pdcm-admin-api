package org.cancermodels.mappings;

import java.util.List;
import lombok.Data;

/**
 * Class with information about the number of mapped and unmapped terms by entity type and provider
 */
@Data
public class MappingSummaryByTypeAndProvider {
  private String entityTypeName;
  private List<SummaryEntry> summaryEntries;

  @Data
  static class SummaryEntry {
    private String dataSource;
    private int mapped;
    private int unmapped;
    private int totalTerms;
    private double progress;
  }
}
