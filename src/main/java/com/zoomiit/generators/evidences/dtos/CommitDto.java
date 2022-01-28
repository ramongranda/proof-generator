/**
 * GitCommit.java 18-sep-2019
 *
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The Class GitCommit.
 *
 */
@Data
@RequiredArgsConstructor
public class CommitDto implements Serializable {

  private static final long serialVersionUID = 1L;

  @NonNull
  private String revision;

  @NonNull
  private String author;

  @NonNull
  private Date date;

  @NonNull
  private String shortMessage;

  @NonNull
  private String fullMessage;

  private Map<String, List<String>> changes = null;

  private List<ChangedPathDto> files = new ArrayList<>();

  /**
   * Anade el changed path.
   *
   * @param c c
   */
  public void addChangedPath(ChangedPathDto c) {
    this.files.add(c);
  }

}
