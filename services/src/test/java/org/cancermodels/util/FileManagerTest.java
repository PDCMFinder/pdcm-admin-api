package org.cancermodels.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FileManagerTest {

    private static final String testDataDir = "src/test/testdata/dataset1/";

    @Test
    void getTsvFilesWithKeyWordsRecursiveFindTreatmentFiles() {
        File directory = new File(testDataDir);
        List<String> keyWords = Arrays.asList("drug", "treatment");
        var result = FileManager.getTsvFilesWithKeyWordsRecursive(directory, keyWords);

        List<File> expected = Arrays.asList(
            new File( testDataDir + "TRACE/treatment/TRACE_patienttreatment-Sheet1.tsv"),
            new File(testDataDir + "TRACE/drug/TRACE_drugdosing-Sheet1.tsv"),
            new File(testDataDir + "PDMR/treatment/PDMR_patienttreatment-Sheet1.tsv")
        );
        assertThat(result).hasSameElementsAs(expected);
    }

    @Test
    void getTsvFilesWithKeyWordsRecursiveFindDiagnosisFiles() {
        File directory = new File(testDataDir);
        List<String> keyWords = List.of("metadata-patient_sample");
        var result = FileManager.getTsvFilesWithKeyWordsRecursive(directory, keyWords);

        List<File> expected = Arrays.asList(
            new File(testDataDir + "PDMR/PDMR_metadata-patient_sample.tsv"),
            new File(testDataDir + "TRACE/TRACE_metadata-patient_sample.tsv")
        );
        assertThat(result).hasSameElementsAs(expected);
    }
}
