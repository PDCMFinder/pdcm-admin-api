package org.cancermodels.suggestions.index;

import org.apache.commons.lang3.StringUtils;

public class TextFormatter {
  public static String abbreviateMaxTextLength(String text) {
    return StringUtils.abbreviate(text, Constants.MAX_TEXT_LENGTH);
  }
}
