insert into ENTITY_TYPE (id, mapping_rules_file_name) values (1, 'diagnosis_mappings.json', 'diagnosis');
insert into ENTITY_TYPE (id, mapping_rules_file_name) values (2, 'treatment_mappings.json', 'treatment');

insert into MAPPING_KEY (id, key, weight, entity_type_id) values (1, 'DataSource', 0, 1);
insert into MAPPING_KEY (id, key, weight, entity_type_id) values (2, 'SampleDiagnosis', 0.9, 1);
insert into MAPPING_KEY (id, key, weight, entity_type_id) values (3, 'OriginTissue', 0.08, 1);
insert into MAPPING_KEY (id, key, weight, entity_type_id) values (4, 'TumourType', 0.02, 1);

insert into MAPPING_KEY (id, key, weight, entity_type_id) values (5, 'DataSource', 0, 2);
insert into MAPPING_KEY (id, key, weight, entity_type_id) values (6, 'TreatmentName', 1, 2);