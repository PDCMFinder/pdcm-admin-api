package org.cancermodels.util;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Scanner;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileManager {
  private static final Logger LOG = LoggerFactory.getLogger(FileManager.class);

  public static String getStringFromFile(String path) {
    StringBuilder sb = new StringBuilder();

    try (Stream<String> stream = Files.lines(Paths.get(path))){

      Iterator itr = stream.iterator();
      while (itr.hasNext()) {
        sb.append(itr.next());
      }
      System.out.println("...");
    } catch (Exception e) {
      LOG.error("Failed to load file " + path, e);
    }
    return sb.toString();
  }

  public static String getStringFromUrl(String url) throws IOException {
    String content = "";
    content = new Scanner(new URL(url).openStream(), StandardCharsets.UTF_8).useDelimiter("\\A").next();
    return content;
  }
}
