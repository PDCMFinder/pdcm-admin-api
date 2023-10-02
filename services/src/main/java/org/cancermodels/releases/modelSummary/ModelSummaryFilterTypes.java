package org.cancermodels.releases.modelSummary;

public enum ModelSummaryFilterTypes {
    RELEASE_ID("release_id"),
    MODEL_TYPE("model_type"),
    DATA_SOURCE("data_source"),
    PROJECT("project_name");
    private final String name;

    ModelSummaryFilterTypes(String name)
    {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
