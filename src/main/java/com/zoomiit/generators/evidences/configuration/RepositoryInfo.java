/**
 * RepositoryInfo.java 18-sep-2019
 *
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * The Class RepositoryInfo.
 *
 */
@Data
public class RepositoryInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  private String path;

  private String url;

  private String token;

  private List<String> branches = new ArrayList<>();

  @NotNull
  private String code;

  private boolean enabled = Boolean.TRUE;

}
