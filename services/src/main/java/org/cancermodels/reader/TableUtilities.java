package org.cancermodels.reader;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.selection.Selection;

@Slf4j
public final class TableUtilities {

    public static Table readTsvOrReturnEmpty(File file) {
        Table dataTable;
        log.info("Reading tsv file {}", file);
        System.out.printf("Reading tsv file %s\r", file);
        dataTable = readTsv(file);
        return dataTable;
    }

    public static Table readTsv(File file) {
        CsvReadOptions.Builder builder = CsvReadOptions
                .builder(file)
                .sample(false)
                .separator('\t');
        CsvReadOptions options = builder.build();
        return Table.read().usingOptions(options);
    }

    public static Table removeHeaderRows(Table table, int numberOfRows) {
        return doesNotHaveEnoughRows(table, numberOfRows)
                ? table.emptyCopy()
                : table.dropRange(numberOfRows);
    }

    private static boolean doesNotHaveEnoughRows(Table table, int numberOfRows) {
        return table.rowCount() <= numberOfRows;
    }

    public static Table removeRowsMissingRequiredColumnValue(Table table, String requiredColumn) {
        Selection missing = table.column(requiredColumn).isMissing();
        return table.dropWhere(missing);
    }

    public static Table removeRowsMissingRequiredColumnValue(Table table, StringColumn requiredColumn) {
        return removeRowsMissingRequiredColumnValue(table, requiredColumn.name());
    }

}
