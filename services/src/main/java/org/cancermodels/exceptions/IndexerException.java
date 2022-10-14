package org.cancermodels.exceptions;

/**
 * Clas to encapsulate exceptions regarding the indexer process in lucene.
 */
public class IndexerException extends RuntimeException {

  public IndexerException() {}

  public IndexerException(String message) {
    super(message);
  }

  public IndexerException(Throwable throwable) {
    super(throwable);
  }
}
