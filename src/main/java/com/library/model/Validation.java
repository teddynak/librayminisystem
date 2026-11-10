package com.library.model;

import com.library.exception.LibraryException;

public final class Validation {
  private Validation() {}

  public static String required(String value, String field) {
    if (value == null || value.isBlank()) throw new LibraryException(field + " is required.");
    if (value.trim().length() > 200)
      throw new LibraryException(field + " must be at most 200 characters.");
    return value.trim();
  }

  public static long id(long value) {
    if (value <= 0) throw new LibraryException("Select a valid record.");
    return value;
  }
}
