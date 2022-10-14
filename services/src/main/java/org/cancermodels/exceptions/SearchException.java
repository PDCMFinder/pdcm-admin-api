package org.cancermodels.exceptions;

public class SearchException extends RuntimeException {

  public SearchException() {}

  public SearchException(String message) {
    super(message);
  }

  public SearchException(Throwable throwable) {
    super(throwable);
  }
}
