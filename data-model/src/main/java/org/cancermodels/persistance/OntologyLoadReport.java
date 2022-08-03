package org.cancermodels.persistance;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

public class OntologyLoadReport {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private LocalDateTime loadingDateTime;
    private int numberDiagnosisTerms;
    private int numberTreatmentTerms;
    private int numberRegimenTerms;

    // Used to inform the user about any error in the last reloading process
    @Lob
    private String errorMessage;
}
