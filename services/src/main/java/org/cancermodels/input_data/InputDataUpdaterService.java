package org.cancermodels.input_data;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.cancermodels.process_report.ProcessReportService;
import org.cancermodels.process_report.ProcessResponse;
import org.cancermodels.types.ProcessReportModules;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.RepositoryFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * This service gets from the data repository in GitLab the files that PDCM needs to work
 * (json files and treatment/diagnosis data).
 */
@Service
@Slf4j
public class InputDataUpdaterService {

  private final InputFilesFinder inputFilesFinder;

  @Value("${data-dir}")
  private String dataDir;

  @Value("${providers_data_path}")
  private String providersRootFolderPath;

  @Value("${mapping_path}")
  private String mappingPath;

  private final ProcessReportService processReportService;

  public InputDataUpdaterService(InputFilesFinder inputFilesFinder,
      ProcessReportService processReportService) {
    this.inputFilesFinder = inputFilesFinder;
    this.processReportService = processReportService;
  }

  /**
   * Downloads the files that PDCM Admin needs to work: mapping rules and treatment and sample
   * data.
   */
  public ProcessResponse updateInputData() {
    log.info("Downloading input data");
    try {
      deleteData();
      List<RepositoryFile> files = inputFilesFinder.getListFilesToDownload();
      writeFiles(files);
      registerProcess();
      log.info("End download input data.");

    } catch (GitLabApiException | IOException e) {
      e.printStackTrace();
    }
    return new ProcessResponse("Input data updated.");
  }

  private void registerProcess() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formatDateTime = LocalDateTime.now().format(formatter);
    processReportService.register(ProcessReportModules.INPUT_DATA, "Updated", formatDateTime);
  }

  private void deleteData() throws IOException {
    String providersDataDirectory = dataDir + "/" + providersRootFolderPath;
    String mappingRulesDirectory = dataDir + "/" + mappingPath;
    cleanDirectoryIfExists(providersDataDirectory);
    cleanDirectoryIfExists(mappingRulesDirectory);
  }

  private void cleanDirectoryIfExists(String directoryPath) throws IOException {
    File directory = new File(directoryPath);
    if (directory.exists()) {
      log.warn("Deleting all data in {}", directoryPath);
      FileUtils.cleanDirectory(directory);
    }
  }

  private void writeFiles(List<RepositoryFile> files) throws IOException {
    for (RepositoryFile file : files) {
      log.info("Writing file " + file.getFilePath());
      String path = dataDir + "/" + file.getFilePath();
      FileUtils.writeByteArrayToFile(new File(path), file.getDecodedContentAsBytes());
    }
  }

}
