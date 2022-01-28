/**
 * GitLocalScanner.java 22 ene 2022
 *
 * Copyright 2022 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.repositories.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.zoomiit.generators.evidences.configuration.RepositoryInfo;
import com.zoomiit.generators.evidences.dtos.CommitDto;
import com.zoomiit.generators.evidences.dtos.PdfInformationDto;
import com.zoomiit.generators.evidences.repositories.git.GitScanner;
import org.apache.commons.lang3.Validate;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * The Class GitLocalScanner.
 *
 */
@Component
public class GitLocalScanner extends GitScanner {

  private static final Logger LOG = LoggerFactory.getLogger(GitLocalScanner.class);

  /**
   * {@inheritDoc}
   */
  @Override
  public Flux<PdfInformationDto> generateRDInnovationEvidences(final String username, final String password,
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
  protected List<CommitDto> obtainCommits(final RepositoryInfo gitRepository) {

    final List<CommitDto> commits = new ArrayList<>();

    try (Git git = Git.open(new File(gitRepository.getPath()))) {

      Validate.notNull(git, "Git of repository %s not found", gitRepository.getPath());

      final Repository repository = git.getRepository();
      this.getRepositoryCommits(gitRepository, commits, git, repository);

    } catch (final Exception except) {
      LOG.error("[GitLocalScanner::obtainCommits] Error get commits of repository {}", gitRepository, except);
    }
    return commits;
  }

}
