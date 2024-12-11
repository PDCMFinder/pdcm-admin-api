package org.cancermodels.input_data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.cancermodels.input_data.exceptions.InputFileDownloadException;
import org.cancermodels.mappings.EntityTypeService;
import org.cancermodels.pdcm_admin.persistance.EntityType;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.TreeItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class detects the files that PDCM Admin needs from the data repository.
 */
@Component
@Slf4j
public class InputFilesFinder {

  // Map with path (relative to a provider folder) and the list of patterns for the files we are interested in:
  private Map<String, List<String>> filesFinderMap;

  private static final String PATIENT_SAMPLE_PATTERN = ".*metadata-patient_sample.*\\.tsv";
  private static final String DRUG_PATTERN = ".*drug.*\\.tsv";
  private static final String TREATMENT_PATTERN = ".*treatment.*\\.tsv";
  private static final String PATIENT_TREATMENT_FOLDER = "/treatment";
  private static final String DRUG_DOSING_FOLDER = "/drug";

  @Value("${providers_data_path}")
  private String providersRootFolderPath;

  @Value("${mapping_path}")
  private String mappingPath;

  @Value("${data_repo_gitlab_branch}")
  private String dataRepoGitlabBranch;

  //private final GitLabApi gitLabApi=null;

  private final GitLabApiInstantiator gitLabApiInstantiator;

  private final EntityTypeService entityTypeService;

  public InputFilesFinder(GitLabApiInstantiator gitLabApiInstantiator,
      EntityTypeService entityTypeService) {
    this.entityTypeService = entityTypeService;
    this.gitLabApiInstantiator = gitLabApiInstantiator;
    initFilesFinderMap();
  }

  private void initFilesFinderMap() {
    filesFinderMap = new HashMap<>();

    // Files that are in the root of the folder
    String providerFolderRoot = "";
    filesFinderMap.put(providerFolderRoot, Collections.singletonList(PATIENT_SAMPLE_PATTERN));

    // Files that are under the treatment subfolder:
    filesFinderMap.put(PATIENT_TREATMENT_FOLDER, Collections.singletonList(TREATMENT_PATTERN));

    // Files that are under the drug subfolder:
    filesFinderMap.put(DRUG_DOSING_FOLDER, Collections.singletonList(DRUG_PATTERN));
  }

  /**
   * Find the paths of the files that need to be downloaded from the GitLab repo.
   * @return List of paths
   */
  public List<RepositoryFile> getListFilesToDownload() throws GitLabApiException, IOException {
    log.info("Finding input files...");
    GitLabApi gitLabApi = gitLabApiInstantiator.getGitLabApiInstance();
    List<RepositoryFile> files = new ArrayList<>();
    List<RepositoryFile> filesFromProviders = getListFilesToDownloadFromProviders(gitLabApi);
    List<RepositoryFile> filesFromMappingsFolder = getFilesFromMappingFolder(gitLabApi);
    files.addAll(filesFromProviders);
    files.addAll(filesFromMappingsFolder);
    return files;

  }

  private List<RepositoryFile> getFilesFromMappingFolder(GitLabApi gitLabApi) {
    log.info("Finding mapping files...");
    List<RepositoryFile> mappingFiles = new ArrayList<>();
    for (EntityType entityType : entityTypeService.getAll()) {
      String path = mappingPath + "/" + entityType.getMappingRulesFileName();
      RepositoryFile file = null;
      try {
        file = gitLabApi.getRepositoryFileApi().getFile(
            GitLabApiConstants.PROJECT_ID, path, dataRepoGitlabBranch);
      } catch (GitLabApiException e) {
        String error = String.format(
            "File %s not found in the data repository. Error: %s", path, e.getMessage());
        throw new InputFileDownloadException(error);
      }
      mappingFiles.add(file);
    }
    return mappingFiles;

  }

  private List<RepositoryFile> getListFilesToDownloadFromProviders(GitLabApi gitLabApi)
      throws GitLabApiException, IOException {
    log.info("Calling api to get folders for project {} in path {} ref {}",
        GitLabApiConstants.PROJECT_ID, providersRootFolderPath, dataRepoGitlabBranch);

    List<TreeItem> providerFolders = gitLabApi.getRepositoryApi().getTree(
        GitLabApiConstants.PROJECT_ID, providersRootFolderPath, dataRepoGitlabBranch);

    return processProviderFolders(providerFolders, gitLabApi);
  }

  private List<RepositoryFile> processProviderFolders(List<TreeItem> providerFolders, GitLabApi gitLabApi)
      throws GitLabApiException {
    List<RepositoryFile> files = new ArrayList<>();
    for (TreeItem item : providerFolders) {

      files.addAll(processProviderFolder(item, gitLabApi));
    }

    return files;
  }

  private List<RepositoryFile> processProviderFolder(TreeItem item, GitLabApi gitLabApi)
      throws GitLabApiException {
    log.info("Processing {}", item.getPath());
    List<RepositoryFile> paths = new ArrayList<>();

    for (String folder : filesFinderMap.keySet()) {
      // Bring the files from the respective subfolder
      List<TreeItem> files = gitLabApi.getRepositoryApi()
          .getTree(GitLabApiConstants.PROJECT_ID, item.getPath() + folder, dataRepoGitlabBranch);

      // Get the ones that match with the expected patterns
      String regex = String.join("|", filesFinderMap.get(folder));

      for (TreeItem treeItem : files) {
        if (treeItem.getPath().matches(regex)) {

          RepositoryFile file = gitLabApi.getRepositoryFileApi().getFile(
              GitLabApiConstants.PROJECT_ID, treeItem.getPath(), dataRepoGitlabBranch);
          paths.add(file);
        }
      }
    }
    return paths;
  }

}
