package org.gate.metropos.repositories;

import org.gate.metropos.enums.BranchProductFields;
import org.gate.metropos.models.BranchProduct;
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

class BranchProductRepositoryTest {
    @Mock
    private DSLContext dsl;
    @Mock
    private Record record;
    @Mock
    private Result<Record> records;

    // Mock JOOQ steps
    @Mock private SelectSelectStep<Record> selectStep;
    @Mock private SelectJoinStep<Record> fromStep;
    @Mock private SelectConditionStep<Record> conditionStep;
    @Mock private InsertSetStep<Record> insertStep;
    @Mock private InsertSetMoreStep<Record> insertSetMoreStep;
    @Mock private InsertResultStep<Record> insertResultStep;
    @Mock private UpdateSetFirstStep<Record> updateStep;
    @Mock private UpdateSetMoreStep<Record> updateSetMoreStep;
    @Mock private UpdateConditionStep<Record> updateConditionStep;

    @InjectMocks
    private BranchProductRepository repository;

    private BranchProduct mockBranchProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        setupMockData();
        setupMockQueries();
    }

    private void setupMockData() {
        mockBranchProduct = BranchProduct.builder()
                .id(1L)
                .branchId(1L)
                .productId(1L)
                .quantity(10)
                .build();

        // Setup record mocks
        when(record.get(BranchProductFields.ID.toField(), Long.class)).thenReturn(mockBranchProduct.getId());
        when(record.get(BranchProductFields.BRANCH_ID.toField(), Long.class)).thenReturn(mockBranchProduct.getBranchId());
        when(record.get(BranchProductFields.PRODUCT_ID.toField(), Long.class)).thenReturn(mockBranchProduct.getProductId());
        when(record.get(BranchProductFields.QUANTITY.toField(), Integer.class)).thenReturn(mockBranchProduct.getQuantity());
    }

    private void setupMockQueries() {
        // Mock Select Chain
        when(dsl.select()).thenReturn(selectStep);
        when(selectStep.from(any(Table.class))).thenReturn(fromStep);
        when(fromStep.where(any(Condition.class))).thenReturn(conditionStep);
        when(conditionStep.fetch()).thenReturn(records);

        // Mock Insert Chain
        when(dsl.insertInto(any(Table.class))).thenReturn(insertStep);
        when(insertStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(insertSetMoreStep);
        when(insertSetMoreStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(insertSetMoreStep);
        when(insertSetMoreStep.returning()).thenReturn(insertResultStep);
        when(insertResultStep.fetchOne()).thenReturn(record);

        // Mock Update Chain
        when(dsl.update(any(Table.class))).thenReturn(updateStep);
        when(updateStep.set(any(Field.class), any(Integer.class))).thenReturn(updateSetMoreStep);
        when(updateSetMoreStep.where(any(Condition.class))).thenReturn(updateConditionStep);
        when(updateConditionStep.and(any(Condition.class))).thenReturn(updateConditionStep);
    }

    @Test
    void addProductToBranchSuccessful() {
        BranchProduct result = repository.addProductToBranch(1L, 1L, 10);

        assertNotNull(result);
        assertEquals(mockBranchProduct.getId(), result.getId());
        assertEquals(mockBranchProduct.getBranchId(), result.getBranchId());
        assertEquals(mockBranchProduct.getProductId(), result.getProductId());
        assertEquals(mockBranchProduct.getQuantity(), result.getQuantity());
    }

    @Test
    void updateQuantitySuccessful() {
        repository.updateQuantity(1L, 1L, 20);

        verify(updateStep).set(any(Field.class), eq(20));
        verify(updateConditionStep).execute();
    }

    @Test
    void getProductsByBranchSuccessful() {
        when(records.map(any())).thenReturn(Arrays.asList(mockBranchProduct));

        List<BranchProduct> results = repository.getProductsByBranch(1L);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(mockBranchProduct.getId(), results.get(0).getId());
    }

    @Test
    void mapToBranchProductWithNullRecord() {
        when(insertResultStep.fetchOne()).thenReturn(null);

        BranchProduct result = repository.addProductToBranch(1L, 1L, 10);

        assertNull(result);
    }
}
