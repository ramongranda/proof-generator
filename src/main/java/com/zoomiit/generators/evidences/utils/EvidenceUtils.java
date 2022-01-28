/**
 * EvidenceUtils.java 14-sep-2019
 *
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zoomiit.generators.evidences.configuration.AppConfiguration;
import com.zoomiit.generators.evidences.dtos.ChangedPathDto;
import com.zoomiit.generators.evidences.enums.FileType;
import com.zoomiit.generators.evidences.mappers.FileTypeMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.eclipse.jgit.diff.DiffEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class EvidenceUtils.
 *
 */
@Component
public class EvidenceUtils {

  public static final String FECHA_HORA = "dd-MM-yyyy HH:mm";

  public static final String FECHA_MES_ANIO = "MMMM yyyy";

  public static final String FECHA_ANIO_MES = "yyyy-MM";

  @Autowired
  private AppConfiguration appConfiguration;

  @Autowired
  private FileTypeMapper fileTypeMapper;

  private final Map<String, String> replaceCharacter = new HashMap<>();

  /**
   * Instancia un nuevo util.
   */
  public EvidenceUtils() {
    super();
    this.replaceCharacter.put("\t", " ");
    this.replaceCharacter.put("\n", " ");
  }

  /**
   * Elimina el invalid.
   *
   * @param text text
   * @return the string
   */
  public String removeInvalid(final String text) {
    String out = text;
    out = this.removeBlackListWords(out);
    out = this.removeInvalidCharacters(out);
    return out;
  }

  /**
   * Elimina el invalid characters.
   *
   * @param text text
   * @return the string
   */
  public String removeInvalidCharacters(final String text) {
    String out = text;
    for (final Map.Entry<String, String> entry : this.replaceCharacter.entrySet()) {
      if (out != null) {
        out = out.replaceAll(entry.getKey(), entry.getValue());
      }
    }
    String invalid = this.getFirstInvalidCharacter(out);
    while (invalid != null) {

      this.replaceCharacter.put(invalid, "");
      out = out.replaceAll(invalid, "");

      invalid = this.getFirstInvalidCharacter(out);
    }
    return out;
  }

  /**
   * Obtiene first invalid character.
   *
   * @param text text
   * @return first invalid character
   */
  private String getFirstInvalidCharacter(final String text) {
    try {
      if (text != null) {
        PDType1Font.HELVETICA.encode(text);
      }
      return null;
    } catch (final Exception e) {
      final String message = e.getMessage();
      final String invalid = message.substring(0, message.indexOf(' '));

      return "\\u" + invalid.substring(2);
    }
  }

  /**
   * Elimina el black list words.
   *
   * @param text text
   * @return the string
   */
  private String removeBlackListWords(final String text) {
    String out = text;
    if (CollectionUtils.isEmpty(this.appConfiguration.getBlackList())) {
      return out;
    }
    for (final String word : this.appConfiguration.getBlackList()) {
      if ((word != null) && (word.trim().length() > 0)) {
        out = out.replaceAll("(?i)" + word, "**CENSORED**");
      }
    }
    return out;
  }

  /**
   * Obtiene lines.
   *
   * @param stream stream
   * @return lines
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Map<String, List<String>> getLines(final ByteArrayOutputStream stream) throws IOException {
    final Map<String, List<String>> out = new HashMap<>();
    final BufferedReader bufferReader = new BufferedReader(
        new StringReader(stream.toString(StandardCharsets.UTF_8.name())));
    String line;
    String firstLine = null;
    while ((line = bufferReader.readLine()) != null) {
      if (line.contains("diff")) {
        firstLine = this.removeInvalid(line);
        out.put(firstLine, new ArrayList<>());
      }
      out.get(firstLine).add(this.removeInvalid(line));
    }

    return out;
  }

  /**
   * Contains jira codes.
   *
   * @param text text
   * @return true, si termina correctamente
   */
  public boolean evaluateAddCommit(final String text) {

    boolean out = true;

    final List<String> jiraCodes = ListUtils.emptyIfNull(this.appConfiguration.getJiraCodes());

    final List<String> excludeCommits = ListUtils.emptyIfNull(this.appConfiguration.getExcludeCommits());

    if (CollectionUtils.isNotEmpty(jiraCodes)) {
      out = jiraCodes.stream().anyMatch(text::contains);
    }

    return out && excludeCommits.stream().noneMatch(text::contains);
  }

  /**
   * Obtiene files status.
   *
   * @param entries entries
   * @return files status
   */
  public List<ChangedPathDto> recoveryChangedFiles(final List<DiffEntry> entries) {

    final List<ChangedPathDto> files = new ArrayList<>();

    if (CollectionUtils.isEmpty(entries)) {
      return files;
    }

    for (final DiffEntry entry : entries) {
      this.fileTypeMapper.toEnum(entry.getChangeType());
      final FileType type = this.fileTypeMapper.toEnum(entry.getChangeType());
      files.add(new ChangedPathDto(type, entry.getNewPath(), entry.getOldPath()));
    }
    return files;
  }

  /**
   * Format date.
   *
   * @param date date
   * @param pattern pattern
   * @return the string
   */
  public static String format(final Date date, final String pattern) {
    final DateFormat dateFormat = new SimpleDateFormat(pattern);
    return dateFormat.format(date);
  }

  /**
   * Check file not exclude.
   *
   * @param path path
   * @return true, si termina correctamente
   */
  public boolean checkFileExclude(final String path) {

    return this.appConfiguration.getExcludeFiles().stream().parallel().anyMatch(path::contains);

  }

}
