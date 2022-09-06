package org.cancermodels.suggestions.search_engine.util;

import org.apache.commons.lang3.StringUtils;
import org.cancermodels.suggestions.search_engine.util.Constants;

public class TextFormatter {
  public static String abbreviateMaxTextLength(String text) {
    return StringUtils.abbreviate(text, Constants.MAX_TEXT_LENGTH);
  }
}
