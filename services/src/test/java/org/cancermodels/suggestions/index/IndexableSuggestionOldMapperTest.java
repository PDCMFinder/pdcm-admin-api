package org.cancermodels.suggestions.index;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.cancermodels.suggestions.exceptions.NonIndexableDocumentException;
import org.junit.jupiter.api.Test;

class IndexableSuggestionOldMapperTest {

  private IndexableSuggestionMapper instance = new IndexableSuggestionMapper();

  @Test
  void toDocumentWithEmptyIndexableSuggestionNullId() {
    IndexableSuggestion indexableSuggestion = new IndexableSuggestion();

    Exception exception = assertThrows(NonIndexableDocumentException.class, () -> {
      instance.toDocument(indexableSuggestion);
    });
    String expectedMessage = "Id cannot be null.";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void toDocumentWithEmptyIndexableSuggestionNullSourceType() {
    IndexableSuggestion indexableSuggestion = new IndexableSuggestion();
    indexableSuggestion.setId("id");

    Exception exception = assertThrows(NonIndexableDocumentException.class, () -> {
      instance.toDocument(indexableSuggestion);
    });
    String expectedMessage = "Source type cannot be null.";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void toDocumentWithEmptyIndexableSuggestionNullRule() {
    IndexableSuggestion indexableSuggestion = new IndexableSuggestion();
    indexableSuggestion.setId("id");
    indexableSuggestion.setSourceType("Rule");

    Exception exception = assertThrows(NonIndexableDocumentException.class, () -> {
      instance.toDocument(indexableSuggestion);
    });
    String expectedMessage = "Rule cannot be null.";
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

    Exception exception = assertThrows(NonIndexableDocumentException.class, () -> {
      instance.toDocument(indexableSuggestion);
    });
    String expectedMessage = "Rule data cannot be null.";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void toDocumentWithEmptyIndexableSuggestionNullOntology() {
    IndexableSuggestion indexableSuggestion = new IndexableSuggestion();
    indexableSuggestion.setId("id");
    indexableSuggestion.setSourceType("Ontology");

    Exception exception = assertThrows(NonIndexableDocumentException.class, () -> {
      instance.toDocument(indexableSuggestion);
    });
    String expectedMessage = "Ontology cannot be null.";
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
    indexableSuggestion.setRule(indexableRuleSuggestion);

    Document document = instance.toDocument(indexableSuggestion);
    System.out.println(document);
  }
}