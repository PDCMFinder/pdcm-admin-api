package org.cancermodels.suggestions.index;

public class NonIndexableDocumentException extends RuntimeException {

  public NonIndexableDocumentException() {}

  public NonIndexableDocumentException(String message) {
    super(message);
  }
}
