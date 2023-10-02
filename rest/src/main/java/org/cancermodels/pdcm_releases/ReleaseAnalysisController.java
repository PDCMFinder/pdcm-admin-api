package org.cancermodels.pdcm_releases;

import org.cancermodels.filters.Facet;
import org.cancermodels.pdcm_admin.persistance.*;
import org.cancermodels.releases.ReleaseAnalyserService;
import org.cancermodels.releases.ReleaseSummary;
import org.cancermodels.releases.modelSummary.ModelSummaryFilter;
import org.cancermodels.releases.modelSummary.ModelSummaryFilterBuilder;
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
   * List all releases that have been executed in the ETL
   * @return List of {@link Release} objects.
   */
  @GetMapping("")
  public List<Release> listAllReleases()
  {
    return releaseAnalyserService.getAllReleases();
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
  @GetMapping("summary/{id}")
  public ReleaseSummary getReleaseSummary(@PathVariable long id) {
    return releaseAnalyserService.getReleaseSummary(id);
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

  @GetMapping("models/search/{releaseId}")
  public ResponseEntity<?> search(
      @PathVariable Long releaseId,
      Pageable pageable,
      @RequestParam(value = "model_type", required = false) List<String> modelTypes) {

    ModelSummaryFilter filter = ModelSummaryFilterBuilder.getInstance()
        .withModelType(modelTypes)
        .withReleaseId(Collections.singletonList(String.valueOf(releaseId)))
        .build();

    Page<ModelSummary> models = releaseAnalyserService.search(pageable, filter);
    return ResponseEntity.ok(models);
  }

}
