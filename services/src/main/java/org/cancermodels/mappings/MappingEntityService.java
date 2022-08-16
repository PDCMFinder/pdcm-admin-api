package org.cancermodels.mappings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.cancermodels.Status;
import org.cancermodels.persistance.EntityType;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingEntityRepository;
import org.cancermodels.mappings.MappingSummaryByTypeAndProvider.SummaryEntry;
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
  private final Updater updater;

  public MappingEntityService(MappingEntityRepository mappingEntityRepository,
      EntityTypeService entityTypeService,
      SuggestionManager suggestionManager, Updater updater) {
    this.mappingEntityRepository = mappingEntityRepository;
    this.entityTypeService = entityTypeService;
    this.suggestionManager = suggestionManager;
    this.updater = updater;
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

    String mappedKey = Status.MAPPED.getLabel();
    String unmappedKey = Status.UNMAPPED.getLabel();

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
    var res = mappingEntityRepository.findById(id);
    MappingEntity mappingEntity = null;
    if (res.isPresent())
    {
      mappingEntity = res.get();
      List<MappingEntity> mappingEntities = new ArrayList<>();
      mappingEntities.add(mappingEntity);
//      suggestionManager.calculateSuggestions(mappingEntities);
//      mappingEntityRepository.save(mappingEntity);
    }
    return Optional.of(mappingEntity);
  }

  /**
   * Sets the suggestions by rules and by ontologies for all the mapping entities in the system
   */
  public void setMappingSuggestions() {
    Map<String, List<MappingEntity>> mappingEntitiesMappedByType = getMappingEntitiesMappedByType();
    for (String type : mappingEntitiesMappedByType.keySet()) {
      // Process all mappings
      List<MappingEntity> toProcess = mappingEntitiesMappedByType.get(type);
      suggestionManager.calculateSuggestions(toProcess);
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

  /**
   * Updates some values in a mapping entity, if changed: Status, Mapping Term Label, Mapping Term Url
   * @param mappingEntity Entity with the new information
   * @return Mapping after it was updated
   */
  public Optional<MappingEntity> update(int id, MappingEntity mappingEntity) {
    var res = mappingEntityRepository.findById(id);
    if (res.isPresent()) {
      MappingEntity original = res.get();
      return Optional.of(updater.update(original, mappingEntity)) ;
    } else {
      return Optional.empty();
    }
  }

}
