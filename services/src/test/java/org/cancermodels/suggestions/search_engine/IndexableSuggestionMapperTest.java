package org.cancermodels.suggestions.search_engine;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.cancermodels.exceptions.IndexerException;
import org.junit.jupiter.api.Test;

class IndexableSuggestionMapperTest {

  private IndexableSuggestionMapper instance = new IndexableSuggestionMapper();

  @Test
  void toDocumentWithEmptyIndexableSuggestionNullId() {
    IndexableSuggestion indexableSuggestion = new IndexableSuggestion();

    Exception exception = assertThrows(IndexerException.class, () -> {
      instance.toDocument(indexableSuggestion);
    });
    String expectedMessage = "Error in document creation: Id cannot be null.";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void toDocumentWithEmptyIndexableSuggestionNullSourceType() {
    IndexableSuggestion indexableSuggestion = new IndexableSuggestion();
    indexableSuggestion.setId("id");

    Exception exception = assertThrows(IndexerException.class, () -> {
      instance.toDocument(indexableSuggestion);
    });
    String expectedMessage = "Error in document creation: Source type cannot be null.";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void toDocumentWithEmptyIndexableSuggestionNullRule() {
    IndexableSuggestion indexableSuggestion = new IndexableSuggestion();
    indexableSuggestion.setId("id");
    indexableSuggestion.setSourceType("Rule");

    Exception exception = assertThrows(IndexerException.class, () -> {
      instance.toDocument(indexableSuggestion);
    });
    String expectedMessage = "Error in document creation: Rule cannot be null.";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void toDocumentWithEmptyIndexableSuggestionNullRuleData() {
    IndexableSuggestion indexableSuggestion = new IndexableSuggestion();
    indexableSuggestion.setId("id");
    indexableSuggestion.setSourceType("Rule");

    IndexableRuleSuggestion indexableRuleSuggestion = new IndexableRuleSuggestion();
    indexableSuggestion.setRule(indexableRuleSuggestion);

    Exception exception = assertThrows(IndexerException.class, () -> {
      instance.toDocument(indexableSuggestion);
    });
    String expectedMessage = "Error in document creation: Rule data cannot be null.";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void toDocumentWithEmptyIndexableSuggestionNullOntology() {
    IndexableSuggestion indexableSuggestion = new IndexableSuggestion();
    indexableSuggestion.setId("id");
    indexableSuggestion.setSourceType("Ontology");

    Exception exception = assertThrows(IndexerException.class, () -> {
      instance.toDocument(indexableSuggestion);
    });
    String expectedMessage = "Error in document creation: Ontology cannot be null.";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void toIndexableSuggestion() {
    IndexableSuggestion indexableSuggestion = new IndexableSuggestion();
    indexableSuggestion.setId("id");
    indexableSuggestion.setSourceType("Rule");
    IndexableRuleSuggestion indexableRuleSuggestion = new IndexableRuleSuggestion();
    indexableRuleSuggestion.setMappedTermLabel("mappedTermLabelValue");
    indexableRuleSuggestion.setMappedTermUrl("mappedTermUrlValue");
    Map<String, String> data = new HashMap<>();
    data.put("key1", "value1");
    data.put("key2", "value2");
    data.put("key3", "value3");
    data.put("key4", "value4");
    indexableRuleSuggestion.setData(data);
    indexableRuleSuggestion.setKey("key");
    indexableRuleSuggestion.setEntityTypeName("entityTypeName");
    indexableSuggestion.setRule(indexableRuleSuggestion);

    Document document = instance.toDocument(indexableSuggestion);
    assertEquals("entityTypeName", document.get("rule.entityTypeName"));
    assertEquals("mappedTermUrlValue", document.get("rule.mappedTermUrl"));
    assertEquals("mappedTermLabelValue", document.get("rule.mappedTermLabel"));
    assertEquals("value1", document.get("rule.value.key1"));
    assertEquals("value2", document.get("rule.value.key2"));
    assertEquals("value3", document.get("rule.value.key3"));
    assertEquals("value4", document.get("rule.value.key4"));
  }
}
