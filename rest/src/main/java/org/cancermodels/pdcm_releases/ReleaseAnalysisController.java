package org.cancermodels.pdcm_releases;

import org.cancermodels.filters.Facet;
import org.cancermodels.pdcm_admin.persistance.ModelSummary;
import org.cancermodels.pdcm_admin.persistance.Release;
import org.cancermodels.releases.ReleaseAnalyserService;
import org.cancermodels.releases.modelSummary.ModelSummaryFilter;
import org.cancermodels.releases.modelSummary.ModelSummaryFilterBuilder;
import org.cancermodels.releases.releaseSummary.ReleaseSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;


/**
 * Manages release related data taken from the ETL database.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/releases")
  public class ReleaseAnalysisController {

  private final ReleaseAnalyserService releaseAnalyserService;

  public ReleaseAnalysisController(ReleaseAnalyserService releaseAnalyserService) {
    this.releaseAnalyserService = releaseAnalyserService;
  }

  /**
   * List all releases that have been executed in the ETL. They are returned sorted by date
   * (desc)
   * @return List of {@link Release} objects.
   */
  @GetMapping("")
  public List<Release> listAllReleasesSortedByDate()
  {
    return releaseAnalyserService.getAllReleasesSortedByDate();
  }

  /**
   * Reads all the relevant data from the current release (ETL database).
   *
   * This works like a snapshot of the current state of the data processed in the ETL,
   * considering some high level data only that can be useful to persist between releases.
   */
  @PutMapping("loadCurrentRelease")
  public void loadAllAssociatedDataForCurrentRelease()
  {
    releaseAnalyserService.loadAllAssociatedDataForCurrentRelease();
  }

  @DeleteMapping("/{releaseId}")
  public void deleteRelease(@PathVariable Long releaseId) {
    releaseAnalyserService.deleteRelease(releaseId);
  }

  @GetMapping("modelsByReleasePage/{releaseId}")
  public ResponseEntity<?> getModelsByRelease(@PathVariable Long releaseId, Pageable pageable) {
    Page<ModelSummary> models = releaseAnalyserService.getModelsByReleasePage(releaseId, pageable);
    return ResponseEntity.ok(models);
  }

  @GetMapping("getFiltersForModels/{releaseId}")
  public List<Facet> getFiltersForModels(@PathVariable Long releaseId) {
    return releaseAnalyserService.getFacetsForModels(releaseId);
  }
  
  /**
   * Retrieves a list of models based on the provided parameters for a specific release.
   *
   * @param releaseId The ID of the release for which models should be retrieved.
   * @param pageable The pageable information for controlling the pagination of the results.
   * @param viewName The name of the view to be used for filtering the models (default is "allModels").
   *                 Possible values:
   *                 - "allModels": All models in the release
   *                 - "paediatricModels": Paediatric models in the release
   * @param modelTypes A list of model types to filter the models (optional).
   * @return A ResponseEntity containing a list of models that match the given criteria.
   */
  @GetMapping("models/search/{releaseId}")
  public ResponseEntity<?> search(
      @PathVariable Long releaseId,
      Pageable pageable,
      @RequestParam(
          value = "viewName", required = false, defaultValue = "allModels") String viewName,
      @RequestParam(
          value = "modelType", required = false) List<String> modelTypes) {

    ModelSummaryFilter filter = ModelSummaryFilterBuilder.getInstance()
        .withModelType(modelTypes)
        .withReleaseId(Collections.singletonList(String.valueOf(releaseId)))
        .build();

    Page<ModelSummary> models = releaseAnalyserService.search(viewName, pageable, filter);
    return ResponseEntity.ok(models);
  }

  @GetMapping("getAllReleasesSummaries")
  public List<ReleaseSummary> getAllReleasesSummaries() {
    return releaseAnalyserService.getAllReleasesSummaries();
  }
}
