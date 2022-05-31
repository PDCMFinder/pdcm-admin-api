DROP TABLE mapping_value cascade;
DROP TABLE entity_type_by_project cascade;
DROP TABLE mapping_entity cascade;
DROP TABLE mapping_key cascade;
DROP TABLE project cascade;

CREATE TABLE project (
    id INTEGER NOT NULL,
    name VARCHAR2
);

ALTER TABLE project ADD CONSTRAINT pk_PROJECT PRIMARY KEY (id);

CREATE TABLE entity_type_by_project (
    id INTEGER NOT NULL,
    name VARCHAR2,
    project_id INTEGER
);

ALTER TABLE entity_type_by_project ADD CONSTRAINT pk_entity_type_by_project PRIMARY KEY (id);

ALTER TABLE entity_type_by_project
    ADD CONSTRAINT fk_entity_type_by_project_project
    FOREIGN KEY (project_id)
    REFERENCES project (id);

ALTER TABLE entity_type_by_project
ADD CONSTRAINT uc_entity_type_by_project UNIQUE (name,project_id);

CREATE TABLE mapping_key (
    id INTEGER NOT NULL,
    entity_type_by_project_id INTEGER,
    key VARCHAR2
);

ALTER TABLE mapping_key ADD CONSTRAINT pk_mapping_key PRIMARY KEY (id);

ALTER TABLE mapping_key
    ADD CONSTRAINT fk_mapping_key_entity_type_by_project
    FOREIGN KEY (entity_type_by_project_id)
    REFERENCES entity_type_by_project (id);

ALTER TABLE mapping_key
ADD CONSTRAINT uc_mapping_key UNIQUE (entity_type_by_project_id, key);

CREATE TABLE mapping_entity (
    id INTEGER NOT NULL,
    entity_type_by_project_id INTEGER,
    mapped_term_label VARCHAR2,
    mapped_term_url VARCHAR2,
    status VARCHAR2
);

ALTER TABLE mapping_entity ADD CONSTRAINT pk_mapping_entity PRIMARY KEY (id);

CREATE TABLE mapping_value (
    id INTEGER NOT NULL,
    mapping_entity_id INTEGER,
    key_id INTEGER,
    value VARCHAR2
);

ALTER TABLE mapping_value ADD CONSTRAINT pk_mapping_value PRIMARY KEY (id);

ALTER TABLE mapping_value
ADD CONSTRAINT uc_mapping_value UNIQUE (key_id, value);
