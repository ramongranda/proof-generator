/**
 * Commit.java 18-sep-2019
 * <p>
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.model;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The Class Commit.
 * <p>
 * This class represents a commit with its revision, author, date, messages, changes, and files.
 */
@Data
@RequiredArgsConstructor
public class Commit implements Serializable {

  @Serial
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

  private List<ChangedPath> files = new ArrayList<>();

  /**
   * Adds a changed path to the list of files.
   *
   * @param c the changed path to add
   */
  public void addChangedPath(ChangedPath c) {
    this.files.add(c);
  }

}
