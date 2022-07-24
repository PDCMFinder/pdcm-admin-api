package org.cancermodels.suggestions.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.cancermodels.MappingEntity;
import org.springframework.stereotype.Component;

/**
 * Allows to search good matches for a {@link MappingEntity} in a Lucene index.
 */
@Slf4j
@Component
public class SuggestionsSearcher {

  private final MappingEntityQueryBuilder mappingEntityQueryBuilder;
  private final LuceneIndexReader luceneIndexReader;
  private final IndexableSuggestionMapper indexableSuggestionMapper;

  public SuggestionsSearcher(
      MappingEntityQueryBuilder mappingEntityQueryBuilder,
      LuceneIndexReader luceneIndexReader,
      IndexableSuggestionMapper indexableSuggestionMapper) {
    this.mappingEntityQueryBuilder = mappingEntityQueryBuilder;
    this.luceneIndexReader = luceneIndexReader;
    this.indexableSuggestionMapper = indexableSuggestionMapper;
  }

  /**
   * Search the top 10 suggestions for the given {@code MappingEntity}.
   * @param mappingEntity The mapping entity for which the suggestions are going to be searched.
   * @return A list with the top 10 suggestions found in lucene, sorted by their score.
   * A suggestion gets return as a hit if:
   *  - It is a rule suggestion and its values (example dataSource, sampleDiagnosis, etc) are
   *  similar enough to the values of the the given {@code MappingEntity}.
   *  + It is a ontology suggestion and the main value of the {@code MappingEntity}  (for
   *  example, sampleDiagnosis for a diagnosis or treatmentName for a treatment) is similar enough
   *  to the label, definition or synonym in the ontology.
   */
  public List<IndexableSuggestionResult> searchTopSuggestions(MappingEntity mappingEntity)
      throws IOException {
    Objects.requireNonNull(mappingEntity);
    log.info("Entity values: " + mappingEntity.getValuesAsMap());

    Query suggestionQuery = mappingEntityQueryBuilder.buildSuggestionQuery(mappingEntity);
    TopDocs topDocs = luceneIndexReader.search(suggestionQuery);
    List<IndexableSuggestionResult> topSuggestions = processTopDocs(topDocs);

    return topSuggestions;
  }

  private List<IndexableSuggestionResult> processTopDocs(TopDocs topDocs) throws IOException {
    List<IndexableSuggestionResult> indexableSuggestionResults = new ArrayList<>();

    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
      Document document = luceneIndexReader.getDocument(scoreDoc);
      IndexableSuggestion indexableSuggestion =
          indexableSuggestionMapper.toIndexableSuggestion(document);

      IndexableSuggestionResult result = new IndexableSuggestionResult();
      result.setIndexableSuggestion(indexableSuggestion);
      result.setScore(scoreDoc.score);
      indexableSuggestionResults.add(result);
    }
    return indexableSuggestionResults;
  }
}
