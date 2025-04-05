package com.zoomiit.generators.evidences;

import com.zoomiit.generators.evidences.model.PdfInformation;
import com.zoomiit.generators.evidences.pdf.PdfGenerator;
import com.zoomiit.generators.evidences.repositories.git.GitScanner;
import com.zoomiit.generators.evidences.repositories.local.GitLocalScanner;
import com.zoomiit.generators.evidences.repositories.svn.SvnScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * The Class Application.
 */
@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class Application implements CommandLineRunner {

  private final GitScanner gitScanner;

  private final SvnScanner svnScanner;

  private final GitLocalScanner gitLocalScanner;

  private final PdfGenerator pdfGenerator;

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
  @Override
  public void run(final String... args) throws Exception {
    if (args.length < 2) {
      log.error("** Invalid arguments usage: java -jar evidences-generator-0.0.1.jar <user> <pass>");
    }
    if (args.length >= 2) {
      try {
        final ApplicationHome home = new ApplicationHome(Application.class);
        final File baseDirectory = home.getDir().getAbsoluteFile();

        final Flux<PdfInformation> git =
              this.gitScanner.generateRDInnovationEvidences(args[0], args[1], baseDirectory);
        final Flux<PdfInformation> svn =
              this.svnScanner.generateRDInnovationEvidences(args[0], args[1], baseDirectory);
        final Flux<PdfInformation> local =
              this.gitLocalScanner.generateRDInnovationEvidences(args[0], args[1], baseDirectory);

        final Flux<PdfInformation> pdfsInfo = Flux.merge(git, svn, local);

        pdfsInfo.subscribe(pdfInfo -> this.pdfGenerator.generateReport(pdfInfo, baseDirectory));

        log.info("FINISH!!");

      } catch (final Exception excep) {
        log.error(excep.getMessage(), excep);
      }
    }
  }

}
