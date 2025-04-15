package org.cancermodels.pdcm_admin.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(uniqueConstraints=
@UniqueConstraint(columnNames = {"url", "type"}))
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)

public class OntologyTerm {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_seq_gen")
    @SequenceGenerator(name = "hibernate_seq_gen", sequenceName = "hibernate_sequence", allocationSize = 1)
    private int id;

    @NonNull
    @EqualsAndHashCode.Include
    @ToString.Include
    private String key;

    @NonNull
    @ToString.Include
    private String url;

    @NonNull
    @ToString.Include
    private String label;

    @NonNull
    @EqualsAndHashCode.Include
    @ToString.Include
    private String type;

    @ElementCollection
    private Set<String> synonyms;

    @NonNull
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    private String description;

}
