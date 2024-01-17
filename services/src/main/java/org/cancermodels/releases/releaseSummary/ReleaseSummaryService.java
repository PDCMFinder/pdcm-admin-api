package org.cancermodels.releases.releaseSummary;

import org.cancermodels.pdcm_admin.persistance.Release;
import org.cancermodels.pdcm_admin.persistance.ReleaseMetric;
import org.cancermodels.pdcm_admin.persistance.ReleaseMetricRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In charge of formatting the data to create suitable {@link ReleaseSummary} objects
 */
@Component
public class ReleaseSummaryService {
    private final ReleaseMetricRepository releaseMetricRepository;

    public ReleaseSummaryService(ReleaseMetricRepository releaseMetricRepository) {

        this.releaseMetricRepository = releaseMetricRepository;
    }

    public ReleaseSummary getReleaseSummary(Release release) {
        ReleaseSummary releaseSummary = new ReleaseSummary();
        List<ReleaseMetric> metrics = releaseMetricRepository.findAllByRelease(release);
        releaseSummary.setName(release.getName());
        releaseSummary.setDate(release.getDate());
        releaseSummary.setMetrics(toKeyValueMap(metrics));
        return releaseSummary;
    }

    private Map<String, Long> toKeyValueMap(List<ReleaseMetric> metrics) {
        Map<String, Long> keyValueMap = new HashMap<>();
        metrics.forEach(x -> {
            keyValueMap.put(x.getKey(), x.getValue());
        });
        return keyValueMap;
    }

    public List<ReleaseSummary> getReleasesSummaries(List<Release> releases) {
        List<ReleaseSummary> releaseSummaries = new ArrayList<>();
        releases.forEach(x -> releaseSummaries.add(getReleaseSummary(x)));
        return releaseSummaries;
    }
}
