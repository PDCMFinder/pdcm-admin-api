package org.cancermodels.suggestions.exceptions;

public class NonIndexableDocumentException extends RuntimeException {

  public NonIndexableDocumentException() {}

  public NonIndexableDocumentException(String message) {
    super(message);
  }
}
