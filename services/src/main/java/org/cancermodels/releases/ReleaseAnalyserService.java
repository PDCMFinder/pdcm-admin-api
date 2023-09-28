package org.cancermodels.releases;

import lombok.extern.slf4j.Slf4j;
import org.cancermodels.filters.Facet;
import org.cancermodels.pdcm_admin.persistance.ModelSummary;
import org.cancermodels.pdcm_admin.persistance.Release;
import org.cancermodels.pdcm_etl.ReleaseInfo;
import org.cancermodels.pdcm_etl.ReleaseInfoRepository;
import org.cancermodels.pdcm_etl.SearchIndex;
import org.cancermodels.pdcm_etl.SearchIndexRepository;
import org.cancermodels.releases.modelSummary.ModelSummaryFilter;
import org.cancermodels.releases.modelSummary.ModelSummaryService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ReleaseAnalyserService {
    private final ReleaseService releaseService;

    private final ModelSummaryService modelSummaryService;
    private final ReleaseInfoRepository releaseInfoRepository;
    private final SearchIndexRepository searchIndexRepository;

    private final ModelMapper modelMapper = new ModelMapper();

    public ReleaseAnalyserService(
        ReleaseService releaseService,
        ModelSummaryService modelSummaryService,
        ReleaseInfoRepository releaseInfoRepository,
        SearchIndexRepository searchIndexRepository) {

        this.releaseService = releaseService;
        this.modelSummaryService = modelSummaryService;

        this.releaseInfoRepository = releaseInfoRepository;
        this.searchIndexRepository = searchIndexRepository;
    }

    public List<Release> getAllReleases() {
        return releaseService.getAllReleases();
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
            currentRelease = releaseService.save(currentRelease);
        }
        saveAllAssociatedReleaseData(currentRelease);
        log.info("Data for current release saved");
    }

    private void saveAllAssociatedReleaseData(Release release) {
        log.info("Getting all associated data");
        List<ModelSummary> modelSummaries = getCurrentReleaseModels(release);
        log.info("Writing all associated data");
        modelSummaryService.saveAll(modelSummaries);
    }

    private void deleteAllAssociatedReleaseData(Release release) {
        log.warn("Deleting all associated data for release " + release);
        List<ModelSummary> modelSummaries = modelSummaryService.findAllByRelease(release);
        log.warn("Deleting {} model summaries", modelSummaries.size());
        modelSummaryService.deleteAll(modelSummaries);
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
        return releaseService.findByNameAndDate(releaseInfo.getName(), releaseInfo.getDate());
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

    public ReleaseSummary getReleaseSummary(long id) {
        Release release = releaseService.getReleaseByIdOrFail(id);
        List<ModelSummary> models = modelSummaryService.findAllByRelease(release);
        long totalNumberOfModels = models.size();
        long numberOfPdxModels = getCountByType(models, "PDX");
        long numberOfCellLineModels = getCountByType(models, "cell line");
        long numberOfOrganoidModels = getCountByType(models, "organoid");
        ReleaseSummary releaseSummary = new ReleaseSummary();
        releaseSummary.setName(release.getName());
        releaseSummary.setDate(release.getDate());
        releaseSummary.setTotalModelsCount(totalNumberOfModels);
        releaseSummary.setPdxModelsCount(numberOfPdxModels);
        releaseSummary.setCellLineModelsCount(numberOfCellLineModels);
        releaseSummary.setOrganoidModelsCount(numberOfOrganoidModels);

        return releaseSummary;
    }

    private long getCountByType(List<ModelSummary> models, String modelType) {
        return models.stream()
            .filter(model -> modelType.equals(model.getModelType()))
            .count();
    }

    public Page<ModelSummary> getModelsByReleasePage(Long releaseId, Pageable pageable) {
        return modelSummaryService.findByReleaseId(releaseId, pageable);
    }

    public List<Facet> getFacetsForModels(long releaseId) {
        Release release = releaseService.getReleaseByIdOrFail(releaseId);
        return modelSummaryService.getFacetsForModels(release);
    }

    public Page<ModelSummary> search(Pageable pageable, ModelSummaryFilter modelSummaryFilter) {
        return modelSummaryService.search(pageable, modelSummaryFilter);
    }
}
