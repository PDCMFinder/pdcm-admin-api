package org.cancermodels.suggestions.exceptions;

public class QueryException extends RuntimeException {

  public QueryException() {}

  public QueryException(String message) {
    super(message);
  }

  public QueryException(Throwable throwable) {
    super(throwable);
  }
}
