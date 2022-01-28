/**
 * ChangedPathDto.java 18-sep-2019
 *
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.dtos;

import java.io.Serializable;

import com.zoomiit.generators.evidences.enums.FileType;
import lombok.Data;

/**
 * The Class ChangedPathDto.
 *
 */
@Data
public class ChangedPathDto implements Serializable {

  private static final long serialVersionUID = 1L;

  private FileType type;

  private String path;

  private String copyPath;

  private Long copyRevision;

  private FileDiffDto file = null;

  /**
   * Instancia un nuevo changed path.
   *
   * @param type type
   * @param path path
   * @param copyPath copy path
   * @param copyRevision copy revision
   */
  public ChangedPathDto(final FileType type, final String path, final String copyPath, final long copyRevision) {
    this.type = type;
    this.path = path;
    this.copyPath = copyPath;
    this.copyRevision = copyRevision;
  }

  /**
   * Instancia un nuevo changed path.
   *
   * @param type type
   * @param path path
   * @param copyPath copy path
   */
  public ChangedPathDto(final FileType type, final String path, final String copyPath) {
    this.type = type;
    this.path = path;
    this.copyPath = copyPath;
  }

}
