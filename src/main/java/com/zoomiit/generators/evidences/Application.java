/**
 * Application.java 14-sep-2019
 *
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences;

import java.io.File;

import com.zoomiit.generators.evidences.dtos.PdfInformationDto;
import com.zoomiit.generators.evidences.pdf.PdfGenerator;
import com.zoomiit.generators.evidences.repositories.git.GitScanner;
import com.zoomiit.generators.evidences.repositories.local.GitLocalScanner;
import com.zoomiit.generators.evidences.repositories.svn.SvnScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import reactor.core.publisher.Flux;

/**
 * The Class Application.
 *
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  @Autowired
  private GitScanner gitScanner;

  @Autowired
  private SvnScanner svnScanner;

  @Autowired
  private GitLocalScanner gitLocalScanner;

  @Autowired
  private PdfGenerator pdfGenerator;

  /**
   * Main method.
   *
   * @param args entry arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }

  /**
   * {@inheritDoc}
   */
  /* @Override */
  @Override
  public void run(final String... args) throws Exception {
    if (args.length < 2) {
      LOG.error("** Invalid arguments usage: java -jar evidences-generator-0.0.1.jar <user> <pass>");
    }
    if (args.length >= 2) {
      try {
        final ApplicationHome home = new ApplicationHome(Application.class);
        final File baseDirectory = home.getDir().getAbsoluteFile();

        final Flux<PdfInformationDto> git =
            this.gitScanner.generateRDInnovationEvidences(args[0], args[1], baseDirectory);
        final Flux<PdfInformationDto> svn =
            this.svnScanner.generateRDInnovationEvidences(args[0], args[1], baseDirectory);
        final Flux<PdfInformationDto> local =
            this.gitLocalScanner.generateRDInnovationEvidences(args[0], args[1], baseDirectory);

        final Flux<PdfInformationDto> pdfsInfo = Flux.merge(git, svn, local);

        pdfsInfo.subscribe(pdfInfo -> this.pdfGenerator.generateReport(pdfInfo, baseDirectory));

        LOG.info("FINISH!!");

      } catch (final Exception excep) {
        LOG.error(excep.getMessage(), excep);
      }
    }
  }

}
