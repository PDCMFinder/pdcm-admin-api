package org.cancermodels.reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.Table;

/**
 * A class to read data from the /data folder
 */
@Slf4j
@Component
public class DataReader {

  public Map<String, Table> getTableByFile(Path targetDirectory, PathMatcher filter) {

    Map<String, Table> tableByFile = readAllTsvFilesIn(targetDirectory, filter);

    tableByFile = TableSetUtilities.removeProviderNameFromFilename(tableByFile);
    TableSetUtilities.removeDescriptionColumn(tableByFile);
    tableByFile = TableSetUtilities.removeHeaderRows(tableByFile);

    return tableByFile;
  }

  private Map<String, Table> readAllTsvFilesIn(Path targetDirectory, PathMatcher filter) {
    HashMap<String, Table> tables = new HashMap<>();
    try (final Stream<Path> stream = Files.list(targetDirectory)) {
      stream
          .filter(filter::matches)
          .forEach(path -> tables.put(
              path.getFileName().toString(),
              TableUtilities.readTsvOrReturnEmpty(path.toFile()))
          );
    } catch (IOException e) {
      log.error("There was an error reading the files", e);
    }
    return tables;
  }

}
