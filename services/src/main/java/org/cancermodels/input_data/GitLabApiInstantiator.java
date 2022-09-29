package org.cancermodels.input_data;

import org.gitlab4j.api.GitLabApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provides a single instance of a {@link GitLabApi} objet
 */
@Component
class GitLabApiInstantiator {

  private static GitLabApi gitLabApi;

  @Value("${data_repo_gitlab_token}")
  private String dataRepoGitlabToken;

  // Returns (or create if not exists) a GitLabApi instance to communicate with your GitLab server.
  GitLabApi getGitLabApiInstance() {
    if (gitLabApi == null) {
      gitLabApi = new GitLabApi(GitLabApiConstants.GITLAB_URL, dataRepoGitlabToken);
    }
    return gitLabApi;
  }

}
