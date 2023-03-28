package org.cancermodels.mappings.search;

import java.sql.Clob;
import java.sql.SQLException;
import java.util.*;

import org.cancermodels.pdcm_admin.types.Status;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.pdcm_admin.persistance.MappingEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/** Provides the logic to search Mapping Entities in the system. */
@Service
public class SearchService {

  private final MappingEntityRepository mappingEntityRepository;

  public SearchService(MappingEntityRepository mappingEntityRepository) {
    this.mappingEntityRepository = mappingEntityRepository;
  }

  /**
   * Search {@code Mapping Entities} using filters.
   * @param pageable Request page information.
   * @param mappingsFilter Filters to apply in the search.
   * @return A list of {@link MappingEntity}, paginated.
   */
  public Page<MappingEntity> search(Pageable pageable, MappingsFilter mappingsFilter) {

    Specification<MappingEntity> specs = buildSpecifications(mappingsFilter);
    return mappingEntityRepository.findAll(specs, pageable);
  }

  /**
   * Counts the number of elements by status after apply filters.
   * @param filter Filter to apply in the query.
   * @return Map with the name of the status as key and the count as value.
   */
  public Map<String, Long> countStatusWithFilter(MappingsFilter filter) {
    Map<String, Long> counts = new HashMap<>();
    Specification<MappingEntity> specs = buildSpecifications(filter);

    for (Status status : Status.values()) {
      counts.put(status.getLabel(), getCountByStatus(status.getLabel(), specs));
    }
    return counts;
  }

  private long getCountByStatus(String status, Specification<MappingEntity> specs) {
    Specification<MappingEntity> specsByStatus= specs.and(MappingsSpecs.withStatus(
        Collections.singletonList(status)));
    return mappingEntityRepository.count(specsByStatus);

  }

  public List<String> getAllTreatmentsAndDiagnosis() {
    List<String> treatmentsAndDiagnosis = new ArrayList<>();
    List<Object[]> list = mappingEntityRepository.getAllTreatmentsAndDiagnosis();
    for (Object[] row : list) {
      Clob clob = (Clob)row[0];
      try {
        String value = clob.getSubString(1, (int) clob.length());
        treatmentsAndDiagnosis.add(value);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
    return treatmentsAndDiagnosis;
  }


  private Specification<MappingEntity> buildSpecifications(MappingsFilter mappingsFilter) {
    return Specification.where(
        MappingsSpecs.withStatus(mappingsFilter.getStatus())
            .and(MappingsSpecs.withMappingType(mappingsFilter.getMappingTypes()))
            .and(MappingsSpecs.withLabel(mappingsFilter.getLabels()))
            .and(MappingsSpecs.withMappingQuery(mappingsFilter.getMappingQuery())
            .and(MappingsSpecs.withEntityTypeNames(mappingsFilter.getEntityTypeNames())))
        );
  }
}
