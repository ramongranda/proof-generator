package com.zoomiit.generators.evidences.dtos.tree;

import java.io.Serializable;

import com.zoomiit.generators.evidences.enums.FileType;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FileDto implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  @NonNull
  private String name;

  @NonNull
  private FileType type;

}
