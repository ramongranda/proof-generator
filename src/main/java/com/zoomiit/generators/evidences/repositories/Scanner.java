package com.zoomiit.generators.evidences.repositories;

import com.zoomiit.generators.evidences.configuration.AppConfiguration;
import com.zoomiit.generators.evidences.configuration.RepositoryInfo;
import com.zoomiit.generators.evidences.model.Commit;
import com.zoomiit.generators.evidences.model.PdfInformation;
import com.zoomiit.generators.evidences.utils.EvidenceUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * The Class Scanner.
 * Scans repositories and generates evidence reports.
 */
@RequiredArgsConstructor
public abstract class Scanner {

  protected final AppConfiguration appConfiguration;

  protected final EvidenceUtils util;

  /**
   * Obtains pdf information.
   *
   * @param username      username
   * @param password      password
   * @param baseDirectory base directory
   * @return pdf information
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public abstract Flux<PdfInformation> generateRDInnovationEvidences(final String username, final String password,
                                                                     final File baseDirectory) throws IOException;

  /**
   * Generate report.
   *
   * @param repo     repo
   * @param since    since
   * @param username username
   */
  protected PdfInformation generateEvidences(final RepositoryInfo repo, final Date since, final String username) {

    PdfInformation out = null;

    if (repo.isEnabled()) {

      final List<Commit> commits = this.obtainCommits(repo);

      if (CollectionUtils.isNotEmpty(commits)) {

        out = PdfInformation.builder().username(username)
              .date(EvidenceUtils.format(since, EvidenceUtils.FECHA_MES_ANIO)).commits(commits)
              .name(repo.getCode()).build();

      }
    }
    return out;

  }

  /**
   * Generate evidences.
   *
   * @param repositories repositories
   * @param username     username
   * @return the flux
   */
  protected Flux<PdfInformation> generateEvidences(final List<RepositoryInfo> repositories, final String username) {
    return Flux.create((final FluxSink<PdfInformation> sink) -> CollectionUtils.emptyIfNull(repositories).forEach(repo -> {
      final PdfInformation evidences =
            this.generateEvidences(repo, this.appConfiguration.getSince(), username);
      if (Objects.nonNull(evidences)) {
        sink.next(evidences);
      }
    }));
  }

  /**
   * Obtain commits.
   *
   * @param repo repo
   * @return the list
   */
  protected abstract List<Commit> obtainCommits(final RepositoryInfo repo);

}
