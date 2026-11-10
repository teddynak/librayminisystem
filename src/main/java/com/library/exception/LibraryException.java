package com.library.exception;

/** A recoverable rule or storage failure suitable for display to the user. */
public class LibraryException extends RuntimeException {
  public LibraryException(String message) {
    super(message);
  }

  public LibraryException(String message, Throwable cause) {
    super(message, cause);
  }
}
