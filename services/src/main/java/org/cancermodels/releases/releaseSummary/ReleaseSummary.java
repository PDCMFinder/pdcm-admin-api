package org.cancermodels.releases.releaseSummary;

import lombok.Data;
import org.cancermodels.pdcm_admin.persistance.ReleaseMetric;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ReleaseSummary {
    private String name;
    private LocalDateTime date;
    private Map<String, Long> metrics;
}
