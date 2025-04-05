/**
 * RepositoryConfiguration.java 18-sep-2019
 * <p>
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.configuration;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class RepositoryConfiguration.
 * <p>
 * This class holds the configuration for repositories, including SSL verification,
 * enabling status, and a list of repository information.
 */
@Data
public class RepositoryConfiguration implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * Indicates whether SSL verification is enabled.
   */
  private boolean sslVerify = Boolean.FALSE;

  /**
   * Indicates whether the repository configuration is enabled.
   */
  private boolean enabled = Boolean.FALSE;

  /**
   * A list of repository information.
   */
  private List<RepositoryInfo> repositories = new ArrayList<>();

}
