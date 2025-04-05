package com.zoomiit.generators.evidences.repositories.svn;

import com.zoomiit.generators.evidences.configuration.AppConfiguration;
import com.zoomiit.generators.evidences.configuration.RepositoryInfo;
import com.zoomiit.generators.evidences.enums.FileType;
import com.zoomiit.generators.evidences.model.ChangedPath;
import com.zoomiit.generators.evidences.model.Commit;
import com.zoomiit.generators.evidences.model.FileDiff;
import com.zoomiit.generators.evidences.model.PdfInformation;
import com.zoomiit.generators.evidences.repositories.Scanner;
import com.zoomiit.generators.evidences.utils.EvidenceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

/**
 * The Class SvnScanner.
 * Scans SVN repositories and generates evidence reports.
 */
@Slf4j
@Component
public class SvnScanner extends Scanner {

  private String username;
  private String password;
  private SVNRepository repository;
  private SVNClientManager client;

  /**
   * Instantiates a new SvnScanner.
   *
   * @param appConfiguration the application configuration
   * @param util             the evidence utilities
   */
  public SvnScanner(final AppConfiguration appConfiguration, final EvidenceUtils util) {
    super(appConfiguration, util);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Flux<PdfInformation> generateRDInnovationEvidences(final String username, final String password,
                                                            final File baseDirectory) throws IOException {

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
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings({"rawtypes"})
  protected List<Commit> obtainCommits(final RepositoryInfo repository) {
    final List<Commit> commits = new ArrayList<>();
    try {

      for (final String branch : CollectionUtils.emptyIfNull(repository.getBranches())) {
        final SVNURL url = SVNURL.parseURIEncoded(repository.getUrl() + '/' + branch);
        this.getSVNRepo(url);
        log.info("Getting data from {} ...", repository);
        final long startRevision = this.repository.getDatedRevision(this.appConfiguration.getSince());
        final long endRevision = this.repository.getDatedRevision(this.appConfiguration.getUntil());

        final List<FileDiff> diff = this.getDifferences(url, startRevision, endRevision);

        final Collection logEntries =
              this.repository.log(new String[]{""}, null, startRevision, endRevision, true, true);
        for (final Iterator entries = logEntries.iterator(); entries.hasNext(); ) {
          this.aggregateEntriesCommits(commits, diff, entries);
        }
      }

    } catch (final Exception excep) {
      log.error("[SvnScanner:: obtainCommits] repository {} ", repository, excep);
    }
    return commits;
  }

  /**
   * Aggregates entries commits.
   *
   * @param commits the list of commits to be populated
   * @param diff    the list of file differences
   * @param entries the log entries iterator
   */
  @SuppressWarnings({"rawtypes"})
  private void aggregateEntriesCommits(final List<Commit> commits, final List<FileDiff> diff,
                                       final Iterator entries) {

    final SVNLogEntry logEntry = (SVNLogEntry) entries.next();
    if ((logEntry.getAuthor() != null)
          && logEntry.getAuthor().equals(this.username)
          && this.util.evaluateAddCommit(logEntry.getMessage())) {
      final Commit rev = new Commit(String.valueOf(logEntry.getRevision()), logEntry.getAuthor(),
            logEntry.getDate(),
            logEntry.getMessage(), logEntry.getMessage());

      if (!logEntry.getChangedPaths().isEmpty()) {
        final Set<String> changedPathsSet = logEntry.getChangedPaths().keySet();
        this.scanChangedPaths(diff, logEntry, rev, changedPathsSet);
      }
      commits.add(rev);
    }

  }

  /**
   * Scans changed paths.
   *
   * @param diff            the list of file differences
   * @param logEntry        the log entry
   * @param rev             the commit DTO to be populated
   * @param changedPathsSet the set of changed paths
   */
  private void scanChangedPaths(final List<FileDiff> diff, final SVNLogEntry logEntry, final Commit rev,
                                final Set<String> changedPathsSet) {
    for (final String string : changedPathsSet) {
      final SVNLogEntryPath entryPath = logEntry.getChangedPaths().get(string);

      if (!this.util.checkFileExclude(entryPath.getPath())) {

        final ChangedPath cp = new ChangedPath(FileType.getType(entryPath.getType()),
              entryPath.getPath(),
              entryPath.getPath(), entryPath.getCopyRevision());
        final Map<String, List<String>> lines = new HashMap<>();

        diff.removeIf(item -> this.util.checkFileExclude(item.getPath()));

        for (final FileDiff file : diff) {

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
   * Gets the differences between revisions.
   *
   * @param url           the repository URL
   * @param startRevision the start revision
   * @param endRevision   the end revision
   * @return the list of file differences
   * @throws SVNException if an SVN exception occurs
   * @throws IOException  if an I/O exception occurs
   */
  private List<FileDiff> getDifferences(final SVNURL url, final long startRevision, final long endRevision)
        throws SVNException, IOException {

    final List<FileDiff> diff = new ArrayList<>();

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
            diff.add(new FileDiff("/" + lineCode.replaceAll("Index: ", "")));
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
   * Gets the SVN repository.
   *
   * @param url the repository URL
   * @throws SVNException if an SVN exception occurs
   */
  private void getSVNRepo(final SVNURL url) throws SVNException {
    DAVRepositoryFactory.setup();
    SVNRepositoryFactoryImpl.setup();
    FSRepositoryFactory.setup();
    log.info("Connection to {} ...", url);

    this.repository = SVNRepositoryFactory.create(url);

    final ISVNAuthenticationManager auth = SVNWCUtil.createDefaultAuthenticationManager(this.username,
          this.password.toCharArray());

    this.repository.setAuthenticationManager(auth);
    this.client = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(Boolean.TRUE), auth);

  }

}

