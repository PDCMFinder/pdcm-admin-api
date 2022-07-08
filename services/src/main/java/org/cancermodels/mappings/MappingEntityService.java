package org.cancermodels.mappings;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.cancermodels.EntityType;
import org.cancermodels.MappingEntity;
import org.cancermodels.MappingEntityRepository;
import org.cancermodels.MappingEntityStatus;
import org.cancermodels.mappings.MappingSummaryByTypeAndProvider.SummaryEntry;
import org.cancermodels.mappings.automatic.AutomaticMappingManager;
import org.cancermodels.mappings.search.MappingsFilter;
import org.cancermodels.mappings.search.MappingsSpecs;
import org.cancermodels.mappings.suggestions.SuggestionManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class MappingEntityService {

  private final MappingEntityRepository mappingEntityRepository;
  private final EntityTypeService entityTypeService;

  private final SuggestionManager suggestionManager;
  private final AutomaticMappingManager automaticMappingManager;

  public MappingEntityService(MappingEntityRepository mappingEntityRepository,
      EntityTypeService entityTypeService,
      SuggestionManager suggestionManager,
      AutomaticMappingManager automaticMappingManager) {
    this.mappingEntityRepository = mappingEntityRepository;
    this.entityTypeService = entityTypeService;
    this.suggestionManager = suggestionManager;
    this.automaticMappingManager = automaticMappingManager;
  }

  public Page<MappingEntity> findPaginatedAndFiltered(
      Pageable pageable, MappingsFilter mappingsFilter) {

    Specification<MappingEntity> specs = buildSpecifications(mappingsFilter);
    return mappingEntityRepository.findAll(specs, pageable);
  }

  private Specification<MappingEntity> buildSpecifications(MappingsFilter mappingsFilter)
  {
    Specification<MappingEntity> specifications =
        Specification.where(
            MappingsSpecs.withStatus(mappingsFilter.getStatus())
                .and(MappingsSpecs.withMappingQuery(mappingsFilter.getMappingQuery())
                .and(MappingsSpecs.withEntityTypeNames(mappingsFilter.getEntityTypeNames()))
            ));
    return specifications;
  }

  public MappingSummaryByTypeAndProvider getSummaryByTypeAndProvider(String entityTypeName) {
    MappingSummaryByTypeAndProvider mappingSummaryByTypeAndProvider = new MappingSummaryByTypeAndProvider();
    List<SummaryEntry> summaryEntries = new ArrayList<>();
    mappingSummaryByTypeAndProvider.setEntityTypeName(entityTypeName);

    Map<String, Map<String, Integer>> data = new HashMap<>();

    String mappedKey = MappingEntityStatus.MAPPED.getDescription();
    String unmappedKey = MappingEntityStatus.UNMAPPED.getDescription();

    List<Object[]> list = mappingEntityRepository.countEntityTypeStatusByProvider(entityTypeName);
    for (Object[] row : list) {
      String dataSource = row[0].toString();
      String status  = row[1].toString();
      int count = Integer.parseInt(row[2].toString());
      if (!data.containsKey(dataSource)) {
        data.put(dataSource, new HashMap<>());
      }
      data.get(dataSource).put(status, count);
    }

    for (String dataSource : data.keySet()) {
      SummaryEntry summaryEntry = new SummaryEntry();
      summaryEntry.setDataSource(dataSource);
      Map<String, Integer> countByDataSource = data.get(dataSource);

      if (countByDataSource.containsKey(mappedKey)) {
        summaryEntry.setMapped(countByDataSource.get(mappedKey));
      }
      if (countByDataSource.containsKey(unmappedKey)) {
        summaryEntry.setUnmapped(countByDataSource.get(unmappedKey));
      }
      int totalTerms = summaryEntry.getMapped() + summaryEntry.getUnmapped();
      summaryEntry.setTotalTerms(totalTerms);
      summaryEntry.setProgress(summaryEntry.getMapped()*1.0 / totalTerms );
      summaryEntries.add(summaryEntry);
    }

    mappingSummaryByTypeAndProvider.setSummaryEntries(summaryEntries);
    return mappingSummaryByTypeAndProvider;
  }

  public List<MappingEntity> getAllByTypeName(String entityTypeName) {
    return mappingEntityRepository.findAllByEntityTypeNameIgnoreCase(entityTypeName);
  }

  public Optional<MappingEntity> findById(int id) {
    return mappingEntityRepository.findById(id);
  }

  /**
   * Sets the suggestions by rules and by ontologies for all the mapping entities in the system
   */
  public void setMappingSuggestions() {
    Map<String, List<MappingEntity>> mappingEntitiesMappedByType = getMappingEntitiesMappedByType();
    suggestionManager.calculateSuggestions(mappingEntitiesMappedByType);
  }

  public void setMappingSuggestionsForOneEntity(int entityId) {
    Optional<MappingEntity> mappingEntityOptional = findById(entityId);
    if (mappingEntityOptional.isPresent())
    {
      MappingEntity mappingEntity = mappingEntityOptional.get();
      // Put entity into a map to be able to use existing method that processes a map of entities
      Map<String, List<MappingEntity>> map = new HashMap<>();
      map.put(mappingEntity.getEntityType().getName(), Arrays.asList(mappingEntity));
      suggestionManager.calculateSuggestions(map);
    }
  }

  public void setAutomaticMappings() {
    Map<String, List<MappingEntity>> mappingEntitiesMappedByType = getMappingEntitiesMappedByType();
    for (String type : mappingEntitiesMappedByType.keySet()) {
      List<MappingEntity> unmappedMappingEntities = mappingEntitiesMappedByType.get(type).stream()
          .filter(x -> x.getStatus().equals(Status.UNMAPPED.getLabel())).collect(
          Collectors.toList());
      List<MappingEntity> all = mappingEntitiesMappedByType.get(type);
      automaticMappingManager.calculateAutomaticMappings(all, type);
    }

  }

  private Map<String, List<MappingEntity>> getMappingEntitiesMappedByType() {
    Map<String, List<MappingEntity>> map = new HashMap<>();
    for (EntityType entityType : entityTypeService.getAll()) {
      String entityTypeName = entityType.getName();
      List<MappingEntity> mappingEntitiesByType = getAllByTypeName(entityTypeName);
      map.put(entityTypeName.toLowerCase(), mappingEntitiesByType);
    }
    return map;
  }

}
