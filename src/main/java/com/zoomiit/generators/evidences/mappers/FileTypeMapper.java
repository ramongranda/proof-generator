/**
 * FileTypeMapper.java 20-sep-2019
 * <p>
 * Copyright 2019 ZOOMIIT. Departamento de Sistemas
 */
package com.zoomiit.generators.evidences.mappers;

import com.zoomiit.generators.evidences.enums.FileType;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;

/**
 * Mapper interface for converting ChangeType to FileType.
 */
@Mapper
public interface FileTypeMapper {

  /**
   * Converts a ChangeType to a corresponding FileType.
   *
   * @param changeType the ChangeType to convert
   * @return the corresponding FileType
   */
  @ValueMapping(source = "ADD", target = "ADD")
  @ValueMapping(source = "MODIFY", target = "MODIFIED")
  @ValueMapping(source = "DELETE", target = "DELETE")
  @ValueMapping(source = "RENAME", target = "REPLACED")
  @ValueMapping(source = "COPY", target = "ADD")
  FileType toEnum(ChangeType changeType);

}
