package org.cancermodels.suggestions.index;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LuceneIndexWriter {
  @Value( "${lucene_index_dir}" )
  private String luceneIndexDir;

  private static IndexWriter indexWriter;

  public LuceneIndexWriter() {
  }

  private IndexWriter getIndexWriter() throws IOException {
    if (indexWriter == null) {
      indexWriter = createWriter();
    }
    return indexWriter;
  }

  private IndexWriter createWriter() throws IOException {
    log.info("Creating index at {}", luceneIndexDir);
    FSDirectory dir = FSDirectory.open(Paths.get(luceneIndexDir));
    IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
    return new IndexWriter(dir, config);
  }

  public void writeDocuments(List<Document> documents) throws IOException {
    log.info("Start writing {} documents", documents.size());
    IndexWriter indexWriter = getIndexWriter();

    indexWriter.addDocuments(documents);
    indexWriter.commit();
//    indexWriter.close();
    log.info("Finished writing documents. Index closed.");
  }

  public void deleteDocuments(Query query) throws IOException {
    IndexWriter indexWriter = getIndexWriter();
    indexWriter.deleteDocuments(query);
    indexWriter.commit();
//    indexWriter.close();
  }
}
