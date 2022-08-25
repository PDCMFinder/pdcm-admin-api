package org.cancermodels.suggestions.indexers;

import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * Indexes the documents needed to have mappings suggestions.
 */
@Component
public class Indexer {

  private final OntologiesIndexer ontologiesIndexer;
  private final RulesIndexer rulesIndexer;
  private final HelperDocumentsIndexer helperDocumentsIndexer;

  public Indexer(
      OntologiesIndexer ontologiesIndexer,
      RulesIndexer rulesIndexer,
      HelperDocumentsIndexer helperDocumentsIndexer) {
    this.ontologiesIndexer = ontologiesIndexer;
    this.rulesIndexer = rulesIndexer;
    this.helperDocumentsIndexer = helperDocumentsIndexer;
  }

  public void indexRules() throws IOException {
    rulesIndexer.index();
  }

  public void indexOntologies() throws IOException {
    ontologiesIndexer.index();
  }

  public void index() throws IOException {
    rulesIndexer.index();
    ontologiesIndexer.index();
  }

  public void indexHelperDocuments() throws IOException {
    helperDocumentsIndexer.index();
  }

}
