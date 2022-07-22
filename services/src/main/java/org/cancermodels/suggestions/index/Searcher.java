package org.cancermodels.suggestions.index;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;


public class Searcher {

  @Value( "${lucene_index_dir}" )
  private String luceneIndexDir;

  private IndexSearcher indexSearcher;
  private QueryParser queryParser;
  private Query query;

  public Searcher(String indexDirectoryPath) throws IOException {

  }
}
