package org.cancermodels.admin;

import org.cancermodels.input_data.InputDataUpdaterService;
import org.cancermodels.process_report.ProcessResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Downloads the data that PDCM Admin needs from the data repository in GitLab.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/updateInputData")
public class InputDataUpdaterController {

  private final InputDataUpdaterService inputDataUpdaterService;

  public InputDataUpdaterController(
      InputDataUpdaterService inputDataDownloaderService) {
    this.inputDataUpdaterService = inputDataDownloaderService;
  }

  @PostMapping
  public ProcessResponse updateInputData() {
    return inputDataUpdaterService.updateInputData();
  }
}
