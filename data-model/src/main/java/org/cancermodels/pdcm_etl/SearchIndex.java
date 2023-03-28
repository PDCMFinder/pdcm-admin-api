package org.cancermodels.pdcm_etl;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Table(name ="search_index", schema = "pdcm_api")
public class SearchIndex {
    @Id
    private Long pdcmModelId;

    private String externalModelId;

    private String modelType;

    private String dataSource;
}
