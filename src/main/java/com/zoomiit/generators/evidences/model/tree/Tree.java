package com.zoomiit.generators.evidences.model.tree;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.HashMap;
import java.util.List;

/**
 * The Class Tree.
 * <p>
 * This class represents a tree structure with a path and child nodes.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Tree extends HashMap<String, List<FileTree>> {

  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * The path of the tree.
   */
  private String path;

  /**
   * The child nodes of the tree.
   */
  private Tree childs;

}
