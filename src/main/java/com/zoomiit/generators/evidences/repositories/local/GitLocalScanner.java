/**
 * GitLocalScanner.java 22 ene 2022
 * <p>
 * Copyright 2022 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.repositories.local;

import com.zoomiit.generators.evidences.configuration.AppConfiguration;
import com.zoomiit.generators.evidences.configuration.RepositoryInfo;
import com.zoomiit.generators.evidences.model.Commit;
import com.zoomiit.generators.evidences.model.PdfInformation;
import com.zoomiit.generators.evidences.repositories.git.GitScanner;
import com.zoomiit.generators.evidences.utils.EvidenceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The Class GitLocalScanner.
 */
@Slf4j
@Component
public class GitLocalScanner extends GitScanner {

  /**
   * Instantiates a new GitLocalScanner.
   *
   * @param appConfiguration the application configuration
   * @param util             the evidence utils
   */
  public GitLocalScanner(final AppConfiguration appConfiguration, final EvidenceUtils util) {
    super(appConfiguration, util);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Flux<PdfInformation> generateRDInnovationEvidences(final String username, final String password,
                                                            final File baseDirectory) throws IOException {

    if (Objects.isNull(this.appConfiguration)
          || Objects.isNull(this.appConfiguration.getLocal())
          || !this.appConfiguration.getLocal().isEnabled()) {
      return Flux.empty();
    }

    super.username = username;

    return this.generateEvidences(this.appConfiguration.getLocal().getRepositories(), username);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected List<Commit> obtainCommits(final RepositoryInfo gitRepository) {

    final List<Commit> commits = new ArrayList<>();

    try (Git git = Git.open(new File(gitRepository.getPath()))) {

      Validate.notNull(git, "Git of repository %s not found", gitRepository.getPath());

      final Repository repository = git.getRepository();
      this.getRepositoryCommits(gitRepository, commits, git, repository);

    } catch (final Exception except) {
      log.error("[GitLocalScanner::obtainCommits] Error get commits of repository {}", gitRepository, except);
    }
    return commits;
  }

}
