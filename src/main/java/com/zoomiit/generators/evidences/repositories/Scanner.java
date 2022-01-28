/**
 * Scanner.java 22 ene 2022
 *
 * Copyright 2022 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.repositories;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.zoomiit.generators.evidences.configuration.AppConfiguration;
import com.zoomiit.generators.evidences.configuration.RepositoryInfo;
import com.zoomiit.generators.evidences.dtos.CommitDto;
import com.zoomiit.generators.evidences.dtos.PdfInformationDto;
import com.zoomiit.generators.evidences.utils.EvidenceUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * The Interface Scanner.
 *
 */
public abstract class Scanner {

  @Autowired
  protected AppConfiguration appConfiguration;

  @Autowired
  protected EvidenceUtils util;

  /**
   * Obtains pdf information.
   *
   * @param username username
   * @param password password
   * @param baseDirectory base directory
   * @return pdf information
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public abstract Flux<PdfInformationDto> generateRDInnovationEvidences(final String username, final String password,
      final File baseDirectory) throws IOException;

  /**
   * Generate report.
   *
   * @param repo repo
   * @param since since
   * @param username username
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected PdfInformationDto generateEvidences(final RepositoryInfo repo, final Date since, final String username) {

    PdfInformationDto out = null;

    if (repo.isEnabled()) {

      final List<CommitDto> commits = this.obtainCommits(repo);

      if (CollectionUtils.isNotEmpty(commits)) {

        out = PdfInformationDto.builder().username(username)
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
   * @param username username
   * @return the flux
   */
  protected Flux<PdfInformationDto> generateEvidences(final List<RepositoryInfo> repositories, final String username) {
    return Flux.create((final FluxSink<PdfInformationDto> sink) -> CollectionUtils.emptyIfNull(repositories).stream().forEach(repo -> {
      final PdfInformationDto evidences =
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
  protected abstract List<CommitDto> obtainCommits(final RepositoryInfo repo);

}
