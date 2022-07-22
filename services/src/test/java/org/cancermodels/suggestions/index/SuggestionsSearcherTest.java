package org.cancermodels.suggestions.index;

import static org.cancermodels.general.MappingEntityCreator.createMappingEntityTestInstance;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.TotalHits.Relation;
import org.cancermodels.MappingEntity;
import org.cancermodels.general.QueryTestUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SuggestionsSearcherTest {

  @Mock
  private MappingEntityQueryBuilder mappingEntityQueryBuilder;
  @Mock
  private  LuceneIndexReader luceneIndexReader;

  @InjectMocks
  private SuggestionsSearcher instance;

  @Test
  void searchTopSuggestions() throws IOException {
    MappingEntity mappingEntity = createMappingEntityTestInstance();

    Query dummyQuery = QueryTestUtilities.getDummyQuery();

    TopDocs topDocs = buildTestTopDocs();

    when(mappingEntityQueryBuilder.buildSuggestionQuery(mappingEntity)).thenReturn(dummyQuery);
    when(luceneIndexReader.search(dummyQuery)).thenReturn(topDocs);

    instance.searchTopSuggestions(mappingEntity);
  }

  private TopDocs buildTestTopDocs() {
    ScoreDoc[] scoreDocs = new ScoreDoc[5];
    ScoreDoc scoreDoc1 = new ScoreDoc(1, 0.1f);
    ScoreDoc scoreDoc2 = new ScoreDoc(2, 0.2f);
    ScoreDoc scoreDoc3 = new ScoreDoc(3, 0.3f);
    ScoreDoc scoreDoc4 = new ScoreDoc(4, 0.4f);
    ScoreDoc scoreDoc5 = new ScoreDoc(5, 0.5f);
    scoreDocs[0] = scoreDoc1;
    scoreDocs[1] = scoreDoc2;
    scoreDocs[2] = scoreDoc3;
    scoreDocs[3] = scoreDoc4;
    scoreDocs[4] = scoreDoc5;



    TotalHits hits = new TotalHits(5, Relation.EQUAL_TO);

    TopDocs topDocs = new TopDocs(hits, scoreDocs);
    return topDocs;
  }
}