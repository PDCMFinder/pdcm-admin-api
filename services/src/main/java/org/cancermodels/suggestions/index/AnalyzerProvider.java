package org.cancermodels.suggestions.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.springframework.stereotype.Component;

/**
 * Provides a centralised instance of a Lucene Analyser so all processes
 * requiring one (indexing and searching) use the same one.
 */
@Component
public class AnalyzerProvider {

  private static Analyzer analyzer;

  /**
   * Returns the analyser to be used in the application.
   * @return {@link Analyzer} object configured with stop works for English.
   */
  public Analyzer getAnalyzer() {
    if (analyzer == null) {
      CharArraySet enStopSet = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;
      analyzer = new StandardAnalyzer(enStopSet);
    }
    return analyzer;
  }
}
