package org.cancermodels.reader;

import org.junit.jupiter.api.Test;
import tech.tablesaw.api.Table;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DataReaderTest {
    private static final String testDataDir = "src/test/testdata/dataset2/";

    @Test
    void getTableByFile_DiagnosisData_ExpectedRowsObtained() {
        List<String> keyWords = List.of("metadata-patient_sample");

        Map<String, Table> tables = DataReader.getTableByFile(Path.of(testDataDir + "PROVIDER-A"), keyWords);

        String key = "metadata-patient_sample.tsv";
        assertTrue(tables.containsKey(key), "Expected table to contain " + key);
        Table diagnosisData = tables.get(key);
        assertEquals(diagnosisData.rowCount(), 10, "Unexpected number of rows");
        List<List<String>> expectedTreatmentNames = List.of(
            List.of("Adenocarcinoma - colon", "Primary", "Digestive/Gastrointestinal"),
            List.of("Invasive breast carcinoma", "Primary", "Breast"),
            List.of("Cholangiocar.- intra/extrahepatic", "Primary", "Digestive/Gastrointestinal"),
            List.of("Cholangiocar.- intra/extrahepatic", "Primary", "Digestive/Gastrointestinal"),
            List.of("Cholangiocar.- intra/extrahepatic", "Primary", "Digestive/Gastrointestinal"),
            List.of("Laryngeal squamous cell carcinoma", "Primary", "Head and Neck"),
            List.of("Ovarian cancer, NOS", "Metastatic", "Gynecologic"),
            List.of("Serous endometrial adenocarcinoma", "Metastatic", "Gynecologic"),
            List.of("Melanoma", "Metastatic", "Skin"),
            List.of("Lung adenocarcinoma", "Metastatic", "Respiratory/Thoracic")
        );

        List<List<String>> obtainedTreatmentNames = new ArrayList<>();
        diagnosisData.forEach(row -> {
            obtainedTreatmentNames.add(
                List.of(
                    row.getString("diagnosis"),
                    row.getString("tumour_type"),
                    row.getString("primary_site")));
        });
        assertThat(obtainedTreatmentNames).hasSameElementsAs(expectedTreatmentNames);
    }

    @Test
    void getTableByFile_PatientTreatmentData_ExpectedRowsObtained() {
        List<String> keyWords = List.of("drug", "treatment");

        Map<String, Table> tables = DataReader.getTableByFile(Path.of(testDataDir + "PROVIDER-A"), keyWords);

        String key = "patienttreatment-Sheet1.tsv";
        assertTrue(tables.containsKey(key), "Expected table to contain " + key);
        Table diagnosisData = tables.get(key);
        assertEquals(diagnosisData.rowCount(), 10, "Unexpected number of rows");
        List<String> expectedTreatmentNames = List.of(
            "Anastrozole",
            "Radiation Exposure",
            "Radiation Exposure",
            "Radiation Exposure",
            "Carboplatin+Paclitaxel",
            "Bevacizumab+Carboplatin+Pegylated Liposomal Doxorubicin Hydrochloride",
            "Carboplatin+Gemcitabine",
            "Bevacizumab+Carboplatin+Gemcitabine",
            "Nab-paclitaxel",
            "Autologous Dendritic Cells Transduced with Wild-type p53 Adenovirus Vaccine"
        );

        List<String> obtainedTreatmentNames = new ArrayList<>();
        diagnosisData.forEach(row -> {
            obtainedTreatmentNames.add(row.getString("treatment_name"));
        });
        assertThat(obtainedTreatmentNames).hasSameElementsAs(expectedTreatmentNames);
    }

    @Test
    void getTableByFile_DrugDosingData_ExpectedRowsObtained() {
        List<String> keyWords = List.of("drug", "treatment");

        Map<String, Table> tables = DataReader.getTableByFile(Path.of(testDataDir + "PROVIDER-A"), keyWords);

        String key = "drugdosing-Sheet1.tsv";
        assertTrue(tables.containsKey(key), "Expected table to contain " + key);
        Table diagnosisData = tables.get(key);
        assertEquals(diagnosisData.rowCount(), 8, "Unexpected number of rows");
        List<String> expectedTreatmentNames = List.of(
            "Dactolisib",
            "Trabectedin",
            "Dactolisib",
            "Trabectedin",
            "Dactolisib",
            "Trabectedin",
            "Dactolisib",
            "Trabectedin"
        );

        List<String> obtainedTreatmentNames = new ArrayList<>();
        diagnosisData.forEach(row -> {
            obtainedTreatmentNames.add(row.getString("treatment_name"));
        });
        assertThat(obtainedTreatmentNames).hasSameElementsAs(expectedTreatmentNames);
    }
}