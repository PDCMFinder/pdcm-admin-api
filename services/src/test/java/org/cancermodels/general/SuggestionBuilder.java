package org.cancermodels.general;

import org.cancermodels.persistance.Suggestion;
import org.cancermodels.types.Source;

public class SuggestionBuilder {

  private int id;
  private String sourceType;
  private double score;
  private double relativeScore;
  private String suggestedTermUrl;

  public Suggestion build() {
    Suggestion suggestion = new Suggestion();
    suggestion.setId(id);
    suggestion.setSourceType(sourceType);
    suggestion.setScore(score);
    suggestion.setRelativeScore(relativeScore);
    suggestion.setSuggestedTermUrl(suggestedTermUrl);

    return suggestion;
  }

  public SuggestionBuilder setId(int id) {
    this.id = id;
    return this;
  }

  public SuggestionBuilder setSourceType(String sourceType) {
    this.sourceType = sourceType;
    return this;
  }

  public SuggestionBuilder setScore(double score) {
    this.score = score;
    return this;
  }

  public SuggestionBuilder setRelativeScore(double relativeScore) {
    this.relativeScore = relativeScore;
    return this;
  }

  public SuggestionBuilder setSuggestedTermUrl(String suggestedTermUrl) {
    this.suggestedTermUrl = suggestedTermUrl;
    return this;
  }


  public static void main(String[] args) {
    //
    SuggestionBuilder suggestionBuilder = new SuggestionBuilder();
    Suggestion suggestion =
        suggestionBuilder
            .setId(1)
            .setSourceType(Source.RULE.getLabel())
            .setScore(32.5)
            .setRelativeScore(90.1)
            .build();

//    System.out.println(Util.prettyPrint(mappingEntity));
    System.out.println(suggestion);
  }
}
