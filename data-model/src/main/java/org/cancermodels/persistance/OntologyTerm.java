package org.cancermodels.persistance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @Lob
    @ElementCollection
    private Set<String> synonyms;

    @NonNull
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    private String description;

}
