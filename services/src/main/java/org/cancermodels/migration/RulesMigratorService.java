package org.cancermodels.migration;

import java.util.List;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntityRepository;
import org.springframework.stereotype.Service;

// Migrate old rules to new format
@Service
public class RulesMigratorService {

  private final OldRulesReader oldRulesReader;
  private final MappingEntityRepository mappingEntityRepository;

  public RulesMigratorService(OldRulesReader oldRulesReader,
      MappingEntityRepository mappingEntityRepository) {
    this.oldRulesReader = oldRulesReader;
    this.mappingEntityRepository = mappingEntityRepository;
  }

  public void loadOldRulesInDb() {
    mappingEntityRepository.deleteAll();
    List<MappingEntity> diagnosisRules = oldRulesReader.readRules("diagnosis_mappings.json");
    List<MappingEntity> treatmentRules = oldRulesReader.readRules("treatment_mappings.json");
    mappingEntityRepository.saveAll(diagnosisRules);
    mappingEntityRepository.saveAll(treatmentRules);
  }
}
