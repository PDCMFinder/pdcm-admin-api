package org.cancermodels.mappings.suggestions;

import org.cancer_models.entity2ontology.common.model.TargetEntity;
import org.cancer_models.entity2ontology.common.model.TargetEntityDataFields;
import org.cancermodels.pdcm_admin.persistance.*;
import org.cancermodels.pdcm_admin.types.Source;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Component
public class E2oSuggestionMapper {

    private final MappingEntityRepository mappingEntityRepository;
    private final OntologyTermRepository ontologyTermRepository;

    public E2oSuggestionMapper(MappingEntityRepository mappingEntityRepository, OntologyTermRepository ontologyTermRepository) {
        this.mappingEntityRepository = mappingEntityRepository;
        this.ontologyTermRepository = ontologyTermRepository;
    }

    Suggestion e2oSuggestionToSuggestion(org.cancer_models.entity2ontology.map.model.Suggestion e2oSuggestion) {
        Suggestion suggestion = new Suggestion();
        String targetEntityType = e2oSuggestion.getTargetEntity().targetType().getValue().toLowerCase();
        String sourceType = targetEntityType.equals("rule") ? Source.RULE.getLabel() : Source.ONTOLOGY.getLabel();
        suggestion.setSourceType(sourceType);
        suggestion.setScore(e2oSuggestion.getRawScore());
        suggestion.setRelativeScore(e2oSuggestion.getScore());
        suggestion.setSuggestedTermUrl(e2oSuggestion.getTermUrl());
        suggestion.setSuggestedTermLabel(e2oSuggestion.getTermLabel());

        // If the suggestion represents a rule, we need to attach the mapping entity it represents
        if (e2oSuggestion.getTargetEntity().targetType().getValue().equalsIgnoreCase(Source.RULE.getLabel())) {
            String mappingEntityKey = e2oSuggestion.getTargetEntity().id();
            Optional<MappingEntity> optSuggestedMappingEntity = mappingEntityRepository.findByMappingKey(mappingEntityKey);
            optSuggestedMappingEntity.ifPresent(suggestion::setMappingEntity);
        } else {
            // We need to extract the ontology term information
            String ontologyTermKey = e2oSuggestion.getTargetEntity().id();
            OntologyTerm ontologyTerm;
            Optional<OntologyTerm> optionalOntologyTerm = ontologyTermRepository.findByKey(ontologyTermKey);
            ontologyTerm = optionalOntologyTerm.orElseGet(() -> createOntologyTerm(e2oSuggestion));
            ontologyTermRepository.save(ontologyTerm);
            suggestion.setOntologyTerm(ontologyTerm);
        }
        return suggestion;
    }

    private OntologyTerm createOntologyTerm(org.cancer_models.entity2ontology.map.model.Suggestion e2oSuggestion) {
        String label = null;
        String description = null;
        List<String> synonyms = null;
        TargetEntity targetEntity = e2oSuggestion.getTargetEntity();
        TargetEntityDataFields dataFields = targetEntity.dataFields();
        if (dataFields.hasStringField("label")) {
            label = dataFields.getStringField("label");
        }
        if (dataFields.hasStringField("description")) {
            description = dataFields.getStringField("description");
        }
        if (dataFields.hasListField("synonyms")) {
            synonyms = dataFields.getListField("synonyms");
        }
        OntologyTerm ontologyTerm = new OntologyTerm();
        ontologyTerm.setType(targetEntity.targetType().getValue());
        ontologyTerm.setKey(targetEntity.id());
        assert label != null;
        ontologyTerm.setLabel(label);
        ontologyTerm.setUrl(e2oSuggestion.getTermUrl());
        assert description != null;
        ontologyTerm.setDescription(description);
        if (synonyms != null) {
            ontologyTerm.setSynonyms(new HashSet<>(synonyms));
        }
        return ontologyTerm;
    }
}
