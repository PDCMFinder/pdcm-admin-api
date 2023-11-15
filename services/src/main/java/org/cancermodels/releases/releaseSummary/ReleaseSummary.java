package org.cancermodels.releases.releaseSummary;

import lombok.Data;
import org.cancermodels.pdcm_admin.persistance.ReleaseMetric;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReleaseSummary {
    private String name;
    private LocalDateTime date;
    private List<ReleaseMetric> metrics;
}
