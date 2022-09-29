package org.cancermodels.input_data.exceptions;

public class InputFileDownloadException extends RuntimeException {

  public InputFileDownloadException() {}

  public InputFileDownloadException(String message) {
    super(message);
  }

  public InputFileDownloadException(Throwable throwable) {
    super(throwable);
  }

}
