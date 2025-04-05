/**
 * PdfInformation.java 19-sep-2019
 * <p>
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class PdfInformation.
 * <p>
 * This class holds the information for a PDF, including username, date, name, and a list of commits.
 */
@Data
@Builder
public class PdfInformation implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String username;

  private String date;

  private String name;

  @Builder.Default
  private List<Commit> commits = new ArrayList<>();

}
