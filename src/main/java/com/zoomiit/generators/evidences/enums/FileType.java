/**
 * FileType.java 17 Sep 2019
 * <p>
 * Copyright 2019 ZOOMIIT.
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
