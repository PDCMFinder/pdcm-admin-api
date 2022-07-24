package org.cancermodels.suggestions.index;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OntologiesIndexer {
  private final OboParser oboParser;
  private final IndexableSuggestionMapper mapper;
  private final QueryHelper queryManager;
  private final LuceneIndexWriter luceneIndexWriter;

  @Value( "${obo_file_path}" )
  private String oboFilePath;

  public OntologiesIndexer(OboParser oboParser,
      IndexableSuggestionMapper mapper,
      QueryHelper queryManager,
      LuceneIndexWriter luceneIndexWriter) {
    this.oboParser = oboParser;
    this.mapper = mapper;
    this.queryManager = queryManager;
    this.luceneIndexWriter = luceneIndexWriter;
  }

  public void index() throws IOException {
    log.info("Start index process for ontologies");
    log.info("Getting ontologies to index");
    List<NcitTerm> data = getData();
    log.info("Got {} ontologies", data.size());
    List<IndexableSuggestion> ontologySuggestions = createIndexableSuggestions(data);
    List<Document> documents = ontologySuggestions.stream().map(mapper::toDocument).collect(
        Collectors.toList());
    Query allOntologyDocuments = queryManager.getTermQuery("sourceType", "Ontology");
    luceneIndexWriter.deleteDocuments(allOntologyDocuments);
    luceneIndexWriter.writeDocuments(documents);
    log.info("Ontologies indexed");
  }

  private List<NcitTerm> getData() {
    return oboParser.parseOboFile(oboFilePath);
  }

  private List<IndexableSuggestion> createIndexableSuggestions(List<NcitTerm> data) {
    return data.stream().map(this::ncitTermToIndexableSuggestion).collect(Collectors.toList());
  }

  private IndexableSuggestion ncitTermToIndexableSuggestion(NcitTerm ncitTerm) {
    IndexableSuggestion indexableSuggestion = new IndexableSuggestion();
    indexableSuggestion.setId(ncitTerm.getId());
    indexableSuggestion.setSourceType("Ontology");
    IndexableOntologySuggestion ontology = new IndexableOntologySuggestion();
    ontology.setOntologyTermId(ncitTerm.getId());
    ontology.setDefinition(ncitTerm.getDefinition());
    ontology.setOntologyTermLabel(ncitTerm.getName());
    ontology.setSynonyms(getFormattedSynonyms(ncitTerm.getSynonyms()));
    indexableSuggestion.setOntology(ontology);

    return indexableSuggestion;
  }

  private Set<String> getFormattedSynonyms(Set<String> synonyms) {
    Set<String> formattedSynonyms = new HashSet<>();
    for (String element : synonyms) {
      try {
        List<String> tokens = TokenizerHelper.tokenize("", element.toLowerCase());
        String formatted = String.join(" ", tokens);
        formattedSynonyms.add(formatted);
      } catch (IOException e) {
        log.error("Cannot tokenize synonym " + element);
        formattedSynonyms.add(element);
      }
    }
    return formattedSynonyms;
  }
}
