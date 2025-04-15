insert into admin_app.ENTITY_TYPE (id, mapping_rules_file_name, name) values (1, 'diagnosis_mappings.json', 'diagnosis');
insert into admin_app.ENTITY_TYPE (id, mapping_rules_file_name, name) values (2, 'treatment_mappings.json', 'treatment');

insert into admin_app.MAPPING_KEY (id, key, entity_type_id) values (1, 'DataSource', 1);
insert into admin_app.MAPPING_KEY (id, key, entity_type_id) values (2, 'SampleDiagnosis', 1);
insert into admin_app.MAPPING_KEY (id, key, entity_type_id) values (3, 'OriginTissue', 1);
insert into admin_app.MAPPING_KEY (id, key, entity_type_id) values (4, 'TumorType', 1);

insert into admin_app.MAPPING_KEY (id, key, entity_type_id) values (5, 'DataSource', 2);
insert into admin_app.MAPPING_KEY (id, key, entity_type_id) values (6, 'TreatmentName', 2);

