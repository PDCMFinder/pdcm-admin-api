package org.cancermodels;

/**
 * Represents a mapping suggestion.
 */
public interface Suggestion<T> {

  /**
   * Value from 0 to 100 representing how good the suggestion is.
   */
  int getScore();

  /**
   * Returns the suggestion object (Mapping entity or ontology suggestion for instance).
   * @return Object representing the suggestion (It's an object of the class implementing this
   * interface).
   */
  T getSuggestion();

  /**
   * Informs what was used for the suggestion (Rule/Ontology for instance).
   * @return String with the source of the suggestion.
   */
  Source getSource();

  /**
   * Gets the url of the ontology term in the suggestion.
   * @return Ontology term url.
   */
  String getTermUrl();

  /**
   * Gets the label of the ontology term in the suggestion.
   * @return Ontology term label.
   */
  String getTermLabel();
}
