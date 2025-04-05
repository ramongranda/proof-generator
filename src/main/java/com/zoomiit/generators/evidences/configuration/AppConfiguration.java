/**
 * AppConfiguration.java
 * <p>
 * Copyright 2025 ZOOMIIT.
 * <p>
 * This file is part of the Proof Generator project.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package com.zoomiit.generators.evidences.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Class AppConfiguration.
 * <p>
 * This class holds the application configuration properties for the scanner.
 */
@Data
@Component
@ConfigurationProperties("app.scanner")
public class AppConfiguration {

  /**
   * The start date for scanning.
   */
  @NotNull
  @DateTimeFormat(pattern = "dd/MM/yyyy")
  private Date since;

  /**
   * The end date for scanning.
   */
  @NotNull
  @DateTimeFormat(pattern = "dd/MM/yyyy")
  private Date until;

  /**
   * The committer's name.
   */
  @NotNull
  private String committer;

  /**
   * The PON (Project Order Number).
   */
  @NotNull
  private String pon;

  /**
   * A list of items to be blacklisted.
   */
  private List<String> blackList = new ArrayList<>();

  /**
   * A list of JIRA codes.
   */
  private List<String> jiraCodes = new ArrayList<>();

  /**
   * A list of commits to be excluded.
   */
  private List<String> excludeCommits = new ArrayList<>();

  /**
   * A list of files to be excluded.
   */
  private List<String> excludeFiles = new ArrayList<>();

  /**
   * The configuration for Git repositories.
   */
  private RepositoryConfiguration git = new RepositoryConfiguration();

  /**
   * The configuration for SVN repositories.
   */
  private RepositoryConfiguration svn = new RepositoryConfiguration();

  /**
   * The configuration for local repositories.
   */
  private RepositoryConfiguration local = new RepositoryConfiguration();

}
