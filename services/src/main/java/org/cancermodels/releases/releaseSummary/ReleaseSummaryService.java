package org.cancermodels.releases.releaseSummary;

import org.cancermodels.pdcm_admin.persistance.Release;
import org.cancermodels.pdcm_admin.persistance.ReleaseMetric;
import org.cancermodels.pdcm_admin.persistance.ReleaseMetricRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
        releaseSummary.setMetrics(metrics);
        return releaseSummary;
    }

    public List<ReleaseSummary> getReleasesSummaries(List<Release> releases) {
        List<ReleaseSummary> releaseSummaries = new ArrayList<>();
        releases.forEach(x -> releaseSummaries.add(getReleaseSummary(x)));
        return releaseSummaries;
    }
}
