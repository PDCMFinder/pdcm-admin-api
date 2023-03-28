package org.cancermodels.pdcm_releases;

import org.cancermodels.pdcm_admin.persistance.*;
import org.cancermodels.releases.ReleaseAnalyserService;
import org.springframework.web.bind.annotation.*;

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
  @PutMapping("test")
  public void test() {
  }

}
