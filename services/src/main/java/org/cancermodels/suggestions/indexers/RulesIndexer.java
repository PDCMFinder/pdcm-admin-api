package org.cancermodels.suggestions.indexers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingEntityRepository;
import org.cancermodels.Status;
import org.cancermodels.suggestions.index.Constants;
import org.cancermodels.suggestions.index.IndexableRuleSuggestion;
import org.cancermodels.suggestions.index.IndexableSuggestion;
import org.cancermodels.suggestions.index.IndexableSuggestionMapper;
import org.cancermodels.suggestions.index.LuceneIndexWriter;
import org.cancermodels.suggestions.index.QueryHelper;
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
    rule.setEntityTypeName(mappingEntity.getEntityType().getName());
    rule.setData(formatRuleValues(mappingEntity.getValuesAsMap()));
    rule.setMappedTermLabel(mappingEntity.getMappedTermLabel());
    rule.setMappedTermUrl(mappingEntity.getMappedTermUrl());
    indexableSuggestion.setRule(rule);

    return indexableSuggestion;
  }


  private Map<String, String> formatRuleValues(Map<String, String> values) {
    Map<String, String> formattedValues = new HashMap<>();
    for (String key : values.keySet()) {
      formattedValues.put(key, formatText(values.get(key)));
    }
    return formattedValues;
  }

  private String formatText(String text) {
    return StringUtils.abbreviate(text, Constants.MAX_TEXT_LENGTH);
  }
}