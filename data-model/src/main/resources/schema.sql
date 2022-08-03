DROP TABLE mapping_value cascade;
DROP TABLE entity_type cascade;
DROP TABLE mapping_entity cascade;
DROP TABLE mapping_key cascade;

CREATE TABLE entity_type (
    id INTEGER NOT NULL,
    name VARCHAR2,
    mapping_rules_file_name VARCHAR2
);

ALTER TABLE entity_type ADD CONSTRAINT pk_entity_type PRIMARY KEY (id);

CREATE TABLE mapping_key (
    id INTEGER NOT NULL,
    entity_type_id INTEGER,
    key VARCHAR2,
    weight DECIMAL,
    search_on_ontology BOOLEAN,
    search_on_ontology_position INTEGER
);

ALTER TABLE mapping_key ADD CONSTRAINT pk_mapping_key PRIMARY KEY (id);

ALTER TABLE mapping_key
    ADD CONSTRAINT fk_mapping_key_entity_type
    FOREIGN KEY (entity_type_id)
    REFERENCES entity_type (id);

ALTER TABLE mapping_key
ADD CONSTRAINT uc_mapping_key UNIQUE (entity_type_id, key);

CREATE TABLE mapping_entity (
    id INTEGER NOT NULL,
    entity_type_id INTEGER,
    mapped_term_label VARCHAR2,
    mapped_term_url VARCHAR2,
    status VARCHAR2,
    date_created TIMESTAMP,
    date_updated TIMESTAMP
);

ALTER TABLE mapping_entity ADD CONSTRAINT pk_mapping_entity PRIMARY KEY (id);

CREATE TABLE mapping_value (
    id INTEGER NOT NULL,
    mapping_entity_id INTEGER,
    key_id INTEGER,
    value CLOB
);

ALTER TABLE mapping_value ADD CONSTRAINT pk_mapping_value PRIMARY KEY (id);

ALTER TABLE mapping_value
    ADD CONSTRAINT fk_mapping_value_mapping_entity
    FOREIGN KEY (mapping_entity_id)
    REFERENCES mapping_entity (id) ON DELETE CASCADE;

-- Cannot add index to clob
-- ALTER TABLE mapping_value
-- ADD CONSTRAINT uc_mapping_value UNIQUE (key_id, value);

CREATE TABLE mapping_entity_suggestion (
    id INTEGER NOT NULL,
    mapping_entity_id INTEGER,
    suggested_mapping_entity_id INTEGER,
    score NUMERIC(4,2)
);

ALTER TABLE mapping_entity_suggestion ADD CONSTRAINT pk_mapping_entity_suggestion PRIMARY KEY (id);

ALTER TABLE mapping_entity_suggestion
    ADD CONSTRAINT fk_mapping_entity_suggestion_mapping_entity_01
    FOREIGN KEY (mapping_entity_id)
    REFERENCES mapping_entity (id) ON DELETE CASCADE;

ALTER TABLE mapping_entity_suggestion
    ADD CONSTRAINT fk_mapping_entity_suggestion_mapping_entity_02
    FOREIGN KEY (suggested_mapping_entity_id)
    REFERENCES mapping_entity (id) ON DELETE CASCADE;

CREATE TABLE ontology_term (
    id INTEGER NOT NULL,
    description CLOB,
    label VARCHAR2,
    type VARCHAR2,
    url VARCHAR2
);

ALTER TABLE ontology_term ADD CONSTRAINT pk_ontology_term PRIMARY KEY (id);

CREATE TABLE ontology_term_synomyms (
    ontology_term_id INTEGER NOT NULL,
    synonyms VARCHAR2
);

ALTER TABLE ontology_term_synomyms
    ADD CONSTRAINT fk_ontology_term_synomyms_ontology_term
    FOREIGN KEY (ontology_term_id)
    REFERENCES ontology_term (id);

CREATE TABLE ontology_load_report (
    id INTEGER NOT NULL,
    error_message CLOB,
    loading_date_time TIMESTAMP,
    number_diagnosis_terms  INTEGER NOT NULL,
    number_treatment_terms  INTEGER NOT NULL,
    number_regimen_terms  INTEGER NOT NULL
);

CREATE TABLE unprocessed_ontology_url (
    id INTEGER NOT NULL,
    URL VARCHAR2,
    TYPE VARCHAR2,
    attempts  INTEGER NOT NULL,
    error_message  VARCHAR2,
    note  VARCHAR2,
    raw_term_url VARCHAR2
);
