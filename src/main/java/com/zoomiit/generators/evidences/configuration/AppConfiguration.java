/**
 * GitConfiguration.java 14-sep-2019
 *
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.configuration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

/**
 * The Class AppConfiguration.
 *
 */
@Data
@Component
@ConfigurationProperties("app.scanner")
public class AppConfiguration {

  @NotNull
  @DateTimeFormat(pattern = "dd/MM/yyyy")
  private Date since;

  @NotNull
  @DateTimeFormat(pattern = "dd/MM/yyyy")
  private Date until;

  @NotNull
  private String committer;

  @NotNull
  private String pon;

  private List<String> blackList = new ArrayList<>();

  private List<String> jiraCodes = new ArrayList<>();

  private List<String> excludeCommits = new ArrayList<>();

  private List<String> excludeFiles = new ArrayList<>();

  private RepositoryConfiguration git = new RepositoryConfiguration();

  private RepositoryConfiguration svn = new RepositoryConfiguration();

  private RepositoryConfiguration local = new RepositoryConfiguration();

}
