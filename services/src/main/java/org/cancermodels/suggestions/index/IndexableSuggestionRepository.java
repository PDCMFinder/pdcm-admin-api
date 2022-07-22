package org.cancermodels.suggestions.index;

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

  public void testFuzzySearch() throws IOException, ParseException {
    String stringToSearch = "kanser the bajina";
//    stringToSearch = ":vaginal id:cancer id:nos~2";
    Term term = new Term("ontology.synonym", stringToSearch);
    Query query = new FuzzyQuery(term);
    TopDocs topDocs = reader.search(query);
    ScoreDoc[] hits = topDocs.scoreDocs;
    log.info("@@@Number of hits: " + hits.length);

    for (ScoreDoc scoreDoc : hits) {
      Document doc = reader.getDocument(scoreDoc);
      System.out.println(doc.toString());
    }

    processResults(reader.search("ontology.synonym", stringToSearch));

//Recombinant Amphiregulin
    SpanQuery[] labelClauses = new SpanQuery[2];
    labelClauses[0] = new SpanMultiTermQueryWrapper<>(new FuzzyQuery(new Term("ontology.label", "recombinant")));
    labelClauses[1] = new SpanMultiTermQueryWrapper<>(new FuzzyQuery(new Term("ontology.label", "amphiregulin")));
    SpanNearQuery labelQuery = new SpanNearQuery(labelClauses, 0, true);

    FuzzyQuery f = new FuzzyQuery(new Term("ontology.synonym", "vaxinal"));
    BoostQuery b = new BoostQuery(f, 1.2f);
    QueryBuilder.TermAndBoost tb = new TermAndBoost(new Term("ontology.label", "recombinant"), 1.2f);

    System.out.println("boosted " + b);
    SpanQuery[] synonymClauses = new SpanQuery[3];
    synonymClauses[0] = new SpanMultiTermQueryWrapper(f);
    synonymClauses[1] = new SpanMultiTermQueryWrapper<>(new FuzzyQuery(new Term("ontology.synonym", "kancer")));
    synonymClauses[2] = new SpanMultiTermQueryWrapper<>(new FuzzyQuery(new Term("ontology.synonym", "nos")));
    SpanNearQuery synonymsQuery = new SpanNearQuery(synonymClauses, 0, true);

    BooleanQuery.Builder builder = new Builder();
    builder.add(labelQuery, Occur.SHOULD);
    builder.add(synonymsQuery, BooleanClause.Occur.SHOULD);


    System.out.println("trying boolean builder");
    processResults(reader.search(builder.build()));

    FuzzyQuery f1 = new FuzzyQuery(new Term("ontology.synonym", "vaxinal"));
    FuzzyQuery f2 = new FuzzyQuery(new Term("ontology.synonym", "kancer"));
    FuzzyQuery f3 = new FuzzyQuery(new Term("ontology.synonym", "nos"));

    BoostQuery b1 = new BoostQuery(f1, 1.2f);
    BoostQuery b2 = new BoostQuery(f2, 1.2f);
    BoostQuery b3 = new BoostQuery(f3, 1.2f);

    BooleanQuery.Builder builder2 = new Builder();
    builder2.add(b1, Occur.SHOULD);
    builder2.add(b2, BooleanClause.Occur.SHOULD);
    builder2.add(b3, BooleanClause.Occur.SHOULD);
    System.out.println("query new " + builder2.build());
    processResults(reader.search(builder2.build()));
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
