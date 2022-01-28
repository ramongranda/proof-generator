/**
 * dffas.java 14-sep-2019
 *
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.converters;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * The Class LocalDateConverter.
 *
 */
@Component
@ConfigurationPropertiesBinding
public class DateConverter implements Converter<String, Date> {

  /**
   * {@inheritDoc}
   */
  @Override
  public Date convert(final String source) {
    if (source == null) {
      return null;
    }
    final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    try {
      return dateFormat.parse(source);
    } catch (final ParseException e) {
      return null;
    }
  }
}
