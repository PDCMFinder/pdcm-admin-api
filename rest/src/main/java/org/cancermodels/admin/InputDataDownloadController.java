package org.cancermodels.admin;

import org.cancermodels.input_data.InputDataDownloaderService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Downloads the data that PDCM Admin needs from the data repository in GitLab.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/downloadInputData")
public class InputDataDownloadController {

  private final InputDataDownloaderService inputDataDownloaderService;

  public InputDataDownloadController(
      InputDataDownloaderService inputDataDownloaderService) {
    this.inputDataDownloaderService = inputDataDownloaderService;
  }

  @PostMapping
  public void downloadInputData() {
    inputDataDownloaderService.downloadInputData();
  }
}
