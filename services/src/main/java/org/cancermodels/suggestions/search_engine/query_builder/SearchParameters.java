package org.cancermodels.suggestions.search_engine.query_builder;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class SearchParameters {

  // Multiplier for all rules (make them more important than ontologies)
  @Value("${rule_multiplier}")
  private double ruleMultiplier;

  // Multiplier for a query term involving only a value from a field.
  // Eg: (SampleDiagnosis: carcinoma)^0.2
  @Value("${term_multiplier}")
  private double termMultiplier;

  // Multiplier for a query term involving more than 1 value from a field.
  // Eg: (SampleDiagnosis: lung carcinoma)^0.2
  // Not used at the moment
  @Value("${multi_term_multiplier}")
  private double multiTermMultiplier;

  // Multiplier for a query term involving only a value from a field.
  // Eg: (SampleDiagnosis: "carcinoma")^0.2
  @Value("${phrase_multiplier}")
  private double phraseMultiplier;

  // Multiplier for a query term involving only a value from a field.
  // Eg: (SampleDiagnosis: "lung carcinoma")^0.2
  // Not used at the moment
  @Value("${multi_term_phrase_multiplier}")
  private double multiTermPhraseMultiplier;

  @Value("${ontology_label_weight}")
  private double ontologyLabelWeight;

  @Value("${ontology_definition_weight}")
  private double ontologyDefinitionWeight;

  @Value("${ontology_synonym_weight}")
  private double ontologySynonymWeight;

  public String toString() {
    return
        "ruleMultiplier=" + ruleMultiplier + "\n" +
        "termMultiplier=" + termMultiplier + "\n" +
        "multiTermMultiplier=" + multiTermMultiplier + "\n" +
        "phraseMultiplier=" + phraseMultiplier + "\n" +
        "multiTermPhraseMultiplier=" + multiTermPhraseMultiplier + "\n" +
        "ontologyLabelWeight=" + ontologyLabelWeight + "\n" +
        "ontologyDefinitionWeight=" + ontologyDefinitionWeight + "\n" +
        "ontologySynonymWeight=" + ontologySynonymWeight;
  }
}
