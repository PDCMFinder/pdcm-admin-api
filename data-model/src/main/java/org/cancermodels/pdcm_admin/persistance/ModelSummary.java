package org.cancermodels.pdcm_admin.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

/**
 * A reduced representation of a model as stored in the search_index table.
 */
@Entity
@Data
@NoArgsConstructor
public class ModelSummary {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_seq_gen")
  @SequenceGenerator(name = "hibernate_seq_gen", sequenceName = "hibernate_sequence", allocationSize = 1)
  @JsonIgnore
  private Integer id;

  @Transient
  @JsonIgnore
  private Long pdcmModelId;

  @ManyToOne
  @JsonIgnore
  @JoinColumn(name = "release_id", nullable = false)
  private Release release;

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
  private String qualityAssurance;
  private String xenograftModelSpecimens;
  private String treatmentList;
  private String modelTreatmentList;
  private String scores;
  private Boolean paediatric;
}
