package org.cancermodels.mappings;

import java.time.LocalDateTime;
import org.cancermodels.pdcm_admin.types.MappingType;
import org.cancermodels.pdcm_admin.types.Status;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.pdcm_admin.persistance.MappingEntityRepository;
import org.springframework.stereotype.Component;

/**
 * Manages the changes in a Mapping entity, like for example the transition between status.
 */
@Component
public class MappingEntityUpdater {

  private final MappingEntityRepository mappingEntityRepository;

  public MappingEntityUpdater(MappingEntityRepository mappingEntityRepository) {
    this.mappingEntityRepository = mappingEntityRepository;
  }

  /**
   * Updates a Mapping Entity ({@code original}).
   * @param original The original {@link MappingEntity} to be updated (db version).
   * @param withChanges The {@link MappingEntity} with the changes.
   * @param mappingType The way the mapping is done (automatic, manual) in case
   *                    we are updating mapping term/url.
   * @return The {@link MappingEntity} object after being saved into the db.
   */
  public MappingEntity update(
      MappingEntity original, MappingEntity withChanges, MappingType mappingType) {
    boolean mappedTermChanged = processMappedTermChange(original, withChanges, mappingType);
    boolean statusChanged = processStatusChange(original, withChanges, mappingType);

    if (mappedTermChanged || statusChanged) {
      original.setDateUpdated(LocalDateTime.now());
      mappingEntityRepository.save(original);
    }

    return original;
  }

  private boolean processMappedTermChange(MappingEntity original, MappingEntity withChanges,
      MappingType mappingType) {
    boolean changed = false;
    String originalMappedTermUrl =
        original.getMappedTermUrl() == null ? "" : original.getMappedTermUrl() ;
    String newMappedTermUrl =
        withChanges.getMappedTermUrl() == null ? "" : withChanges.getMappedTermUrl() ;

    if (!originalMappedTermUrl.equalsIgnoreCase(newMappedTermUrl) ) {
      original.setMappedTermUrl(newMappedTermUrl);
      original.setSource(withChanges.getSource());
      String newMappedTermLabel = withChanges.getMappedTermLabel();
      original.setMappedTermLabel(newMappedTermLabel);
      original.setMappingType(mappingType.getLabel());
      changed = true;
    }

    // A new mapping was generated
    if (originalMappedTermUrl.equals("") && !newMappedTermUrl.equals("")) {
      // Simulate a request of status change
      withChanges.setStatus(Status.MAPPED.getLabel());
    }

    return changed;
  }

  /**
   * Checks that if status changed the transitions are valid:
   *  - Unmapped -> Mapped
   *  - Unmapped -> Request
   *  - Unmapped -> Review (only if automatic)
   *  - Mapped -> Review
   *  - Review -> Mapped
   *  - Review -> Request
   *  - Request -> Unmapped
   *  @param original Mapping Entity it is in the database.
   * @param withChanges Edited Mapping entity.
   */
  private boolean processStatusChange(MappingEntity original, MappingEntity withChanges, MappingType mappingType) {
    boolean changed = false;
    String originalStatus = original.getStatus();
    String newStatus = withChanges.getStatus();
    boolean valid = false;
    if (!originalStatus.equalsIgnoreCase(newStatus)) {
      changed = true;

      /* Valid transitions */

      // Unmapped -> Mapped
      if (Status.UNMAPPED.getLabel().equalsIgnoreCase(originalStatus)
          && Status.MAPPED.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
      }
      // Unmapped -> Request
      else if (Status.UNMAPPED.getLabel().equalsIgnoreCase(originalStatus)
          && Status.REQUEST.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
      }
      // Unmapped -> Review (only if automatic)
      else if (Status.UNMAPPED.getLabel().equalsIgnoreCase(originalStatus)
          && Status.REVIEW.getLabel().equalsIgnoreCase(newStatus)
          && mappingType.equals(MappingType.AUTOMATIC_REVIEW)) {
        valid = true;
      }
      // Mapped -> Review
      else if (Status.MAPPED.getLabel().equalsIgnoreCase(originalStatus)
          && Status.REVIEW.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
      }
      // Review -> Mapped
      else if (Status.REVIEW.getLabel().equalsIgnoreCase(originalStatus)
          && Status.MAPPED.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
      }
      // Review -> Request
      else if (Status.REVIEW.getLabel().equalsIgnoreCase(originalStatus)
          && Status.REQUEST.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
      }
      // Review -> Unmapped
      else if (Status.REVIEW.getLabel().equalsIgnoreCase(originalStatus)
          && Status.UNMAPPED.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
      }
      // Request -> Unmapped
      else if (Status.REQUEST.getLabel().equalsIgnoreCase(originalStatus)
          && Status.UNMAPPED.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
        original.setMappedTermUrl(null);
        original.setMappedTermLabel(null);
        original.setSource(null);
        original.setMappingType(null);
      }
      original.setStatus(newStatus);

      // Status did not change
    } else {
      valid = true;
    }

    if (!valid) {
      // TODO: change to specific exception
      throw new IllegalArgumentException(
          String.format(
              "Cannot change status from [%s] to [%s] (method: %s)", originalStatus, newStatus, mappingType.getLabel()));
    }
    return changed;
  }

}
