package org.cancermodels.releases.releaseSummary;

import org.cancermodels.pdcm_admin.persistance.ModelSummary;
import org.cancermodels.pdcm_admin.persistance.Release;
import org.cancermodels.pdcm_admin.persistance.ReleaseMetric;
import org.cancermodels.pdcm_admin.persistance.ReleaseMetricRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReleaseMetricService {
    private final ReleaseMetricRepository releaseMetricRepository;

    public ReleaseMetricService(ReleaseMetricRepository releaseMetricRepository) {
        this.releaseMetricRepository = releaseMetricRepository;
    }

    public List<ReleaseMetric> findAllByRelease(Release release) {
        return releaseMetricRepository.findAllByRelease(release);
    }

    public void saveAll(List<ReleaseMetric> releaseCounts) {
        releaseMetricRepository.saveAll(releaseCounts);
    }

    public void deleteAll(List<ReleaseMetric> releaseCounts) {
        releaseMetricRepository.deleteAll(releaseCounts);
    }

    public List<ReleaseMetric> generateCounts(Release release, List<ModelSummary> modelSummaries) {
        List<ReleaseMetric> releaseCounts = new ArrayList<>();
        long totalNumberOfModels = modelSummaries.size();
        long numberOfPdxModels = getCountByType(modelSummaries, "PDX");
        long numberOfCellLineModels = getCountByType(modelSummaries, "cell line");
        long numberOfOrganoidModels = getCountByType(modelSummaries, "organoid");
        long numberOfOtherModels = getCountByType(modelSummaries, "other");
        releaseCounts.add(buildReleaseCounts(release, "totalNumberOfModels", totalNumberOfModels));
        releaseCounts.add(buildReleaseCounts(release, "numberOfPdxModels", numberOfPdxModels));
        releaseCounts.add(buildReleaseCounts(release, "numberOfCellLineModels", numberOfCellLineModels));
        releaseCounts.add(buildReleaseCounts(release, "numberOfOrganoidModels", numberOfOrganoidModels));
        releaseCounts.add(buildReleaseCounts(release, "numberOfOtherModels", numberOfOtherModels));
        return releaseCounts;
    }

    private ReleaseMetric buildReleaseCounts(Release release, String key, Long value) {
        ReleaseMetric releaseMetric = new ReleaseMetric();
        releaseMetric.setRelease(release);
        releaseMetric.setKey(key);
        releaseMetric.setValue(value);
        return releaseMetric;
    }

    private long getCountByType(List<ModelSummary> models, String modelType) {
        return models.stream()
            .filter(model -> modelType.equals(model.getModelType()))
            .count();
    }

}
