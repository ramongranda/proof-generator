/**
 * SvnScanner.java 23 ene 2022
 *
 * Copyright 2022 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.repositories.svn;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.zoomiit.generators.evidences.configuration.RepositoryInfo;
import com.zoomiit.generators.evidences.dtos.ChangedPathDto;
import com.zoomiit.generators.evidences.dtos.CommitDto;
import com.zoomiit.generators.evidences.dtos.FileDiffDto;
import com.zoomiit.generators.evidences.dtos.PdfInformationDto;
import com.zoomiit.generators.evidences.enums.FileType;
import com.zoomiit.generators.evidences.repositories.Scanner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import reactor.core.publisher.Flux;

/**
 * The Class SvnScanner.
 *
 */
@Component
public class SvnScanner extends Scanner {

  private static final Logger LOG = LoggerFactory.getLogger(SvnScanner.class);

  private String username;

  private String password;

  private SVNRepository repository;

  private SVNClientManager client;

  /**
   * {@inheritDoc}
   */
  @Override
  public Flux<PdfInformationDto> generateRDInnovationEvidences(final String username, final String password,
      final File baseDirectory)
      throws IOException {

    if (Objects.isNull(this.appConfiguration)
        || Objects.isNull(this.appConfiguration.getSvn())
        || !this.appConfiguration.getSvn().isEnabled()) {
      return Flux.empty();
    }

    this.username = username;
    this.password = password;

    return this.generateEvidences(this.appConfiguration.getSvn().getRepositories(), username);
  }

  /**
   * Obtain commits.
   *
   * @param repository repository
   * @param map map
   * @throws SVNException
   * @throws IOException
   */
  @Override
  @SuppressWarnings({"rawtypes"})
  protected List<CommitDto> obtainCommits(final RepositoryInfo repository) {
    final List<CommitDto> commits = new ArrayList<>();
    try {

      for (final String branch : CollectionUtils.emptyIfNull(repository.getBranches())) {
        final SVNURL url = SVNURL.parseURIEncoded(repository.getUrl() + '/' + branch);
        this.getSVNRepo(url);
        LOG.info("Getting data from {} ...", repository);
        final long startRevision = this.repository.getDatedRevision(this.appConfiguration.getSince());
        final long endRevision = this.repository.getDatedRevision(this.appConfiguration.getUntil());

        final List<FileDiffDto> diff = this.getDifferences(url, startRevision, endRevision);

        final Collection logEntries =
            this.repository.log(new String[]{""}, null, startRevision, endRevision, true, true);
        for (final Iterator entries = logEntries.iterator(); entries.hasNext();) {
          this.aggregateEntriesCommits(commits, diff, entries);
        }
      }

    } catch (final Exception excep) {
      LOG.error("[SvnScanner:: obtainCommits] repository {} ", repository, excep);
    }
    return commits;
  }

  /**
   * Aggregate entries commits.
   *
   * @param commits commits
   * @param diff diff
   * @param logEntries log entries
   */
  @SuppressWarnings({"rawtypes"})
  private void aggregateEntriesCommits(final List<CommitDto> commits, final List<FileDiffDto> diff,
      final Iterator entries) {

    final SVNLogEntry logEntry = (SVNLogEntry) entries.next();
    if ((logEntry.getAuthor() != null)
        && logEntry.getAuthor().equals(this.username)
        && this.util.evaluateAddCommit(logEntry.getMessage())) {
      final CommitDto rev = new CommitDto(String.valueOf(logEntry.getRevision()), logEntry.getAuthor(),
          logEntry.getDate(),
          logEntry.getMessage(), logEntry.getMessage());

      if (logEntry.getChangedPaths().size() > 0) {
        final Set<String> changedPathsSet = logEntry.getChangedPaths().keySet();
        this.scanChangedPaths(diff, logEntry, rev, changedPathsSet);
      }
      commits.add(rev);
    }

  }

  /**
   * Scan changed paths.
   *
   * @param diff diff
   * @param logEntry log entry
   * @param rev rev
   * @param changedPathsSet changed paths set
   */
  private void scanChangedPaths(final List<FileDiffDto> diff, final SVNLogEntry logEntry, final CommitDto rev,
      final Set<String> changedPathsSet) {
    for (final String string : changedPathsSet) {
      final SVNLogEntryPath entryPath = logEntry.getChangedPaths().get(string);

      if (!this.util.checkFileExclude(entryPath.getPath())) {

        final ChangedPathDto cp = new ChangedPathDto(FileType.getType(entryPath.getType()),
            entryPath.getPath(),
            entryPath.getPath(), entryPath.getCopyRevision());
        final Map<String, List<String>> lines = new HashMap<>();

        diff.removeIf(item -> this.util.checkFileExclude(item.getPath()));

        for (final FileDiffDto file : diff) {

          if (entryPath.getPath().contains(file.getPath())) {
            cp.setFile(file);
          }
          lines.put(file.getPath(), file.getLines());
        }

        rev.setChanges(lines);
        rev.addChangedPath(cp);
      }
    }
  }

  /**
   * Obtiene differences.
   *
   * @param url url
   * @param startRevision start revision
   * @param endRevision end revision
   * @return differences
   * @throws SVNException de SVN exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private List<FileDiffDto> getDifferences(final SVNURL url, final long startRevision, final long endRevision)
      throws SVNException, IOException {

    final List<FileDiffDto> diff = new ArrayList<>();

    final SVNDiffClient diffClient = this.client.getDiffClient();

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    diffClient.doDiff(url, SVNRevision.create(startRevision), url, SVNRevision.create(endRevision),
        SVNDepth.UNKNOWN, Boolean.TRUE, baos);

    final String diffRaw = baos.toString(StandardCharsets.UTF_8);

    final String[] lines = diffRaw.split("\\r?\\n");
    String[] arrayOfString1;
    final int j = (arrayOfString1 = lines).length;
    for (int i = 0; i < j; i++) {
      final String line = arrayOfString1[i];
      if (line != null) {
        final String parsedLine = StringUtils.stripAccents(line);
        if (parsedLine.matches("\\A\\p{ASCII}*\\z")) {
          final String lineCode = this.util.removeInvalidCharacters(line);
          if (lineCode.contains("Index: ")) {
            diff.add(new FileDiffDto("/" + lineCode.replaceAll("Index: ", "")));
          } else if (!diff.isEmpty()) {
            diff.get(diff.size() - 1).addLine(this.util.removeInvalid(lineCode));
          }
        }
      }
    }
    baos.close();
    return diff;
  }

  /**
   * Obtiene SVN repo.
   *
   * @param repository repository
   * @return
   * @return SVN repo
   * @throws SVNException
   */
  private void getSVNRepo(final SVNURL url) throws SVNException {
    DAVRepositoryFactory.setup();
    SVNRepositoryFactoryImpl.setup();
    FSRepositoryFactory.setup();
    LOG.info("Connection to {} ...", url);

    this.repository = SVNRepositoryFactory.create(url);

    final ISVNAuthenticationManager auth = SVNWCUtil.createDefaultAuthenticationManager(this.username,
        this.password.toCharArray());

    this.repository.setAuthenticationManager(auth);
    this.client = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(Boolean.TRUE), auth);

  }

}
