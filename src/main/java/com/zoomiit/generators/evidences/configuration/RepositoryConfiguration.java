/**
 * RepositoryConfiguration.java 18-sep-2019
 *
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * The Class RepositoryConfiguration.
 *
 */
@Data
public class RepositoryConfiguration implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean sslVerify = Boolean.FALSE;

  private boolean enabled = Boolean.FALSE;

  private List<RepositoryInfo> repositories = new ArrayList<>();

}
