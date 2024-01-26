package org.cancermodels.mappings;

import org.cancermodels.pdcm_admin.EntityTypeName;
import org.cancermodels.general.MappingEntityBuilder;
import org.cancermodels.pdcm_admin.persistance.MappingEntity;
import org.cancermodels.pdcm_admin.persistance.MappingEntityRepository;
import org.cancermodels.pdcm_admin.types.MappingType;
import org.cancermodels.pdcm_admin.types.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MappingEntityUpdaterTest {

    @Mock
    private MappingEntityRepository mappingEntityRepository;
    private MappingEntityUpdater instance;

    private final MappingEntityBuilder mappingEntityBuilder = new MappingEntityBuilder();

    @BeforeEach
    public void setup()
    {
        instance = new MappingEntityUpdater(mappingEntityRepository);
    }

    @Test
    void update_unchangedEntityTypeManual_saveMethodNotCalled() {
        MappingEntity original = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.UNMAPPED)
            .build();
        MappingEntity withChanges = original;
        MappingEntity result = instance.update(original, withChanges, MappingType.MANUAL);

        verify(mappingEntityRepository, never()).save(result);
        assertEquals(original.getDateUpdated(), result.getDateUpdated());
    }

    @Test
    void update_mappedTermChanged_entityUpdatedAndSaveMethodCalled() {
        MappingEntity original = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.MAPPED)
            .setMappedTermUrl("url1")
            .build();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        original.setDateUpdated(yesterday);

        MappingEntity withChanges = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.MAPPED)
            .setMappedTermUrl("url2")
            .build();
        withChanges.setDateUpdated(yesterday);

        MappingEntity result = instance.update(original, withChanges, MappingType.MANUAL);

        verify(mappingEntityRepository).save(result);
        assertTrue(result.getDateUpdated().isAfter(yesterday), "Update date didn't change");
        assertEquals(withChanges.getMappedTermUrl(), result.getMappedTermUrl());
        assertEquals(withChanges.getSource(), result.getSource());
        assertEquals(withChanges.getMappedTermLabel(), result.getMappedTermLabel());
        assertEquals(withChanges.getEntityType(), result.getEntityType());

    }

    @Test
    void update_statusChangedUnmappedToMapped_statusAndDateUpdatedAndSaveMethodCalled() {
        MappingEntity original = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.UNMAPPED)
            .build();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        original.setDateUpdated(yesterday);

        MappingEntity withChanges = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.MAPPED)
            .setMappedTermUrl("url2")
            .build();

        MappingEntity result = instance.update(original, withChanges, MappingType.MANUAL);

        verify(mappingEntityRepository).save(result);
        assertTrue(result.getDateUpdated().isAfter(yesterday), "Update date didn't change");
        assertEquals(withChanges.getMappedTermUrl(), result.getMappedTermUrl());
        assertEquals(withChanges.getSource(), result.getSource());
        assertEquals(withChanges.getMappedTermLabel(), result.getMappedTermLabel());
        assertEquals(withChanges.getEntityType(), result.getEntityType());

    }

    @Test
    void update_statusChangedUnmappedToRequest_statusAndDateUpdatedAndSaveMethodCalled() {
        MappingEntity original = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.UNMAPPED)
            .build();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        original.setDateUpdated(yesterday);

        MappingEntity withChanges = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.REQUEST)
            .build();

        MappingEntity result = instance.update(original, withChanges, MappingType.MANUAL);

        verify(mappingEntityRepository).save(result);
        assertTrue(result.getDateUpdated().isAfter(yesterday), "Update date didn't change");
        assertEquals(withChanges.getMappedTermUrl(), result.getMappedTermUrl());
        assertEquals(withChanges.getSource(), result.getSource());
        assertEquals(withChanges.getMappedTermLabel(), result.getMappedTermLabel());
        assertEquals(withChanges.getEntityType(), result.getEntityType());
    }

    @Test
    void update_statusChangedUnmappedToReviewTypeAutomatic_Review_statusAndDateUpdatedAndSaveMethodCalled() {
        MappingEntity original = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.UNMAPPED)
            .build();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        original.setDateUpdated(yesterday);

        MappingEntity withChanges = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.REVIEW)
            .build();

        MappingEntity result = instance.update(original, withChanges, MappingType.AUTOMATIC_REVIEW);

        verify(mappingEntityRepository).save(result);
        assertTrue(result.getDateUpdated().isAfter(yesterday), "Update date didn't change");
        assertEquals(withChanges.getMappedTermUrl(), result.getMappedTermUrl());
        assertEquals(withChanges.getSource(), result.getSource());
        assertEquals(withChanges.getMappedTermLabel(), result.getMappedTermLabel());
        assertEquals(withChanges.getEntityType(), result.getEntityType());
    }

    @Test
    void update_statusChangedRequestToUnmapped_statusAndDateUpdatedAndSaveMethodCalled() {
        MappingEntity original = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.REQUEST)
            .build();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        original.setDateUpdated(yesterday);

        MappingEntity withChanges = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.UNMAPPED)
            .build();

        MappingEntity result = instance.update(original, withChanges, MappingType.MANUAL);

        verify(mappingEntityRepository).save(result);
        assertTrue(result.getDateUpdated().isAfter(yesterday), "Update date didn't change");
        assertEquals(withChanges.getMappedTermUrl(), result.getMappedTermUrl());
        assertEquals(withChanges.getSource(), result.getSource());
        assertEquals(withChanges.getMappedTermLabel(), result.getMappedTermLabel());
        assertEquals(withChanges.getEntityType(), result.getEntityType());
    }

    @Test
    void update_statusChangedMappedToReview_statusAndDateUpdatedAndSaveMethodCalled() {
        MappingEntity original = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.MAPPED)
            .build();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        original.setDateUpdated(yesterday);

        MappingEntity withChanges = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.REVIEW)
            .build();

        MappingEntity result = instance.update(original, withChanges, MappingType.MANUAL);

        verify(mappingEntityRepository).save(result);
        assertTrue(result.getDateUpdated().isAfter(yesterday), "Update date didn't change");
        assertEquals(withChanges.getMappedTermUrl(), result.getMappedTermUrl());
        assertEquals(withChanges.getSource(), result.getSource());
        assertEquals(withChanges.getMappedTermLabel(), result.getMappedTermLabel());
        assertEquals(withChanges.getEntityType(), result.getEntityType());
    }

    @Test
    void update_statusChangedReviewToMapped_statusAndDateUpdatedAndSaveMethodCalled() {
        MappingEntity original = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.REVIEW)
            .build();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        original.setDateUpdated(yesterday);

        MappingEntity withChanges = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.MAPPED)
            .build();

        MappingEntity result = instance.update(original, withChanges, MappingType.MANUAL);

        verify(mappingEntityRepository).save(result);
        assertTrue(result.getDateUpdated().isAfter(yesterday), "Update date didn't change");
        assertEquals(withChanges.getMappedTermUrl(), result.getMappedTermUrl());
        assertEquals(withChanges.getSource(), result.getSource());
        assertEquals(withChanges.getMappedTermLabel(), result.getMappedTermLabel());
        assertEquals(withChanges.getEntityType(), result.getEntityType());
    }

    @Test
    void update_statusChangedReviewToUnmapped_statusAndDateUpdatedAndSaveMethodCalled() {
        MappingEntity original = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.REVIEW)
            .build();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        original.setDateUpdated(yesterday);

        MappingEntity withChanges = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.UNMAPPED)
            .build();

        MappingEntity result = instance.update(original, withChanges, MappingType.MANUAL);

        verify(mappingEntityRepository).save(result);
        assertTrue(result.getDateUpdated().isAfter(yesterday), "Update date didn't change");
        assertEquals(withChanges.getMappedTermUrl(), result.getMappedTermUrl());
        assertEquals(withChanges.getSource(), result.getSource());
        assertEquals(withChanges.getMappedTermLabel(), result.getMappedTermLabel());
        assertEquals(withChanges.getEntityType(), result.getEntityType());
    }

    @Test
    void update_statusChangedReviewToRequest_statusAndDateUpdatedAndSaveMethodCalled() {
        MappingEntity original = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.REVIEW)
            .build();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        original.setDateUpdated(yesterday);

        MappingEntity withChanges = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.REQUEST)
            .build();

        MappingEntity result = instance.update(original, withChanges, MappingType.MANUAL);

        verify(mappingEntityRepository).save(result);
        assertTrue(result.getDateUpdated().isAfter(yesterday), "Update date didn't change");
        assertEquals(withChanges.getMappedTermUrl(), result.getMappedTermUrl());
        assertEquals(withChanges.getSource(), result.getSource());
        assertEquals(withChanges.getMappedTermLabel(), result.getMappedTermLabel());
        assertEquals(withChanges.getEntityType(), result.getEntityType());
    }

    @Test
    void update_statusChangedUnmappedToReviewTypeManual_Exception() {
        MappingEntity original = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.UNMAPPED)
            .build();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        original.setDateUpdated(yesterday);

        MappingEntity withChanges = mappingEntityBuilder
            .setEntityType(EntityTypeName.Treatment)
            .setValues(MappingEntityBuilder.createTreatmentValues("TRACE", "Aspirin"))
            .setStatus(Status.REVIEW)
            .build();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> instance.update(original, withChanges, MappingType.MANUAL),
            "Exception not thrown");
        assertThat(
            "Not expected message",
            thrown.getMessage(),
            is("Cannot change status from [Unmapped] to [Review] (method: Manual)"));
    }

}
