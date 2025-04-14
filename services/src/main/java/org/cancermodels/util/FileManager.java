package org.cancermodels.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileManager {
  private static final Logger LOG = LoggerFactory.getLogger(FileManager.class);

  public static String getStringFromFile(String path) {
    StringBuilder sb = new StringBuilder();

    try (Stream<String> stream = Files.lines(Paths.get(path))){

      Iterator<String> itr = stream.iterator();
      while (itr.hasNext()) {
        sb.append(itr.next());
      }
      System.out.println("...");
    } catch (Exception e) {
        LOG.error("Failed to load file {}", path, e);
    }
    return sb.toString();
  }

  public static String getStringFromUrl(String url) throws IOException {
    String content = "";
    content = new Scanner(new URL(url).openStream(), StandardCharsets.UTF_8).useDelimiter("\\A").next();
    return content;
  }

  // Find the names of all tsv files in `directory` and its subdirectories whose name contains any of the values
  // in `keywords`.
  public static List<File> getTsvFilesWithKeyWordsRecursive(File directory, List<String> keywords) {
    List<File> files = new ArrayList<>();
    String[] extensions = {"tsv"};
    Collection<File> allRecursiveFiles = FileUtils.listFiles(directory, extensions, true);
    // Get only the ones whose name contains any of the keyWords
    allRecursiveFiles.forEach(file -> {
      String fileName = file.getName().toLowerCase();
      for (String keyword : keywords) {
        if (fileName.contains(keyword.toLowerCase())) {
          files.add(file);
        }
      }

    });
    return files;
  }

  /**
   * Reads a resource file and creates a copy of it as a temp file, returning this path.
   * This is a solution to the issue of not finding a file when using relative paths in deployments
   * like kubernetes
   * @param resourcePath Path to the resource file
   * @return the path of the copy of the resource file as a temp file
   */
  public static String getTmpPathForResource(String resourcePath) throws IOException {
    String fileName = resourcePath.substring(
        resourcePath.lastIndexOf('/') + 1,
        resourcePath.lastIndexOf('.'));
    String ext = resourcePath.substring(resourcePath.lastIndexOf('.') + 1);
    LOG.info("Get tmp path for resource {} with ext {}", fileName, ext);
    try (InputStream is = FileManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
      if (is == null) {
        throw new FileNotFoundException("Resource not found: " + resourcePath);
      }

      // Create a temp file
      Path tempFile = Files.createTempFile(fileName, ext);
      tempFile.toFile().deleteOnExit(); // optional: clean up on JVM exit

      // Copy content from resource to temp file
      Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);

      return tempFile.toAbsolutePath().toString(); // return usable path
    }
  }
}
