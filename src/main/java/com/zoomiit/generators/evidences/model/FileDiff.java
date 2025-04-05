/**
 * FileDiff.java 18-sep-2019
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
import java.util.List;

/**
 * The Class FileDiff.
 * <p>
 * This class holds the differences for a file, including its path and a list of changed lines.
 */
@Data
@RequiredArgsConstructor
public class FileDiff implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @NonNull
  private String path;

  private List<String> lines = new ArrayList<>();

  /**
   * Adds a line to the list of changed lines.
   *
   * @param line the line to add
   */
  public void addLine(final String line) {
    this.lines.add(line);
  }

}
