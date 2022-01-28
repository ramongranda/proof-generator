/**
 * GitScanner.java 23 ene 2022
 *
 * Copyright 2022 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.repositories.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.zoomiit.generators.evidences.configuration.RepositoryInfo;
import com.zoomiit.generators.evidences.dtos.CommitDto;
import com.zoomiit.generators.evidences.dtos.PdfInformationDto;
import com.zoomiit.generators.evidences.repositories.Scanner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.AndRevFilter;
import org.eclipse.jgit.revwalk.filter.AuthorRevFilter;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Instancia un nuevo git scanner.
 *
 */
@Component
public class GitScanner extends Scanner {

  private static final Logger LOG = LoggerFactory.getLogger(GitScanner.class);

  protected String username;

  protected String password;

  /**
   * {@inheritDoc}
   */
  @Override
  public Flux<PdfInformationDto> generateRDInnovationEvidences(final String username, final String password,
      final File baseDirectory) throws IOException {

    if (Objects.isNull(this.appConfiguration)
        || Objects.isNull(this.appConfiguration.getGit())
        || !this.appConfiguration.getGit().isEnabled()) {
      return Flux.empty();
    }
    this.username = username;
    this.password = password;
    return this.generateEvidences(this.appConfiguration.getGit().getRepositories(), username);
  }

  /**
   * Obtain git commits.
   *
   * @param gitRepository git repository
   * @param map map
   * @return the map
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  protected List<CommitDto> obtainCommits(final RepositoryInfo gitRepository) {

    final List<CommitDto> commits = new ArrayList<>();

    for (final String branch : CollectionUtils.emptyIfNull(gitRepository.getBranches())) {
      final Git git = this.getGitRepository(gitRepository.getUrl(), branch, gitRepository.getToken());
      Validate.notNull(git, "Git branch %s of repository %s not found", branch, gitRepository.getUrl());
      final Repository repository = git.checkout().setName(branch).getRepository();
      this.getRepositoryCommits(gitRepository, commits, git, repository);
    }
    return commits;
  }

  /**
   * Obtains repository commits.
   *
   * @param gitRepository git repository
   * @param commits commits
   * @param git git
   * @param repository repository
   * @return repository commits
   */
  protected void getRepositoryCommits(final RepositoryInfo gitRepository, final List<CommitDto> commits,
      final Git git, final Repository repository) {
    try {
      LOG.info("Getting data from {} ...", repository);
      final List<Ref> allRefs = repository.getRefDatabase().getRefs();
      final RevWalk walk = new RevWalk(repository);
      for (final Ref ref : allRefs) {
        walk.markStart(walk.parseCommit(ref.getObjectId()));
      }

      final RevFilter between = CommitTimeRevFilter.between(this.appConfiguration.getSince(),
          this.appConfiguration.getUntil());
      final RevFilter author = AuthorRevFilter.create(this.username);

      final RevFilter filter = AndRevFilter.create(between, author);
      walk.setRevFilter(filter);

      for (final RevCommit commitData : walk) {
        final CommitDto gitCommit = new CommitDto(commitData.getName(), commitData.getAuthorIdent().getName(),
            new Date(Long.parseLong(String.valueOf(commitData.getCommitTime())) * 1000L),
            commitData.getShortMessage(), commitData.getFullMessage());
        if (commitData.getParents().length > 0) {
          this.readCommits(gitRepository, git, commitData, gitCommit);
        }
        commits.add(gitCommit);
      }
      walk.reset();
      walk.close();
    } catch (final Exception except) {
      LOG.error("[GitScanner::getRepositoryCommits] Error get commits of repository {}", gitRepository, except);
    }
  }

  /**
   * Read commits.
   *
   * @param gitRepository git repository
   * @param git git
   * @param commitData commit data
   * @param gitCommit git commit
   */
  protected void readCommits(final RepositoryInfo gitRepository, final Git git, final RevCommit commitData,
      final CommitDto gitCommit) {

    try (ObjectReader reader = git.getRepository().newObjectReader()) {
      final CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
      oldTreeIter.reset(reader, commitData.getTree());
      final CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
      newTreeIter.reset(reader, commitData.getParents()[0].getTree());

      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      final DiffFormatter diffFormater = new DiffFormatter(out);
      diffFormater.setRepository(git.getRepository());
      final List<DiffEntry> entries = diffFormater.scan(newTreeIter, oldTreeIter);

      entries.removeIf(item -> this.util.checkFileExclude(
          item.getNewPath().equals("/dev/null") ? item.getOldPath() : item.getNewPath()));

      gitCommit.setFiles(this.util.recoveryChangedFiles(entries));
      diffFormater.format(entries);
      diffFormater.close();
      gitCommit.setChanges(this.util.getLines(out));
    } catch (final Exception except) {
      LOG.error("[GitScanner::readCommits] Error get commits of repository {}", gitRepository, except);
    }
  }

  /**
   * Obtiene git repository.
   *
   * @param gitRepository git repository
   * @return git repository
   */
  private Git getGitRepository(final String url, final String branch, final String token) {

    CredentialsProvider cp = null;

    if (Objects.isNull(token)) {
      cp = new UsernamePasswordCredentialsProvider(this.username, this.password);
    } else {
      cp = new UsernamePasswordCredentialsProvider(token, StringUtils.EMPTY);
    }

    Path localPath;
    try {
      LOG.info("Connection to repository {} and branch {}...", url, branch);
      localPath = Files.createTempDirectory("GitRepository");
      final CloneCommand clone = Git.cloneRepository().setURI(url).setBranch(branch).setCredentialsProvider(cp)
          .setDirectory(localPath.toFile());
      this.disableSSLVerify(URI.create(url));
      return clone.call();

    } catch (final Exception excep) {
      LOG.error(excep.getLocalizedMessage(), excep);
      return null;
    }

  }

  /**
   * Disable SSL verify.
   *
   * @param gitServer git server
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ConfigInvalidException de config invalid exception
   */
  private void disableSSLVerify(final URI gitServer) throws IOException, ConfigInvalidException {
    if (gitServer.getScheme().equals("https") && !this.appConfiguration.getGit().isSslVerify()) {
      final FileBasedConfig config = SystemReader.getInstance().openUserConfig(null, FS.DETECTED);
      synchronized (config) {
        config.load();
        config.setBoolean("http", "https://"
            + gitServer.getHost() + ':'
            + (gitServer.getPort() == -1 ? 443 : gitServer.getPort()), "sslVerify", false);
        config.save();
      }
    }
  }

}
