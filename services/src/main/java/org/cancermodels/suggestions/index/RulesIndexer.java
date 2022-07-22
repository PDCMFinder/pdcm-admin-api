package org.cancermodels.suggestions.index;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntityRepository;
import org.cancermodels.mappings.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RulesIndexer {
  private final MappingEntityRepository mappingEntityRepository;
  private final IndexableSuggestionMapper mapper;
  private final QueryHelper queryManager;
  private final LuceneIndexWriter luceneIndexWriter;

  public RulesIndexer(
      MappingEntityRepository mappingEntityRepository,
      IndexableSuggestionMapper mapper,
      QueryHelper queryManager,
      LuceneIndexWriter luceneIndexWriter) {
    this.mappingEntityRepository = mappingEntityRepository;
    this.mapper = mapper;
    this.queryManager = queryManager;
    this.luceneIndexWriter = luceneIndexWriter;
  }

  public void index() throws IOException {
    log.info("Start index process for rules");
    log.info("Getting rules to index");
    List<MappingEntity> data = getData();
    log.info("Got {} rules", data.size());
    List<IndexableSuggestion> rulesSuggestions = createIndexableSuggestions(data);
    List<Document> documents = rulesSuggestions.stream().map(mapper::toDocument).collect(
        Collectors.toList());
    Query allRuleDocuments = queryManager.getTermQuery("sourceType", "Rule");
    luceneIndexWriter.deleteDocuments(allRuleDocuments);
    luceneIndexWriter.writeDocuments(documents);
    log.info("Rules indexed");
  }

  private List<MappingEntity> getData() {
    return mappingEntityRepository.findAll().stream()
        .filter(x -> x.getStatus().equals(Status.MAPPED.getLabel()))
        .collect(Collectors.toList());
  }

  private List<IndexableSuggestion> createIndexableSuggestions(List<MappingEntity> data) {
    return data.stream().map(this::mappingEntityToIndexableSuggestion).collect(Collectors.toList());
  }

  private IndexableSuggestion mappingEntityToIndexableSuggestion(MappingEntity mappingEntity) {
    IndexableSuggestion indexableSuggestion = new IndexableSuggestion();
    indexableSuggestion.setId(mappingEntity.getId().toString());
    indexableSuggestion.setSourceType("Rule");
    IndexableRuleSuggestion rule = new IndexableRuleSuggestion();
    rule.setData(mappingEntity.getValuesAsMap());
    rule.setMappedTermLabel(mappingEntity.getMappedTermLabel());
    rule.setMappedTermUrl(mappingEntity.getMappedTermUrl());
    indexableSuggestion.setRule(rule);

    return indexableSuggestion;
  }
}
