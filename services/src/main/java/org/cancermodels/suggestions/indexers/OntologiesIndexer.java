package org.cancermodels.suggestions.indexers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.cancermodels.ontologies.OntologyService;
import org.cancermodels.persistance.OntologyTerm;
import org.cancermodels.suggestions.search_engine.util.Constants;
import org.cancermodels.suggestions.search_engine.IndexableOntologySuggestion;
import org.cancermodels.suggestions.search_engine.IndexableSuggestion;
import org.cancermodels.suggestions.search_engine.IndexableSuggestionMapper;
import org.cancermodels.suggestions.search_engine.LuceneIndexWriter;
import org.cancermodels.suggestions.search_engine.query_builder.QueryHelper;
import org.cancermodels.types.Source;
import org.springframework.stereotype.Component;

/**
 * Indexes all the OLS ontologies in the system as lucene documents.
 */
@Slf4j
@Component
public class OntologiesIndexer {
  private final IndexableSuggestionMapper mapper;
  private final QueryHelper queryManager;
  private final LuceneIndexWriter luceneIndexWriter;
  private final OntologyService ontologyService;

  public OntologiesIndexer(
      IndexableSuggestionMapper mapper,
      QueryHelper queryManager,
      LuceneIndexWriter luceneIndexWriter,
      OntologyService ontologyService) {
    this.mapper = mapper;
    this.queryManager = queryManager;
    this.luceneIndexWriter = luceneIndexWriter;
    this.ontologyService = ontologyService;
  }

  public void index() throws IOException {
    log.info("Start index process for ontologies");
    log.info("Getting ontologies to index");
    List<OntologyTerm> data = getData();
    log.info("Got {} ontologies", data.size());
    List<IndexableSuggestion> ontologySuggestions = createIndexableSuggestions(data);
    List<Document> documents = ontologySuggestions.stream().map(mapper::toDocument).collect(
        Collectors.toList());
    Query allOntologyDocuments = queryManager.getTermQuery("sourceType", "Ontology");
    luceneIndexWriter.deleteDocuments(allOntologyDocuments);
    luceneIndexWriter.writeDocuments(documents);
    log.info("Ontologies indexed");
  }

  private List<OntologyTerm> getData() {
    return ontologyService.getAll();
  }

  private List<IndexableSuggestion> createIndexableSuggestions(List<OntologyTerm> data) {
    return data.stream().map(this::ncitTermToIndexableSuggestion).collect(Collectors.toList());
  }

  private IndexableSuggestion ncitTermToIndexableSuggestion(OntologyTerm ontologyTerm) {
    IndexableSuggestion indexableSuggestion = new IndexableSuggestion();
    indexableSuggestion.setId(ontologyTerm.getId()+"");
    indexableSuggestion.setSourceType(Source.ONTOLOGY.getLabel());
    IndexableOntologySuggestion ontology = new IndexableOntologySuggestion();
    ontology.setNcit(getNcitIdFromUrl(ontologyTerm.getUrl()));
    String definition = getFormattedDescription(ontologyTerm.getDescription());
    ontology.setDefinition(definition);
    ontology.setOntologyTermLabel(getFormattedLabel(ontologyTerm.getLabel()));
    ontology.setSynonyms(getFormattedSynonyms(ontologyTerm.getSynonyms(), ontologyTerm.getLabel()));
    ontology.setKey(ontologyTerm.getKey());
    indexableSuggestion.setOntology(ontology);

    return indexableSuggestion;
  }

  private String getNcitIdFromUrl(String url) {
    int idx = url.lastIndexOf("/") + 1;
    return url.substring(idx);
  }

  private String getFormattedLabel(String originalLabel) {
    String formatted = originalLabel;
    formatted = handlePossibleRegimen(formatted);
    return formatted;
  }

  // Allow a definition with only a max number of words
  private String getFormattedDescription(String originalDescription) {
    String formatted = originalDescription;
    List<String> words = Arrays.asList(originalDescription.split(" "));
    if (words.size() > Constants.MAX_NUMBER_TERMS_BY_QUERY) {
      formatted = String.join(" ", words.subList(0, Constants.MAX_NUMBER_TERMS_BY_QUERY));
    }
    return formatted;
  }

  private Set<String> getFormattedSynonyms(Set<String> synonyms, String label) {
    Set<String> formattedSynonyms = new HashSet<>();
    for (String element : synonyms) {

      if (element.length() < Constants.MAX_WORD_LENGTH) {
        String formatted = element.toLowerCase();
        // No need to add a synonym that is the same as the label.
        if (!label.equalsIgnoreCase(formatted)) {
          formatted = handlePossibleRegimen(formatted);
          formatted = formatted.trim();
          formattedSynonyms.add(formatted);
        }
      }
    }
    return formattedSynonyms;
  }

  private String handlePossibleRegimen(String originalText) {
    String formattedText = originalText;
    formattedText = formattedText.replace("/", "_and_");
    formattedText = formattedText.replace("-", "_and_");
    return formattedText;
  }
}
