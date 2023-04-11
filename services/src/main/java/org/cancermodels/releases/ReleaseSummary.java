package org.cancermodels.releases;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReleaseSummary {
    private String name;
    private LocalDateTime date;
    private long totalModelsCount;
    private long pdxModelsCount;
    private long cellLineModelsCount;
    private long organoidModelsCount;
}
