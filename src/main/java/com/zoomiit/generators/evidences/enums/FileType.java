/**
 * FileType.java 17-sep-2019
 *
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.enums;

/**
 * The Enum FileType.
 *
 */
public enum FileType {
  ADD, DELETE, MODIFIED, REMPLACED, NONE;

  /**
   * Obtiene type.
   *
   * @param type type
   * @return type
   */
  public static FileType getType(final char type) {
    FileType out = null;
    switch (type) {
      case 'A':
        out = FileType.ADD;
        break;
      case 'M':
        out = FileType.MODIFIED;
        break;
      case 'D':
        out = FileType.DELETE;
        break;
      case 'R':
        out = FileType.REMPLACED;
        break;
      default:
        out = FileType.NONE;
    }
    return out;
  }
}
