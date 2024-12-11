package org.cancermodels.suggestions.search_engine;

import java.io.IOException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.queries.spans.SpanNearQuery;
import org.apache.lucene.queries.spans.SpanQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.util.QueryBuilder.TermAndBoost;
import org.cancermodels.suggestions.FieldsNames;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IndexableSuggestionRepository {

  private LuceneIndexReader reader;

  public IndexableSuggestionRepository(LuceneIndexReader reader) {
    this.reader = reader;
  }

  public void processResults(TopDocs topDocs) throws IOException {
    System.out.println();
    System.out.println("Showing results....");
    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
      Document doc = reader.getDocument(scoreDoc);
      String id = doc.get(FieldsNames.ID.getName());
      String label = doc.get(FieldsNames.ONTOLOGY_LABEL.getName());
      String definition = doc.get(FieldsNames.ONTOLOGY_DEFINITION.getName());
      var synonyms = doc.getValues(FieldsNames.ONTOLOGY_SYNONYM.getName());
      System.out.println("id: " + id);
      System.out.println("label: " + label);
      System.out.println("definition: " + definition);
      System.out.println("synonyms: " + Arrays.toString(synonyms));
      System.out.println("score:" + scoreDoc.score);
      System.out.println();
      }
  }
}
