/**
 * FileTypeMapper.java 20-sep-2019
 *
 * Copyright 2019 INDITEX. Departamento de Sistemas
 */
package com.zoomiit.generators.evidences.mappers;

import com.zoomiit.generators.evidences.enums.FileType;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;

@Mapper
public interface FileTypeMapper {

  @ValueMapping(source = "ADD", target = "ADD")
  @ValueMapping(source = "MODIFY", target = "MODIFIED")
  @ValueMapping(source = "DELETE", target = "DELETE")
  @ValueMapping(source = "RENAME", target = "REMPLACED")
  @ValueMapping(source = "COPY", target = "ADD")
  FileType toEnum(ChangeType changeType);

}
