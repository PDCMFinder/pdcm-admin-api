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
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.Suggestion;
import org.cancermodels.persistance.Suggestion.OntologySuggestion;
import org.cancermodels.persistance.Suggestion.RuleSuggestion;
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
  public List<Suggestion> searchTopSuggestions(MappingEntity mappingEntity) {
    Objects.requireNonNull(mappingEntity);
    List<Suggestion> topSuggestions = new ArrayList<>();
    log.info("Entity values: " + mappingEntity.getValuesAsMap());

    try {

      Query suggestionQuery = mappingEntityQueryBuilder.buildSuggestionQuery(mappingEntity);
      TopDocs topDocs = luceneIndexReader.search(suggestionQuery);
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

    if (indexableSuggestion.getSourceType().equalsIgnoreCase("rule"))
    {
      IndexableRuleSuggestion indexableRuleSuggestion = indexableSuggestion.getRule();
      Suggestion.RuleSuggestion ruleSuggestion = new RuleSuggestion();

      suggestion.setSuggestedTermUrl(indexableRuleSuggestion.getMappedTermUrl());
      suggestion.setSuggestedTermLabel(indexableRuleSuggestion.getMappedTermLabel());
      ruleSuggestion.setMappingEntityId(Integer.parseInt(indexableSuggestion.getId()));
      ruleSuggestion.setData(indexableRuleSuggestion.getData());
      suggestion.setRuleSuggestion(ruleSuggestion);
    }
    if (indexableSuggestion.getSourceType().equalsIgnoreCase("ontology"))
    {
      IndexableOntologySuggestion indexableOntologySuggestion
          = indexableSuggestion.getOntology();
      Suggestion.OntologySuggestion ontologySuggestion = new OntologySuggestion();

      suggestion.setSuggestedTermUrl("http://purl.obolibrary.org/obo/" +
          indexableOntologySuggestion.getOntologyTermId().replace(":", "_"));
      suggestion.setSuggestedTermLabel(indexableOntologySuggestion.getOntologyTermLabel());

      ontologySuggestion.setDefinition(indexableOntologySuggestion.getDefinition());
      ontologySuggestion.setSynonyms(indexableOntologySuggestion.getSynonyms());

      suggestion.setOntologySuggestion(ontologySuggestion);
    }
    return suggestion;
  }



}
