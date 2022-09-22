insert into ENTITY_TYPE (id, mapping_rules_file_name, name) values (1, 'diagnosis_mappings.json', 'diagnosis');
insert into ENTITY_TYPE (id, mapping_rules_file_name, name) values (2, 'treatment_mappings.json', 'treatment');

insert into MAPPING_KEY (id, key, entity_type_id) values (1, 'DataSource', 1);
insert into MAPPING_KEY (id, key, entity_type_id) values (2, 'SampleDiagnosis', 1);
insert into MAPPING_KEY (id, key, entity_type_id) values (3, 'OriginTissue', 1);
insert into MAPPING_KEY (id, key, entity_type_id) values (4, 'TumorType', 1);

insert into MAPPING_KEY (id, key, entity_type_id) values (5, 'DataSource', 2);
insert into MAPPING_KEY (id, key, entity_type_id) values (6, 'TreatmentName', 2);


insert into KEY_SEARCH_CONFIGURATION
(ID, MAIN_FIELD, SEARCH_ON_ONTOLOGY, MULTI_FIELD_QUERY, WEIGHT, KEY_ID)
values
(1, false, false, false, 0, 1);

insert into KEY_SEARCH_CONFIGURATION
(ID, MAIN_FIELD, SEARCH_ON_ONTOLOGY, MULTI_FIELD_QUERY, WEIGHT, KEY_ID)
values
(2, true, true, true, 1, 2);

insert into KEY_SEARCH_CONFIGURATION
(ID, MAIN_FIELD, SEARCH_ON_ONTOLOGY, MULTI_FIELD_QUERY, WEIGHT, KEY_ID)
values
(3, false, true, true, 0.5, 3);

insert into KEY_SEARCH_CONFIGURATION
(ID, MAIN_FIELD, SEARCH_ON_ONTOLOGY, MULTI_FIELD_QUERY, WEIGHT, KEY_ID)
values
(4, false,  false, false, 0.1, 4);

insert into KEY_SEARCH_CONFIGURATION
(ID, MAIN_FIELD, SEARCH_ON_ONTOLOGY, MULTI_FIELD_QUERY, WEIGHT, KEY_ID)
values
(5, false, false, false, 0, 5);

insert into KEY_SEARCH_CONFIGURATION
(ID, MAIN_FIELD, SEARCH_ON_ONTOLOGY, MULTI_FIELD_QUERY, WEIGHT, KEY_ID)
values
(6, true, true, false, 1.3, 6);
