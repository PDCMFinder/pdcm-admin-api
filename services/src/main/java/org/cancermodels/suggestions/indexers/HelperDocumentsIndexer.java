package org.cancermodels.suggestions.indexers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingEntityRepository;
import org.cancermodels.persistance.MappingValue;
import org.cancermodels.suggestions.search_engine.IndexableOntologySuggestion;
import org.cancermodels.suggestions.search_engine.IndexableRuleSuggestion;
import org.cancermodels.suggestions.search_engine.IndexableSuggestion;
import org.cancermodels.suggestions.search_engine.IndexableSuggestionMapper;
import org.cancermodels.suggestions.search_engine.LuceneIndexWriter;
import org.cancermodels.suggestions.search_engine.MappingValueConfHelper;
import org.cancermodels.suggestions.search_engine.query_builder.QueryHelper;
import org.cancermodels.suggestions.search_engine.util.Constants;
import org.springframework.stereotype.Component;

/**
 * Create customised documents that help with the calculation of the relative scores of the
 * suggestions. The documents created here represent a "perfect" match so we can use their scores
 * to check how good real results actually are.
 */
@Slf4j
@Component
public class HelperDocumentsIndexer {

  private final MappingValueConfHelper mappingValueConfHelper;
  private final MappingEntityRepository mappingEntityRepository;
  private final IndexableSuggestionMapper mapper;
  private final QueryHelper queryHelper;
  private final LuceneIndexWriter luceneIndexWriter;

  public HelperDocumentsIndexer(
      MappingValueConfHelper mappingValueConfHelper,
      MappingEntityRepository mappingEntityRepository,
      IndexableSuggestionMapper mapper,
      QueryHelper queryManager,
      LuceneIndexWriter luceneIndexWriter) {
    this.mappingValueConfHelper = mappingValueConfHelper;
    this.mappingEntityRepository = mappingEntityRepository;
    this.mapper = mapper;
    this.queryHelper = queryManager;
    this.luceneIndexWriter = luceneIndexWriter;
  }

  public void index() throws IOException {
    List<MappingEntity> data = getData();
    List<IndexableSuggestion> helperSuggestions = createIndexableSuggestions(data);
    List<Document> documents = helperSuggestions.stream().map(mapper::toDocument).collect(
        Collectors.toList());
    Query allHelperDocuments = queryHelper.getTermQuery("sourceType", Constants.HELPER_DOCUMENT_TYPE);
    luceneIndexWriter.deleteDocuments(allHelperDocuments);
    luceneIndexWriter.writeDocuments(documents);
    log.info("Helper documents indexed");
  }

  private List<IndexableSuggestion> createIndexableSuggestions(List<MappingEntity> data) {
    return data.stream().map(this::buildHelperSuggestion).collect(Collectors.toList());
  }

  private List<MappingEntity> getData() {
    return mappingEntityRepository.findAll();
  }

  public IndexableSuggestion buildHelperSuggestion(MappingEntity mappingEntity) {
    IndexableSuggestion indexableSuggestion = new IndexableSuggestion();
    indexableSuggestion.setId(mappingEntity.getMappingKey() + "_helper");
    indexableSuggestion.setSourceType(Constants.HELPER_DOCUMENT_TYPE);
    IndexableRuleSuggestion indexableRuleSuggestion =
        buildHelperIndexableRuleSuggestion(mappingEntity);
    IndexableOntologySuggestion indexableOntologySuggestion =
        buildHelperIndexableOntologySuggestion(mappingEntity);
    indexableSuggestion.setRule(indexableRuleSuggestion);
    indexableSuggestion.setOntology(indexableOntologySuggestion);

    return indexableSuggestion;
  }

  private IndexableRuleSuggestion buildHelperIndexableRuleSuggestion(MappingEntity mappingEntity) {
    IndexableRuleSuggestion indexableRuleSuggestion = new IndexableRuleSuggestion();
    indexableRuleSuggestion.setData(mappingEntity.getValuesAsMap());
    indexableRuleSuggestion.setEntityTypeName(mappingEntity.getEntityType().getName());
    indexableRuleSuggestion.setMappedTermLabel("N/A");
    indexableRuleSuggestion.setMappedTermUrl("N/A");
    indexableRuleSuggestion.setKey("N/A");
    return indexableRuleSuggestion;
  }

  // Builds an artificial ontology term that will be the "perfect match". We emulate this
  // by assigning a label that corresponds to the text of the main value in the entity.
  // It sounds like a good idea to make the synonym(s) and definition to match this value,
  // but given how large those fields can be, this artificial document would be "too good" and
  // would hide good real suggestions
  private IndexableOntologySuggestion buildHelperIndexableOntologySuggestion(MappingEntity mappingEntity) {
    IndexableOntologySuggestion indexableOntologySuggestion = new IndexableOntologySuggestion();
    MappingValue mainValue = mappingValueConfHelper.getMainValue(mappingEntity.getMappingValues());
    String text = mainValue.getValue();
    indexableOntologySuggestion.setOntologyTermLabel(text);
    String definition = "N/A";
    indexableOntologySuggestion.setDefinition(definition);
    List<String> synonyms = new ArrayList<>();
    synonyms.add("N/A");
    indexableOntologySuggestion.setSynonyms(new HashSet<>(synonyms));
    indexableOntologySuggestion.setNcit("N/A");
    indexableOntologySuggestion.setKey("N/A");
    return indexableOntologySuggestion;
  }

}
