package org.gate.metropos.repositories;

import org.gate.metropos.enums.BranchFields;
import org.gate.metropos.models.Branch;
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
import static org.mockito.Mockito.when;

class BranchRepositoryTest {
    @Mock
    private DSLContext dsl;
    @Mock
    private Record record;
    @Mock
    private Result<Record> records;
    @Mock
    private SelectSelectStep<Record> selectStep;
    @Mock
    private SelectJoinStep<Record> fromStep;
    @Mock
    private SelectConditionStep<Record> conditionStep;
    @Mock
    private InsertSetStep<Record> insertStep;
    @Mock
    private InsertSetMoreStep<Record> insertSetMoreStep;
    @Mock
    private InsertResultStep<Record> insertResultStep;
    @Mock
    private UpdateSetFirstStep<Record> updateSetFirstStep;
    @Mock
    private UpdateSetMoreStep<Record> updateSetMoreStep;
    @Mock
    private UpdateConditionStep<Record> updateConditionStep;
    @Mock
    private UpdateResultStep<Record> updateResultStep;

    @InjectMocks
    private BranchRepository repository;

    private Branch mockBranch;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock Select Query Chain
        when(dsl.select()).thenReturn(selectStep);
        when(selectStep.from((Table<?>) any())).thenReturn(fromStep);
        when(fromStep.where(any(Condition.class))).thenReturn(conditionStep);
        when(conditionStep.fetchOne()).thenReturn(record);

        // Mock Insert Chain
        when(dsl.insertInto(any(Table.class))).thenReturn(insertStep);
        when(insertStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(insertSetMoreStep);
        when(insertSetMoreStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(insertSetMoreStep);
        when(insertSetMoreStep.returning()).thenReturn(insertResultStep);
        when(insertResultStep.fetchOne()).thenReturn(record);

        // Mock Update Chain
        when(dsl.update(any(Table.class))).thenReturn(updateSetFirstStep);
        when(updateSetFirstStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(updateSetMoreStep);
        when(updateSetMoreStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(updateSetMoreStep);
        when(updateSetMoreStep.where(any(Condition.class))).thenReturn(updateConditionStep);
        when(updateConditionStep.returning()).thenReturn(updateResultStep);
        when(updateResultStep.fetchOne()).thenReturn(record);

        initializeMockBranch();

        // Mock Record Values
        when(record.get(BranchFields.ID.toField(), Long.class)).thenReturn(mockBranch.getId());
        when(record.get(BranchFields.BRANCH_CODE.toField(), String.class)).thenReturn(mockBranch.getBranchCode());
        when(record.get(BranchFields.NAME.toField(), String.class)).thenReturn(mockBranch.getName());
        when(record.get(BranchFields.CITY.toField(), String.class)).thenReturn(mockBranch.getCity());
        when(record.get(BranchFields.ADDRESS.toField(), String.class)).thenReturn(mockBranch.getAddress());
        when(record.get(BranchFields.PHONE.toField(), String.class)).thenReturn(mockBranch.getPhone());
        when(record.get(BranchFields.IS_ACTIVE.toField(), Boolean.class)).thenReturn(mockBranch.isActive());
        when(record.get(BranchFields.NUMBER_OF_EMPLOYEES.toField(), Integer.class)).thenReturn(mockBranch.getNumberOfEmployees());
    }

    @Test
    void findByIdWhenRecordExists() {
        Long id = mockBranch.getId();

        Branch result = repository.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(mockBranch.getBranchCode(), result.getBranchCode());
        assertEquals(mockBranch.getName(), result.getName());
    }

    @Test
    void findByIdWhenRecordDoesNotExist() {
        when(conditionStep.fetchOne()).thenReturn(null);

        Branch result = repository.findById(999L);

        assertNull(result);
    }

    @Test
    void findByBranchCodeWhenRecordExists() {
        String branchCode = mockBranch.getBranchCode();

        Branch result = repository.findByBranchCode(branchCode);

        assertNotNull(result);
        assertEquals(branchCode, result.getBranchCode());
        assertEquals(mockBranch.getName(), result.getName());
    }

    @Test
    void createBranch() {
        Branch branchToCreate = createTestBranch(1L, "TEST");

        Branch result = repository.createBranch(branchToCreate);

        assertNotNull(result);
        assertEquals(mockBranch.getBranchCode(), result.getBranchCode());
        assertEquals(mockBranch.getName(), result.getName());
    }


    @Test
    void updateBranch() {
        Branch branchToUpdate = createTestBranch(1L, "UPD");

        Branch result = repository.updateBranch(branchToUpdate);

        assertNotNull(result);
        assertEquals(branchToUpdate.getId(), result.getId());
        assertEquals(mockBranch.getName(), result.getName());
    }

    @Test
    void getAllBranches() {
        when(fromStep.fetch()).thenReturn(records);
        when(records.map(any())).thenReturn(Arrays.asList(
                createTestBranch(1L, "BR1"),
                createTestBranch(2L, "BR2")
        ));

        List<Branch> results = repository.getAllBranches();

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    private Branch createTestBranch(Long id, String prefix) {
        return Branch.builder()
                .id(id)
                .branchCode(prefix + "001")
                .name(prefix + " Test Branch")
                .city(prefix + " City")
                .address(prefix + " Address")
                .phone("123-456-" + id)
                .isActive(true)
                .numberOfEmployees(0)
                .build();
    }

    private void initializeMockBranch() {
        this.mockBranch = createTestBranch(1L, "TEST");
    }
}
