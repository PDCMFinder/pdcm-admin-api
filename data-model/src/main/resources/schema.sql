CREATE TABLE admin_app.entity_type (
    id INTEGER NOT NULL,
    name TEXT,
    mapping_rules_file_name TEXT
);

ALTER TABLE admin_app.entity_type ADD CONSTRAINT pk_entity_type PRIMARY KEY (id);

CREATE TABLE admin_app.mapping_key (
    id INTEGER NOT NULL,
    entity_type_id INTEGER,
    key TEXT
);

ALTER TABLE admin_app.mapping_key ADD CONSTRAINT pk_mapping_key PRIMARY KEY (id);

ALTER TABLE admin_app.mapping_key
    ADD CONSTRAINT fk_mapping_key_entity_type
    FOREIGN KEY (entity_type_id)
    REFERENCES admin_app.entity_type (id);

ALTER TABLE admin_app.mapping_key
ADD CONSTRAINT uc_mapping_key UNIQUE (entity_type_id, key);

CREATE TABLE admin_app.mapping_entity (
    id INTEGER NOT NULL,
    mapping_key TEXT NOT NULL,
    entity_type_id INTEGER,
    mapped_term_label TEXT,
    mapped_term_url TEXT,
    status TEXT,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    mapping_type TEXT,
    source TEXT
);

ALTER TABLE admin_app.mapping_entity ADD CONSTRAINT pk_mapping_entity PRIMARY KEY (id);

CREATE TABLE admin_app.mapping_value (
    id INTEGER NOT NULL,
    mapping_entity_id INTEGER,
    key_id INTEGER,
    value TEXT
);

ALTER TABLE admin_app.mapping_value ADD CONSTRAINT pk_mapping_value PRIMARY KEY (id);

ALTER TABLE admin_app.mapping_value
    ADD CONSTRAINT fk_mapping_value_mapping_entity
    FOREIGN KEY (mapping_entity_id)
    REFERENCES admin_app.mapping_entity (id) ON DELETE CASCADE;


ALTER TABLE admin_app.mapping_value
ADD CONSTRAINT uc_mapping_value UNIQUE (mapping_entity_id, key_id);

CREATE TABLE admin_app.suggestion (
    id INTEGER NOT NULL,
    relative_score NUMERIC NOT NULL,
    score NUMERIC NOT NULL,
    source_type TEXT,
    suggested_term_label TEXT,
    suggested_term_url TEXT,
    suggested_mapping_entity_id INTEGER,
    mapping_entity_id  INTEGER NOT NULL
);

ALTER TABLE admin_app.suggestion ADD CONSTRAINT pk_suggestion PRIMARY KEY (id);

ALTER TABLE admin_app.suggestion
    ADD CONSTRAINT fk_suggestion_mapping_entity_01
    FOREIGN KEY (suggested_mapping_entity_id)
    REFERENCES admin_app.mapping_entity (id);

ALTER TABLE admin_app.suggestion
    ADD CONSTRAINT fk_suggestion_mapping_entity_02
    FOREIGN KEY (mapping_entity_id)
    REFERENCES admin_app.mapping_entity (id);

CREATE TABLE admin_app.process_report (
    id INTEGER NOT NULL,
    module TEXT,
    attribute TEXT,
    value TEXT,
    date TIMESTAMP
);

ALTER TABLE admin_app.process_report ADD CONSTRAINT pk_process_report PRIMARY KEY (id);

-- Views
CREATE VIEW admin_app.diagnosis_data_vw AS (
    SELECT
        mapping_key,
        (SELECT value FROM admin_app.mapping_value WHERE mapping_entity_id=me.id AND key_id = 1) AS data_source,
        (SELECT value FROM admin_app.mapping_value WHERE mapping_entity_id=me.id AND key_id = 2) AS diagnosis,
        (SELECT value FROM admin_app.mapping_value WHERE mapping_entity_id=me.id AND key_id = 3) AS primary_tissue,
        (SELECT value FROM admin_app.mapping_value WHERE mapping_entity_id=me.id AND key_id = 4) AS tumor_type,
        mapped_term_label,
        mapped_term_url,
        status,
        source
    FROM admin_app.MAPPING_ENTITY me
    WHERE entity_type_id = 1
);

CREATE VIEW admin_app.treatment_data_vw AS (
     SELECT
         mapping_key,
         (SELECT value FROM admin_app.mapping_value WHERE mapping_entity_id=me.id AND key_id = 5) AS data_source,
         (SELECT value FROM admin_app.mapping_value WHERE mapping_entity_id=me.id AND key_id = 6) AS treatment_name,
         mapped_term_label,
         mapped_term_url,
         status,
         source
     FROM admin_app.MAPPING_ENTITY me
     WHERE entity_type_id = 2
);

-- Permissions
GRANT ALL
    ON ALL TABLES
    IN SCHEMA admin_app TO k8spdcmapiro;
