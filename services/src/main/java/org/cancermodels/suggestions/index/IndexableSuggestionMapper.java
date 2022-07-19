package org.cancermodels.suggestions.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.springframework.stereotype.Component;

@Component
public class IndexableSuggestionMapper {

  public Document toDocument(IndexableSuggestion indexableSuggestion) {
    Document document = new Document();
    document.add(new StringField("id", indexableSuggestion.getId() , Field.Store.YES));
    document.add(
        new StringField("sourceType", indexableSuggestion.getSourceType() , Field.Store.YES));

    List<Field> ontologyFields = getOntologyFields(indexableSuggestion.getOntology());
    ontologyFields.forEach(document::add);

    List<Field> ruleFields = getRuleFields(indexableSuggestion.getRule());
    ruleFields.forEach(document::add);

    return document;
  }

  private List<Field> getOntologyFields(IndexableOntologySuggestion ontology) {
    List<Field> fields = new ArrayList<>();
    if (ontology != null) {
      fields.add(new TextField(
          "ontology.ontologyTermLabel", ontology.getOntologyTermLabel(), Field.Store.YES));
      fields.add(new TextField(
          "ontology.definition", ontology.getDefinition(), Field.Store.YES));
      fields.add(new TextField(
          "ontology.definition", ontology.getDefinition(), Field.Store.YES));
      for (String synonym : ontology.getSynonyms()) {
        fields.add(new TextField("ontology.synonym", synonym, Field.Store.YES));
      }
    }
    return fields;
  }

  private List<Field> getRuleFields(IndexableRuleSuggestion rule) {
    List<Field> fields = new ArrayList<>();
    if (rule != null) {
      fields.add(new TextField(
          "rule.mappedTermUrl", rule.getMappedTermUrl(), Field.Store.YES));
      fields.add(new TextField(
          "rule.mappedTermLabel", rule.getMappedTermLabel(), Field.Store.YES));

      Map<String, String> data = rule.getData();
      for (String key : data.keySet()) {
        fields.add(new TextField("rule." + key, data.get(key), Field.Store.YES));
      }
    }
    return fields;
  }
}
