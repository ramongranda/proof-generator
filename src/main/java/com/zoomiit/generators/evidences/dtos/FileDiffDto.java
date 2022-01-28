/**
 * FileDiffDto.java 18-sep-2019
 *
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The Class FileDiffDto.
 *
 */
@Data
@RequiredArgsConstructor
public class FileDiffDto implements Serializable {

  private static final long serialVersionUID = 1L;

  @NonNull
  private String path;

  private List<String> lines = new ArrayList<>();

  /**
   * Anade el line.
   *
   * @param line line
   */
  public void addLine(final String line) {

    this.lines.add(line);

  }

}
