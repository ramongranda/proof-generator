/**
 * ChangedPath.java 18-sep-2019
 * <p>
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.model;

import com.zoomiit.generators.evidences.enums.FileType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * The Class ChangedPath.
 * <p>
 * This class represents a changed path with its type, path, copy path, copy revision, and file diff.
 */
@Data
public class ChangedPath implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private FileType type;

  private String path;

  private String copyPath;

  private Long copyRevision;

  private FileDiff file = null;

  /**
   * Instantiates a new changed path.
   *
   * @param type         the type of the file
   * @param path         the path of the file
   * @param copyPath     the copy path of the file
   * @param copyRevision the copy revision of the file
   */
  public ChangedPath(final FileType type, final String path, final String copyPath, final long copyRevision) {
    this.type = type;
    this.path = path;
    this.copyPath = copyPath;
    this.copyRevision = copyRevision;
  }

  /**
   * Instantiates a new changed path.
   *
   * @param type     the type of the file
   * @param path     the path of the file
   * @param copyPath the copy path of the file
   */
  public ChangedPath(final FileType type, final String path, final String copyPath) {
    this.type = type;
    this.path = path;
    this.copyPath = copyPath;
  }

}
