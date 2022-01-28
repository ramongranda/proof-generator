package com.zoomiit.generators.evidences.dtos.tree;

import java.util.HashMap;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The Class Tree.
 *
 * @param <V> the value type
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Tree extends HashMap<String, List<FileDto>> {

  private static final long serialVersionUID = 1L;

  private String path;

  private Tree childs;

}
