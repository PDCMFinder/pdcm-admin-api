package org.cancermodels.reader;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import tech.tablesaw.api.Table;

public class TableSetUtilities {

    private TableSetUtilities() { throw new IllegalStateException("Utility class"); }

    static Map<String, Table> removeHeaderRows(Map<String, Table> tableSet) {
        return tableSet.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        e -> TableUtilities.removeHeaderRows(e.getValue(), 4)
                ));
    }

    @Deprecated
    static Map<String, Table> removeBlankRows(Map<String, Table> tableSet) {
        return tableSet.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        e ->  TableUtilities.removeRowsMissingRequiredColumnValue(
                                e.getValue(),
                                e.getValue().column(0).asStringColumn())
                ));
    }

    static Map<String, Table> removeHeaderRowsIfPresent(Map<String, Table> tableSet) {
        return tableSet.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        e -> removeHeaderRowsIfPresent(e.getValue())));
    }

    static Table removeHeaderRowsIfPresent(Table table) {
        return table.columnNames().contains("Field")
                ? TableUtilities.removeHeaderRows(table, 4)
                : table;
    }

    static void removeDescriptionColumn(Map<String, Table> tableSet) {
        tableSet.values().forEach(t -> removeColumnIfExists(t, "Field"));
    }

    static void removeColumnIfExists(Table table, String columnToRemove) {
        if (table.columnNames().contains(columnToRemove))
            table.removeColumns(columnToRemove);
    }

    static Map<String, Table> removeProviderNameFromFilename(Map<String, Table> tableSet) {
        return tableSet.entrySet().stream().collect(
                Collectors.toMap(
                        e -> substringAfterIfContainsSeparator(e.getKey(), "_"),
                        e -> e.getValue().setName(substringAfterIfContainsSeparator(e.getKey(), "_"))
                ));
    }

    static String substringAfterIfContainsSeparator(String string, String separator) {
        return string.contains(separator)
                ? StringUtils.substringAfter(string, separator)
                : string;
    }

}
