package org.cancermodels.suggestions.index;

import java.io.IOException;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
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

  private static IndexSearcher indexSearcher;

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
    TopDocs hits = getIndexSearcher().search(query, 10);
    log.info(hits.scoreDocs.length + " hits");
    return hits;
  }

  public Query createQueryByLabel(String label) throws ParseException {
    QueryParser qp = new QueryParser("ontologyTermLabel", new EnglishAnalyzer());
    Query firstNameQuery = qp.parse(label);
    return firstNameQuery;
  }

  private static TopDocs searchByFirstName(String firstName, IndexSearcher searcher) throws Exception
  {
    QueryParser qp = new QueryParser("firstName", new EnglishAnalyzer());
    Query firstNameQuery = qp.parse(firstName);
    TopDocs hits = searcher.search(firstNameQuery, 10);
    return hits;
  }

  public TopDocs search(String field, String queryString)
      throws IOException, ParseException {
    log.info("Search with field [using QueryParser]: {} and queryString {}" , field, queryString);

    Query query = new QueryParser(field, new EnglishAnalyzer())
        .parse(queryString);
    log.info("Compiled query {}", query.toString());
    return getIndexSearcher().search(query, 10);
  }

  public TopDocs search(Query query) throws IOException {
    log.info("Search with query: " + query.toString());
    return getIndexSearcher().search(query, 10);
  }

  public Document getDocument(ScoreDoc scoreDoc) throws IOException {
    return getIndexSearcher().doc(scoreDoc.doc);
  }
}
