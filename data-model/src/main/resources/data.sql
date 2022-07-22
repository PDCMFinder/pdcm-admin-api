insert into ENTITY_TYPE (id, mapping_rules_file_name, name) values (1, 'diagnosis_mappings.json', 'diagnosis');
insert into ENTITY_TYPE (id, mapping_rules_file_name, name) values (2, 'treatment_mappings.json', 'treatment');

insert into MAPPING_KEY (id, key, weight, entity_type_id, search_on_ontology, search_on_ontology_position)
values (1, 'DataSource', 0, 1, false, null);
insert into MAPPING_KEY (id, key, weight, entity_type_id, search_on_ontology, search_on_ontology_position)
values (2, 'SampleDiagnosis', 0.9, 1, true, 1);
insert into MAPPING_KEY (id, key, weight, entity_type_id, search_on_ontology, search_on_ontology_position)
values (3, 'OriginTissue', 0.08, 1, true, 0);
insert into MAPPING_KEY (id, key, weight, entity_type_id, search_on_ontology, search_on_ontology_position)
values (4, 'TumourType', 0.02, 1, false, null);

insert into MAPPING_KEY (id, key, weight, entity_type_id, search_on_ontology, search_on_ontology_position)
values (5, 'DataSource', 0, 2, false, null);
insert into MAPPING_KEY (id, key, weight, entity_type_id, search_on_ontology, search_on_ontology_position)
 values (6, 'TreatmentName', 1, 2, true, 0);