/**
 * PdfInformationDto.java 19-sep-2019
 *
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * The Class PdfInformationDto.
 *
 */
@Data
@Builder
public class PdfInformationDto implements Serializable {

  private static final long serialVersionUID = 1L;

  private String username;

  private String date;

  private String name;

  @Builder.Default
  private List<CommitDto> commits = new ArrayList<>();

}
