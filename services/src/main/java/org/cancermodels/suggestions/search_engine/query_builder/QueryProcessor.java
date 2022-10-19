package org.cancermodels.suggestions.search_engine.query_builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.cancermodels.persistance.OntologyTermRepository;
import org.cancermodels.persistance.Suggestion;
import org.cancermodels.exceptions.SearchException;
import org.cancermodels.suggestions.search_engine.IndexableOntologySuggestion;
import org.cancermodels.suggestions.search_engine.IndexableSuggestion;
import org.cancermodels.suggestions.search_engine.IndexableSuggestionMapper;
import org.cancermodels.suggestions.search_engine.LuceneIndexReader;
import org.springframework.stereotype.Component;

/**
 * Execute a query and get resulting documents
 */
@Component
public class QueryProcessor {

  private final LuceneIndexReader luceneIndexReader;
  private final IndexableSuggestionMapper indexableSuggestionMapper;
  private final OntologyTermRepository ontologyTermRepository;

  public QueryProcessor(
      LuceneIndexReader luceneIndexReader,
      IndexableSuggestionMapper indexableSuggestionMapper,
      OntologyTermRepository ontologyTermRepository) {
    this.luceneIndexReader = luceneIndexReader;
    this.indexableSuggestionMapper = indexableSuggestionMapper;
    this.ontologyTermRepository = ontologyTermRepository;
  }

  public List<Suggestion> execute(Query query) {
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

      Suggestion suggestion = resultToSuggestion(indexableSuggestion);
      suggestion.setScore(scoreDoc.score);
      suggestions.add(suggestion);
    }
    return suggestions;
  }

  private Suggestion resultToSuggestion(IndexableSuggestion indexableSuggestion) {
    Suggestion suggestion = new Suggestion();
    suggestion.setSourceType(indexableSuggestion.getSourceType());

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
