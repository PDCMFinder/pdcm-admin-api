package org.cancermodels.mappings;

import java.time.LocalDateTime;
import org.cancermodels.Status;
import org.cancermodels.persistance.MappingEntity;
import org.cancermodels.persistance.MappingEntityRepository;
import org.springframework.stereotype.Component;

@Component
public class Updater {

  private final MappingEntityRepository mappingEntityRepository;

  public Updater(MappingEntityRepository mappingEntityRepository) {
    this.mappingEntityRepository = mappingEntityRepository;
  }


  public MappingEntity update(MappingEntity original, MappingEntity withChanges) {
    boolean statusChanged = checkStatusChange(original, withChanges);

    if (statusChanged) {
      original.setStatus(withChanges.getStatus());
      original.setDateUpdated(LocalDateTime.now());
      mappingEntityRepository.save(original);
    }

    return original;
  }

  /**
   * Checks that if status changed the transitions are valid:
   *  - Unmapped -> Mapped
   *  - Unmapped -> Request
   *  - Mapped -> Revise
   *  - Revise -> Mapped
   *  - Request -> Mapped
   *
   * @param original Mapping Entity it is in the database.
   * @param withChanges Edited Mapping entity.
   */
  private boolean checkStatusChange(MappingEntity original, MappingEntity withChanges) {
    boolean changed = false;
    String originalStatus = original.getStatus();
    String newStatus = withChanges.getStatus();
    boolean valid = false;
    if (!originalStatus.equalsIgnoreCase(newStatus)) {
      changed = true;
      // Valid transitions
      if (Status.UNMAPPED.getLabel().equalsIgnoreCase(originalStatus)
          && Status.MAPPED.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
      }
      else if (Status.UNMAPPED.getLabel().equalsIgnoreCase(originalStatus)
          && Status.REQUEST.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
      }
      else if (Status.MAPPED.getLabel().equalsIgnoreCase(originalStatus)
          && Status.REVISE.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
      }
      else if (Status.REVISE.getLabel().equalsIgnoreCase(originalStatus)
          && Status.MAPPED.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
      }
      else if (Status.REQUEST.getLabel().equalsIgnoreCase(originalStatus)
          && Status.MAPPED.getLabel().equalsIgnoreCase(newStatus)) {
        valid = true;
      }
      // Status did not change
    } else {
      valid = true;
    }

    if (!valid) {
      // TODO: change to specific exception
      throw new IllegalArgumentException(
          String.format( "Cannot change status from [%s] to [%s]", originalStatus, newStatus));
    }
    return changed;
  }
}
