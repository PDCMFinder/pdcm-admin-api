package org.cancermodels.util;

public class Utilities {
  public static String urlToNCIt(String url) {
    int index = url.lastIndexOf("/") + 1;
    return url.substring(index).replace("_", ":");
  }
}
