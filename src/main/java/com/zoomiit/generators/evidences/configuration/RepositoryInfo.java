/**
 * RepositoryInfo.java 18-sep-2019
 * <p>
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.configuration;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class RepositoryInfo.
 * <p>
 * This class holds the information for a repository, including its path, URL, token,
 * branches, code, and enabled status.
 */
@Data
public class RepositoryInfo implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The path of the repository.
   */
  private String path;

  /**
   * The URL of the repository.
   */
  private String url;

  /**
   * The token used for accessing the repository.
   */
  private String token;

  /**
   * A list of branches in the repository.
   */
  private List<String> branches = new ArrayList<>();

  /**
   * The code of the repository.
   */
  @NotNull
  private String code;

  /**
   * Indicates whether the repository is enabled.
   */
  private boolean enabled = Boolean.TRUE;

}
