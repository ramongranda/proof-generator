/**
 * PdfGenerator.java 23 ene 2022
 *
 * Copyright 2022 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.pdf;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import com.zoomiit.generators.evidences.configuration.AppConfiguration;
import com.zoomiit.generators.evidences.dtos.ChangedPathDto;
import com.zoomiit.generators.evidences.dtos.CommitDto;
import com.zoomiit.generators.evidences.dtos.PdfInformationDto;
import com.zoomiit.generators.evidences.utils.EvidenceUtils;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rst.pdfbox.layout.elements.ControlElement;
import rst.pdfbox.layout.elements.Document;
import rst.pdfbox.layout.elements.Frame;
import rst.pdfbox.layout.elements.Paragraph;
import rst.pdfbox.layout.shape.Rect;
import rst.pdfbox.layout.shape.Stroke;
import rst.pdfbox.layout.text.BaseFont;

/**
 * The Class PdfGenerator.
 *
 */
@Data
@Component
public class PdfGenerator {

  private static final Logger LOG = LoggerFactory.getLogger(PdfGenerator.class);

  @Autowired
  private EvidenceUtils util;

  @Autowired
  private AppConfiguration appConfiguration;

  private Path root;

  /**
   * Generate git report.
   *
   * @param pdfInformationDto pdf information dto
   * @param file file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void generateReport(final PdfInformationDto pdfInformationDto, final File file) {

    if (Objects.nonNull(pdfInformationDto) && CollectionUtils.isNotEmpty(pdfInformationDto.getCommits())) {

      this.initialize(file);

      pdfInformationDto.getCommits().removeIf(item -> CollectionUtils.isEmpty(item.getFiles()));
      try {
        final Document document = this.generateHead(pdfInformationDto);

        Paragraph subtitle = new Paragraph();
        subtitle.addText("Índice " + pdfInformationDto.getName(), 16.0F, PDType1Font.HELVETICA_BOLD);

        Frame subtitleFrame = new Frame(subtitle);
        subtitleFrame.setShape(new Rect());
        subtitleFrame.setMargin(0.0F, 0.0F, 5.0F, 15.0F);

        document.add(subtitleFrame);
        this.generateIndex(document, pdfInformationDto, pdfInformationDto);
        document.add(ControlElement.NEWPAGE);
        subtitle = new Paragraph();
        subtitle.addText("Detalles " + pdfInformationDto.getName(), 16.0F, PDType1Font.HELVETICA_BOLD);

        subtitleFrame = new Frame(subtitle);
        subtitleFrame.setShape(new Rect());
        subtitleFrame.setMargin(0.0F, 0.0F, 0.0F, 5.0F);

        document.add(subtitleFrame);
        this.generateDetail(document, pdfInformationDto, pdfInformationDto);
        this.savePdf(pdfInformationDto.getUsername(), pdfInformationDto.getName(), document);
      } catch (final Exception excep) {
        LOG.error("[PdfGenerator:generateReport] -> Error generating the report {} ", pdfInformationDto, excep);
      }
    }

  }

  /**
   * Initialize.
   *
   * @param file file
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
   * Generatehead.
   *
   * @param pdfInformationDto pdf information
   * @return the document
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private Document generateHead(final PdfInformationDto pdfInformationDto) throws IOException {
    final Document document = new Document(20.0F, 20.0F, 20.0F, 60.0F);

    final Paragraph title = new Paragraph();
    title.addText(
        "Commits de "
            + this.appConfiguration.getCommitter() + " (" + pdfInformationDto.getUsername() + ") en el proyecto "
            + this.appConfiguration.getPon() + ": " + pdfInformationDto.getDate(),
        18.0F, PDType1Font.HELVETICA_BOLD);

    final Frame titleFrame = new Frame(title);
    titleFrame.setShape(new Rect());
    titleFrame.setMargin(0.0F, 0.0F, 0.0F, 10.0F);

    document.add(titleFrame);
    return document;
  }

  /**
   * Generate detail.
   *
   * @param document document
   * @param proyect proyect
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void generateDetail(final Document document, final PdfInformationDto proyect,
      final PdfInformationDto pdfInformationDto)
      throws IOException {

    for (final CommitDto commit : proyect.getCommits()) {

      if (this.util.evaluateAddCommit(commit.getShortMessage())) {

        final Frame divider = new Frame(new Paragraph(), Float.valueOf(515.0F), Float.valueOf(1.0F));
        divider.setShape(new Rect());
        divider.setBorder(Color.LIGHT_GRAY, new Stroke(1.0F));
        divider.setMargin(0.0F, 0.0F, 15.0F, 15.0F);

        document.add(divider);

        Paragraph paragraph = new Paragraph();
        paragraph.addMarkup("{color:#0052cc}"
            + this.appConfiguration.getCommitter()
            + "{color:#172b4d} committed {color:#0052cc}" + commit.getRevision() + "(" + pdfInformationDto.getUsername()
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
   * Adds commits.
   *
   * @param document document
   * @param commit commit
   * @param frame frame
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void addCommits(final Document document, final CommitDto commit, final Frame frame) throws IOException {
    Paragraph paragraph;
    for (final String diff : commit.getChanges().keySet()) {

      for (final String line : commit.getChanges().get(diff)) {
        paragraph = new Paragraph();
        paragraph.setLineSpacing(1F);
        paragraph.addText(this.util.removeInvalidCharacters(line), 8.0F, PDType1Font.HELVETICA);

        final Frame changesFrame = new Frame(paragraph, Float.valueOf(515.0F), null);
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
   * Generate index.
   *
   * @param document document
   * @param proyect proyect
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void generateIndex(final Document document, final PdfInformationDto proyect,
      final PdfInformationDto pdfInformationDto)
      throws IOException {
    for (final CommitDto commit : proyect.getCommits()) {

      if (this.util.evaluateAddCommit(commit.getShortMessage())) {

        Paragraph paragraph = new Paragraph();
        paragraph.addMarkup("{color:#0052cc}"
            + this.appConfiguration.getCommitter()
            + "{color:#172b4d} committed {color:#0052cc}" + commit.getRevision() + " (" + pdfInformationDto.getUsername()
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
   * Generate files list.
   *
   * @param document document
   * @param files files
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void generateFilesList(final Document document, final List<ChangedPathDto> files) throws IOException {
    if (!CollectionUtils.isEmpty(files)) {

      for (final ChangedPathDto file : files) {
        final Paragraph paragraph = new Paragraph();
        String text = "";
        switch (file.getType()) {
          case MODIFIED:
            text = this.util.removeInvalid(file.getPath()) + " {color:#0052cc} Modificado";
            break;
          case DELETE:
            text = this.util.removeInvalid(file.getCopyPath()) + " {color:#de350b} Eliminado";
            break;
          case ADD:
            text = this.util.removeInvalid(file.getPath()) + " {color:#00875a} Añadido";
            break;
          default:
            text = this.util.removeInvalid(file.getPath());
        }
        paragraph.addMarkup(text, 6.0F, BaseFont.Helvetica);
        final Frame line = new Frame(paragraph);
        line.setShape(new Rect());
        line.setMargin(0F, 0.0F, 2.0F, 2.0F);
        document.add(line);
      }
    }
  }

  /**
   * Save pdf.
   *
   * @param username username
   * @param subcode subcode
   * @param document document
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void savePdf(final String username, final String subcode, final Document document) throws IOException {

    final String fileName = new StringBuilder(EvidenceUtils.format(this.appConfiguration.getSince(), EvidenceUtils.FECHA_ANIO_MES))
        .append('_').append(username).append('_').append(subcode).append(".pdf").toString();
    final OutputStream outputStream = Files.newOutputStream(this.root.resolve(fileName));
    document.save(outputStream);
    outputStream.close();
    LOG.info("Filename {} GENERATED!!", fileName);

  }

}
