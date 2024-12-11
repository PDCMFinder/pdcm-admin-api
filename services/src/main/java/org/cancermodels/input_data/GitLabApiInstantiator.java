package org.cancermodels.input_data;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provides a single instance of a {@link GitLabApi} objet
 */
@Component
public class GitLabApiInstantiator {

  private static final Logger log = LoggerFactory.getLogger(GitLabApiInstantiator.class);
  private static GitLabApi gitLabApi;

  @Value("${data_repo_gitlab_token}")
  private String dataRepoGitlabToken;

  // Returns (or create if not exists) a GitLabApi instance to communicate with your GitLab server.
  GitLabApi getGitLabApiInstance() {
    if (gitLabApi == null) {
      try {
        gitLabApi = new GitLabApi(GitLabApiConstants.GITLAB_URL, dataRepoGitlabToken);
      } catch (Exception e) {
        log.error("Error initializing GitLab API", e);
        throw new RuntimeException("Error initializing GitLab API", e);
      }

    }
    return gitLabApi;
  }

}
