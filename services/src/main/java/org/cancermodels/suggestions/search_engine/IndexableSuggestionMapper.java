package org.cancermodels.suggestions.search_engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.cancermodels.exceptions.IndexerException;
import org.cancermodels.suggestions.FieldsNames;
import org.cancermodels.suggestions.search_engine.util.Constants;
import org.cancermodels.pdcm_admin.types.Source;
import org.springframework.stereotype.Component;

@Component
public class IndexableSuggestionMapper {

  public Document toDocument(IndexableSuggestion indexableSuggestion) {
    Document document = new Document();
    validateIndexableSuggestion(indexableSuggestion);
    document.add(
        new StringField(
            FieldsNames.ID.getName(), indexableSuggestion.getId(), Field.Store.YES));
    document.add(
        new StringField(
            FieldsNames.SOURCE_TYPE.getName(),
            indexableSuggestion.getSourceType(),
            Field.Store.YES));

    List<Field> ontologyFields = getOntologyFields(indexableSuggestion.getOntology());
    ontologyFields.forEach(document::add);

    List<Field> ruleFields = getRuleFields(indexableSuggestion.getRule());
    ruleFields.forEach(document::add);

    return document;
  }

  private void validateIndexableSuggestion(IndexableSuggestion indexableSuggestion) {
    if (indexableSuggestion.getId() == null) {
      throw new IndexerException("Error in document creation: Id cannot be null.");
    }
    if (indexableSuggestion.getSourceType() == null) {
      throw new IndexerException("Error in document creation: Source type cannot be null.");
    }

    if (indexableSuggestion.getSourceType().equalsIgnoreCase(Source.RULE.getLabel())
        && indexableSuggestion.getRule() == null) {
      throw new IndexerException("Error in document creation: Rule cannot be null.");
    }

    if (indexableSuggestion.getSourceType().equalsIgnoreCase(Source.RULE.getLabel())
        && indexableSuggestion.getRule() != null
        && indexableSuggestion.getRule().getData() == null) {
      throw new IndexerException("Error in document creation: Rule data cannot be null.");
    }

    if (indexableSuggestion.getSourceType().equalsIgnoreCase(Source.ONTOLOGY.getLabel())
        && indexableSuggestion.getOntology() == null) {
      throw new IndexerException("Error in document creation: Ontology cannot be null.");
    }

  }

  private List<Field> getOntologyFields(IndexableOntologySuggestion ontology) {
    List<Field> fields = new ArrayList<>();
    if (ontology != null) {
      fields.add(new TextField(FieldsNames.ONTOLOGY_LABEL.getName(), ontology.getOntologyTermLabel(), Field.Store.YES));

      fields.add(new TextField(FieldsNames.ONTOLOGY_DEFINITION.getName(), ontology.getDefinition(), Field.Store.YES));

      for (String synonym : ontology.getSynonyms()) {
        fields.add(new TextField(FieldsNames.ONTOLOGY_SYNONYM.getName(), synonym, Field.Store.YES));
      }

      fields.add(new TextField(FieldsNames.ONTOLOGY_NCIT_TERM.getName(), ontology.getNcit(), Field.Store.YES));

      fields.add(new TextField(FieldsNames.ONTOLOGY_KEY.getName(), ontology.getNcit(), Field.Store.YES));

      fields.add(new TextField(FieldsNames.ONTOLOGY_TYPE.getName(), ontology.getType(), Field.Store.YES));
    }

    return fields;
  }

  private List<Field> getRuleFields(IndexableRuleSuggestion rule) {
    List<Field> fields = new ArrayList<>();
    if (rule != null) {
      fields.add(
          new TextField(
              FieldsNames.RULE_MAPPED_TERM_URL.getName(),
              rule.getMappedTermUrl(),
              Field.Store.YES));
      fields.add(
          new TextField(
              FieldsNames.RULE_MAPPED_TERM_LABEL.getName(),
              rule.getMappedTermLabel(),
              Field.Store.YES));
      fields.add(
          new StringField(
              FieldsNames.RULE_ENTITY_TYPE_NAME.getName(),
              rule.getEntityTypeName(),
              Field.Store.YES));

      fields.add(
          new StringField(
              FieldsNames.RULE_KEY.getName(),
              rule.getKey(),
              Field.Store.YES));

      Map<String, String> data = rule.getData();
      for (String key : data.keySet()) {
        // Make first letter lowercase to be consistent with the names of other fields
        String keyFirstLowercase = Character.toLowerCase(key.charAt(0)) + key.substring(1);
        String fieldName = FieldsNames.RULE_VALUE.getName() + keyFirstLowercase;
        fields.add(new TextField(fieldName, data.get(key), Field.Store.YES));
      }
    }
    return fields;
  }

  public IndexableSuggestion toIndexableSuggestion(Document document) {
    IndexableSuggestion indexableSuggestion = new IndexableSuggestion();
    String id = document.get(FieldsNames.ID.getName());
    String sourceType = document.get(FieldsNames.SOURCE_TYPE.getName());
    indexableSuggestion.setId(id);
    indexableSuggestion.setSourceType(sourceType);

    if (sourceType.equalsIgnoreCase(Source.RULE.getLabel())) {
      addRuleData(indexableSuggestion, document);
    } else if (sourceType.equalsIgnoreCase(Source.ONTOLOGY.getLabel())) {
      addOntologyData(indexableSuggestion, document, id);
    } else if (sourceType.equalsIgnoreCase(Constants.HELPER_DOCUMENT_TYPE)) {
      addRuleData(indexableSuggestion, document);
      addOntologyData(indexableSuggestion, document, id);
    }

    return indexableSuggestion;

  }

  private void addRuleData(IndexableSuggestion indexableSuggestion, Document document) {
    String mappedTermUrl = document.get(FieldsNames.RULE_MAPPED_TERM_URL.getName());
    String mappedTermLabel = document.get(FieldsNames.RULE_MAPPED_TERM_LABEL.getName());
    String entityTypeName = document.get(FieldsNames.RULE_ENTITY_TYPE_NAME.getName());
    String key = document.get(FieldsNames.RULE_KEY.getName());
    Map<String, String> values = new HashMap<>();
    List<IndexableField> valueFields = document.getFields().stream()
        .filter(x -> x.name().startsWith(FieldsNames.RULE_VALUE.getName())).collect(
            Collectors.toList());
    for (IndexableField field : valueFields) {
      values.put(field.name(), document.get(field.name()));
    }

    IndexableRuleSuggestion ruleSuggestion = new IndexableRuleSuggestion();

    ruleSuggestion.setMappedTermUrl(mappedTermUrl);
    ruleSuggestion.setMappedTermLabel(mappedTermLabel);
    ruleSuggestion.setData(values);
    ruleSuggestion.setEntityTypeName(entityTypeName);
    ruleSuggestion.setKey(key);

    indexableSuggestion.setRule(ruleSuggestion);
  }

  private void addOntologyData(IndexableSuggestion indexableSuggestion, Document document, String id) {
    String label = document.get(FieldsNames.ONTOLOGY_LABEL.getName());
    String definition = document.get(FieldsNames.ONTOLOGY_DEFINITION.getName());
    Set<String> synonyms = Set.of(document.getValues(FieldsNames.ONTOLOGY_SYNONYM.getName()));
    String ncitTerm = document.get(FieldsNames.ONTOLOGY_NCIT_TERM.getName());
    String key = document.get(FieldsNames.ONTOLOGY_KEY.getName());

    IndexableOntologySuggestion ontologySuggestion = new IndexableOntologySuggestion();

    ontologySuggestion.setNcit(ncitTerm);
    ontologySuggestion.setOntologyTermLabel(label);
    ontologySuggestion.setDefinition(definition);
    ontologySuggestion.setSynonyms(synonyms);
    ontologySuggestion.setKey(key);

    indexableSuggestion.setOntology(ontologySuggestion);
  }
}
