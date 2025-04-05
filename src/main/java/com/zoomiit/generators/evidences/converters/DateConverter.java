/**
 * DateConverter.java 14-sep-2019
 * <p>
 * Copyright 2019 ZOOMIIT.
 */
package com.zoomiit.generators.evidences.converters;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Class DateConverter.
 * <p>
 * This class converts a String into a Date object using the format "dd/MM/yyyy".
 */
@Component
@ConfigurationPropertiesBinding
public class DateConverter implements Converter<String, Date> {

  /**
   * Converts a String into a Date object.
   *
   * @param source the source string to convert
   * @return the converted Date object, or null if parsing fails
   */
  @Override
  public Date convert(final String source) {
    final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    try {
      return dateFormat.parse(source);
    } catch (final ParseException e) {
      return null;
    }
  }
}
