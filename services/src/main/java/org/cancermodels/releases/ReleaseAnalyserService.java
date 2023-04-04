package org.cancermodels.releases;

import lombok.extern.slf4j.Slf4j;
import org.cancermodels.pdcm_admin.persistance.ModelSummary;
import org.cancermodels.pdcm_admin.persistance.ModelSummaryRepository;
import org.cancermodels.pdcm_admin.persistance.Release;
import org.cancermodels.pdcm_admin.persistance.ReleaseRepository;
import org.cancermodels.pdcm_etl.ReleaseInfo;
import org.cancermodels.pdcm_etl.ReleaseInfoRepository;
import org.cancermodels.pdcm_etl.SearchIndex;
import org.cancermodels.pdcm_etl.SearchIndexRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReleaseAnalyserService {
    private final ReleaseRepository releaseRepository;
    private final ReleaseInfoRepository releaseInfoRepository;
    private final SearchIndexRepository searchIndexRepository;
    private final ModelSummaryRepository modelSummaryRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    public ReleaseAnalyserService(
        ReleaseRepository releaseRepository,
        ReleaseInfoRepository releaseInfoRepository,
        SearchIndexRepository searchIndexRepository,
        ModelSummaryRepository modelSummaryRepository) {

        this.releaseRepository = releaseRepository;
        this.releaseInfoRepository = releaseInfoRepository;
        this.searchIndexRepository = searchIndexRepository;
        this.modelSummaryRepository = modelSummaryRepository;
    }

    public List<Release> getAllReleases() {
        return releaseRepository.findAll();
    }

    /**
     * Reads all the relevant data from the current release (ETL database).
     * This works like a snapshot of the current state of the data processed in the ETL,
     * considering some high level data only that can be useful to persist between releases.
     */
    public void loadAllAssociatedDataForCurrentRelease() {
        log.info("Loading all associated data for current release");
        ReleaseInfo currentEtlRelease = getCurrentReleaseFromEtl();
        Optional<Release> releaseOpt = findExistingReleaseAdminDb(currentEtlRelease);
        Release currentRelease;
        if (releaseOpt.isPresent()) {
            currentRelease = releaseOpt.get();
            // Delete existing data
            deleteAllAssociatedReleaseData(currentRelease);
        }
        else {
            currentRelease = new Release(currentEtlRelease.getName(), currentEtlRelease.getDate());
            currentRelease = releaseRepository.save(currentRelease);
        }
        saveAllAssociatedReleaseData(currentRelease);
        log.info("Data for current release saved");
    }

    private void saveAllAssociatedReleaseData(Release release) {
        log.info("Getting all associated data");
        List<ModelSummary> modelSummaries = getCurrentReleaseModels(release);
        log.info("Writing all associated data");
        modelSummaryRepository.saveAll(modelSummaries);
    }

    private void deleteAllAssociatedReleaseData(Release release) {
        log.warn("Deleting all associated data for release " + release);
        List<ModelSummary> modelSummaries = modelSummaryRepository.findAllByRelease(release);
        log.warn("Deleting {} model summaries", modelSummaries.size());
        modelSummaryRepository.deleteAll(modelSummaries);
    }

    // Get the single record that should exist in release_info, namely the current ETL release.
    private ReleaseInfo getCurrentReleaseFromEtl() {
        List<ReleaseInfo> releaseList = releaseInfoRepository.findAll();
        // There should be one and only one
        if (releaseList.isEmpty()) {
            throw new IllegalArgumentException(
                "There is no data in release_info table.");
        }
        if (releaseList.size() > 1) {
            throw new IllegalArgumentException(
                "There are %d releases. There should one and only one release in the release_info table");
        }
        return releaseList.get(0);
    }

    private Optional<Release> findExistingReleaseAdminDb(ReleaseInfo releaseInfo) {
        return releaseRepository.findByNameAndDate(releaseInfo.getName(), releaseInfo.getDate());
    }

    private List<ModelSummary> getCurrentReleaseModels(Release release) {
        var tmp = searchIndexRepository.findAll();
        List<ModelSummary> modelSummaries = searchIndexRepository.findAll().stream()
            .map(this::searchIndexToModelSummary).collect(Collectors.toList());
        modelSummaries.forEach(x -> x.setRelease(release));
        return modelSummaries;
    }

    private ModelSummary searchIndexToModelSummary(SearchIndex searchIndex) {
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        ModelSummary modelSummary = modelMapper.map(searchIndex, ModelSummary.class);
        return modelSummary;
    }
}
