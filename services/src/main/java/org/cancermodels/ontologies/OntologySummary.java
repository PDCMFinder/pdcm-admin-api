package org.cancermodels.ontologies;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/**
 * This class holds information about the current status of the ontologies (
 */
@Data
public class OntologySummary {
  private long totalCount;
  private Map<String, Long> countsByType;
  private Map<String, Long> countAddedTermsLatestLoadByType;
  private Map<String, Long> countAddedTermsPreviousLoadByType;
  private LocalDateTime latestLoadingDate;
  private LocalDateTime previousLoadingDate;
  private String errors;
}
