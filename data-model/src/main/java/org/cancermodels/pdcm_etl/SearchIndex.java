package org.cancermodels.pdcm_etl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity

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
    private Boolean paediatric;
}
