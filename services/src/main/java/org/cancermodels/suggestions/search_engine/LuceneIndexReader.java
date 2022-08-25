package org.cancermodels.suggestions.search_engine;

import java.io.IOException;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LuceneIndexReader {

  @Value( "${lucene_index_dir}" )
  private String luceneIndexDir;

  @Value("${number_of_suggested_mappings}")
  private int numberOfSuggestedMappings;

  private static IndexSearcher indexSearcher;

  private final AnalyzerProvider analyzerProvider;

  public LuceneIndexReader(AnalyzerProvider analyzerProvider) {
    this.analyzerProvider = analyzerProvider;
  }

  public IndexSearcher getIndexSearcher() throws IOException {
    if (indexSearcher == null) {
      indexSearcher = createSearcher();
    }
    return indexSearcher;
  }

  private IndexSearcher createSearcher() throws IOException {
    Directory dir = FSDirectory.open(Paths.get(luceneIndexDir));
    IndexReader reader = DirectoryReader.open(dir);
    IndexSearcher searcher = new IndexSearcher(reader);
    return searcher;
  }

  public TopDocs executeQuery(Query query) throws IOException {
    log.info("Executing query: [" + query.toString() + "]");
    TopDocs hits = getIndexSearcher().search(query, numberOfSuggestedMappings);
    log.info(hits.scoreDocs.length + " hits");
    return hits;
  }

  public Query createQueryByLabel(String label) throws ParseException {
    QueryParser qp = new QueryParser("ontologyTermLabel", analyzerProvider.getAnalyzer());
    Query firstNameQuery = qp.parse(label);
    return firstNameQuery;
  }

  public TopDocs search(String field, String queryString)
      throws IOException, ParseException {
//    log.info("Search with field [using QueryParser]: {} and queryString {}" , field, queryString);

    Query query = new QueryParser(field, analyzerProvider.getAnalyzer())
        .parse(queryString);
    log.info("Compiled query {}", query.toString());
    return trySearch(query, numberOfSuggestedMappings);
  }

  public TopDocs search(Query query) throws IOException {
    log.info("Search with query: {\n" + query.toString() + "\n}");
    return trySearch(query, numberOfSuggestedMappings);
  }

  private TopDocs trySearch(Query query, int maxHits) {
    boolean retry = true;
    while (retry)
    {
      try
      {
        retry = false;
        return getIndexSearcher().search(query, maxHits);
      }
      catch (IndexSearcher.TooManyClauses | IOException e)
      {
        // The default is 1024.
        int maxCount = IndexSearcher.getMaxClauseCount();
        int newCount = maxCount + 500;
        log.error("Too many hits for query: " + maxCount + ".  Increasing to " + newCount);
        log.error(query.toString());
        IndexSearcher.setMaxClauseCount(newCount);
        retry = true;
      }
    }
    return null;
  }

  public Document getDocument(ScoreDoc scoreDoc) throws IOException {
    return getIndexSearcher().doc(scoreDoc.doc);
  }
}
