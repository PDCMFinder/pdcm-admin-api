package org.cancermodels.suggestions.search_engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.OntologySuggestion;
import org.cancermodels.persistance.RuleSuggestion;
import org.cancermodels.persistance.RuleSuggestionData;
import org.cancermodels.persistance.Suggestion;
import org.cancermodels.suggestions.exceptions.SuggestionCalculationException;
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
  public List<Suggestion> searchTopSuggestions(MappingEntity mappingEntity) throws IOException {
    Objects.requireNonNull(mappingEntity);
    List<Suggestion> topSuggestions;
    log.info("Searching suggestions for {}", mappingEntity.getId());
    log.info("Entity values: " + mappingEntity.getValuesAsMap());
    System.out.println("Searching suggestions for: " + mappingEntity.getMappingKey());
    System.out.println("Entity values: " + mappingEntity.getValuesAsMap());

    Query suggestionQuery = mappingEntityQueryBuilder.buildSuggestionQuery(mappingEntity);
    topSuggestions = retrieveDocsByQuery(suggestionQuery);
    setRelativeScoreValues(topSuggestions, mappingEntity);

    return topSuggestions;
  }

  public Suggestion getHelperSuggestion(MappingEntity mappingEntity) throws IOException {
    return getHelperDocumentByMappingEntity(mappingEntity);
  }

  private void setRelativeScoreValues(List<Suggestion> suggestions, MappingEntity mappingEntity)
      throws IOException {
    // Get the helper document that represents the doc with a perfect score
    Suggestion helperDocSuggestion = getHelperDocumentByMappingEntity(mappingEntity);
    double maxScore = helperDocSuggestion.getScore();
    System.out.println("maxScore: " + maxScore);
    suggestions.forEach(x -> {
      double relativeScore = x.getScore() * 100 / maxScore;
      x.setRelativeScore(relativeScore);
    });
  }

  private Suggestion getHelperDocumentByMappingEntity(MappingEntity mappingEntity)
      throws IOException {
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
      throw new SuggestionCalculationException(exception);
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

      RuleSuggestion ruleSuggestion = new RuleSuggestion();

      suggestion.setSuggestedTermUrl(indexableRuleSuggestion.getMappedTermUrl());
      suggestion.setSuggestedTermLabel(indexableRuleSuggestion.getMappedTermLabel());
      ruleSuggestion.setKey(indexableSuggestion.getId());
      setRuleData(indexableRuleSuggestion.getData(), ruleSuggestion);
      ruleSuggestion.setEntityTypeName(indexableRuleSuggestion.getEntityTypeName());
      suggestion.setRuleSuggestion(ruleSuggestion);
    }
    IndexableOntologySuggestion indexableOntologySuggestion
        = indexableSuggestion.getOntology();
    if (indexableOntologySuggestion != null)
    {

      OntologySuggestion ontologySuggestion = new OntologySuggestion();

      suggestion.setSuggestedTermUrl("http://purl.obolibrary.org/obo/" +
          indexableOntologySuggestion.getNcit().replace(":", "_"));
      suggestion.setSuggestedTermLabel(indexableOntologySuggestion.getOntologyTermLabel());

      ontologySuggestion.setDefinition(indexableOntologySuggestion.getDefinition());
      ontologySuggestion.setSynonyms(indexableOntologySuggestion.getSynonyms());

      suggestion.setOntologySuggestion(ontologySuggestion);
    }
    return suggestion;
  }

  private void setRuleData(Map<String, String> data, RuleSuggestion ruleSuggestion) {

    ruleSuggestion.getMappingValues().clear();
    for (String key : data.keySet()) {
      RuleSuggestionData element = new RuleSuggestionData();

      element.setKey(key);
      element.setValue(data.get(key));
      element.setRuleSuggestion(ruleSuggestion);
      ruleSuggestion.getMappingValues().add(element);
    }
  }

}
