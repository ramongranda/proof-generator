/**
 * FileType.java
 * <p>
 * Copyright 2025 ZOOMIIT.
 * <p>
 * This file is part of the Proof Generator project.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package com.zoomiit.generators.evidences.enums;

/**
 * The Enum FileType.
 * Represents the types of file operations.
 */
public enum FileType {
  /**
   * File added.
   */
  ADD,

  /**
   * File deleted.
   */
  DELETE,

  /**
   * File modified.
   */
  MODIFIED,

  /**
   * File replaced.
   */
  REPLACED,

  /**
   * No operation.
   */
  NONE;

  /**
   * Gets the FileType based on a character.
   *
   * @param type the character representing the file type
   * @return the corresponding FileType
   */
  public static FileType getType(final char type) {
    return switch (type) {
      case 'A' -> FileType.ADD;
      case 'M' -> FileType.MODIFIED;
      case 'D' -> FileType.DELETE;
      case 'R' -> FileType.REPLACED;
      default -> FileType.NONE;
    };
  }
}
