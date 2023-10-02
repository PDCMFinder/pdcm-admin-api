package org.cancermodels.pdcm_etl;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;
import java.util.Map;

@Entity
@TypeDefs({
    @TypeDef(name = "json", typeClass = JsonType.class)
})
@Getter
@Setter
@Table(name ="search_index", schema = "pdcm_api")
public class SearchIndex {
    @Id
    private Long pdcmModelId;

    private String externalModelId;
    private String dataSource;
    private String projectName;
    private String providerName;
    private String modelType;
    private String histology;
    private String cancerSystem;
    private String datasetAvailable;
    private String licenseName;
    private String primarySite;
    private String collectionSite;
    private String tumourType;
    private String cancerGrade;
    private String cancerGradingSystem;
    private String cancerStage;
    private String cancerStagingSystem;
    private String patientAge;
    private String patientSex;
    private String patientHistory;
    private String patientEthnicity;
    private String patientEthnicityAssessmentMethod;
    private String patientInitialDiagnosis;
    private String patientTreatmentStatus;
    private String patientAgeAtInitialDiagnosis;
    private String patientSampleId;
    private String patientSampleCollectionDate;
    private String patientSampleCollectionEvent;
    @Column(name="patient_sample_months_since_collection_1")
    private String patientSampleMonthsSinceCollection1;
    private String patientSampleVirologyStatus;
    private String patientSampleSharable;
    private String patientSampleTreatedAtCollection;
    private String patientSampleTreatedPriorToCollection;
    private String pdxModelPublications;
    private String treatmentList;
    private String modelTreatmentList;
    private String scores;
}
