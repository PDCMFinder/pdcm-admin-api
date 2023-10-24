package org.cancermodels.reader;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.cancermodels.util.FileManager;
import org.springframework.stereotype.Component;
import tech.tablesaw.api.Table;

/**
 * A class to read data from the /data folder
 */
@Slf4j
@Component
public class DataReader {

  public static Map<String, Table> getTableByFile(Path targetDirectory, List<String> keywords) {

    Map<String, Table> tableByFile = readAllTsvFilesIn(targetDirectory, keywords);
    tableByFile = TableSetUtilities.removeProviderNameFromFilename(tableByFile);
    TableSetUtilities.removeDescriptionColumn(tableByFile);
    return tableByFile;
  }

  private static Map<String, Table> readAllTsvFilesIn(Path targetDirectory, List<String> keywords) {
    HashMap<String, Table> tables = new HashMap<>();
    List<File> files =  FileManager.getTsvFilesWithKeyWordsRecursive(
        targetDirectory.toFile(), keywords);
    files.forEach(file -> tables.put(file.getName(), TableUtilities.readTsvOrReturnEmpty(file)));
    return tables;
  }

}
