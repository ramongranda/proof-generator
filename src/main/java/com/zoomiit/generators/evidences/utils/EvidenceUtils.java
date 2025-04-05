/**
 * EvidenceUtils.java 14-sep-2019
 * <p>
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.utils;

import com.zoomiit.generators.evidences.configuration.AppConfiguration;
import com.zoomiit.generators.evidences.enums.FileType;
import com.zoomiit.generators.evidences.mappers.FileTypeMapper;
import com.zoomiit.generators.evidences.model.ChangedPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.eclipse.jgit.diff.DiffEntry;
import org.springframework.stereotype.Component;

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

/**
 * The Class EvidenceUtils.
 */
@Component
public class EvidenceUtils {

  public static final String FECHA_HORA = "dd-MM-yyyy HH:mm";

  public static final String FECHA_MES_ANIO = "MMMM yyyy";

  public static final String FECHA_ANIO_MES = "yyyy-MM";

  private final AppConfiguration appConfiguration;

  private final FileTypeMapper fileTypeMapper;

  private final Map<String, String> replaceCharacter = new HashMap<>();

  /**
   * Instantiates a new EvidenceUtils.
   *
   * @param appConfiguration the application configuration
   * @param fileTypeMapper   the file type mapper
   */
  public EvidenceUtils(final AppConfiguration appConfiguration, final FileTypeMapper fileTypeMapper) {
    super();
    this.appConfiguration = appConfiguration;
    this.fileTypeMapper = fileTypeMapper;
    this.replaceCharacter.put("\t", " ");
    this.replaceCharacter.put("\n", " ");
  }

  /**
   * Formats a date to a string with the given pattern.
   *
   * @param date    the date
   * @param pattern the pattern
   * @return the formatted string
   */
  public static String format(final Date date, final String pattern) {
    final DateFormat dateFormat = new SimpleDateFormat(pattern);
    return dateFormat.format(date);
  }

  /**
   * Removes invalid characters and blacklisted words from the text.
   *
   * @param text the text
   * @return the cleaned string
   */
  public String removeInvalid(final String text) {
    String out = text;
    out = this.removeBlackListWords(out);
    out = this.removeInvalidCharacters(out);
    return out;
  }

  /**
   * Removes invalid characters from the text.
   *
   * @param text the text
   * @return the cleaned string
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
      assert out != null;
      out = out.replaceAll(invalid, "");

      invalid = this.getFirstInvalidCharacter(out);
    }
    return out;
  }

  /**
   * Gets the first invalid character in the text.
   *
   * @param text the text
   * @return the first invalid character
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
   * Removes blacklisted words from the text.
   *
   * @param text the text
   * @return the cleaned string
   */
  private String removeBlackListWords(final String text) {
    String out = text;
    if (CollectionUtils.isEmpty(this.appConfiguration.getBlackList())) {
      return out;
    }
    for (final String word : this.appConfiguration.getBlackList()) {
      if ((word != null) && (!word.trim().isEmpty())) {
        out = out.replaceAll("(?i)" + word, "**CENSORED**");
      }
    }
    return out;
  }

  /**
   * Gets lines from the ByteArrayOutputStream.
   *
   * @param stream the stream
   * @return the lines
   * @throws IOException if an I/O exception occurs
   */
  public Map<String, List<String>> getLines(final ByteArrayOutputStream stream) throws IOException {
    final Map<String, List<String>> out = new HashMap<>();
    final BufferedReader bufferReader = new BufferedReader(
          new StringReader(stream.toString(StandardCharsets.UTF_8)));
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
   * Evaluates if a commit should be added based on its message.
   *
   * @param text the commit message
   * @return true if the commit should be added, false otherwise
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
   * Recovers changed files from the list of DiffEntry.
   *
   * @param entries the entries
   * @return the list of changed path DTOs
   */
  public List<ChangedPath> recoveryChangedFiles(final List<DiffEntry> entries) {

    final List<ChangedPath> files = new ArrayList<>();

    if (CollectionUtils.isEmpty(entries)) {
      return files;
    }

    for (final DiffEntry entry : entries) {
      this.fileTypeMapper.toEnum(entry.getChangeType());
      final FileType type = this.fileTypeMapper.toEnum(entry.getChangeType());
      files.add(new ChangedPath(type, entry.getNewPath(), entry.getOldPath()));
    }
    return files;
  }

  /**
   * Checks if a file path should be excluded.
   *
   * @param path the file path
   * @return true if the file should be excluded, false otherwise
   */
  public boolean checkFileExclude(final String path) {

    return this.appConfiguration.getExcludeFiles().stream().parallel().anyMatch(path::contains);

  }

}
