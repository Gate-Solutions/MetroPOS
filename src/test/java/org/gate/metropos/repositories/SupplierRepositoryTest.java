package org.gate.metropos.repositories;

import org.gate.metropos.enums.SupplierFields;
import org.gate.metropos.models.Supplier;
import org.jooq.*;
import org.jooq.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SupplierRepositoryTest {
    @Mock private DSLContext dsl;
    @Mock private Record record;
    @Mock private Result<Record> records;
    @Mock private SelectSelectStep<Record> selectStep;
    @Mock private SelectJoinStep<Record> fromStep;
    @Mock private SelectConditionStep<Record> conditionStep;
    @Mock private InsertSetStep<Record> insertStep;
    @Mock private InsertSetMoreStep<Record> insertSetMoreStep;
    @Mock private InsertResultStep<Record> insertResultStep;
    @Mock private UpdateSetFirstStep<Record> updateStep;
    @Mock private UpdateSetMoreStep<Record> updateSetMoreStep;
    @Mock private UpdateConditionStep<Record> updateConditionStep;
    @Mock private UpdateResultStep<Record> updateResultStep;

    @InjectMocks
    private SupplierRepository repository;

    private Supplier mockSupplier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        setupMockData();
        setupMockQueries();
    }

    private void setupMockData() {
        mockSupplier = Supplier.builder()
                .id(1L)
                .name("Test Supplier")
                .email("supplier@test.com")
                .phone("1234567890")
                .ntnNumber("NTN123")
                .isActive(true)
                .build();

        when(record.get(SupplierFields.ID.toField(), Long.class)).thenReturn(mockSupplier.getId());
        when(record.get(SupplierFields.NAME.toField(), String.class)).thenReturn(mockSupplier.getName());
        when(record.get(SupplierFields.EMAIL.toField(), String.class)).thenReturn(mockSupplier.getEmail());
        when(record.get(SupplierFields.PHONE.toField(), String.class)).thenReturn(mockSupplier.getPhone());
        when(record.get(SupplierFields.NTN_NUMBER.toField(), String.class)).thenReturn(mockSupplier.getNtnNumber());
        when(record.get(SupplierFields.IS_ACTIVE.toField(), Boolean.class)).thenReturn(mockSupplier.isActive());
    }

    private void setupMockQueries() {
        // Select Chain
        when(dsl.select()).thenReturn(selectStep);
        when(selectStep.from(any(Table.class))).thenReturn(fromStep);
        when(fromStep.where(any(Condition.class))).thenReturn(conditionStep);
        when(conditionStep.fetchOne()).thenReturn(record);

        // Insert Chain
        when(dsl.insertInto(any(Table.class))).thenReturn(insertStep);
        when(insertStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(insertSetMoreStep);
        when(insertSetMoreStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(insertSetMoreStep);
        when(insertSetMoreStep.returning(any(Field[].class))).thenReturn(insertResultStep);
        when(insertSetMoreStep.returning(any(SelectFieldOrAsterisk[].class))).thenReturn(insertResultStep);
        when(insertResultStep.fetchOne()).thenReturn(record);

        // Update Chain
        when(dsl.update(any(Table.class))).thenReturn(updateStep);
        when(updateStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(updateSetMoreStep);
        when(updateSetMoreStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(updateSetMoreStep);
        when(updateSetMoreStep.where(any(Condition.class))).thenReturn(updateConditionStep);
        when(updateConditionStep.returning(any(SelectFieldOrAsterisk[].class))).thenReturn(updateResultStep);
        when(updateResultStep.fetchOne()).thenReturn(record);
    }

    @Test
    void findByIdSuccessful() {
        Supplier result = repository.findById(1L);

        assertNotNull(result);
        assertEquals(mockSupplier.getId(), result.getId());
        assertEquals(mockSupplier.getName(), result.getName());
    }

    @Test
    void findByEmailSuccessful() {
        Supplier result = repository.findByEmail("supplier@test.com");

        assertNotNull(result);
        assertEquals(mockSupplier.getEmail(), result.getEmail());
    }

    @Test
    void createSupplierSuccessful() {
        Supplier result = repository.createSupplier(mockSupplier);

        assertNotNull(result);
        assertEquals(mockSupplier.getName(), result.getName());
        assertEquals(mockSupplier.getEmail(), result.getEmail());
        assertTrue(result.isActive());
    }

    @Test
    void updateSupplierSuccessful() {
        Supplier result = repository.updateSupplier(mockSupplier);

        assertNotNull(result);
        assertEquals(mockSupplier.getName(), result.getName());
        assertEquals(mockSupplier.getEmail(), result.getEmail());
    }

    @Test
    void getAllSuppliersSuccessful() {
        when(fromStep.fetch()).thenReturn(records);
        when(records.map(any())).thenReturn(Arrays.asList(mockSupplier));

        List<Supplier> results = repository.getAllSuppliers();

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void setSupplierStatusSuccessful() {
        repository.setSupplierStatus(1L, false);

        verify(updateStep).set(any(Field.class), eq(false));
        verify(updateConditionStep).execute();
    }
}
