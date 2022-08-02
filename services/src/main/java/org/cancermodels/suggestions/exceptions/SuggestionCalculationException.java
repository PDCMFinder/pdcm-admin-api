package org.cancermodels.suggestions.exceptions;

public class SuggestionCalculationException extends RuntimeException {

  public SuggestionCalculationException() {}

  public SuggestionCalculationException(String message) {
    super(message);
  }

  public SuggestionCalculationException(Throwable throwable) {
    super(throwable);
  }
}
