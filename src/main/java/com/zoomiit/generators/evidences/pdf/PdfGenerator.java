/**
 * PdfGenerator.java 23 ene 2022
 * <p>
 * Copyright 2022 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.pdf;

import com.zoomiit.generators.evidences.configuration.AppConfiguration;
import com.zoomiit.generators.evidences.model.ChangedPath;
import com.zoomiit.generators.evidences.model.Commit;
import com.zoomiit.generators.evidences.model.PdfInformation;
import com.zoomiit.generators.evidences.utils.EvidenceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;
import rst.pdfbox.layout.elements.ControlElement;
import rst.pdfbox.layout.elements.Document;
import rst.pdfbox.layout.elements.Frame;
import rst.pdfbox.layout.elements.Paragraph;
import rst.pdfbox.layout.shape.Rect;
import rst.pdfbox.layout.shape.Stroke;
import rst.pdfbox.layout.text.BaseFont;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * The Class PdfGenerator.
 * Generates PDF reports based on commit information.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PdfGenerator {

  private final EvidenceUtils util;

  private final AppConfiguration appConfiguration;

  private Path root;

  /**
   * Generates a PDF report.
   *
   * @param pdfInformation the PDF information DTO
   * @param file           the file to save the report
   */
  public void generateReport(final PdfInformation pdfInformation, final File file) {

    if (Objects.nonNull(pdfInformation) && CollectionUtils.isNotEmpty(pdfInformation.getCommits())) {

      this.initialize(file);

      pdfInformation.getCommits().removeIf(item -> CollectionUtils.isEmpty(item.getFiles()));
      try {
        final Document document = this.generateHead(pdfInformation);

        Paragraph subtitle = new Paragraph();
        subtitle.addText("Índice " + pdfInformation.getName(), 16.0F, PDType1Font.HELVETICA_BOLD);

        Frame subtitleFrame = new Frame(subtitle);
        subtitleFrame.setShape(new Rect());
        subtitleFrame.setMargin(0.0F, 0.0F, 5.0F, 15.0F);

        document.add(subtitleFrame);
        this.generateIndex(document, pdfInformation, pdfInformation);
        document.add(ControlElement.NEWPAGE);
        subtitle = new Paragraph();
        subtitle.addText("Detalles " + pdfInformation.getName(), 16.0F, PDType1Font.HELVETICA_BOLD);

        subtitleFrame = new Frame(subtitle);
        subtitleFrame.setShape(new Rect());
        subtitleFrame.setMargin(0.0F, 0.0F, 0.0F, 5.0F);

        document.add(subtitleFrame);
        this.generateDetail(document, pdfInformation, pdfInformation);
        this.savePdf(pdfInformation.getUsername(), pdfInformation.getName(), document);
      } catch (final Exception excep) {
        log.error("[PdfGenerator:generateReport] -> Error generating the report {} ", pdfInformation, excep);
      }
    }

  }

  /**
   * Initialize the root path for saving the PDF.
   *
   * @param file the file
   */
  private void initialize(final File file) {
    try {
      this.root = Paths.get(file.getPath(), "EVIDENCES/"
            + EvidenceUtils
            .format(this.appConfiguration.getSince(), EvidenceUtils.FECHA_ANIO_MES).toUpperCase());
      Files.createDirectories(this.root);
    } catch (final IOException e) {
      // NO DATA
    }

  }

  /**
   * Generates the header of the PDF document.
   *
   * @param pdfInformation the PDF information DTO
   * @return the document with the header
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private Document generateHead(final PdfInformation pdfInformation) throws IOException {
    final Document document = new Document(20.0F, 20.0F, 20.0F, 60.0F);

    final Paragraph title = new Paragraph();
    title.addText(
          "Commits de "
                + this.appConfiguration.getCommitter() + " (" + pdfInformation.getUsername() + ") en el proyecto "
                + this.appConfiguration.getPon() + ": " + pdfInformation.getDate(),
          18.0F, PDType1Font.HELVETICA_BOLD);

    final Frame titleFrame = new Frame(title);
    titleFrame.setShape(new Rect());
    titleFrame.setMargin(0.0F, 0.0F, 0.0F, 10.0F);

    document.add(titleFrame);
    return document;
  }

  /**
   * Generates the detail section of the PDF document.
   *
   * @param document       the document
   * @param proyect        the project information
   * @param pdfInformation the PDF information DTO
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void generateDetail(final Document document, final PdfInformation proyect,
                              final PdfInformation pdfInformation)
        throws IOException {

    for (final Commit commit : proyect.getCommits()) {

      if (this.util.evaluateAddCommit(commit.getShortMessage())) {

        final Frame divider = new Frame(new Paragraph(), 515.0F, 1.0F);
        divider.setShape(new Rect());
        divider.setBorder(Color.LIGHT_GRAY, new Stroke(1.0F));
        divider.setMargin(0.0F, 0.0F, 15.0F, 15.0F);

        document.add(divider);

        Paragraph paragraph = new Paragraph();
        paragraph.addMarkup("{color:#0052cc}"
              + this.appConfiguration.getCommitter()
              + "{color:#172b4d} committed {color:#0052cc}" + commit.getRevision() + "(" + pdfInformation.getUsername()
              + ") {color:#172b4d}"
              + EvidenceUtils.format(commit.getDate(), EvidenceUtils.FECHA_HORA), 9.0F, BaseFont.Helvetica);
        final Frame commitInfoFrame = new Frame(paragraph);
        commitInfoFrame.setShape(new Rect());

        document.add(commitInfoFrame);

        paragraph = new Paragraph();
        paragraph.setLineSpacing(1.5F);
        paragraph.addText(this.util.removeInvalidCharacters(commit.getFullMessage()), 10.0F,
              PDType1Font.HELVETICA);
        final Frame frame = new Frame(paragraph);
        frame.setShape(new Rect());
        frame.setMargin(0.0F, 0.0F, 15.0F, 15.0F);
        frame.setPadding(1.0F, 1.0F, 1.0F, 15.0F);
        document.add(frame);
        if (commit.getChanges() != null) {
          this.addCommits(document, commit, frame);
        }
      }
    }
  }

  /**
   * Adds commit changes to the PDF document.
   *
   * @param document the document
   * @param commit   the commit information
   * @param frame    the frame
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void addCommits(final Document document, final Commit commit, final Frame frame) throws IOException {
    Paragraph paragraph;
    for (final String diff : commit.getChanges().keySet()) {

      for (final String line : commit.getChanges().get(diff)) {
        paragraph = new Paragraph();
        paragraph.setLineSpacing(1F);
        paragraph.addText(this.util.removeInvalidCharacters(line), 8.0F, PDType1Font.HELVETICA);

        final Frame changesFrame = new Frame(paragraph, 515.0F, null);
        changesFrame.setShape(new Rect());
        changesFrame.setBackgroundColor(Color.WHITE);
        if (line.startsWith("+")) {
          changesFrame.setBackgroundColor(new Color(221, 255, 221));
        } else if (line.startsWith("-")) {
          changesFrame.setBackgroundColor(new Color(254, 232, 233));
        }
        if (line.startsWith("---") || line.startsWith("+++")) {
          changesFrame.setBackgroundColor(Color.WHITE);
        }

        changesFrame.setPadding(1.0F, 1.0F, 1.0F, 1.0F);
        document.add(changesFrame);
      }
      paragraph = new Paragraph();
      final Frame espacio = new Frame(paragraph);
      frame.setShape(new Rect());
      espacio.setBackgroundColor(Color.WHITE);
      espacio.setPadding(1.0F, 1.0F, 1.0F, 15.0F);
      document.add(espacio);

    }
  }

  /**
   * Generates the index section of the PDF document.
   *
   * @param document       the document
   * @param project        the project information
   * @param pdfInformation the PDF information DTO
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void generateIndex(final Document document, final PdfInformation project,
                             final PdfInformation pdfInformation)
        throws IOException {
    for (final Commit commit : project.getCommits()) {

      if (this.util.evaluateAddCommit(commit.getShortMessage())) {

        Paragraph paragraph = new Paragraph();
        paragraph.addMarkup("{color:#0052cc}"
              + this.appConfiguration.getCommitter()
              + "{color:#172b4d} committed {color:#0052cc}" + commit.getRevision() + " (" + pdfInformation.getUsername()
              + ") {color:#172b4d}"
              + EvidenceUtils.format(commit.getDate(), EvidenceUtils.FECHA_HORA), 9.0F, BaseFont.Helvetica);

        Frame commitInfoFrame = new Frame(paragraph);
        document.add(commitInfoFrame);

        paragraph = new Paragraph();
        paragraph.addText(this.util.removeInvalidCharacters(commit.getShortMessage()), 9.0F,
              PDType1Font.HELVETICA);
        document.add(paragraph);
        commitInfoFrame.setShape(new Rect());
        commitInfoFrame.setMargin(0.0F, 0.0F, 5.0F, 5.0F);

        paragraph = new Paragraph();
        paragraph.addMarkup("Ficheros modificados:", 9.0F, BaseFont.Helvetica);
        commitInfoFrame = new Frame(paragraph);
        commitInfoFrame.setShape(new Rect());
        commitInfoFrame.setMargin(0.0F, 0.0F, 5.0F, 5.0F);
        document.add(commitInfoFrame);

        this.generateFilesList(document, commit.getFiles());

        paragraph = new Paragraph();
        final Frame frame = new Frame(paragraph);
        frame.setShape(new Rect());
        frame.setMargin(0.0F, 0.0F, 10.0F, 10.0F);

        document.add(frame);
      }
    }
  }

  /**
   * Generates the list of files in the PDF document.
   *
   * @param document the document
   * @param files    the list of changed files
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void generateFilesList(final Document document, final List<ChangedPath> files) throws IOException {
    if (!CollectionUtils.isEmpty(files)) {

      for (final ChangedPath file : files) {
        final Paragraph paragraph = new Paragraph();
        String text = switch (file.getType()) {
          case MODIFIED -> this.util.removeInvalid(file.getPath()) + " {color:#0052cc} Modificado";
          case DELETE -> this.util.removeInvalid(file.getCopyPath()) + " {color:#de350b} Eliminado";
          case ADD -> this.util.removeInvalid(file.getPath()) + " {color:#00875a} Añadido";
          default -> this.util.removeInvalid(file.getPath());
        };
        paragraph.addMarkup(text, 6.0F, BaseFont.Helvetica);
        final Frame line = new Frame(paragraph);
        line.setShape(new Rect());
        line.setMargin(0F, 0.0F, 2.0F, 2.0F);
        document.add(line);
      }
    }
  }

  /**
   * Saves the PDF document to the specified path.
   *
   * @param username the username
   * @param subcode  the subcode
   * @param document the document
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void savePdf(final String username, final String subcode, final Document document) throws IOException {

    final String fileName = EvidenceUtils.format(this.appConfiguration.getSince(), EvidenceUtils.FECHA_ANIO_MES) +
          '_' + username + '_' + subcode + ".pdf";
    final OutputStream outputStream = Files.newOutputStream(this.root.resolve(fileName));
    document.save(outputStream);
    outputStream.close();
    log.info("Filename {} GENERATED!!", fileName);

  }

}
