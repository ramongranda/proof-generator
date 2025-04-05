package com.zoomiit.generators.evidences.model.tree;

import com.zoomiit.generators.evidences.enums.FileType;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * The Class FileTree.
 * <p>
 * This class represents a file with its name and type.
 */
@Data
@RequiredArgsConstructor
public class FileTree implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The name of the file.
   */
  @NonNull
  private String name;

  /**
   * The type of the file.
   */
  @NonNull
  private FileType type;

}
