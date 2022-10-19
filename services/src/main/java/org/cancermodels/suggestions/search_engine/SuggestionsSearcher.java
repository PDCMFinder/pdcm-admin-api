package org.cancermodels.suggestions.search_engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingEntityRepository;
import org.cancermodels.persistance.OntologyTermRepository;
import org.cancermodels.persistance.Suggestion;
import org.cancermodels.exceptions.SearchException;
import org.cancermodels.suggestions.search_engine.query_builder.MappingEntityQueryBuilder;
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
  private final OntologyTermRepository ontologyTermRepository;
  private final MappingEntityRepository mappingEntityRepository;

  public SuggestionsSearcher(
      MappingEntityQueryBuilder mappingEntityQueryBuilder,
      LuceneIndexReader luceneIndexReader,
      IndexableSuggestionMapper indexableSuggestionMapper,
      OntologyTermRepository ontologyTermRepository,
      MappingEntityRepository mappingEntityRepository) {
    this.mappingEntityQueryBuilder = mappingEntityQueryBuilder;
    this.luceneIndexReader = luceneIndexReader;
    this.indexableSuggestionMapper = indexableSuggestionMapper;
    this.ontologyTermRepository = ontologyTermRepository;
    this.mappingEntityRepository = mappingEntityRepository;
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
  public List<Suggestion> searchTopSuggestions(MappingEntity mappingEntity)  {
    Objects.requireNonNull(mappingEntity);
    List<Suggestion> topSuggestions;
    log.info("Searching suggestions for {}", mappingEntity.getId());
    log.info("Entity values: " + mappingEntity.getValuesAsMap());

    Query suggestionQuery = mappingEntityQueryBuilder.buildSuggestionQuery(mappingEntity);
    topSuggestions = retrieveDocsByQuery(suggestionQuery);
    setRelativeScoreValues(topSuggestions, mappingEntity);

    return topSuggestions;
  }

  public Suggestion getHelperSuggestion(MappingEntity mappingEntity) throws SearchException {
    return getHelperDocumentByMappingEntity(mappingEntity);
  }

  private void setRelativeScoreValues(List<Suggestion> suggestions, MappingEntity mappingEntity)
 {
    // Get the helper document that represents the doc with a perfect score
    Suggestion helperDocSuggestion = getHelperDocumentByMappingEntity(mappingEntity);
    double maxScore = helperDocSuggestion.getScore();
    suggestions.forEach(x -> {
      double relativeScore = x.getScore() * 100 / maxScore;
      x.setRelativeScore(relativeScore);
    });
  }

  private Suggestion getHelperDocumentByMappingEntity(MappingEntity mappingEntity)
      throws SearchException {
    Query query = mappingEntityQueryBuilder.buildHelperDocumentQuery(mappingEntity);
    List<Suggestion> results = retrieveDocsByQuery(query);
    // We expect only one document
    return results.get(0);
  }

  private List<Suggestion> retrieveDocsByQuery(Query query) {
    List<Suggestion> topSuggestions;
    try {
      TopDocs topDocs = luceneIndexReader.search(query);
      topSuggestions = processTopDocs(topDocs);
    } catch (Exception exception) {
      throw new SearchException(exception);
    }
    return topSuggestions;
  }

  private List<Suggestion> processTopDocs(TopDocs topDocs) throws IOException {
    List<Suggestion> suggestions = new ArrayList<>();

    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
      Document document = luceneIndexReader.getDocument(scoreDoc);
      IndexableSuggestion indexableSuggestion =
          indexableSuggestionMapper.toIndexableSuggestion(document);

      IndexableSuggestionResult result = new IndexableSuggestionResult();
      result.setIndexableSuggestion(indexableSuggestion);
      result.setScore(scoreDoc.score);

      Suggestion suggestion = resultToSuggestion(indexableSuggestion);
      suggestion.setScore(scoreDoc.score);
      suggestions.add(suggestion);
    }
    return suggestions;
  }

  private Suggestion resultToSuggestion(IndexableSuggestion indexableSuggestion) {
    Suggestion suggestion = new Suggestion();
    suggestion.setSourceType(indexableSuggestion.getSourceType());

    IndexableRuleSuggestion indexableRuleSuggestion = indexableSuggestion.getRule();

    if (indexableRuleSuggestion != null)
    {
      suggestion.setSuggestedTermUrl(indexableRuleSuggestion.getMappedTermUrl());
      suggestion.setSuggestedTermLabel(indexableRuleSuggestion.getMappedTermLabel());

      MappingEntity mappingEntity = mappingEntityRepository.findByMappingKey(
          indexableRuleSuggestion.getKey()).orElse(null);
      suggestion.setMappingEntity(mappingEntity);
    }
    IndexableOntologySuggestion indexableOntologySuggestion
        = indexableSuggestion.getOntology();

    if (indexableOntologySuggestion != null)
    {
      suggestion.setSuggestedTermUrl("http://purl.obolibrary.org/obo/" +
          indexableOntologySuggestion.getNcit().replace(":", "_"));
      suggestion.setSuggestedTermLabel(indexableOntologySuggestion.getOntologyTermLabel());

      suggestion.setOntologyTerm(
          ontologyTermRepository.findByKey(indexableOntologySuggestion.getKey()));

    }
    return suggestion;
  }

}
